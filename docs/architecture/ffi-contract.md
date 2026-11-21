# Rust/native FFI contract

Canonical cross-platform boundary: versioned C ABI. Android uses a small JNI shim delegating to the Rust facade. iOS bridge generator (UniFFI vs C wrapper) remains deferred until a spike.

## ABI v2

- `locco_abi_version() -> u32` returns 2.
- `locco_core_ping() -> i32` remains a foundation smoke symbol.
- `locco_schema_version() -> u32` returns the canonical SQLite schema version from locco-kernel.
- `locco_migration_statement_count(version) -> u32` returns the ordered statement count, or zero for an unsupported version.
- `locco_migration_statement_len(version, index) -> i64` returns UTF-8 byte length, or -1 for an unsupported statement.
- `locco_migration_statement_copy(version, index, output, capacity) -> i32` copies UTF-8 bytes into caller-owned memory without a terminator. Codes: 0 success, -1 unknown statement, -2 null output, -3 insufficient capacity. Caller guarantees a writable span of capacity bytes.
- `locco_buffer_free(ptr, len)` remains reserved; no Rust-owned output buffer is returned by this migration API.

All schema statements and version sequencing live in locco-kernel. Platform shells execute the ordered statements inside their database transaction; they do not duplicate DDL or choose migration versions. Android's JNI adapter exposes schema version, statement count and statement strings to SQLiteOpenHelper.

## Safety rules

Use the `locco_` prefix; explicit ABI version; UTF-8 pointer+length inputs; caller-owned output buffers or explicit free for any future Rust-owned buffers; no Rust references cross the boundary; validate null/length/encoding; no panic unwinds; stable error codes; no secrets in diagnostics; bounded synchronous foundation calls; future long jobs use durable IDs/events. Changes require an ADR and parity tests.

## Compatibility

Android refuses to open the database if the loaded ABI is not v2. SQLite schema version is independent from ABI version and advances only through sequential Rust-owned migrations. Downgrades are rejected; migrations never drop user data. Future incompatible bridge changes require a new ABI version and a documented compatibility plan.
