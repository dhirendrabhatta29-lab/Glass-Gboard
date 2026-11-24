package com.drdisagree.glasslayer.xposed

import com.drdisagree.glasslayer.xposed.apps.AppModule
import com.drdisagree.glasslayer.xposed.hook.HookEnv
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * Gboard-only package dispatcher.
 *
 * No SystemUI, Settings, package-installer, or other application hooks are
 * started by this fork.
 */
class ModuleMain : XposedModule() {

    private var processName: String? = null

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        processName = param.processName
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (!param.isFirstPackage) return
        if (param.packageName != AppModule.GBOARD_PACKAGE) return
        if (processName != null && processName != AppModule.GBOARD_PACKAGE) return
        if (!AppModule.claim()) return

        HookEnv.init(this, param.classLoader)
        AppModule.start(param.applicationInfo)
    }
}
