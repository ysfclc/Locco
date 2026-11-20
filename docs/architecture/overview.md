# Architecture overview

**Status:** System-wide target model; detailed contract is in target-system-blueprint.md. This overview uses the same five planes and data-authority rules as that blueprint.

Locco is one Android-first, local-first mobile intelligence and action environment. Rust owns domain identity, invariants, canonical mutations, deterministic policy and verification. Kotlin/Compose owns native experience and Android lifecycle. Platform and runtime integrations are replaceable adapters. iOS is a future native shell over shared contracts and remains gated.

## Five cooperating planes

| Plane | Responsibility | Primary owner |
|---|---|---|
| Experience | Native workspace, intent/conversation, browser chrome, Model Hub, evidence, approvals, artifacts and system controls | Android UI; no domain authority |
| Platform/adapters | Android lifecycle, WebView or selected browser engine, profiles, files/share, permissions, downloads, local model runtimes, notifications and OS integrations | Kotlin/platform adapters |
| Intelligence | Catalogue discovery, retrieval, bounded context, local inference, DOM observation/Stitching proposals and evaluation | Rust orchestration contracts plus replaceable runtime/observer adapters |
| Control | TaskSpec, requirement graph, capability assembly, readiness, deterministic policy, approvals, durable execution and postcondition verification | Rust application services and domain invariants |
| Data | Stable canonical records, evidence/provenance, entity graph, encrypted blobs, migrations, relational persistence, audit journals and rebuildable indexes | Rust domain/persistence contracts plus platform key provider |

The domain kernel is a logical ownership boundary across the Control and Data responsibilities, not a sixth plane. The physical relational store is the canonical source of truth. Append-only event/execution journals preserve transition and side-effect evidence; they do not make the whole system event-sourced. Binary evidence/assets may live in an encrypted blob store referenced by canonical rows. Search indexes, embeddings and caches are rebuildable projections.

## Dependency direction

Native UI → Android platform adapters → versioned FFI/application commands → Rust application services → Rust domain contracts. Long-running commands return durable operation IDs; native adapters claim outbox effects and poll/ack durable events. No Rust worker calls Kotlin/Swift callbacks.

The domain kernel imports no Android API, Kotlin/JVM type, UI, browser engine, network client, model SDK, database driver or OS key API. Application services depend on typed ports; adapters implement those ports. Rust validates every durable command and model-generated proposal before canonical mutation.

## User and system flow

Intent or an authorized source enters through the native shell or a platform adapter. Acquisition preserves a consented, provenance-bearing capture. The data/domain layer records canonical evidence and derived observations separately. The intelligence layer retrieves only relevant evidence under a bounded context contract. The Capability Compiler resolves a user-defined objective into a typed, versioned requirement and execution package. The control plane checks authority, policy, exact approvals, resources and postconditions. Platform adapters execute only the operations allowed by the package and return verifiable outcomes.

This is a general-purpose composition pipeline. Product examples are test scenarios, not hardcoded planner branches.

## Native presentation flow

Intent → feature ViewModel/reducer → use case → Rust command/query → projection/event → UI.

Reducers own feature-local presentation state. Rust application services own domain orchestration and durable transitions. Navigation owns destination stacks; ViewModels own transient/restorable UI state; the canonical store owns durable data. The UI never invents success while a command, download, login or external action is still pending.

## Degradation and failure

Offline, model unavailable, browser observer unsupported, profile unavailable, permission denied, source blocked, storage full, interrupted and uncertain are typed states. Missing AI must not block deterministic search, capture, evidence inspection, core navigation or manual alternatives. Unknown is not success.
