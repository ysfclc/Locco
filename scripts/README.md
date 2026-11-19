# Build and ingestion scripts

- `build-rust-android.sh`: compile Rust cdylib for arm64-v8a, armeabi-v7a and x86_64.
- `build-android-debug.sh`: assemble debug APK; invokes Rust build if arm64 library is absent.
- `verify-foundation.sh`: structural/JSON/action-pin checks.
- `validate_phase_zip.py`: validate and normalize untrusted phase ZIP; never executes content.

Scripts are intentionally explicit and fail closed. The environment must supply Android SDK/NDK, JDK, Gradle, Rust and cargo-ndk as documented in docs/operations/local-development.md.