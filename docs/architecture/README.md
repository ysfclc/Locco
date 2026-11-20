# Architecture library

The target architecture is under active review. This index separates accepted baseline contracts from proposed subsystem designs; a proposal is not an accepted decision or implementation proof.

| Document | Scope |
|---|---|
| overview.md | Canonical five-plane system model, data authority and dependency flow |
| module-boundaries.md | Rust, Android, platform adapter and iOS boundaries (actual vs proposed) |
| data-model.md | Stable records, evidence, identity, tasks, capabilities, execution and artifacts |
| storage-security-and-recovery.md | Encryption boundaries, key lifecycle, backup/restore, browser-session continuity and deletion |
| ffi-contract.md | FFI boundary invariants and implementation status |
| ffi-command-event-protocol.md | Versioned C ABI, JSON envelopes, event replay and platform-effect transport |
| durable-execution-and-recovery.md | Transactional outbox, leases, cancellation, uncertainty and process recovery |
| source-adapters.md | Authorized acquisition methods, browser capture and provenance |
| constraints.md | Device, source and trust constraints |
| context-feeding-contract.md | Evidence admission and bounded context |
| local-ai-and-context.md | Model/runtime registry, local inference, retrieval and memory |
| android-platform-policy.md | Android lifecycle, permission and runtime feature constraints |
| ui-ux.md | Normative native experience, accessibility and interaction invariants |
| native-experience.md | Locco interaction domains, task journeys and design iteration process |
| reference-inspiration-audit.md | Source-audited Memo-1/Memo-2 lessons, limitations and non-copying rules |
| requirement-traceability.md | Product requirements mapped to architecture owners and planned acceptance evidence |
| observability-evaluation.md | Privacy-safe diagnostics and golden-set evaluation |
| target-system-blueprint.md | Product invariants, five-plane ownership, data authority, dependencies and trust boundaries |
| capability-compiler.md | General-purpose task decomposition, capability assembly and execution package |
| browser-dom-and-session.md | Native browser, account authentication, profile/session lifecycle and DOM Stitching |
| browser-engine-evaluation.md | WebView vs GeckoView device spike; Auth Tab is a separate authentication handoff |
| model-ecosystem.md | Catalogue, artifact manifests, runtimes, provider endpoints, device compatibility and lifecycle |
| architecture-assurance-register.md | Evidence-based gap register and architecture acceptance checklist |
| diagrams/system.mmd | Target system relationships |
| diagrams/browser-auth.mmd | Browser profiles, authentication handoff and session boundaries |
| diagrams/dom-stitching.mmd | Isolated DOM observation, Stitching and action validation |
| diagrams/model-ecosystem.mmd | Catalogue-to-runtime/device-fit lifecycle |
| diagrams/capability-compiler.mmd | Generic objective-to-execution compilation |
| diagrams/execution-recovery.mmd | Durable async execution, interruption and uncertainty |

## Document precedence

1. The founding mission, product vision, scope and explicit user requirements define product intent and non-goals.
2. Accepted ADRs record binding architectural choices. A proposal cannot silently supersede an accepted ADR.
3. target-system-blueprint.md defines cross-system ownership, trust boundaries and canonical authority.
4. Supporting subsystem contracts refine the blueprint and must remain consistent with it.
5. requirement-traceability.md maps intent to evidence; architecture-assurance-register.md and foundation-status.md record unresolved gates.
6. Diagrams explain relationships but do not override textual contracts.

Source requirements: LOC-0003 (Phase 0), LOC-0004 (architecture audit) and LOC-0005 (account login/session continuity). Existing vision, constraints, ADRs and founding brief remain authoritative unless a reviewed ADR explicitly changes them.

ADR-0013 records a browser-engine investigation; it intentionally selects neither WebView nor GeckoView yet.

Status terms must distinguish PROPOSED, IMPLEMENTED, TESTED, DEVICE-VERIFIED, EXTERNALLY-CONSTRAINED, UNVERIFIED and BLOCKED. A document is not implementation evidence.

ADR-0014 proposes the FFI transport; ADR-0015 proposes durable execution/outbox semantics. Both require contract tests and independent review before acceptance.
