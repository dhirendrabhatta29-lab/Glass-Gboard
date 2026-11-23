#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA_ROOT="$ROOT/app/src/main/java/com/drdisagree/glasslayer"

required=(
  "$JAVA_ROOT/xposed/ModuleMain.kt"
  "$JAVA_ROOT/xposed/apps/AppModule.kt"
  "$JAVA_ROOT/xposed/systemui/KeyboardGlass.kt"
  "$JAVA_ROOT/xposed/systemui/KeyboardSlabDrawable.kt"
  "$JAVA_ROOT/ui/keyboard/KeyboardSettingsActivity.kt"
)
for file in "${required[@]}"; do
  if [[ ! -f "$file" ]]; then
    echo "ERROR: required Gboard-only source file is missing: ${file#$ROOT/}" >&2
    exit 1
  fi
done

# These files belong to the old all-features app and caused the supplied CI
# log to fail when they were still present in the checked-out repository.
forbidden=(
  "data/mapper/GlassPrefMapper.kt"
  "ui/about/AboutScreen.kt"
  "data/repository/LicenseRepositoryImpl.kt"
  "xposed/systemui/BouncerGlass.kt"
  "xposed/systemui/BrightnessGlass.kt"
  "xposed/systemui/NotificationGlass.kt"
  "xposed/systemui/VolumePanelGlass.kt"
)
for relative in "${forbidden[@]}"; do
  if [[ -e "$JAVA_ROOT/$relative" ]]; then
    echo "ERROR: stale full-GlassLayer source remains: app/src/main/java/com/drdisagree/glasslayer/$relative" >&2
    echo "Replace the repository source tree with this Gboard-only version; do not just extract over the old tree." >&2
    exit 1
  fi
done

echo "Gboard-only source tree check passed."
