package com.drdisagree.glasslayer.xposed.systemui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import com.drdisagree.glasslayer.xposed.glass.GlassJoin
import com.drdisagree.glasslayer.xposed.glass.PressSpring
import com.drdisagree.glasslayer.xposed.glass.ViewGlassDrawable
import java.lang.ref.WeakReference

/** State retained for a view whose stock background is rendered beneath the glass layer. */
internal class UnderlayTarget(
    val surfaceKey: String,
    val active: () -> Boolean,
    val tint: () -> Float,
    var glassEnabled: Boolean,
    var openEdges: Boolean,
    var joinWith: ((View, GlassJoin) -> Boolean)?,
    var clipJoin: Boolean,
    var tintInGlass: Boolean,
    var source: ((View) -> Drawable?)?,
    var behind: ((View) -> Int)?
) {
    var imageContent: Boolean = false
    var rimOnTop: Boolean = false
    var segmented: Boolean = false
    var sheenOnTop: Boolean = false
    var backdrop: ((View) -> Drawable?)? = null
    var rimExtend: ((View) -> Int)? = null
    var glassFade: (() -> Float)? = null
    var scrim: (() -> Int)? = null

    val join = GlassJoin()
    var joinActive: Boolean = false
    var tinted: Drawable? = null
    var appliedAlpha: Int = -1
    var stockAlpha: Int = OPAQUE
    var glass: ViewGlassDrawable? = null
    var glassSource: Drawable? = null
    var isolate: Boolean = false
    var dim: Int = Color.TRANSPARENT
    var glassDim: Int = Color.TRANSPARENT
    var baseResolved: Boolean = false
    var base: WeakReference<View>? = null

    companion object {
        const val OPAQUE = 255
    }
}

/** Press animation and original ripple state for a keyboard view. */
internal class ViewPress(val active: () -> Boolean) {
    var x: Float = 0f
    var y: Float = 0f
    val spring = PressSpring()
    var stockRipple: ColorStateList? = null
}

/** Alpha-composites a foreground color over a background color. */
internal object ColorBlend {
    fun over(foreground: Int, background: Int): Int {
        val foregroundAlpha = Color.alpha(foreground)
        val backgroundAlpha = Color.alpha(background)
        val inverseForegroundAlpha = 255 - foregroundAlpha
        val outputAlpha = foregroundAlpha + (backgroundAlpha * inverseForegroundAlpha + 127) / 255
        if (outputAlpha == 0) return Color.TRANSPARENT

        fun channel(foregroundChannel: Int, backgroundChannel: Int): Int {
            val premultiplied = foregroundChannel * foregroundAlpha +
                (backgroundChannel * backgroundAlpha * inverseForegroundAlpha + 127) / 255
            return (premultiplied + outputAlpha / 2) / outputAlpha
        }

        return Color.argb(
            outputAlpha,
            channel(Color.red(foreground), Color.red(background)),
            channel(Color.green(foreground), Color.green(background)),
            channel(Color.blue(foreground), Color.blue(background))
        )
    }
}
