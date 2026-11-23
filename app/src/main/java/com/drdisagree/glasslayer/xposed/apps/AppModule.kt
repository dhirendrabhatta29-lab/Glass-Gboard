package com.drdisagree.glasslayer.xposed.apps

import android.content.pm.ApplicationInfo
import com.drdisagree.glasslayer.xposed.hook.HookEnv
import com.drdisagree.glasslayer.xposed.prefs.ModulePrefs
import com.drdisagree.glasslayer.xposed.systemui.GlassConfig
import com.drdisagree.glasslayer.xposed.systemui.KeyboardGlass
import com.drdisagree.glasslayer.xposed.systemui.ViewGlassUnderlay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Gboard-only entry point.
 *
 * The KeyboardGlass hook itself is intentionally kept unchanged from the
 * upstream GlassLayer source. Only the package scope and installer list are
 * narrowed in this extraction.
 */
object AppModule {

    const val GBOARD_PACKAGE = "com.google.android.inputmethod.latin"
    val PACKAGES = setOf(GBOARD_PACKAGE)

    private val started = AtomicBoolean(false)

    fun claim(): Boolean = started.compareAndSet(false, true)

    fun start(info: ApplicationInfo?) {
        // The upstream app-process module uses this mode for app surfaces.
        GlassConfig.liveGlass = false
        ModulePrefs.init(HookEnv.module, info?.deviceProtectedDataDir)
        GlassConfig.reload()

        ModulePrefs.addListener {
            GlassConfig.reload()
            try {
                KeyboardGlass.refresh()
                ViewGlassUnderlay.refresh()
            } catch (throwable: Throwable) {
                HookEnv.log("Keyboard refresh failed", throwable)
            }
        }

        // The draw interception is shared infrastructure required by the
        // original KeyboardGlass implementation; install it before keyboard hooks.
        listOf(
            "ViewGlassUnderlay" to ViewGlassUnderlay::install,
            "KeyboardGlass" to KeyboardGlass::install
        ).forEach { (name, install) ->
            try {
                HookEnv.owner = name
                val hooks = install()
                HookEnv.log(if (hooks > 0) "Gboard $name: $hooks hooks" else "Gboard $name: no hooks matched")
            } catch (throwable: Throwable) {
                HookEnv.log("Gboard $name install failed", throwable)
            }
        }
        HookEnv.owner = ""
    }
}
