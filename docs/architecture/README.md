# Architecture library

The target architecture is under active review. Do not treat every proposal as an accepted decision or implemented capability.

| Document | Scope |
|---|---|
| target-system-blueprint.md | Product invariants, system planes, ownership, dependency and trust boundaries |
| capability-compiler.md | General-purpose task decomposition, capability assembly and execution package |
| browser-dom-and-session.md | Native browser, account authentication, profile/session lifecycle and DOM Stitching |
| model-ecosystem.md | Catalogue, artifact manifests, runtimes, device compatibility and model lifecycle |
| native-experience.md | Original native UX architecture, navigation research and iterative design process |
| architecture-assurance-register.md | Evidence-based gap register and architecture acceptance checklist |
| browser-engine-evaluation.md | WebView vs GeckoView evidence spike; Auth Tab is a separate authentication handoff |
| diagrams/system.mmd | Target system relationship diagram |

Source requirements: LOC-0004 (architecture audit) and LOC-0005 (account login/session continuity). Existing vision, constraints, ADRs and the original mission brief remain authoritative until a reviewed ADR explicitly changes them.

ADR-0013 records the investigation with no engine selection yet.

Status terms must distinguish PROPOSED, IMPLEMENTED, TESTED, DEVICE-VERIFIED, EXTERNALLY-CONSTRAINED, UNVERIFIED and BLOCKED. A document is not implementation evidence.
