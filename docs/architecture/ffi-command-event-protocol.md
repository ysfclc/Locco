# Rust/native command, query and event protocol

**Status:** Proposed contract for LOC-0004; ABI/schema details require contract tests before implementation.  
**Scope:** Rust core ↔ Kotlin/Android today; future Swift/iOS compatibility over the same domain protocol.

## 1. Design decisions

- Keep one versioned C ABI as the cross-platform binary boundary. Android JNI is a thin marshalling shim; Swift bindings are deferred.
- Use bounded UTF-8 JSON envelopes for small commands, queries, acknowledgements and event batches. JSON is transport serialization, not the canonical storage serializer or a signature/hash canonicalization format.
- Never pass domain structs, Kotlin objects, Rust references, allocator-owned containers, exceptions or callbacks across the ABI.
- Long-running work is accepted durably and addressed by operation/run/effect IDs. It never blocks a JNI call or a UI frame.
- Rust emits durable events and platform-effect requests. Native code pulls/claims them and returns typed outcomes. Rust does not call into Kotlin/Swift from an arbitrary worker thread.
- Binary content is passed by opaque, scoped blob references or platform streams. Do not copy model weights, page snapshots or large files through a JSON FFI buffer.
- Every command/query/event has separate protocol schema version and ABI version. A breaking wire change must be explicit and tested.

## 2. Process and handle lifecycle

The Android Application composition root opens one Locco engine handle per application process. Activities, ViewModels and WorkManager integrations use the same process-owned service; an Activity does not create and destroy its own domain engine.

Engine open validates storage/key-provider configuration, opens canonical persistence and migrations, and returns a handle plus readiness diagnostics. It must not silently wipe or reset data. A migration/key failure returns a typed recovery state.

Engine close stops accepting new calls, waits for or safely interrupts bounded in-process work, releases native resources, and invalidates that handle. Durable executions remain persisted for the next process.

The initial Android application uses one process. Multi-process engine access is not assumed. If introduced later it needs an explicit single-writer/IPC design and cross-process locking tests.

## 3. ABI surface

Proposed function families (exact C typedef names are finalized with the header and ABI test):

| Function | Contract |
|---|---|
| ABI version | Return binary ABI version, no side effects |
| Engine open / close | Create and destroy an opaque process-owned engine handle |
| Command submit | Validate and transactionally accept a short command; return accepted/rejected/deduplicated result and durable operation ID |
| Query | Execute a bounded read-only query and return a projection |
| Events read | Read a bounded durable event batch after a sequence cursor |
| Events acknowledge | Acknowledge a consumer cursor only after that consumer durably applied the batch |
| Effect claim | Claim a pending platform effect under a lease and return its typed request |
| Effect complete | Complete, fail or mark uncertain a claimed effect using the matching fencing token |
| Command cancel | Request cancellation by durable operation/run ID |
| Buffer free | Release a returned Rust-owned byte buffer using its exact pointer/length pair |

The current ABI/Ping functions prove linking only. This table is a proposed contract, not implemented symbols.

## 4. Wire envelopes

Command request:
- protocol version and unique request ID.
- stable command ID and idempotency key for retryable submissions.
- correlation ID and optional causation ID.
- actor/authority reference supplied by trusted native composition code, never invented by a model.
- deadline and resource budget where applicable.
- command type and typed payload.

A duplicate command with the same idempotency scope/key and same canonical request resolves to the original result; reuse with a different request is rejected as a conflict.

Response:
- protocol version and request ID.
- status: accepted, completed-inline, duplicate, rejected or failed.
- durable operation ID when work continues asynchronously.
- typed error code, retryability and safe user-facing message.
- current canonical revision/cursor where relevant.

Queries are read-only, bounded and paginated. Results carry schema version, projection revision and continuation cursor. They cannot execute operations or mutate authority.

Events carry stable event ID, monotonic per-store sequence, schema version, type, aggregate/operation/run IDs, correlation/causation IDs, UTC time and bounded typed payload/reference. Event payloads contain no secrets or arbitrary source page dumps.

## 5. Event delivery semantics

Events are durable records in the canonical local store or its associated journal. Consumers poll with an after-sequence cursor and bounded limit; they receive a next sequence and has-more flag.

