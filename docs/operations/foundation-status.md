# Foundation status ledger

As of 2026-11-22. Update only with reproducible evidence.

| Gate | Status | Evidence/blocker |
|---|---|---|
| Product identity/scope | Drafted | docs/product |
| Architecture, context contract and FFI | Drafted | docs/architecture |
| ADRs | Drafted | docs/decisions |
| 7 agent roles | Present | .agent/agents |
| 10+ skills | Present | .agent/skills (13) |
| Security doctrine | Drafted | docs/security |
| ZIP validator adversarial tests | Locally verified | 9 Python tests passed in isolated local harness |
| Foundation archive | Created and CRC-checked | incoming/foundation.zip; 104 tracked source files |
| Rust workspace compile/tests | Verified in CI | Rust 1.86 format, tests and clippy pass on observer schema v3 commit `e693a878` |
| Cargo.lock | Committed | core/Cargo.lock; Rust workspace currently has no third-party crate dependencies |
| Three Android Rust ABIs | Pending latest Android CI | NDK/cargo-ndk workflow currently running for observer implementation |
| Android debug APK | Pending latest Android CI | Android workflow assembles APK, runs unit tests and emulator instrumentation; earlier emulator boot stalled, CI now caches SDK image and bounds startup |
| Security and policy checks | Pending latest CI | Workflow validation, dependency audit, hostile ZIP tests and secret scan run on GitHub Actions |
| Gradle dependency verification | Not enforced yet | generate and commit verification metadata from a trusted Gradle build |
| Observer device smoke | Not run | must enable notification access, allowlist a test app, receive a notification with Locco backgrounded, then verify feed and revocation on target Redmi device |
| Branch protection/environment reviewers | Not configured here | owner must enable in GitHub repository settings |
| Brainstorm source archive | Transcribed | mission brief is preserved as a content transcription; original conversation remains authoritative |

Do not mark Phase 0 complete until every required gate has reproducible evidence.


## Android observer first slice (prototype, not accepted yet)

- [x] NotificationListenerService adapter registered behind Android's explicit notification-listener permission.
- [x] User-managed package allowlist; no source package is observed by default.
- [x] Metadata-only event acquisition; notification content and extras are not read.
- [x] Rust-owned schema v3 with unique idempotency key and provenance fields.
- [x] SQLCipher persistence API and native Observer feed.
- [x] Instrumentation test for encrypted persistence, reopen and duplicate suppression.
- [ ] Latest Android CI green, including emulator instrumentation.
- [ ] Real Redmi device proof while source app and Locco are backgrounded.
- [ ] Permission revocation and process-restart behavior verified on device.
- [ ] Legacy plaintext database migration implemented and verified before sensitive-data readiness.
