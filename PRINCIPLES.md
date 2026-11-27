# Locco principles

These are product and engineering invariants, not optional preferences. They are interpreted together with [GOVERNANCE.md](GOVERNANCE.md); its continuity, feedback-retention and evidence gates are binding across the repository.

## Product identity and continuity
1. Locco owns its identity; no donor repository, agent, prompt or first use case defines it.
2. Locco is one continuously evolving product in this repository. Ordinary bugs, regressions and feature ideas extend or repair the existing application; they do not create a replacement app, package, shell, repository or clean-slate scaffold.
3. A separate product may be created only when the user explicitly directs creation of a separate product. A new domain or workflow should compose Locco's existing capabilities by default.
4. User feedback is durable product memory. Every bug, regression, feature request, UX issue, product constraint and correction is captured as a GitHub Issue with a permanent `LOC-####` ID and links to implementation and evidence.
5. New feedback does not erase, replace, downgrade or silently defer previously reported bugs, accepted features, constraints, decisions or failed attempts. Supersession requires an explicit user decision and a traceable record.
6. The newest request is not automatically the highest priority or a complete replacement product brief. Conflicts are surfaced; they are not silently resolved by an agent.

## Platform and architecture
7. Local-first: no mandatory account, paid API, hosted database or cloud model.
8. Native mobile only: Android primary, iOS anticipated. No HTML, web UI, PWA or browser-delivered product.
9. Rust owns domain truth, IDs, invariants and cross-platform contracts; shells own OS lifecycle and UX.
10. Preserve source evidence and provenance before interpretation; never overwrite evidence with AI output.
11. Deterministic code handles parsing, validation, policy, dedupe and state transitions; AI handles bounded semantic ambiguity.
12. Context is scarce: task-specific retrieval, hard budgets, no transcript-as-memory.
13. AI cannot grant permissions, execute arbitrary tools or certify success.
14. External actions require a declared capability, exact payload, authority, appropriate approval and postcondition evidence.
15. Sources, models, storage and bridges are replaceable adapters.
16. No bypass of authentication, CAPTCHA, paywall, anti-bot or OS security.
17. Zero-budget baseline; paid services optional with local/manual fallback.
18. No silent scope creep; consequential changes require ADRs and user approval where product identity, data, security, permissions or breaking contracts change.
19. Design for reversibility; document failure modes, migration paths, rollback and costs.

## Quality and completion
20. Bug fixes preserve the existing product and include a regression test for the exact failure whenever technically possible.
21. Feature work preserves adjacent existing workflows and cannot be declared complete as a mock, stub, placeholder, disconnected screen or demo.
22. Persistent-data, package, signing and FFI changes are compatibility changes. They require versioning, tested migration/recovery and in-place upgrade evidence; destructive reset is not a routine fix.
23. A test is never deleted or weakened merely to turn CI green. A behavior contract changes only through a documented and approved decision.
24. Every change links back to feedback IDs, a baseline SHA, acceptance criteria, regression evidence and rollback.
25. "Done" means independently reproducible evidence, not a green compile, an unexecuted workflow, a verbal claim or a generated APK that was never verified.
