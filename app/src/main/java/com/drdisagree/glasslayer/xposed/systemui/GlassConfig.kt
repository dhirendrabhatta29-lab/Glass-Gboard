package com.drdisagree.glasslayer.xposed.systemui

import android.content.res.Resources
import com.drdisagree.glasslayer.data.source.PrefKeys
import com.drdisagree.glasslayer.xposed.glass.ComposeLiquidGlass
import com.drdisagree.glasslayer.xposed.glass.LiquidGlassStyle
import com.drdisagree.glasslayer.xposed.prefs.ModulePrefs

/** Runtime configuration used exclusively by the extracted Gboard hooks. */
object GlassConfig {
    private var masterEnabled = PrefKeys.DEFAULT_ENABLED

    var keyboard = PrefKeys.DEFAULT_MODULE
        private set
    var keyboardTint = PrefKeys.DEFAULT_KEYBOARD_TINT / 100f
        private set
    var keyboardKeyTint = PrefKeys.DEFAULT_KEYBOARD_KEY_TINT / 100f
        private set
    var keyboardAccentTint = PrefKeys.DEFAULT_KEYBOARD_ACCENT_TINT / 100f
        private set
    var buttonPress = false
        private set

    @Volatile
    var liveGlass = false

    val enabled: Boolean get() = masterEnabled
    val keyboardActive: Boolean get() = enabled && keyboard
    val glassAvailable: Boolean get() = liveGlass && enabled && ComposeLiquidGlass.isAvailable

    @Volatile
    private var global = SurfaceStyle(
        PrefKeys.DEFAULT_BLUR_RADIUS * Resources.getSystem().displayMetrics.density,
        LiquidGlassStyle()
    )

    fun surface(key: String): SurfaceStyle = global

    fun reload() {
        masterEnabled = ModulePrefs.boolean(PrefKeys.ENABLED, PrefKeys.DEFAULT_ENABLED)
        keyboard = ModulePrefs.boolean(PrefKeys.KEYBOARD, PrefKeys.DEFAULT_MODULE)
        keyboardTint = ModulePrefs.float(PrefKeys.KEYBOARD_TINT, PrefKeys.DEFAULT_KEYBOARD_TINT) / 100f
        keyboardKeyTint = ModulePrefs.float(PrefKeys.KEYBOARD_KEY_TINT, PrefKeys.DEFAULT_KEYBOARD_KEY_TINT) / 100f
        keyboardAccentTint = ModulePrefs.float(PrefKeys.KEYBOARD_ACCENT_TINT, PrefKeys.DEFAULT_KEYBOARD_ACCENT_TINT) / 100f
        buttonPress = false
        val blur = ModulePrefs.float(PrefKeys.BLUR_RADIUS, PrefKeys.DEFAULT_BLUR_RADIUS)
        global = SurfaceStyle(blur * Resources.getSystem().displayMetrics.density, LiquidGlassStyle())
    }
}