Delivery is **at least once**, not exactly once. If the process dies before the consumer stores its cursor, events may be replayed. Every event has a stable ID/sequence and consumers must apply updates idempotently. Consumer cursor persistence is atomic with the consumer's own durable projection/update.

A UI projection can be rebuilt from canonical queries; event delivery is a notification/update mechanism, not the source of truth. A consumer must never acknowledge a cursor before its state update is durable.

Retention/compaction is separate from read/ack. It may prune only data outside the recovery/audit retention policy and after all required consumers/checkpoints have advanced. User-visible execution history cannot disappear merely because the UI last read a batch.

## 6. Platform effect delivery

Android owns execution of operations that require Android APIs, browser instances, user interaction, lifecycle or permissions. Rust owns whether the request is valid and authorized.

1. Rust transactionally writes the execution transition and an effect-outbox record.
2. The Android adapter claims the effect with a lease, stable effect ID, attempt number and unguessable fencing token.
3. The adapter validates current platform state, permissions, profile/tab/document epoch and foreground requirements before starting.
4. It reports started and then a typed completion, failure or uncertain result. A completion must include the matching effect ID and fencing token.
5. A stale worker cannot complete an effect after a newer lease/attempt has taken ownership.
6. Expired leases are not proof that the side effect did not occur. Non-idempotent effects move to UNCERTAIN and require receipt/postcondition lookup or user review before retry.

Effect request payloads are typed and bounded. No command allows arbitrary Kotlin reflection, arbitrary intent launch, arbitrary shell execution, arbitrary JavaScript evaluation or unrestricted file access.

## 7. Memory ownership and errors

- Every input pointer is borrowed for the duration of the call only and must be paired with an explicit byte length.
- Null pointers, impossible lengths, invalid UTF-8, unsupported schema versions and over-limit messages return stable error codes.
- The caller must provide readable memory for the declared input length; native code cannot make an invalid foreign pointer safe.
- Returned buffers are Rust-owned opaque byte slices. The caller treats them as immutable bytes and calls the exact free function once. Never free with Kotlin/JVM/Swift allocators.
- No panic may unwind through C/JNI. Each exported entry point contains panics and converts them to a stable internal error code; release builds must not rely on panics for expected control flow.
- Errors and logs contain codes, safe explanations and correlation IDs only—never user content, credentials, cookies, private page text or tokens.
- Buffer limits are explicit, checked before allocation, and configurable only through trusted engine configuration.

## 8. Concurrency, cancellation and deadlines

- Calls on one engine handle are thread-safe under documented synchronization; no lock is held while invoking Android UI or blocking platform APIs.
- Short commands perform validation and durable acceptance only. Queries have bounded work and response limits.
- Long operations return an operation/run ID and progress via durable events.
- Cancellation is a request, not proof that an external side effect was undone. The run records whether cancellation happened before dispatch, during an operation or after a possibly completed side effect.
- Deadlines and resource budgets are enforced by the Rust coordinator and adapter; an expired deadline cannot bypass approval or trigger an unbounded retry.
- No native callback is invoked from Rust worker threads. Kotlin/Swift pull events and claim effects from lifecycle-controlled execution context.

## 9. Contract verification

Required tests before implementing product commands:
- C header compiles as C and C++; Android JNI loads and ABI version matches.
- Exact output-buffer ownership/free, zero-length and malformed input tests.
- Null/length/UTF-8/oversized/schema-version negative tests.
- Panic containment and stable error-code tests.
- Multi-thread command/query/event interleaving and no-deadlock tests.
- Duplicate command idempotency and command replay after process death.
- Event cursor replay, duplicate application and durable acknowledgement tests.
- Effect lease expiry, fencing-token rejection and uncertain-side-effect tests.
- Rust/Kotlin golden serialization compatibility; future Swift parity uses the same fixtures.
- No secret/private data appears in error or diagnostic fixtures.

## 10. Open decisions

Resolve by ADR/spike before freezing ABI v1:
- Exact JSON schema/enum encoding and whether a later binary format is justified.
- Concrete engine-handle and owned-buffer C header declarations.
- Database driver, WAL/locking and engine-open recovery behavior.
- Event retention/compaction and per-consumer cursor storage.
- Whether all platform effects use this outbox or a subset uses narrowly scoped synchronous JNI.
- Future iOS binding generator and handle lifetime.
