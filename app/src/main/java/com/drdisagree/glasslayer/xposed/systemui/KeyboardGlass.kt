package com.drdisagree.glasslayer.xposed.systemui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.ColorUtils
import com.drdisagree.glasslayer.data.source.PrefKeys
import com.drdisagree.glasslayer.xposed.hook.Guard
import com.drdisagree.glasslayer.xposed.hook.HookEnv
import com.drdisagree.glasslayer.xposed.hook.call
import com.drdisagree.glasslayer.xposed.hook.hookAllAfter
import java.util.Collections
import java.util.WeakHashMap
import kotlin.math.roundToInt

object KeyboardGlass {

    private const val IME_SERVICE = "android.inputmethodservice.InputMethodService"
    private const val NAV_FRAME = "android.inputmethodservice.navigationbar.NavigationBarFrame"
    private const val PANEL_SPAN = 0.95f
    private const val BAR_SPAN = 0.6f
    private const val OPAQUE = 255
    private const val SAMPLE_SIZE = 64
    private const val TOP_RADIUS_DP = 12f

    private val windows = Collections.newSetFromMap(WeakHashMap<View, Boolean>())
    private val panels = Collections.newSetFromMap(WeakHashMap<View, Boolean>())
    private val navFrames = Collections.newSetFromMap(WeakHashMap<View, Boolean>())
    private val panelColors = WeakHashMap<View, Int>()
    private val layerColors = WeakHashMap<View, Int>()
    private val blurs = WeakHashMap<View, Drawable>()
    private val stockRadii = WeakHashMap<GradientDrawable, FloatArray>()
    private val stockClips = WeakHashMap<ViewGroup, Boolean>()
    private val stockPaddingClips = WeakHashMap<ViewGroup, Boolean>()
    private val mutated = Collections.newSetFromMap(WeakHashMap<Drawable, Boolean>())
    private val mainHandler = Handler(Looper.getMainLooper())
    private var reportedState: String? = null

    fun install(): Int = HookEnv.findClass(IME_SERVICE).hookAllAfter("onStartInputView") { chain, _ ->
        val window = chain.thisObject.call("getWindow")?.call("getWindow") as? Window ?: return@hookAllAfter
        val decor = window.decorView
        decor.post { Guard.run("KeyboardGlass.attach", Unit) { track(decor) } }
    }

    fun refresh() {
        mainHandler.post {
            panels.toList().forEach { panel ->
                Guard.run("KeyboardGlass.refresh", Unit) {
                    applyPanel(panel)
                    panel.invalidate()
                }
            }
            reportState()
        }
    }

    private fun track(decor: View) {
        if (windows.add(decor)) {
            decor.viewTreeObserver.addOnGlobalLayoutListener {
                Guard.run("KeyboardGlass.layout", Unit) { scan(decor) }
            }
        }
        scan(decor)
    }

    private fun scan(decor: View) {
        if (decor.width <= 0) return
        walk(decor, decor.width, null)
        hideNavScrim(decor)
        panels.toList().forEach(::applyPanel)
        reportState()
    }

    private fun reportState() {
        val state = "panels=${panels.size} enabled=${GlassConfig.enabled} keyboard=${GlassConfig.keyboardActive}"
        if (state == reportedState) return
        reportedState = state
        HookEnv.log("KeyboardGlass: $state")
    }

