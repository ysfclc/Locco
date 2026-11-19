# ADR-0009: Android Kotlin/Compose shell and pinned native build baseline

Status: Accepted · 2026-11-19 · Reversal cost: Medium

Context: Android is primary, native OS integration is central, and Rust owns cross-platform domain logic. The first known device is mid-range Android with limited thermal headroom.

Decision: Kotlin + Jetpack Compose shell, Navigation Compose, JNI bridge to Rust C ABI. Foundation build baseline: minSdk 26, compile/target SDK 35, JDK 17, AGP 8.7.3, Kotlin 2.0.21 + Compose compiler plugin, Gradle 8.9, NDK 27.2.12479018, ABIs arm64-v8a/armeabi-v7a/x86_64.

Alternatives: Flutter/React Native (extra bridge/native friction); native Kotlin domain (duplicated core); lower API baseline (more compatibility work, fewer modern APIs).

Rationale: Compose provides native adaptive UI and Rust JNI is a narrow boundary. API 35 is a foundation/sideload baseline, not a promise for Play distribution.

Failure: SDK/plugin churn, JNI complexity, targetSdk policy changes. Keep versions centralized and update as a reviewed set.

Reversal: UI toolkit is medium-cost while domain stays in Rust. API baseline changes are low-to-medium cost with device regression tests.