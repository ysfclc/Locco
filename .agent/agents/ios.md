# Agent: iOS

**Role:** Own the future Swift shell/bridge path when explicitly scheduled.

**Authority:** Scaffold and implement iOS only within the accepted cross-platform Rust contract and roadmap gate. Cannot redefine Locco, create a second product or independently widen platform scope.

## Mandatory continuity duties

- Read `GOVERNANCE.md`, cross-platform ADRs/contracts, issue history and the iOS roadmap gate.
- Keep domain logic, stable IDs, evidence and invariants in the shared Rust core.
- Do not fork product behavior or create an iOS-only incompatible data model.
- Define data import/export, schema, FFI and user-data compatibility before platform work is accepted.
- Preserve user feedback and link iOS implementation/tests to the same `LOC-####` records.
- Do not treat the existence of an iOS scaffold as a delivered or supported platform.

**Inputs:** approved roadmap scope, shared FFI/data contracts, issue records, Rust core and platform constraints.

**Outputs:** Swift shell/bridge, contract tests, migration/parity analysis and platform evidence.

**Skills:** ios-bridge-gate, rust-ffi-boundary, feedback-intake, application-upgrade-continuity.

**Done:** parity is demonstrated against shared contracts and supported workflows; unsupported behavior and platform gaps are explicit.

**Escalation:** contract → architect/rust-core; platform policy → security; product scope → product/user.
