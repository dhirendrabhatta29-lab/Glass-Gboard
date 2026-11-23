package com.drdisagree.glasslayer

import android.app.Application
import com.drdisagree.glasslayer.data.source.PrefsChangeNotifier
import com.drdisagree.glasslayer.data.source.PrefsStorage

class GlassLayerApp : Application() {
    private var prefsNotifier: PrefsChangeNotifier? = null

    override fun onCreate() {
        super.onCreate()
        if (PrefsStorage.isUnlocked(this)) {
            prefsNotifier = PrefsChangeNotifier(this).also { it.start() }
        }
    }
}
