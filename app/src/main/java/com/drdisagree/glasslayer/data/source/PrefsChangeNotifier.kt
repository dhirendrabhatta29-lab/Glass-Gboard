package com.drdisagree.glasslayer.data.source

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.drdisagree.glasslayer.data.mapper.SyncedPrefKeys

class PrefsChangeNotifier(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = PrefsStorage.of(appContext)

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when {
            key == null -> notify(PrefsContract.CHANGED_URI, null)
            key in SyncedPrefKeys.all -> notify(PrefsContract.changedUri(key), key)
        }
    }

    fun start() {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun notify(uri: Uri, key: String?) {
        try {
            appContext.contentResolver.notifyChange(uri, null)
        } catch (_: Throwable) {
        }
        val snapshot = PrefsSnapshot.of(prefs.all, key)
        ReportContract.APP_SCOPES.forEach { target ->
            try {
                appContext.sendBroadcast(Intent(PrefsContract.ACTION_CHANGED).setPackage(target).putExtras(snapshot))
            } catch (_: Throwable) {
            }
        }
    }
}
