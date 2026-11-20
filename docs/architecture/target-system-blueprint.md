# Locco target system blueprint

**Status:** Proposed for LOC-0004 review. Not an accepted implementation contract until open decisions and ADRs are resolved.

## 1. Product invariant

Locco is one local-first, mobile-native intelligence and action environment. It connects user-authorized information and capabilities into one inspectable workspace:

Acquire → Preserve → Understand → Connect → Reason → Decide → Act → Verify → Learn.

It is not a collection of pre-programmed vertical workflows. It composes reusable data, knowledge, tools, procedures, policies and evaluators for user-defined goals. Named scenarios are test cases or editable compositions, not hardcoded branches in the app.

The Locco interface is native Android. Remote websites are rendered inside its embedded browser engine; this does not create a Locco web UI. Android is primary. iOS is an anticipated native shell over shared contracts, but remains gated.

## 2. Qualities that outrank convenience

1. One continuing product, one canonical user-owned workspace and stable app identity.
2. Local-first canonical data, evidence and execution history. Network sources and remote models are optional adapters.
3. Immutable evidence before interpretation. Observations, claims, entity projections, model interpretations and curated memory remain distinct.
4. Deterministic authority. Rust owns domain identity, invariants, validation, transitions, capability contracts, policy, approval binding, idempotency and verification.
5. Replaceable intelligence. Models may interpret and propose; they cannot grant authority, mutate canonical state directly or certify side-effect success.
6. General-purpose composition; no per-use-case planner branches.
7. Explicit consent, approvals, takeover, pause, cancellation, uncertainty and recovery.
8. Honest access: capability is available only when runtime and permissions demonstrate it.
9. Typed failure and safe degradation; missing model, network, permission, source or storage must not fabricate success.
10. Full traceability from requirement through decision, implementation, tests and release.

## 3. Five cooperating planes

| Plane | Responsibility | Primary owner |
|---|---|---|
| Experience | Native workspace, browser chrome, chat, model hub, evidence, approvals and settings | Android UI; no domain policy |
| Platform/adapters | WebView, Android lifecycle, permissions, files, local runtimes, notifications and OS integrations | Kotlin/platform adapters |
| Intelligence | Catalogue discovery, retrieval, context, local inference, DOM Stitching and evaluation proposals | Rust orchestration contracts plus replaceable runtime adapters |
| Control | Task specification, capability assembly, readiness, policy, approval, execution journal and postconditions | Rust application/domain |
| Data | Canonical records, evidence, encrypted blobs, migrations, indexes, audit and backup | Rust persistence contracts plus platform key provider |

External websites, model repositories and third-party providers are untrusted boundary systems. Their content is data, not executable authority.

## 4. Responsibility matrix

| Component | Owns | Must not own |
|---|---|---|
| Native shell | Navigation, adaptive layout, presentation, accessibility, visible browser controls and OS lifecycle | Canonical business rules or approval policy |
| Browser adapter | WebView lifecycle, profile binding, cookies/storage, page scripts, DOM observations and user handoff | Model-generated code or business-specific workflows |
| Source adapter | Permission-aware acquisition, format parsing, provenance and source errors | Canonical identity or direct unsanctioned mutation |
| Model catalogue | Model metadata, source revisions, artifact files, tasks, licenses and provenance | Compatibility claims without a runtime test |
| Model manager | Download/import/verify/install/load/unload lifecycle and resource accounting | Domain truth or task authorization |
| Runtime adapter | Inference/embedding/reranking/vision/audio execution and health | Deciding what data may leave device or what action is permitted |
| Context compiler | Select evidence and memory under a task budget | Dumping the full database or transcript into a prompt |
| Capability compiler | Resolve requirements to trusted components, validate readiness and build a typed package | Fixed vertical workflows or permission escalation |
| Execution coordinator | Durable async runs, events, cancellation, checkpoints and recovery | Treating a missing callback as proof that a side effect failed |
| Policy/approval | Permission, scope, exact approval envelope and idempotency rules | Trusting model authority |
| Domain kernel | IDs, invariants, canonical records, commands and transitions | Android, UI, browser, model SDK or storage driver dependencies |
| Persistence | Encrypted canonical store, blobs, migrations, indexes and backup | Competing stores that become separate sources of truth |

## 5. Dependency direction

Native UI → Android platform adapters → versioned FFI/application commands → Rust application services → Rust domain contracts.

Persistence, model engines, browser, OS and source systems implement ports used by application services. The domain kernel imports no Android API, browser type, model SDK, network client, UI type or storage driver.

Android owns work that must run through platform APIs (WebView, permissions, foreground work, Keystore, file picker and Compose lifecycle). It returns typed results/events to Rust. The shell must not reimplement policy.

## 6. Canonical information model

