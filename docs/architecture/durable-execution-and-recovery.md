# Durable execution, outbox and recovery contract

**Status:** Proposed for LOC-0004; implementation deferred until FFI and storage contracts are verified.  
**Authority:** Rust application/domain; Android adapters execute OS/browser/model operations.

## 1. Purpose

Locco tasks may outlive a screen, model inference, browser tab or Android process. A run must remain inspectable and recoverable even when the UI disappears, the renderer crashes, the device reclaims memory or a platform action returns late.

Do not equate an in-memory coroutine, WorkManager request, model callback or browser navigation with canonical task state.

## 2. Durable records

The canonical relational store owns:
- Execution and immutable ExecutionPackage reference.
- ExecutionNode/dependency state and node revision.
- ExecutionAttempt with attempt ID, idempotency key, timestamps, resource budget and checkpoint.
- Approval envelope, hash and expiry.
- PlatformEffect/outbox entry, lease/fencing-token state and typed result.
- Event sequence and consumer cursor/checkpoint.
- Outcome and evidence/receipt references.

Large inputs/outputs are referenced as encrypted blobs; raw secrets and credentials are never stored in execution payloads.

## 3. Transactional dispatch

When a node becomes ready, one database transaction:
1. Verifies current execution/node version and preconditions.
2. Revalidates capability version, permissions, approval and budget.
3. Records the attempt and stable effect ID/idempotency key.
4. Writes the platform-effect outbox entry.
5. Appends the execution transition event.

This prevents the split-brain error where an execution is marked running but its work request was never persisted, or work is queued without the state transition that authorized it.

Android's effect adapter claims only committed outbox entries. It does not independently invent or schedule work based on UI state.

## 4. States and transitions

Run/node states include:
- QUEUED, READY, RUNNING
- WAITING_FOR_PERMISSION, WAITING_FOR_USER, WAITING_FOR_FOREGROUND
- BLOCKED, RETRYABLE
- INTERRUPTED, UNCERTAIN
- COMPLETED, FAILED, CANCELLED

Transitions are validated in Rust. Terminal states cannot silently revert. A new attempt or repair produces a new versioned record, not a rewrite of history.

UNCERTAIN means an external side effect may have occurred but no receipt/postcondition proves the result. It is not interchangeable with FAILED, and cannot be automatically retried.

## 5. At-least-once execution; no false exactly-once promise

The system cannot atomically commit a local database transaction with an external website/app/provider side effect. Therefore exactly once is not a general guarantee.

Use:
- Stable command IDs and idempotency keys for local duplicate submission.
- Stable effect IDs and adapter deduplication for repeated delivery.
- Provider idempotency keys only when the provider officially supports them.
- Precondition/postcondition checks and receipts to detect whether a previous attempt already succeeded.
- UNCERTAIN plus human review when the external outcome cannot be established.

Never retry a final submit/pay/publish/send/delete-style operation merely because a callback timed out. First inspect a receipt, target state or independent evidence. If no proof is available, stop and ask the user.

## 6. Leases and fencing

A platform worker claims an effect with a time-bounded lease and monotonically increasing attempt/fencing token. Every completion references the exact token. A stale worker cannot overwrite a later attempt.

Lease expiry does not establish that execution did not start. The coordinator classifies the effect according to its last durable phase:
- Not started: safely return to ready only when adapter evidence proves no side effect began.
- Started but no result: UNCERTAIN.
- Result recorded: continue deterministic postcondition evaluation.
The adapter must report lifecycle phases durably where possible.

## 7. Cancellation, pause and takeover

- Cancel prevents new nodes/effects from dispatching and signals active adapters.
- Pause stops scheduling new work at a safe checkpoint; it does not claim to reverse a current side effect.
- Takeover yields browser/app focus to the user and suspends conflicting automation.
- Permission revocation cancels dependent nodes before more data/action can occur.
- A user can resume only after current state, preconditions and approvals are revalidated.
- Cancellation after an external commit records the commit outcome and cannot rewrite it as cancelled-without-effect.

## 8. Approval binding

An approval envelope binds:
- Run and node IDs/revision.
- Capability identity/version and operation.
- Exact target origin/account/profile/record where applicable.
- Normalized action parameters and payload hash.
- Required evidence and precondition observation IDs.
- Expected postcondition/evaluator.
- Authority scope, issued/expiry time and one-time nonce.

Any target, payload, capability, origin, observation epoch, expiry or scope change invalidates the approval. Approvals do not authorize unrelated future actions.

## 9. Browser and model integration

Browser DOM actions require a live browser session with current profile/tab/document epoch. Background workers cannot invent a hidden WebView or continue page interactions after renderer destruction. When a page needs foreground interaction, transition to WAITING_FOR_FOREGROUND or WAITING_FOR_USER.

Model inference is a cancellable, resource-budgeted operation. A model callback arriving after cancellation, model unload or execution revision change is discarded by correlation/revision check. Partial model output is not canonical state until validated and explicitly accepted by a Rust command.

## 10. Android scheduling boundary

Rust defines durable readiness, due times, dependencies and budgets. Android WorkManager/foreground services/notifications provide OS-conforming opportunities to run adapters; they are not the source of truth or a guarantee of uninterrupted work.

Use WorkManager for deferrable/retryable work; use a foreground service only when user-visible, policy-permitted work requires it. Browser interaction remains foreground-bound. If Android stops the process, restore from the canonical store and mark uncertain operations honestly.

## 11. Recovery algorithm

At engine startup:
1. Open and integrity-check canonical persistence.
2. Load non-terminal runs and immutable package/attempt records.
3. Reconcile outbox leases and adapter receipts.
4. Mark started-without-result side effects UNCERTAIN.
5. Revalidate permissions, profiles, model availability, resource budget and approval expiry.
6. Return safe not-started work to READY; never replay uncertain commits.
7. Publish recovery events and surface user decisions needed to continue.

Recovery never automatically replays an external write operation.

## 12. Failure and evaluation

Every node records typed failure class, safe diagnostic code, whether any side effect may have occurred, retryability basis, evidence/receipt, adapter/runtime version and next permitted action.

Repair creates a new execution-package/node revision linked to prior work. It may rerun pure/read-only stages under budget; it cannot silently repeat or mutate prior side effects.

## 13. Required tests

- Atomic node-transition + outbox-creation fault injection.
- Crash before claim, after claim, after start, after external commit and before result persistence.
- Duplicate command submit and duplicate effect delivery.
- Lease expiry/fencing-token stale completion.
- Idempotency keys and provider receipt behavior.
- UNCERTAIN blocks auto retry.
- Approval payload/origin/epoch/expiry tampering.
- Pause/cancel/permission revocation while adapter is active.
- Browser process/renderer death and foreground takeover.
- Model cancellation/unload and late callback.
- App process recreation, device reboot, offline recovery and storage full.
- Restore from backup does not replay a previously attempted side effect.

## 14. Open decisions

- Exact relational schema and event retention.
- Lease duration/heartbeat, retry budgets and adapter concurrency.
- Outbox consumer identity and process ownership.
- Device scheduling strategy by operation class.
- Supported compensation/undo capabilities; compensation is a new explicit action, not invisible rollback.
