package com.drdisagree.glasslayer.xposed.glass

import android.graphics.Path
import android.graphics.Rect
import android.view.View
import java.lang.ref.WeakReference

internal class GlassSurface {

    val rect = Rect()
    val corners = FloatArray(4)
    var lastDrawn = 0L
    var originX = Int.MIN_VALUE
    var originY = Int.MIN_VALUE
    var coordinator: Any? = null
    var root: WeakReference<View>? = null

    private val renderers = HashMap<Int, LiquidGlassRenderer>()
    private val fineRenderers = HashMap<Int, LiquidGlassRenderer>()
    private val paths = HashMap<Int, Path>()

    fun path(key: Int): Path = paths.getOrPut(key) { Path() }.apply { reset() }

    fun renderer(key: Int, fine: Boolean = false): LiquidGlassRenderer = if (fine) {
        fineRenderers.getOrPut(key) { LiquidGlassRenderer(FINE_RESOLUTION) }
    } else {
        renderers.getOrPut(key) { LiquidGlassRenderer(GLASS_RESOLUTION) }
    }
}

internal const val GLASS_RESOLUTION = 0.5f
internal const val FINE_RESOLUTION = 1f
