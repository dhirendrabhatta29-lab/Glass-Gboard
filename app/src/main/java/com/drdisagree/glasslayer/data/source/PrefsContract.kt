package com.drdisagree.glasslayer.data.source

import android.net.Uri

object PrefsContract {
    const val AUTHORITY = "com.drdisagree.glasslayer.prefs"
    const val METHOD_READ = "read"
    const val EXTRA_VALUES = "values"
    const val EXTRA_REMOVED = "removed"
    const val ACTION_CHANGED = "com.drdisagree.glasslayer.action.PREFS_CHANGED"
    const val PERMISSION = "com.drdisagree.glasslayer.permission.PREFS"
    private const val PATH_CHANGED = "changed"

    val BASE_URI: Uri = Uri.Builder().scheme("content").authority(AUTHORITY).build()
    val CHANGED_URI: Uri = BASE_URI.buildUpon().appendPath(PATH_CHANGED).build()
    val SCOPE: Set<String> = setOf(ReportContract.GBOARD_PACKAGE)

    fun changedUri(key: String): Uri = CHANGED_URI.buildUpon().appendPath(key).build()

    fun keyOf(uri: Uri?): String? = uri?.pathSegments?.getOrNull(1)
}
