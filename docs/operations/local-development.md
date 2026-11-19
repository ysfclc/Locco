# Local development

Required: Git, Rust stable/rustup, Android SDK + NDK, JDK 17+, Gradle version pinned by CI, cargo-ndk, Python 3 and actionlint. Rust targets: `aarch64-linux-android`, `armv7-linux-androideabi`, `x86_64-linux-android`. Output: `android/app/src/main/jniLibs/<abi>/liblocco_core.so`.

Run `./scripts/verify-foundation.sh`, `./scripts/build-rust-android.sh`, `./scripts/build-android-debug.sh`. Scripts fail clearly if tools/env are missing; no model weights or paid services are downloaded.

Device smoke: install APK, launch, verify ABI label, rotate, background/restore, inspect logcat. Record device/Android version, APK SHA-256 and result. CI build is not device verification.