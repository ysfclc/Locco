# ADR-0015: Durable execution with transactional platform-effect outbox

**Status:** Proposed for LOC-0004 review · 2026-11-20 · Reversal cost: High

## Context

A task can span multiple screens, model calls, browser sessions and Android process lifetimes. A local state update cannot be committed atomically with a remote website/app side effect. Naive retries can duplicate irreversible actions.

## Decision proposal

Persist execution state, attempt history, approvals, event sequence and platform-effect outbox in the canonical relational store. Create a ready effect and the authorized execution transition in one transaction. Platform adapters claim effects under leases with fencing tokens and return typed results.

Delivery is at-least-once. Local command/effect deduplication is mandatory. External side effects must use provider-supported idempotency where available, receipts/postcondition verification where possible, and UNCERTAIN plus user review otherwise. Never automatically retry an uncertain external commit.

Rust is authoritative for task state and policy. Android WorkManager/foreground services provide scheduling and OS execution contexts; they do not own task truth. Browser interaction remains foreground-bound.

## Consequences

- Execution survives UI/process recreation without pretending an in-memory callback is durable.
- An interrupted operation can be classified based on durable phase evidence.
- A platform effect can be redelivered, but a stale lease cannot complete a newer attempt.
- Browser work may wait for foreground/user takeover; no hidden WebView execution is promised.
- Event/execution journals remain audit/recovery evidence; relational rows remain canonical persisted state.

## Alternatives

- Event sourcing for all domain state: not selected; see ADR-0008 and data-model.md.
- Fire-and-forget callbacks: rejected; no durable replay or reliable cancellation.
- Blind retry after timeout/process death: rejected due to duplicate side-effect risk.

## Acceptance evidence

See durable-execution-and-recovery.md. Acceptance requires crash-point fault injection, effect lease/fencing, duplicate delivery, no-blind-retry, approval binding, foreground browser and restore tests.

A non-idempotent adapter call is permitted only after Rust durably records START_INTENT for the claimed effect/fencing token. Crash before this marker can be safely requeued under the adapter contract; crash after it is UNCERTAIN until independently verified.
