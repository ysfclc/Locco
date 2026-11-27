# Skill: Rust cross-build for Android with cargo-ndk

**Trigger:** Rust code, FFI symbols, native dependencies or Android packaging changes that affect .so outputs.

## Inputs and preflight
- Exact commit and Rust workspace/target package.
- Pinned Rust toolchain from rust-toolchain.toml/CI.
- Android NDK version from the workflow/build scripts.
- Required ABIs from Gradle packaging and current product support.

Confirm installed targets and NDK path before building. Do not silently upgrade Rust, NDK or cargo-ndk to make a build pass.

## Procedure
1. Read scripts/build-rust-android.sh and the Android/Rust workflows; follow their pinned versions and output path.
2. Run formatting and host workspace tests first:
   - cargo fmt --manifest-path core/Cargo.toml --all -- --check
   - cargo test --manifest-path core/Cargo.toml --workspace
3. Install only the targets required by the repository. Current CI targets are aarch64-linux-android, armv7-linux-androideabi and x86_64-linux-android.
4. Build the FFI package for each configured ABI using the repository's pinned cargo-ndk command. Current CI output is android/app/src/main/jniLibs.
5. Remove only generated outputs belonging to this build, then verify each expected ABI library exists, is non-empty and is newer/source-linked to the current build. Do not leave a stale library to satisfy a check.
6. Inspect ELF architecture, exported symbols and dynamic dependencies with the NDK/LLVM tools. Compare symbols to the canonical C header/FFI contract.
7. Assemble the APK from those exact outputs. Inspect APK contents and confirm the expected library appears under every supported lib/<abi>/ path.
8. Run smoke tests on an emulator/device for each ABI that can be executed in available infrastructure. Mark unavailable device coverage NOT RUN.
9. Record all commands, tool versions, ABI outputs and hashes. A host test or successful Gradle compile alone is not a native cross-build pass.

## Required evidence
Toolchain/NDK/cargo-ndk versions; Rust test results; each ABI target and ELF inspection; symbol/ABI check; APK source commit/hash; packaged library listing; runtime load evidence and limitations.

## Fail closed
Missing ABI, stale/mismatched .so, unresolved symbol, wrong ELF architecture, panic crossing FFI, missing target or package/library mismatch blocks the native change.
