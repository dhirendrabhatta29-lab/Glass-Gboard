package com.drdisagree.glasslayer.data.source

import android.os.Bundle
import com.drdisagree.glasslayer.data.mapper.SyncedPrefKeys

object PrefsSnapshot {

    fun of(stored: Map<String, *>, key: String?): Bundle {
        val keys = if (key == null) SyncedPrefKeys.all else setOf(key).filter { it in SyncedPrefKeys.all }
        val values = Bundle()
        val removed = ArrayList<String>()
        keys.forEach { name -> if (!values.putValue(name, stored[name])) removed += name }
        return Bundle().apply {
            putBundle(PrefsContract.EXTRA_VALUES, values)
            putStringArrayList(PrefsContract.EXTRA_REMOVED, removed)
        }
    }

    private fun Bundle.putValue(key: String, value: Any?): Boolean {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            else -> return false
        }
        return true
    }
}
