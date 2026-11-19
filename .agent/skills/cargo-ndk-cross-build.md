# Skill: cargo-ndk cross-build

When: building Rust libraries for Android. Preconditions: Rust stable, NDK, cargo-ndk and targets installed.

Procedure: verify NDK; install aarch64-linux-android, armv7-linux-androideabi, x86_64-linux-android; fmt/host tests; cargo ndk for arm64-v8a, armeabi-v7a, x86_64; output to android/app/src/main/jniLibs; inspect ELF ABI/symbols; assemble APK and inspect contents.

Verify all three .so files packaged and correct ABI loads on device/emulator.

Failure: missing NDK/target/linker, stale .so or wrong folder; fail if any ABI absent.