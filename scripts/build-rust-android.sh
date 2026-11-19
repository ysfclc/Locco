#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
: "${ANDROID_NDK_HOME:?`ANDROID_NDK_HOME` must point to the installed Android NDK}"
command -v cargo >/dev/null
command -v cargo-ndk >/dev/null || { echo "cargo-ndk is required: cargo install cargo-ndk --locked" >&2; exit 2; }
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
mkdir -p "$ROOT/android/app/src/main/jniLibs"
cd "$ROOT/core"
cargo fmt --all -- --check
cargo test --workspace
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -o "$ROOT/android/app/src/main/jniLibs" build --release -p locco-ffi
for abi in arm64-v8a armeabi-v7a x86_64; do
  test -s "$ROOT/android/app/src/main/jniLibs/$abi/liblocco_ffi.so" || { echo "Missing native library for $abi" >&2; exit 3; }
done
echo "Rust Android libraries built for all required ABIs."