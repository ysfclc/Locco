#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
command -v gradle >/dev/null || { echo "Gradle 8.9 is required." >&2; exit 2; }
GRADLE_VERSION="$(gradle --version | awk '/^Gradle / { print $2; exit }')"
if [[ "$GRADLE_VERSION" != "8.9" ]]; then
  echo "Expected Gradle 8.9 for Android Gradle Plugin 8.7.3; found $GRADLE_VERSION." >&2
  exit 2
fi
"$ROOT/scripts/build-rust-android.sh"
gradle -p "$ROOT/android" --no-daemon assembleDebug
APK="$ROOT/android/app/build/outputs/apk/debug/app-debug.apk"
"$ROOT/scripts/verify-android-apk.sh" "$APK"
