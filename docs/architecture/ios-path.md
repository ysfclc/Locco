# iOS path — scaffolded, gated

iOS is anticipated but not built in Phase 0. Rust C ABI is the cross-platform contract. Candidate bridges: UniFFI (generated ergonomic bindings) or thin C/Objective-C wrapper (explicit ABI, more manual ownership). Choose after a real core spike.

Gate: Android contract stabilizes; ABI parity fixtures pass; user approves iOS phase; macOS runner and Xcode available. Future build emits XCFramework slices via xcodebuild. Signing, provisioning, privacy declarations and distribution require a separate security/release ADR. No iOS-specific domain logic.