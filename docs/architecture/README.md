# Architecture library

The target architecture is under active review. This index separates existing contracts from new proposals; a proposal is not an accepted decision or implementation proof.

| Document | Scope |
|---|---|
| overview.md | System planes, Rust/native shell direction and dependency flow |
| module-boundaries.md | Rust, Android, platform adapter and iOS boundaries (actual vs proposed) |
| data-model.md | Stable records, evidence, identity, tasks, capabilities, execution and artifacts |
| ffi-contract.md | Versioned Rust/native transport and safety rules |
| source-adapters.md | Authorized acquisition methods, browser capture and provenance |
| constraints.md | Device, source and trust constraints |
| context-feeding-contract.md | Evidence admission and bounded context |
| local-ai-and-context.md | Model/runtime registry, local inference, retrieval and memory |
| android-platform-policy.md | Android lifecycle, permission and runtime feature constraints |
| ui-ux.md | Native experience, accessibility and interaction architecture |
| observability-evaluation.md | Privacy-safe diagnostics and golden-set evaluation |
| target-system-blueprint.md | Product invariants, system planes, ownership, dependencies and trust boundaries |
| capability-compiler.md | General-purpose task decomposition, capability assembly and execution package |
| browser-dom-and-session.md | Native browser, account authentication, profile/session lifecycle and DOM Stitching |
| browser-engine-evaluation.md | WebView vs GeckoView device spike; Auth Tab is a separate authentication handoff |
| model-ecosystem.md | Catalogue, artifact manifests, runtimes, device compatibility and model lifecycle |
| native-experience.md | Original Locco UX direction and iteration process |
| architecture-assurance-register.md | Evidence-based gap register and architecture acceptance checklist |
| diagrams/system.mmd | Target system relationships |
| diagrams/browser-auth.mmd | Browser profiles, authentication handoff and session boundaries |
| diagrams/dom-stitching.mmd | Isolated DOM observation, Stitching and action validation |
| diagrams/model-ecosystem.mmd | Catalogue-to-runtime/device-fit lifecycle |
| diagrams/capability-compiler.mmd | Generic objective-to-execution compilation |
| diagrams/execution-recovery.mmd | Durable async execution, interruption and uncertainty |

Source requirements: LOC-0004 (architecture audit) and LOC-0005 (account login/session continuity). Existing vision, constraints, ADRs and founding brief remain authoritative unless a reviewed ADR explicitly changes them.

ADR-0013 records a browser-engine investigation; it intentionally selects neither WebView nor GeckoView yet.

Status terms must distinguish PROPOSED, IMPLEMENTED, TESTED, DEVICE-VERIFIED, EXTERNALLY-CONSTRAINED, UNVERIFIED and BLOCKED. A document is not implementation evidence.
