package com.drdisagree.glasslayer.data.source

/** The only package allowed to receive this module's preference snapshots. */
object ReportContract {
    const val GBOARD_PACKAGE = "com.google.android.inputmethod.latin"
    val APP_SCOPES = listOf(GBOARD_PACKAGE)
}
