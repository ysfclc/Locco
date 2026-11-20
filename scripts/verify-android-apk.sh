#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APK="$ROOT/android/app/build/outputs/apk/debug/app-debug.apk"
if [[ "$#" -ge 1 ]]; then APK="$1"; fi
BUILD_TOOLS_VERSION="$(printenv BUILD_TOOLS_VERSION || echo 35.0.0)"
SDK_ROOT="$(printenv ANDROID_HOME || true)"
if [[ -z "$SDK_ROOT" ]]; then SDK_ROOT="$(printenv ANDROID_SDK_ROOT || true)"; fi
if [[ -z "$SDK_ROOT" ]]; then echo "ANDROID_HOME or ANDROID_SDK_ROOT is required." >&2; exit 2; fi
test -s "$APK" || { echo "APK missing or empty: $APK" >&2; exit 2; }
AAPT="$SDK_ROOT/build-tools/$BUILD_TOOLS_VERSION/aapt"
APKSIGNER="$SDK_ROOT/build-tools/$BUILD_TOOLS_VERSION/apksigner"
for tool in "$AAPT" "$APKSIGNER"; do test -x "$tool" || { echo "Missing Android SDK tool: $tool" >&2; exit 2; }; done
command -v unzip >/dev/null || { echo "unzip is required." >&2; exit 2; }

BADGING="$("$AAPT" dump badging "$APK")"
grep -Eq "package: name='com.locco.app.debug' versionCode='1' versionName='0.1.0-foundation'" <<<"$BADGING" || {
  echo "Unexpected package/version identity; expected com.locco.app.debug, code 1, version 0.1.0-foundation." >&2
  printf '%s\n' "$BADGING" >&2
  exit 3
}
grep -Fq "sdkVersion:'26'" <<<"$BADGING" || { echo "Expected minSdk 26." >&2; exit 3; }
grep -Fq "targetSdkVersion:'35'" <<<"$BADGING" || { echo "Expected targetSdk 35." >&2; exit 3; }
ENTRIES="$(unzip -Z1 "$APK")"
for abi in arm64-v8a armeabi-v7a x86_64; do
  grep -Fxq "lib/$abi/liblocco_ffi.so" <<<"$ENTRIES" || { echo "Missing liblocco_ffi.so for $abi in APK." >&2; exit 4; }
done
if grep -Eq '^lib/[^/]+/liblocco_core\.so$' <<<"$ENTRIES"; then
  echo "Obsolete liblocco_core.so found in APK." >&2
  exit 4
fi
SIGNATURE="$("$APKSIGNER" verify --verbose --print-certs "$APK" 2>&1)" || {
  echo "APK signature verification failed:" >&2; printf '%s\n' "$SIGNATURE" >&2; exit 5
}
grep -Fq "Verifies" <<<"$SIGNATURE" || { echo "apksigner did not confirm the signature." >&2; exit 5; }
echo "APK package, version, SDK levels, three required native ABIs and signature verified."
printf '%s\n' "$SIGNATURE"
if command -v sha256sum >/dev/null; then sha256sum "$APK"; else shasum -a 256 "$APK"; fi
echo "Verified APK: $APK"
