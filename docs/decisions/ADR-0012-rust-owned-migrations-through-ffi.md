# ADR-0012: Rust-owned SQLite migration catalogue through versioned FFI

Status: Proposed · 2026-11-21 · Owners: Rust Core + Architect · Review: Android + QA + Security

## Context

ADR-0008 makes Rust the owner of canonical schema and migrations while platform shells own database lifecycle. The first Android slice currently defines its schema in Kotlin, so a second native shell could silently diverge. SQLiteOpenHelper also needs sequential, transactional upgrade callbacks that preserve existing version-1 user data.

## Decision

Move canonical SQLite migration statements and the current schema version into `locco-kernel`. The kernel exposes immutable, ordered SQL statements per migration version. The FFI publishes them through ABI v2:

- `locco_schema_version() -> u32`
- `locco_migration_statement_count(version) -> u32`
- `locco_migration_statement_len(version, index) -> i64`
- `locco_migration_statement_copy(version, index, output, capacity) -> i32`

The copy API writes UTF-8 bytes into caller-owned memory; it never returns an allocated Rust pointer. Return codes are stable: 0 success, -1 unknown statement, -2 null output, -3 insufficient capacity. Android JNI adapts the same kernel catalogue into Kotlin strings. Android remains responsible for SQLiteOpenHelper lifecycle, enabling foreign keys, executing statements in the framework's migration transaction, and importing its legacy preferences. It must not own canonical DDL.

Schema v1 reproduces the already shipped first-slice schema. Schema v2 adds `note_sources` and its lookup index, linking notes to source records with cascading parent deletion. Existing v1 installations upgrade sequentially; no destructive reset or downgrade is permitted.

## Alternatives

- Kotlin/Room migrations: rejected because Android would own schema truth and iOS would need a separate implementation.
- Rust-owned SQLite connection: deferred; it adds cross-platform SQLite linking/build complexity and would displace the Android platform lifecycle adapter before there is a demonstrated need.
- Runtime-generated migrations: rejected because schema changes must be deterministic, reviewable and testable.

## Rationale

A single migration catalogue gives every shell the same ordered schema while keeping OS database lifecycle native. Caller-owned output buffers avoid allocator ownership crossing FFI. Versioned statements are auditable and can be tested without a device.

## Failure modes and mitigations

- ABI mismatch: reject startup before opening the database.
- Missing or empty migration: fail the migration transaction; do not advance SQLite's user version.
- SQL failure: SQLiteOpenHelper transaction rolls back the upgrade; existing records remain.
- Downgrade: reject rather than dropping or guessing at data transformations.
- JNI allocation failure: return null and let the database open fail explicitly.
- Plaintext private notes: remains a pre-existing release blocker under ADR-0008; encryption-at-rest design and migration must be completed before Locco is represented as safe for sensitive user content.

## Reversal and cost

The FFI is an adapter, not a storage engine. It can be replaced while preserving the Rust migration catalogue and schema versions. Reversal cost is low before additional data types and platforms ship; high after external installations rely on ABI v2. Future incompatible bridge changes require a new ABI version and an explicit migration path.

## Verification required

- Rust unit tests enumerate every migration and validate the C ABI copy contract and error codes.
- SQLite integration tests execute the exact kernel-provided SQL from version 0 and upgrade a populated v1 database to v2, asserting rows, foreign keys, cascade behavior and note-source joins.
- Android CI assembles all native ABIs and the APK.
- Android instrumentation/device test confirms ABI handshake, fresh install, v1 upgrade and preservation of sessions/notes.
- Architect, Android, QA and Security review before PR readiness or merge.
