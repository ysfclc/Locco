# Skill: iOS bridge gate

**Trigger:** Any iOS shell, Swift binding, XCFramework, Apple CI, signing or platform parity work.

## Entry gate
Do not start implementation until the roadmap explicitly authorizes the iOS phase and the user-approved issue/decision is linked. Confirm Rust ABI and serialization contracts are stable enough for a second consumer. An empty Swift directory or placeholder target is not approval.

## Procedure
1. Read accepted cross-platform ADRs, public C header, ABI/version policy, Android consumer and shared domain fixtures.
2. Inventory required domain operations, error codes, stable IDs, callbacks, ownership rules, threading, cancellation, persistence and import/export.
3. Compare C wrapper, UniFFI or another bridge only against repository constraints. Record build requirements, generated-code policy, license, maintenance, platform coverage, debugging and reversal cost in an ADR.
4. Define minimum supported iOS version, supported device architectures, Xcode/Swift versions, macOS runner requirements, signing/provisioning and privacy declarations before choosing dependencies.
5. Keep the domain in Rust. Swift may adapt platform lifecycle, permissions, presentation and Apple APIs; it must not fork business rules or become a second persistence authority.
6. Specify ownership/lifetimes, async callbacks, thread confinement, error mapping, cancellation and ABI/version negotiation in the bridge contract.
7. Use the same golden fixtures against Rust, Android and Swift. Compare normalized results, IDs, errors and serialization, not just successful compilation.
8. Define cross-platform data import/export and migration behavior. A record created on one shell must not become unreadable on the other without an explicit supported-version rule.
9. Run bridge, parity, lifecycle, permission and archive validation on a real macOS/Xcode runner. A Linux host build cannot prove Apple compilation/signing.
10. Keep the gate closed if parity, signing, tooling or user-data compatibility is not demonstrable.

## Required evidence
Authorization/roadmap gate; accepted ADR; ABI and Swift API version; toolchain/OS/architecture matrix; parity fixture results; data migration/import-export tests; signing/build evidence; unsupported flows.

## Fail conditions
Unapproved platform scope, unstable ABI, separate data model, platform-only domain logic, unowned signing/privacy requirements or missing parity evidence blocks the phase.
