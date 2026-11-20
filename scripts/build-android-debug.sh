#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
command -v gradle >/dev/null || {
  echo "Gradle 8.9 is required. Install it or use the GitHub Actions build." >&2
  exit 2
}

GRADLE_VERSION="$(gradle --version | awk '/^Gradle / { print $2; exit }')"
if [[ "$GRADLE_VERSION" != "8.9" ]]; then
  echo "Expected Gradle 8.9 to match CI; found: ${GRADLE_VERSION:-unknown}" >&2
  exit 2
fi

# Always rebuild all native ABIs from the current Rust source. Do not trust the
# presence of one library (or stale jniLibs from a previous checkout).
"$ROOT/scripts/build-rust-android.sh"

for abi in arm64-v8a armeabi-v7a x86_64; do
  test -s "$ROOT/android/app/src/main/jniLibs/$abi/liblocco_ffi.so" || {
    echo "Native library is missing for $abi." >&2
    exit 3
  }
done

gradle -p "$ROOT/android" --no-daemon assembleDebug

APK="$ROOT/android/app/build/outputs/apk/debug/app-debug.apk"
test -s "$APK"
sha256sum "$APK"
echo "Debug APK: $APK"
