package com.drdisagree.glasslayer.data.source

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.Process

class PrefsProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method != PrefsContract.METHOD_READ) return null
        enforceCaller()
        val context = context ?: return null
        val stored = PrefsStorage.of(context).all
        if (stored.isEmpty()) return null
        return PrefsSnapshot.of(stored, arg)
    }

    private fun enforceCaller() {
        val uid = Binder.getCallingUid()
        if (uid == Process.myUid()) return
        val packages = callingPackage?.let { listOf(it) }
            ?: context?.packageManager?.getPackagesForUid(uid)?.toList().orEmpty()
        if (packages.none { it in PrefsContract.SCOPE }) throw SecurityException("Caller $uid is not in the module scope")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}

