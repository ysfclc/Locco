# ADR-0001: Rust core with native shells

Status: Accepted · 2026-11-19 · Reversal cost: High

Context: Android first, credible iOS path, shared domain behavior and testability independent of UI/model runtime.

Decision: Rust owns domain kernel/contracts. Android is Kotlin/Compose + JNI. Future iOS shares versioned C ABI.

Alternatives: Kotlin-only (later rewrite); Flutter/RN (native integration and Rust boundary remain); duplicate native domains (drift).

Rationale: Rust is an explicit constraint. Thin shells preserve native lifecycle/permissions; minimal C ABI is testable.

Failure: FFI/toolchain complexity, JNI crashes, slower iteration. Keep ABI tiny, fuzz pointer boundaries, cross-build CI.

Reversal: Bridge generator can change behind ABI. Replacing Rust requires migration ADR and golden contract suite.