# Foundation status ledger

As of 2026-11-26. Update only with reproducible evidence.

| Gate | Status | Evidence/blocker |
|---|---|---|
| Product identity/scope | Drafted | docs/product |
| Architecture, context contract and FFI | Drafted | docs/architecture |
| ADRs | Drafted | ADR-0013 remains Proposed pending review and device acceptance |
| 7 agent roles | Present | .agent/agents |
| 10+ skills | Present | .agent/skills (13) |
| Security doctrine | Drafted | docs/security |
| ZIP validator adversarial tests | Verified in CI | Security/policy run #45830686497 passed hostile ZIP validator tests |
| Foundation archive | Created and CRC-checked | incoming/foundation.zip; 104 tracked source files |
| Rust workspace compile/tests | Verified in CI | Exact head `78f53321e96a4171356ad867b7f39f4b71cc2e91`; Rust core run #45830686567 passed |
| Cargo.lock | Committed | core/Cargo.lock |
| Three Android Rust ABIs | Verified in CI | Android workflow built the configured arm64-v8a, armeabi-v7a and x86_64 libraries on exact head |
| Android debug APK | Verified in CI | PR run #45830686417 assembled and uploaded artifact #15121049048; unit tests and 23 emulator instrumentation tests passed |
| Security and policy checks | Verified in CI | Run #45830686497 passed dependency policy/audit, hostile ZIP tests, workflow validation, foundation verification and secret scan |
| Gradle dependency verification | Not enforced yet | Generate and commit verification metadata from a trusted Gradle build |
| Observer device smoke | Not run | Must enable notification access, allowlist a test app, receive a notification with Locco backgrounded, then verify feed and revocation on target Redmi device |
| Branch protection/environment reviewers | Not configured here | Owner must enable and verify in GitHub repository settings |
| Physical-device acceptance | Pending | CI emulator success does not prove behavior on the target Redmi device |
| Release signing | Not verified | Protected release signing secrets and release workflow have not been verified for this branch; only a debug APK exists |
| Brainstorm source archive | Transcribed | Mission brief is preserved as a content transcription; original conversation remains authoritative |

The current debug APK uses application ID `com.locco.app.debug` and is a separate install, not an upgrade-safe release package.

Do not mark Phase 0 complete until every required gate has reproducible evidence.


## Android observer first slice (prototype, not accepted yet)

- [x] NotificationListenerService adapter registered behind Android's explicit notification-listener permission.
- [x] User-managed package allowlist; no source package is observed by default.
- [x] Metadata-only event acquisition; notification content and extras are not read.
- [x] Rust-owned schema v3 with unique idempotency key and provenance fields.
- [x] SQLCipher persistence API and native Observer feed.
- [x] Instrumentation test for encrypted persistence, reopen and duplicate suppression.
- [x] Latest Android CI green, including emulator instrumentation (23 tests, 0 failures on exact head above).
- [ ] Real Redmi device proof while source app and Locco are backgrounded.
- [ ] Permission revocation and process-restart behavior verified on device.
- [x] Legacy v2 plaintext database migration implemented and CI-verified: full v2-to-v6 data preservation, encrypted reopen/integrity checks, unsupported-version preservation, Android framework metadata handling, and unknown-table rejection without modifying the original database.
- [ ] Physical-device migration acceptance and independent security review before sensitive-data readiness.

CI verification is not a substitute for real-device acceptance. Sensitive-data readiness remains blocked until the remaining device and security gates pass.
