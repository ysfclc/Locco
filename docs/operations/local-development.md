# Local development

Required tools:
- Git.
- Rustup/Cargo **1.86.0**, pinned by `rust-toolchain.toml`, with `rustfmt`, `clippy` and the three Android targets.
- Android SDK + NDK `27.2.12479018`; set `ANDROID_NDK_HOME`.
- JDK 17+.
- Gradle **8.9**, matching CI (the repository currently does not include a Gradle wrapper).
- `cargo-ndk` 4.1.2.
- Python 3 and actionlint for foundation/workflow checks.

The FFI artifact name is `liblocco_ffi.so`, not `liblocco_core.so`. Output paths:
- `android/app/src/main/jniLibs/arm64-v8a/liblocco_ffi.so`
- `android/app/src/main/jniLibs/armeabi-v7a/liblocco_ffi.so`
- `android/app/src/main/jniLibs/x86_64/liblocco_ffi.so`

Run:
```bash
./scripts/verify-foundation.sh
./scripts/build-android-debug.sh
```

The debug APK script always rebuilds and validates the Rust FFI libraries for all three ABIs before running `assembleDebug`. Use `./scripts/build-rust-android.sh` separately only when you intentionally want to build/test the native libraries without assembling the APK.

Device smoke: install the APK, launch it, verify the ABI/runtime ping, rotate, background/restore and inspect logcat. Record exact device, Android build, APK SHA-256 and outcome. A CI build is not physical-device verification.
