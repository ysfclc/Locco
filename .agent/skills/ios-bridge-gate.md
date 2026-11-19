# Skill: iOS bridge gate

When: considering iOS code/CI. Preconditions: Android FFI stable, contract tests pass, user approves iOS phase.

Procedure: review C ABI ownership; spike UniFFI vs C wrapper; define XCFramework slices/min iOS; document macOS/Xcode/signing/privacy; add parity tests before UI; keep domain in Rust.

Verify bridge round-trips same fixtures as Android.

Failure: ABI/binding drift or Apple signing dependency. Keep gate closed.