#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
: "${ANDROID_NDK_HOME:?ANDROID_NDK_HOME must point to the installed Android NDK}"

command -v cargo >/dev/null || { echo "Rustup/Cargo is required." >&2; exit 2; }
command -v rustup >/dev/null || { echo "Rustup is required to install Android targets." >&2; exit 2; }
command -v cargo-ndk >/dev/null || {
  echo "cargo-ndk 4.1.2 is required: cargo install cargo-ndk --version 4.1.2 --locked" >&2
  exit 2
}

rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
JNI_LIBS="$ROOT/android/app/src/main/jniLibs"
mkdir -p "$JNI_LIBS"

# Remove only the derived Locco FFI libraries. A failed cross-build must not leave
# an older ABI artifact looking like a fresh output.
for abi in arm64-v8a armeabi-v7a x86_64; do
  mkdir -p "$JNI_LIBS/$abi"
  rm -f "$JNI_LIBS/$abi/liblocco_ffi.so"
done

cd "$ROOT/core"
cargo fmt --all -- --check
cargo test --workspace
cargo clippy --workspace --all-targets -- -D warnings

cargo ndk \
  -t arm64-v8a \
  -t armeabi-v7a \
  -t x86_64 \
  -o "$JNI_LIBS" \
  build --release -p locco-ffi

for abi in arm64-v8a armeabi-v7a x86_64; do
  library="$JNI_LIBS/$abi/liblocco_ffi.so"
  test -s "$library" || {
    echo "Missing native library for $abi: $library" >&2
    exit 3
  }
  printf '%s  %s\n' "$(sha256sum "$library" | cut -d ' ' -f 1)" "$library"
done

echo "Rust FFI libraries built and present for all required Android ABIs."
