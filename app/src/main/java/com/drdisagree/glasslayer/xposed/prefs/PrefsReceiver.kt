package com.drdisagree.glasslayer.xposed.prefs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal class PrefsReceiver(private val onChanged: (Intent) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        onChanged(intent)
    }
}
