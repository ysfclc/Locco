# Local development

Required tools:
- Git.
- Rustup/Cargo **1.86.0**, pinned by rust-toolchain.toml, with rustfmt, clippy and the three Android targets.
- Android SDK + NDK **27.2.12479018**; set ANDROID_NDK_HOME.
- JDK 17+.
- Gradle **8.9**, matching Android Gradle Plugin **8.7.3** and CI. The repository currently does not include a Gradle wrapper.
- cargo-ndk **4.1.2**.
- Python 3 and actionlint for foundation/workflow checks.

The native artifact is liblocco_ffi.so, not liblocco_core.so. Output paths:
- android/app/src/main/jniLibs/arm64-v8a/liblocco_ffi.so
- android/app/src/main/jniLibs/armeabi-v7a/liblocco_ffi.so
- android/app/src/main/jniLibs/x86_64/liblocco_ffi.so

Run from any working directory:
- ./scripts/verify-foundation.sh
- ./scripts/build-android-debug.sh

The Android debug script is the shared local/CI build entrypoint. It checks the active repository-pinned Rust toolchain, runs formatting/tests/Clippy, rebuilds all three ABIs, inspects ELF class/machine and required symbols, assembles the APK, then checks package/version identity, SDK levels, native library presence, signature and SHA-256. A missing tool, stale ABI, wrong architecture or unexpected APK identity fails the build.

Device smoke remains separate: install the exact APK, launch it, verify native ABI ping, rotate, background/restore and inspect logcat. Record device, Android build, APK SHA-256 and outcome. A CI build is not physical-device verification.
