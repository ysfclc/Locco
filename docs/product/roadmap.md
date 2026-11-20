# Roadmap

The roadmap describes cumulative product evolution. **A phase never resets the product, codebase, user data, accepted requirements or unresolved feedback.** Bugs and regressions can be fixed in any phase; the phase number is not permission to defer existing defects or discard earlier work.

All work links to GitHub feedback records (`LOC-####`). At phase start, read the existing issue history and baseline; at phase close, carry all unresolved work forward with an explicit disposition.

## 0 — Foundation
Product authority, architecture, FFI, Rust workspace, empty Android shell, iOS plan, security, agents, skills and CI. Gate: verified three Android Rust ABIs + placeholder APK; docs agree; no product logic. The continuity constitution and executable PR gate are part of the foundation acceptance.

## 1 — Local kernel
DB, migrations, IDs, evidence, entity graph, events, export/restore. Gate: deterministic, migration, round-trip, integrity and offline tests. Existing data is never reset to simplify migrations.

## 2 — Acquisition
Source contract, share/import, file and visible browser capture. Gate: provenance, dedupe, cancellation, resumption and hostile-input tests. Previously working import and capture paths remain regression-tested.

## 3 — Local intelligence
Benchmark model/embedding candidates; context compiler, retrieval, schema validation and golden set. Gate: measured latency/RAM, bounded context and deterministic fallback. Local/offline behavior remains available.

## 4 — Workspace
Native navigation, search, entity/evidence views and review queue. Gate: accessibility, adaptive layout, process restoration and adjacent navigation regressions.

## 5 — Capabilities
Typed plans, policy, approvals, idempotency, execution and verification. Gate: threat review, permission denial/cancellation tests and end-to-end postcondition proof.

## 6 — Expansion/iOS
New sources/platform only after contract parity, data migration plan, resource evaluation and explicit product/architecture approval. No phase starts without prior acceptance evidence.

## Cross-phase continuity gates

- Keep all accepted or unresolved `LOC-####` issues visible.
- Do not close or downgrade an old issue merely because the roadmap advanced.
- Preserve stable Android package/signing identity, data and versioned Rust/FFI contracts.
- Every release is tested as an update from the supported baseline where one exists.
- A bug fix requires regression proof; a feature requires adjacent-flow regression coverage.
- Roadmap status reflects actual code, tests, runtime/device evidence and released artifacts, not planned work.
