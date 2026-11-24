package com.drdisagree.glasslayer.data.source

import android.content.Context
import android.content.SharedPreferences
import android.os.UserManager

object PrefsStorage {

    @Volatile
    private var migrated = false

    fun of(context: Context): SharedPreferences {
        val app = context.applicationContext ?: context
        val device = app.createDeviceProtectedStorageContext()
        migrate(app, device)
        return device.getSharedPreferences(PrefKeys.GROUP, Context.MODE_PRIVATE)
    }

    fun isUnlocked(context: Context): Boolean =
        context.getSystemService(UserManager::class.java)?.isUserUnlocked != false

    @Synchronized
    private fun migrate(app: Context, device: Context) {
        if (migrated || !isUnlocked(app)) return
        migrated = device.moveSharedPreferencesFrom(app, PrefKeys.GROUP)
    }
}