    private fun walk(view: View, rootWidth: Int, panel: View?) {
        if (view.javaClass.name == NAV_FRAME) {
            navFrames.add(view)
            return
        }
        var current = panel
        if (view in panels) {
            current = view
        } else if (view.width > 0 && !ViewGlassUnderlay.isAttached(view)) {
            val background = view.background
            val shape = SolidShapes.of(background)
            when {
                background.hasLayer() -> Unit
                shape != null && panel == null && view.width >= rootWidth * PANEL_SPAN -> {
                    attachPanel(view, shape)
                    current = view
                }
                panel != null && view.width >= panel.width * PANEL_SPAN && (shape != null || background.hasColorFill()) -> attachLayer(view)
                shape != null && panel != null && view.width >= panel.width * BAR_SPAN -> attachLayer(view)
                shape != null && panel != null -> attachKey(view, AccentColors.isAccent(shape.color?.defaultColor ?: Color.TRANSPARENT))
            }
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) walk(view.getChildAt(index), rootWidth, current)
        }
    }

    private fun attachPanel(view: View, shape: GradientDrawable) {
        panels.add(view)
        panelColors[view] = renderedColor(view.background) ?: shape.color?.defaultColor ?: Color.TRANSPARENT
        val slab = KeyboardSlabDrawable(
            blur = { blurOf(view) },
            color = { ColorUtils.setAlphaComponent(slabColor(view), (OPAQUE * GlassConfig.keyboardTint).roundToInt().coerceIn(0, OPAQUE)) },
            topRadius = { topRadius(view) },
            extend = { stripHeight() }
        )
        ViewGlassUnderlay.attach(
            view,
            PrefKeys.KEYBOARD,
            { GlassConfig.keyboardActive },
            { 0f },
            source = { innerFill(it) },
            rimOnTop = true,
            sheenOnTop = true,
            backdrop = { panel -> slab.also { it.setBounds(0, 0, panel.width, panel.height) } },
            rimExtend = { stripHeight() }
        )
    }

    private fun slabColor(panel: View): Int {
        val layer = layerColors.keys.filter { it.isShown }.maxByOrNull { it.width * it.height }
        return layer?.let { layerColors[it] } ?: panelColors[panel] ?: Color.TRANSPARENT
    }

    private fun attachLayer(view: View) {
        val fill = view.background
        val color = renderedColor(fill) ?: SolidShapes.of(fill)?.color?.defaultColor ?: innerColor(fill)?.color
        if (color != null && Color.alpha(color) > 0 && view.height > 0) layerColors[view] = color
        ViewGlassUnderlay.attach(view, PrefKeys.KEYBOARD, { GlassConfig.keyboardActive }, { 0f }, glass = false, source = { innerFill(it) })
    }

    private fun attachKey(view: View, accent: Boolean) {
        ViewGlassUnderlay.attach(
            view,
            PrefKeys.KEYBOARD,
            { GlassConfig.keyboardActive },
            { if (accent) GlassConfig.keyboardAccentTint else GlassConfig.keyboardKeyTint },
            source = { innerFill(it) },
            rimOnTop = true
        )
    }

    private fun hideNavScrim(decor: View) {
        val group = decor as? ViewGroup ?: return
        for (index in 0 until group.childCount) {
            val child = group.getChildAt(index)
            if (child.javaClass == View::class.java && child.background is ColorDrawable && !ViewGlassUnderlay.isAttached(child)) {
                attachLayer(child)
            }
        }
    }

    private fun stripHeight(): Int = navFrames.firstOrNull { it.isShown }?.height ?: 0

    private fun applyPanel(panel: View) {
        val active = GlassConfig.keyboardActive
        applyCorners(panel, active)
        applyClip(panel, active)
        blurs[panel]?.let { updateBlur(it, panel, active) }
    }

    private fun applyClip(panel: View, active: Boolean) {
        var parent = panel.parent as? ViewGroup
        while (parent != null) {
            if (active) {
                if (!stockClips.containsKey(parent)) stockClips[parent] = parent.clipChildren
                if (!stockPaddingClips.containsKey(parent)) stockPaddingClips[parent] = parent.clipToPadding
                if (parent.clipChildren) parent.clipChildren = false
                if (parent.clipToPadding) parent.clipToPadding = false
            } else {
                stockClips.remove(parent)?.let { parent.clipChildren = it }
                stockPaddingClips.remove(parent)?.let { parent.clipToPadding = it }
            }
            parent = parent.parent as? ViewGroup
        }
    }

    private fun applyCorners(panel: View, active: Boolean) {
        val shape = SolidShapes.of(panel.background) ?: return
        if (active) {
            val radius = topRadius(panel)
            val wanted = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
            if (shape.cornerRadii?.contentEquals(wanted) == true) return
            if (!stockRadii.containsKey(shape)) {
                shape.mutate()
                stockRadii[shape] = shape.cornerRadii ?: FloatArray(8) { shape.cornerRadius }
            }
            shape.cornerRadii = wanted
        } else {
            stockRadii.remove(shape)?.let { shape.cornerRadii = it }
        }
    }

    private fun topRadius(panel: View): Float = TOP_RADIUS_DP * panel.resources.displayMetrics.density

    private fun blurOf(panel: View): Drawable? {
        blurs[panel]?.let { return it }
        val blur = panel.call("getViewRootImpl").call("createBackgroundBlurDrawable") as? Drawable ?: return null
        blurs[panel] = blur
        updateBlur(blur, panel, GlassConfig.keyboardActive)
        return blur
    }

    private fun updateBlur(blur: Drawable, panel: View, active: Boolean) {
        val top = if (active) topRadius(panel) else 0f
        blur.call("setCornerRadius", top, top, 0f, 0f)
        val radius = if (active) GlassConfig.surface(PrefKeys.KEYBOARD).blurRadiusPx.roundToInt() else 0
        blur.call("setBlurRadius", radius)
    }

    private fun innerFill(view: View): Drawable? {
        val background = view.background ?: return null
        val fill = SolidShapes.of(background) ?: innerColor(background) ?: return background
        if (mutated.add(fill)) fill.mutate()
        return fill
    }

    private fun renderedColor(drawable: Drawable?): Int? {
        drawable ?: return null
        val stockBounds = Rect(drawable.bounds)
        return try {
            val bitmap = Bitmap.createBitmap(SAMPLE_SIZE, SAMPLE_SIZE, Bitmap.Config.ARGB_8888)
            drawable.setBounds(0, 0, SAMPLE_SIZE, SAMPLE_SIZE)
            drawable.draw(Canvas(bitmap))
            bitmap.getPixel(SAMPLE_SIZE / 2, SAMPLE_SIZE / 2).also { bitmap.recycle() }.takeIf { Color.alpha(it) > 0 }
        } catch (_: Throwable) {
            null
        } finally {
            drawable.bounds = stockBounds
        }
    }

    private fun innerColor(drawable: Drawable?): ColorDrawable? = when (drawable) {
        is ColorDrawable -> drawable
        is DrawableWrapper -> innerColor(drawable.drawable)
        else -> null
    }

    private fun Drawable?.hasLayer(): Boolean = when (this) {
        is LayerDrawable -> true
        is DrawableWrapper -> drawable.hasLayer()
        is DrawableContainer -> current.hasLayer()
        else -> false
    }

    private fun Drawable?.hasColorFill(): Boolean = when (this) {
        is ColorDrawable -> Color.alpha(color) > 0
        is DrawableWrapper -> drawable.hasColorFill()
        else -> false
    }
}
