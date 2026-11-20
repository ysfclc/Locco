#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
NDK_HOME="$(printenv ANDROID_NDK_HOME || true)"
if [[ -z "$NDK_HOME" || ! -d "$NDK_HOME/toolchains/llvm" ]]; then
  echo "ANDROID_NDK_HOME must point to an installed Android NDK." >&2
  exit 2
fi
command -v cargo >/dev/null || { echo "Rustup/Cargo is required." >&2; exit 2; }
command -v rustup >/dev/null || { echo "Rustup is required." >&2; exit 2; }
command -v cargo-ndk >/dev/null || { echo "Install cargo-ndk 4.1.2 with: cargo install cargo-ndk --version 4.1.2 --locked" >&2; exit 2; }

# Enter the repository before invoking rustup so its pinned toolchain is used
# even when this script is launched from an unrelated current directory.
cd "$ROOT/core"
EXPECTED_TOOLCHAIN="$(sed -n 's/^[[:space:]]*channel[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' "$ROOT/rust-toolchain.toml" | head -n 1)"
ACTIVE_TOOLCHAIN="$(rustup show active-toolchain | awk '{print $1}')"
if [[ -z "$EXPECTED_TOOLCHAIN" || "$ACTIVE_TOOLCHAIN" != "$EXPECTED_TOOLCHAIN"* ]]; then
  echo "Expected Rust $EXPECTED_TOOLCHAIN but rustup selected $ACTIVE_TOOLCHAIN." >&2
  exit 2
fi
CARGO_NDK_VERSION="$(cargo ndk --version 2>&1 || true)"
if [[ "$CARGO_NDK_VERSION" != *"4.1.2"* ]]; then
  echo "Expected cargo-ndk 4.1.2; found: $CARGO_NDK_VERSION" >&2
  exit 2
fi

rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
JNI_LIBS="$ROOT/android/app/src/main/jniLibs"
mkdir -p "$JNI_LIBS"

# Remove only Locco's derived libraries so failed builds cannot leave stale
# native files looking like outputs of the current source revision.
for abi in arm64-v8a armeabi-v7a x86_64; do
  mkdir -p "$JNI_LIBS/$abi"
  rm -f "$JNI_LIBS/$abi/liblocco_ffi.so"
done

cargo fmt --all -- --check
cargo test --workspace
cargo clippy --workspace --all-targets -- -D warnings
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -o "$JNI_LIBS" build --release -p locco-ffi

READELF="$NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-readelf"
if [[ ! -x "$READELF" ]]; then
  READELF="$(find "$NDK_HOME/toolchains/llvm/prebuilt" -path '*/bin/llvm-readelf' -type f -print -quit)"
fi
if [[ -z "$READELF" ]]; then
  if command -v llvm-readelf >/dev/null; then READELF="$(command -v llvm-readelf)"
  elif command -v readelf >/dev/null; then READELF="$(command -v readelf)"
  else echo "llvm-readelf or readelf is required." >&2; exit 4
  fi
fi

verify_elf() {
  local abi="$1" expected_class="$2" expected_machine="$3"
  local library="$JNI_LIBS/$abi/liblocco_ffi.so" header symbols actual_class actual_machine
  test -s "$library" || { echo "Missing native library: $library" >&2; return 1; }
  header="$("$READELF" -h "$library")"
  actual_class="$(awk -F: '/Class:/ {gsub(/^[[:space:]]+|[[:space:]]+$/, "", $2); print $2}' <<<"$header")"
  actual_machine="$(awk -F: '/Machine:/ {sub(/^[[:space:]]+/, "", $2); print $2}' <<<"$header")"
  if [[ "$actual_class" != "$expected_class" || "$actual_machine" != "$expected_machine" ]]; then
    echo "Unexpected ELF header for $abi: class=$actual_class machine=$actual_machine" >&2
    return 1
  fi
  symbols="$("$READELF" --dyn-syms --wide "$library")"
  for symbol in locco_abi_version locco_core_ping locco_buffer_free; do
    grep -Eq "[[:space:]]$symbol$" <<<"$symbols" || { echo "Missing exported symbol $symbol in $abi." >&2; return 1; }
  done
  if command -v sha256sum >/dev/null; then sha256sum "$library"; else shasum -a 256 "$library"; fi
  echo "Verified ELF: $abi ($actual_machine, $actual_class)"
}
verify_elf arm64-v8a ELF64 AArch64
verify_elf armeabi-v7a ELF32 ARM
verify_elf x86_64 ELF64 "Advanced Micro Devices X86-64"
echo "All three Rust FFI Android ABIs built and inspected."
