# ADR-0014: Versioned Rust/native command and event protocol

**Status:** Proposed for LOC-0004 review · 2026-11-20 · Reversal cost: High

## Context

The current ABI exposes only version and ping. The product requires durable commands, read-only queries, event replay, platform effects and future iOS parity. Ad hoc JNI methods and callbacks couple domain logic to Android lifecycle and make recovery/threading difficult.

## Decision proposal

Use one versioned C ABI with:
- Opaque engine handles and explicit buffer ownership.
- Bounded UTF-8 JSON command/query/event envelopes; wire schema version separate from ABI version.
- Short synchronous command acceptance and bounded read-only queries.
- Durable operation IDs for long-running work.
- Pull-based event batches with stable sequence cursors and idempotent consumers.
- A transactional platform-effect outbox; Android claims leases and reports typed results.
- No callbacks from Rust worker threads into Kotlin/Swift.
- Typed error codes, strict pointer/length validation and panic containment.

Keep large binary content outside FFI envelopes and pass scoped blob references. Android uses one process-owned engine; iOS binding generation stays gated.

## Consequences

- Kotlin presentation and Android adapters cannot become a second domain authority.
- Process restart can reconstruct durable work from canonical persistence.
- Events/effects are at-least-once; adapters must deduplicate. Exactly-once external side effects are not promised.
- Wire schemas and ABI evolve separately with golden compatibility fixtures.
- FFI implementation cannot begin before buffer ownership, reentrancy and database lifetime contracts are tested.

## Alternatives

- Direct JNI business methods/callbacks: rejected due to platform coupling, thread/lifetime hazards and lack of durable replay.
- Full shared-memory/binary message bus: premature complexity before payload/throughput benchmarks.
- UI-owned task state with WorkManager: rejected as a source of truth because UI and OS schedulers can be recreated independently.

## Acceptance evidence

See ffi-command-event-protocol.md. This proposal becomes accepted only after C header/Android JNI tests, memory ownership tests, concurrency tests, event replay, duplicate-submit and crash-recovery contract tests pass.

The platform-effect protocol also requires a durable START_INTENT acknowledgement before any non-idempotent external call. The native adapter must not perform the operation before Rust confirms that fence.
