package com.drdisagree.glasslayer.data.source

/** Persisted settings exposed by the Gboard-only companion app. */
object PrefKeys {
    const val GROUP = "glass_layer"

    const val ENABLED = "enabled"
    const val BLUR_RADIUS = "blur_radius"
    const val KEYBOARD = "keyboard"
    const val KEYBOARD_TINT = "keyboard_tint"
    const val KEYBOARD_KEY_TINT = "keyboard_key_tint"
    const val KEYBOARD_ACCENT_TINT = "keyboard_accent_tint"

    const val DEFAULT_ENABLED = false
    const val DEFAULT_MODULE = true
    const val DEFAULT_BLUR_RADIUS = 12f
    const val DEFAULT_KEYBOARD_TINT = 85f
    const val DEFAULT_KEYBOARD_KEY_TINT = 45f
    const val DEFAULT_KEYBOARD_ACCENT_TINT = 80f
}
