# Rust/native FFI contract

**Status:** Proposed cross-platform contract; current implementation remains ABI version + ping only.

Canonical boundary: versioned C ABI. Android uses a thin JNI shim delegating to the Rust facade. Future iOS is Swift over the same C ABI; binding-generator selection remains gated.

The detailed protocol is specified in [ffi-command-event-protocol.md](ffi-command-event-protocol.md); durable execution and outbox recovery are specified in [durable-execution-and-recovery.md](durable-execution-and-recovery.md). ADR-0014 and ADR-0015 are proposals, not accepted ABI or implementation claims.

## Invariants

- locco_ symbol prefix and explicit binary ABI version.
- Opaque engine handles with explicit create/destroy lifetime.
- UTF-8 pointer+length inputs; exact owned output-buffer/free semantics.
- Never pass Rust references, allocator-owned containers, Kotlin/Swift objects or exceptions across the boundary.
- Validate null, length, encoding, schema version and configured size limits; no panic unwinds across C/JNI.
- Separate ABI version from wire-schema version.
- Small commands are durably accepted; long-running operations return durable IDs and emit replayable events.
- Read-only queries are bounded and paginated.
- Platform operations use typed, durable effects; no arbitrary native reflection, JS evaluation or shell capability.
- Rust never calls a Kotlin/Swift callback from an arbitrary worker thread.
- No secrets or private page content in errors/diagnostics.

## Current scaffold

Exported symbols:
- locco_abi_version() -> u32
- locco_core_ping() -> i32
- locco_buffer_free(ptr, len) (reserved; no current allocated buffers)

Android JNI exposes ABI version and ping. These prove linkage only. No engine handle, commands, queries, event transport, database or product behavior exists yet.

Implementation must be driven by contract/golden tests before adding product-level commands.
