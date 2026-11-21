# Roadmap

## 0 — Foundation
Product, principles, architecture, FFI, Rust workspace, empty Android shell, iOS plan, security, agents, skills, ZIP protocol and CI. Gate: three Rust Android ABIs + placeholder APK; docs agree; no product logic.

## 1 — Local kernel
**IN PROGRESS:** native session workspace, pure capture kernel, five Kotlin unit tests (written; execution pending), and a local SQLite session store with legacy preference import. Rust now defines validated platform-neutral Source, Evidence, Entity, Relationship and Event records with unit tests. Rust tests/build are not yet executed in this environment.

**Architecture gap to close before acceptance:** the current Android SQLite helper still owns the physical schema and has no explicit migration path. The accepted data contract says Rust owns migrations and cross-platform domain invariants. Design and implement the Rust-owned storage/migration boundary through the versioned FFI before adding evidence/entity persistence APIs to Kotlin. Then wire source/evidence provenance, entity relations, append-only events, searchable retrieval and export/restore. Gate: deterministic domain tests, migration/round-trip/integrity tests, offline tests, Android CI and device evidence.

## 2 — Acquisition
Source contract, share/import, file and visible browser capture. Gate: provenance, dedupe, cancellation and hostile-input tests.

## 3 — Local intelligence
Benchmark model/embedding candidates; context compiler, retrieval, schema validation and golden set. Gate: measured latency/RAM and deterministic fallback.

## 4 — Workspace
Native navigation, search, entity/evidence views, review queue. Gate: accessibility, adaptive layout and process restoration.

## 5 — Capabilities
Typed plans, policy, approvals, idempotency, execution and verification. Gate: threat review and end-to-end proof.

## 6 — Expansion/iOS
New sources/platform only after contract parity and migration plan. No phase starts without prior acceptance evidence.
