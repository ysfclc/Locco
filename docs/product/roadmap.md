# Roadmap

## 0 — Foundation
Product, principles, architecture, FFI, Rust workspace, empty Android shell, iOS plan, security, agents, skills, ZIP protocol and CI. Gate: three Rust Android ABIs + placeholder APK; docs agree; no product logic.

## 1 — Local kernel
**IN PROGRESS:** native session workspace, pure capture kernel, five unit tests (not executed), and a SQLite kernel schema with sessions/notes, sources, entities, entity relations, and event log. Session/note storage now uses atomic SQLite transactions. Legacy SharedPreferences session JSON is imported on first database creation. Foreign-key enforcement is enabled. Build/tests remain unverified here. Next: validate the database and run Android CI; add migration and round-trip tests; then implement evidence/source linking and searchable retrieval. Gate: deterministic, migration, round-trip and offline tests.

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