# GlassLayer for Gboard

A Gboard-only fork of [GlassLayer](https://github.com/Mahmud0808/GlassLayer). It keeps the upstream keyboard glass hook and the rendering code it depends on, while removing the unrelated system-surface hooks and the original multi-page companion app.

## What it does

- Hooks only Google Gboard: `com.google.android.inputmethod.latin`.
- Installs `ViewGlassUnderlay` (shared view-drawing interception required by the keyboard hook) and `KeyboardGlass`.
- Retains the original keyboard panel, key, accent, blur, corner, and navigation-strip implementation.
- Does not hook SystemUI, Android Settings, package installers, or other apps.

## Companion app

The launcher app is a small keyboard-only settings screen:

- **Enable module** — master switch.
- **Keyboard glass** — enables or disables the Gboard surface.
- **Blur radius** — global keyboard blur, 0–32 dp.
- **Keyboard background tint** — 0–100%.
- **Key tint** — 0–100%.
- **Accent tint** — 0–100%.

Settings are stored in device-protected preferences and synchronized to the Gboard process through the retained preference provider/observer bridge. Only the six settings consumed by the keyboard hook are synchronized.

## Requirements

- Android 16 or newer (the upstream project baseline; `minSdk` 36).
- Root and an Xposed implementation compatible with libxposed API 102.
- Google Gboard installed and enabled.
- A compatible Android ROM/device. Hook compatibility can vary by Gboard and Android build.

## Install and enable

1. Build and install the APK.
2. Enable the module in your Xposed manager.
3. Keep its scope limited to **Gboard** (`com.google.android.inputmethod.latin`).
4. Open the companion app and turn on **Enable module** and **Keyboard glass**.
5. Reopen Gboard (or restart its process) if the keyboard was already active during installation.

Do not add SystemUI or other packages to the scope.

## Build locally

```bash
chmod +x ./gradlew
./gradlew assembleRelease
```

The APK is written under `app/build/outputs/apk/release/`.

The included GitHub Actions workflow uses JDK 25 (matching the Gradle daemon JVM requirement), checks that stale all-features source files are not present, builds `assembleRelease`, and uploads the APK as `Gboard-GlassLayer-APK`.

**Important when updating an existing GitHub repository:** replace the repository source tree with this archive. Do not simply extract these files over the old full GlassLayer tree, because old Kotlin files that are not overwritten will still be compiled. The workflow now checks for several of the stale files reported in the supplied build log and fails early with a clear message if they remain.

## Implementation notes

- `KeyboardGlass.kt` and `KeyboardSlabDrawable.kt` are preserved byte-for-byte from the supplied upstream archive.
- `AppModule` and `ModuleMain` dispatch only into Gboard.
- `GlassConfig` was reduced to the settings consumed by the Gboard hooks.
- The preference bridge, liquid-glass renderer, reflection helpers, and drawing underlay remain because the original keyboard code depends on them.
- The former onboarding, status, about, license, tile, SystemUI feature hooks, and unrelated settings/domain/repository code were removed.

## Credits and license

- **Original project and keyboard implementation:** [Mahmud0808 / GlassLayer](https://github.com/Mahmud0808/GlassLayer).
- **Rendering source:** [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) by Kyant.
- **Extraction, Gboard-only wiring, settings reduction, and documentation:** prepared with assistance from OpenAI ChatGPT (GPT-6).

This project is derived from GlassLayer and retains its GPL-3.0 `LICENSE`. Third-party notices are kept under `app/src/main/assets/licenses/`. Preserve applicable license and copyright notices when redistributing this derivative.

## Disclaimer

Unofficial community extraction; not affiliated with Google or the Gboard team. Gboard is a Google product and the name is used only to identify the target application. A successful Android build and device-level runtime test have not yet been verified for this cleanup revision.
