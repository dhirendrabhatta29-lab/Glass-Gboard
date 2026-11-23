package com.drdisagree.glasslayer.data.mapper

import com.drdisagree.glasslayer.data.source.PrefKeys

/** Only values consumed by the Gboard hooks cross the process boundary. */
object SyncedPrefKeys {
    val all: Set<String> = setOf(
        PrefKeys.ENABLED,
        PrefKeys.KEYBOARD,
        PrefKeys.BLUR_RADIUS,
        PrefKeys.KEYBOARD_TINT,
        PrefKeys.KEYBOARD_KEY_TINT,
        PrefKeys.KEYBOARD_ACCENT_TINT
    )
}