- Source and Capture record where material came from and how it entered Locco.
- Evidence is immutable original or losslessly captured material with content hash, source locator and capture metadata.
- Observation is a parser-produced fact candidate linked to evidence spans.
- Entity and Relationship provide stable identity and typed graph links.
- Claim is a proposition with support, contradiction, confidence and provenance.
- Interpretation is derived, versioned output from parser, rule or model.
- Memory is separate, curated and scope-limited, with confirmation and supersession history.
- Task and Goal define desired outcomes and completion conditions.
- Capability components define reusable procedures, tools, data bindings, constraints and evaluators.
- Plan and Execution contain compiled graphs, versions, budgets, approvals, attempts and outcomes.
- Event is append-only state-transition evidence.
- Artifact is a generated/imported deliverable linked to the source evidence and component versions used.

Conflicts coexist. A newer correction may supersede an assertion for retrieval while preserving history. The canonical relational store is the source of truth; append-only events and execution journals provide audit/recovery evidence and do not silently turn the whole system into an event-sourced database. Evidence/blob storage is referenced by canonical records and follows one encryption, key-rotation and deletion policy. Indexes and embeddings are rebuildable and are not canonical truth.

## 7. Commands, events and FFI

All durable mutations pass through typed Rust commands. A command carries operation ID, schema version, actor/authority context, expected record version where relevant and idempotency key where repetition is possible.

Events include stable IDs, correlation IDs, timestamps and typed status, but do not copy private payloads into diagnostics.

The FFI must be a versioned protocol, not a collection of ad hoc JNI functions. The proposed shape requires:
- engine handle creation/destruction and explicit lifetime;
- bounded versioned command/query envelopes;
- owned output buffers and explicit free semantics;
- bounded event drain/subscription with correlation IDs;
- ABI and schema versions;
- panic/error containment and pointer/length/UTF-8/size validation;
- no Rust references, allocator objects or exceptions crossing FFI;
- no secrets in errors.

The proposed command/event transport is specified in ffi-command-event-protocol.md and ADR-0014; durable execution and transactional outbox semantics are specified in durable-execution-and-recovery.md and ADR-0015. These remain proposals until the C/JNI contract tests pass. The current ABI/JNI ping only proves linking.

## 8. Durable execution

Any operation that outlives a UI frame is a durable execution with run ID, typed state, resource/time budget, checkpoint, cancellation and event journal. State transition plus dispatch intent is committed atomically through the platform-effect outbox; delivery is at-least-once and uncertain external effects are never blindly retried.

Required states include queued, ready, running, waiting-for-permission, waiting-for-user, blocked, retryable, interrupted, uncertain, completed, failed and cancelled. A side effect running at process death is uncertain until a postcondition or independent receipt proves its outcome. It must not be replayed blindly.

Android background work must obey platform lifecycle, foreground-service and battery policy. Do not assume an invisible/off-screen WebView runs indefinitely or survives process death.

## 9. Trust boundaries

- Website content, DOM text, files, model metadata/output, imported archives and provider responses are untrusted data.
- Page content cannot change instruction hierarchy or activate a tool.
- A model proposes typed actions; Rust checks capability, target, locator evidence, scope, policy, approval, budgets and current observation.
- Passwords, cookies, passkey assertions, OTPs and recovery codes never enter model context or diagnostics.
- Remote content receives no privileged native bridge or arbitrary file access.
- Network egress is classified; remote inference requires explicit scope/consent and redaction.
- External side effects require exact target/payload/authority/expiry binding, idempotency and postcondition evidence.
- Kill, pause, permission revocation, logout and source changes stop dependent work.

## 10. Failure semantics

Every contract distinguishes unsupported, unavailable, denied, offline, rate-limited, resource-constrained, malformed, stale, ambiguous, interrupted and uncertain.

Examples:
- Model unavailable → lexical search and deterministic/manual operation remain usable.
- DOM unavailable/ambiguous → show page and request selection/takeover.
- Login blocked by provider policy → supported external/system-auth handoff; never claim embedded authentication.
- Storage full → stop writes safely and preserve recoverable material.
- Renderer crash → restore tab metadata and report whether re-authentication is necessary.
- Uncertain external action → find receipt/postcondition before any retry.

## 11. Current status honesty

The target architecture is proposed. The current repository contains a Rust kernel/FFI scaffold and one Compose placeholder Activity. It does not implement this blueprint. Architecture documents are intent, not evidence of runtime behavior.

Use these status labels consistently:
- PROPOSED — designed, not implemented.
- IMPLEMENTED — source exists.
- TESTED — named automated test passed on exact commit.
- DEVICE-VERIFIED — verified on a physical Android device.
- EXTERNALLY-CONSTRAINED — platform/provider imposes a limit.
- UNVERIFIED — evidence missing.
- BLOCKED — dependency or unresolved decision prevents progress.

## 12. Architecture approval gates

Before Phase 1 product implementation:
1. Every user requirement maps to architecture components and acceptance tests.
2. Data, identity, event, FFI, async execution and capability contracts are concrete.
3. Browser/session/DOM, model ecosystem and dynamic compiler have dedicated specifications and tested assumptions.
4. Security boundaries and consent flows are reviewed.
5. High-reversal-cost decisions have accepted ADRs or measured spikes.
6. Roadmap has dependency-aware vertical slices and evidence gates.
7. All unresolved items have visible status and next decision.

No subsystem is accepted because its diagram looks complete.
