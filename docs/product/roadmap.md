# Roadmap

## 0 — Foundation
Product, principles, architecture, FFI, Rust workspace, empty Android shell, iOS plan, security, agents, skills, ZIP protocol and CI. Gate: three Rust Android ABIs + placeholder APK; docs agree; no product logic.

## 1 — Local kernel
**IN PROGRESS:** native session workspace, pure capture kernel, five Kotlin unit tests (written; Android JUnit execution pending), local SQLite session persistence and legacy preference import. Rust defines validated platform-neutral Source, Evidence, Entity, Relationship and Event records.

**Implemented in this slice:** Rust now owns schema versions 1–2 and ordered SQLite DDL. ABI v2 exposes a bounded caller-buffer C API and Android JNI adapter. Android SQLiteOpenHelper executes those migrations, rather than defining tables itself. Version 2 adds note-to-source provenance links. ADR-0012 is proposed and requires architect, Android, QA and security review.

**Acceptance still requires:** Rust CI, all three Android native ABI builds, APK assembly, SQLite tests against the exact kernel migration statements (fresh install and populated v1 upgrade), Android instrumentation/device tests, and privacy review. ADR-0008's encryption-at-rest requirement remains a blocker before handling sensitive private notes as production-safe data. Then implement source/evidence CRUD, entity relation operations, append-only event APIs, lexical retrieval and verified export/restore.

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
