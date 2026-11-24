package com.drdisagree.glasslayer.xposed.glass

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.SystemClock
import android.view.SurfaceControl
import com.drdisagree.glasslayer.xposed.hook.Guard
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt

object BackdropCapture {

    private const val TAG = "BackdropCapture"
    private const val CLIENT_TAG = "BackdropCapture.client"
    private const val MIN_FRAME_INTERVAL_MS = 4L
    private const val CAPTURE_MARGIN = 96
    private const val MIN_SCALE = 0.25f
    private const val MAX_SCALE = 0.75f
    private const val CAPTURE_TIMEOUT_MS = 1000L

    private val clients: MutableSet<BackdropClient> = Collections.newSetFromMap(WeakHashMap())
    private val mainHandler = Handler(Looper.getMainLooper())
    private val inFlight = AtomicBoolean(false)

    @Volatile
    private var inFlightSince = 0L
    private val latestCapture = AtomicReference<PendingCapture?>(null)
    private val clientRect = Rect()
    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    private var running = false

    @Volatile
    var maxFps = 0

    @Volatile
    private var frameIntervalMs = (1000f / DEFAULT_REFRESH_RATE).toLong()
    private val blur = BackdropBlur()

    val isAvailable: Boolean
        get() = ScreenCaptureBridge.isAvailable && !Guard.isBroken(TAG)

    fun register(client: BackdropClient) {
        synchronized(clients) { clients.add(client) }
        start()
    }

    fun captureNow() {
        val worker = handler ?: return
        worker.post {
            Guard.run(TAG, Unit) {
                if (inFlight.get()) return@run
                val snapshot = synchronized(clients) { clients.toList() }
                if (snapshot.isNotEmpty()) captureFor(snapshot)
            }
        }
    }

    fun unregister(client: BackdropClient) {
        synchronized(clients) { clients.remove(client) }
    }

    private fun start() {
        if (running) return
        running = true
        val worker = thread ?: HandlerThread("GlassLayerBackdrop").also {
            it.start()
            thread = it
            handler = Handler(it.looper)
        }
        Handler(worker.looper).post(::safeTick)
    }

    private fun safeTick() {
        val worker = handler ?: return
        val delay = Guard.run(TAG, frameIntervalMs) { tick() }
        if (delay >= 0L && !Guard.isBroken(TAG)) {
            worker.postDelayed(::safeTick, delay)
        } else {
            running = false
            Guard.run(TAG, Unit) { blur.release() }
        }
    }

    private fun tick(): Long {
        val snapshot = synchronized(clients) { clients.toList() }

        if (snapshot.isEmpty()) {
            return -1L
        }

        val startedAt = SystemClock.uptimeMillis()
        if (inFlight.get() && startedAt - inFlightSince > CAPTURE_TIMEOUT_MS) {
            ScreenCaptureBridge.reset()
            inFlight.set(false)
        }
        if (!inFlight.get()) captureFor(snapshot)
        return (frameIntervalMs - (SystemClock.uptimeMillis() - startedAt)).coerceAtLeast(1L)
    }


    private fun safeProcessLatest() {
        Guard.run(TAG, Unit) { processLatest() }
    }

    private fun processLatest() {
        val pending = latestCapture.getAndSet(null) ?: return
        val bitmap = pending.bitmap
        val blurred = blur.blur(bitmap, pending.blurRadius * pending.scale)
        val screenRect = if (pending.fullDisplay) {
            Rect(
                0,
                0,
                (bitmap.width / pending.scale).roundToInt(),
                (bitmap.height / pending.scale).roundToInt()
            )
        } else {
            pending.captureRect
        }
        val frame = BackdropFrame(bitmap, screenRect, pending.scale, blurred, pending.blurRadius, pending.startedAt, pending.token)
        mainHandler.post {
            val targets = synchronized(clients) { clients.toList() }.filter { !pending.exclusive || it.wantsDisplayCapture }
            targets.forEach { client -> Guard.run(CLIENT_TAG, Unit) { client.onBackdrop(frame) } }
        }
    }

    private fun captureFor(snapshot: List<BackdropClient>) {
        val crop = Rect()
        val exclude = LinkedHashSet<SurfaceControl>()
        var displayId = 0
        var scale = 0f
        var blurRadius = 0f
        var fullDisplay = false
        var displayCapture = false
        var refreshRate = 0f
        var token: Any? = null
        val exclusiveClients = snapshot.filter { it.wantsDisplayCapture }
        val exclusive = exclusiveClients.isNotEmpty()
        val participants = if (exclusive) exclusiveClients else snapshot

        participants.forEach { client ->
            if (!client.captureRect(clientRect)) return@forEach
            if (crop.isEmpty) crop.set(clientRect) else crop.union(clientRect)
            client.excludeLayers(exclude)
            token = client.captureToken ?: token
            scale = maxOf(scale, client.preferredScale)
            blurRadius = maxOf(blurRadius, client.preferredBlur)
            fullDisplay = fullDisplay || client.wantsFullDisplay
            displayCapture = displayCapture || client.wantsDisplayCapture
            refreshRate = maxOf(refreshRate, client.refreshRate)
            displayId = client.displayId
        }

        val cap = maxFps
        if (cap > 0) refreshRate = if (refreshRate > 0f) minOf(refreshRate, cap.toFloat()) else cap.toFloat()
        if (refreshRate > 0f) {
            frameIntervalMs = (1000f / refreshRate).toLong().coerceAtLeast(MIN_FRAME_INTERVAL_MS)
        }
        if (crop.isEmpty) return
        crop.inset(-CAPTURE_MARGIN, -CAPTURE_MARGIN)
        if (crop.left < 0) crop.left = 0
        if (crop.top < 0) crop.top = 0

        val startedAt = SystemClock.uptimeMillis()
        inFlightSince = startedAt
        inFlight.set(true)
        val captureScale = scale.coerceIn(MIN_SCALE, MAX_SCALE)
        val captureRect = if (fullDisplay) Rect() else Rect(crop)
        val started = ScreenCaptureBridge.capture(
            displayId,
            captureRect,
            captureScale,
            exclude.toTypedArray(),
            displayCapture
        ) { bitmap ->
            if (bitmap == null) {
                inFlight.set(false)
                return@capture
            }
            val worker = handler
            if (worker == null) {
                inFlight.set(false)
                return@capture
            }
            inFlight.set(false)
            if (exclusive) {
                val screenRect = if (fullDisplay) {
                    Rect(0, 0, (bitmap.width / captureScale).roundToInt(), (bitmap.height / captureScale).roundToInt())
                } else {
                    Rect(captureRect)
                }
                val frame = BackdropFrame(bitmap, screenRect, captureScale, null, 0f, startedAt, token)
                mainHandler.post {
                    val targets = synchronized(clients) { clients.toList() }.filter { it.wantsDisplayCapture }
                    targets.forEach { client -> Guard.run(CLIENT_TAG, Unit) { client.onBackdrop(frame) } }
                }
                return@capture
            }
            val pending = PendingCapture(bitmap, captureRect, captureScale, blurRadius, fullDisplay, startedAt, token, exclusive)
            if (latestCapture.getAndSet(pending) == null) worker.post(::safeProcessLatest)
        }
        if (!started) inFlight.set(false)
    }
}
