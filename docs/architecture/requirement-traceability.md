# Product requirement traceability

**Status:** Initial architecture mapping; acceptance tests are planned, not implemented or passed  
**Source baseline:** original mission brief, structured brainstorm, product vision/scope/personas, LOC-0003/LOC-0004/LOC-0005 and current architecture proposal.

This matrix is the bridge between product intent, architecture ownership and proof of completion. A row is not complete because its document exists. Test identifiers below are proposed names for future test cases; they are not claims that test files or implementations already exist.

| ID | Product requirement | Architecture / owner | Planned acceptance evidence | Current status |
|---|---|---|---|---|
| LOC-R001 | One continuing, Android-native, local-first Locco product; no web app or replacement identity | Vision, scope, target-system-blueprint, ADR-0001/0002/0003; Android shell + Rust contracts | app identity/upgrade test; offline boot and core navigation test; build artifact package/signature check | PROPOSED; foundation gates open |
| LOC-R002 | Preserve original information and provenance before interpretation | data-model, target-system-blueprint, ADR-0004/0008; Rust domain/persistence | evidence.atomic-write; evidence.hash-and-provenance; evidence.source-offset-roundtrip; import-failure-preserves-original | PROPOSED; no canonical store implemented |
| LOC-R003 | Stable entities, relations, observations, claims and conflicting assertions | data-model, target-system-blueprint; Rust kernel | domain.identity-stability; graph.relationship-integrity; claims.support-contradiction; supersession-preserves-history | PROPOSED |
| LOC-R004 | Real human-usable native browser | browser-dom-and-session, browser-engine-evaluation, ADR-0013; Android browser adapter | browser.address-search; browser.navigation-history; browser.tabs-restore; browser.download-resume; browser.site-permissions; browser.no-ai-baseline | PROPOSED; engine unselected |
| LOC-R005 | Website sign-in and session continuity for permitted flows; honest supported handoff otherwise | LOC-0005, browser-dom-and-session, browser-engine-evaluation, ADR-0013 | auth.embedded-form; auth.oauth-pkce-state-nonce; auth.popup-return; auth.mfa-human-handoff; auth.profile-isolation; auth.restart-update; auth.external-handoff-disclosure | BLOCKED pending target-device reproduction/spike |
| LOC-R006 | Structured, bounded DOM observations with real relationships, locators and provenance | browser-dom-and-session; Android observer + Rust contracts | dom.schema-version; dom.node-budget; dom.origin-frame-attribution; dom.secret-redaction; dom.stale-locator-stop; dom.isolated-world-forgery-rejection | PROPOSED; no implementation |
| LOC-R007 | AI can stitch page structures into reusable semantic schemas without executing model code | browser-dom-and-session, capability-compiler; intelligence compiler + deterministic validators | stitch.repeated-record-grouping; stitch.cross-page-schema; stitch.evidence-coverage; stitch.drift-detection; stitch.ambiguous-locator-rejection | PROPOSED |
| LOC-R008 | Rich model catalogue across task types/modalities, sources and artifacts | model-ecosystem; catalog-source adapters | model.catalog-pagination; model.metadata-validation; model.search-filter; model.source-provenance; model.offline-cache; model.license-display | PROPOSED; no Locco catalogue |
| LOC-R009 | Distinguish models, immutable artifacts, runtimes, provider endpoints and measured device readiness | model-ecosystem; catalogue, artifact, runtime and compatibility registries | model.manifest-complete; model.digest-mismatch-quarantine; runtime.capability-matrix; device.load-test; device.thermal-budget; model.activation-requires-load-test | PROPOSED |
| LOC-R010 | Reliable model acquisition, import, install, load, benchmark, use and removal | model-ecosystem; Android storage/download/runtime adapters | model.range-resume; model.atomic-stage; model.import-error-visible; model.cancel-process-death; model.load-unload; model.remove-dependency-warning | PROPOSED |
| LOC-R011 | Generic compiler assembles unfamiliar tasks; no use-case-specific planner branches | capability-compiler; Rust application services | compiler.unfamiliar-site-extraction; compiler.file-web-reconciliation; compiler.capability-gap; compiler.no-vertical-branch; compiler.bounded-graph | PROPOSED |
| LOC-R012 | Reusable capabilities, procedures, knowledge, tools, constraints and evaluators are versioned and trust-labelled | capability-compiler; Rust registry | capability.schema-compatibility; capability.untrusted-promotion-gate; capability.provenance; capability.version-resolution | PROPOSED |
| LOC-R013 | Durable async actions support approval, pause, stop, takeover, cancellation, idempotency and verification | target-system-blueprint, capability-compiler; control/execution services | execution.process-death-recovery; execution.no-blind-retry; approval.payload-binding; execution.postcondition-evidence; execution.kill-switch | PROPOSED |
| LOC-R014 | Local AI is replaceable, bounded and non-authoritative; workspace works without it | local-ai-and-context, model-ecosystem, ADR-0003/0005 | context.budget-admission; context.redaction; inference.cancel-unload; inference.deterministic-fallback; inference.no-model-boot | PROPOSED |
| LOC-R015 | Original, improved native UX inspired by references without copying | native-experience, reference-inspiration-audit; Android UI | ux.ia-task-success; ux.destination-state-restore; ux.compact-expanded; ux.accessibility; ux.controls-have-actions; ux.light-dark | PROPOSED; prototypes not started |
| LOC-R016 | User-authorized source adapters preserve provenance and disclose access limits | source-adapters, ADR-0010; platform adapters | source.permission-scope; source.provenance; source.partial-result-preservation; source.cancel; source.terms-and-rate-limits | PROPOSED |
| LOC-R017 | Local storage, migration, encryption, backup, restore and deletion are safe | ADR-0008, target-system-blueprint; Rust persistence + platform key provider | store.encryption-threat-review; store.migration-rollback; store.backup-restore-roundtrip; store.key-loss-recovery; store.secure-delete-policy | PROPOSED; ADR/spikes incomplete |
| LOC-R018 | Security protects credentials, page content, local data, supply chain and external actions | threat-model, secrets-policy, signing-policy, browser/model specs; Rust + platform | security.prompt-injection-fixtures; security.secret-redaction; security.origin-boundary; security.import-fuzz; security.dependency-sbom; security.approval-replay | PROPOSED |
| LOC-R019 | Future iOS path shares domain contracts but does not expand Phase 0 | ios-path, ADR-0001; future native shell | ios.ffi-contract-parity; ios.domain-golden-suite; explicit product authorization before implementation | GATED / NOT STARTED |

## How to use this matrix

Every delivery issue must reference the relevant requirement IDs and turn the planned evidence into actual named tests or manual/device protocols. Update the row in the same PR when implementation state changes.

Use these states:
- PROPOSED — architecture exists, no implementation evidence.
- IMPLEMENTED — source exists, but behavior is not yet proven.
- TESTED — named automated/manual test passed on the recorded commit/environment.
- DEVICE-VERIFIED — target physical device test passed with device/OS/runtime versions recorded.
- EXTERNALLY-CONSTRAINED — an OS/provider rule limits behavior; supported alternatives are documented.
- BLOCKED — a required prerequisite or decision is unresolved.
- REGRESSED — previously accepted behavior no longer passes.

When a test fails, preserve the regression, failure evidence, affected requirement ID and next action. Do not downgrade or delete the requirement to make the dashboard green.
