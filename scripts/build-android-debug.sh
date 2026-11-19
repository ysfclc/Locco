#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
command -v gradle >/dev/null || { echo "Gradle 8.9+ is required. Install it or use the GitHub Actions build." >&2; exit 2; }
test -f "$ROOT/android/app/src/main/jniLibs/arm64-v8a/liblocco_ffi.so" || "$ROOT/scripts/build-rust-android.sh"
gradle -p "$ROOT/android" --no-daemon assembleDebug
APK="$ROOT/android/app/build/outputs/apk/debug/app-debug.apk"
test -s "$APK"
sha256sum "$APK"
echo "Debug APK: $APK"