# Rust/native FFI contract

Canonical cross-platform boundary: versioned C ABI. Android uses small JNI shim delegating to Rust facade. iOS bridge generator (UniFFI vs C wrapper) deferred until spike.

Rules: `locco_` prefix; explicit ABI version; UTF-8 pointer+length inputs; owned opaque output buffers with explicit free; no Rust references cross boundary; validate null/length/encoding; no panic unwinds; stable error codes; no secrets in diagnostics; bounded synchronous foundation calls; future long jobs use durable IDs/events. Changes require version, ADR and parity tests.

Scaffold symbols: `locco_abi_version() -> u32`, `locco_core_ping() -> i32`. They prove linking only.