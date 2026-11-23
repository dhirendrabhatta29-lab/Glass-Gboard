package com.drdisagree.glasslayer.xposed.prefs

import android.database.ContentObserver
import android.net.Uri

internal class PrefsObserver(private val onChanged: (Uri?) -> Unit) : ContentObserver(null) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        onChanged(uri)
    }
}
