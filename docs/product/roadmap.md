# Locco product roadmap

**Status:** Proposed dependency-aware roadmap, updated for LOC-0004. It must be accepted only after the architecture audit reconciles contracts, ADRs and implementation evidence.

A phase never resets the product, app identity, schema history, accepted requirements, user data or open feedback. Bugs and regressions are fixed whenever found. The roadmap is cumulative; phases group dependencies rather than authorize deferring existing defects.

Every delivery has a LOC issue, exact goal, baseline commit, owner/reviewers, source changes, named acceptance tests, device requirements, artifact evidence, rollback and updated status. A phase is accepted only from evidence, not a checklist tick.

## 0 — Foundation and architecture assurance

**Outcome:** a trustworthy engineering foundation and accepted target architecture.

- 0.1 Governance, issue traceability, change control, review and branch protection.
- 0.2 One pinned toolchain; reproducible Rust formatting/test/Clippy.
- 0.3 Versioned Rust/JNI/FFI command, event and error contract.
- 0.4 Three Android ABI builds and symbol inspection.
- 0.5 Native APK assembly, identity/signature/hash and runtime ABI check.
- 0.6 Security, dependency, workflow, SBOM/provenance and release controls.
- 0.7 Deep architecture audit: data, evidence, browser/auth/DOM, model ecosystem, capability compiler, execution, native UX and acceptance traceability.
- 0.8 Phase 0 independent acceptance and device verification where available.

Gate: governance/CI passes; Rust and APK are built from the same recorded source; three ABIs are inspected; identity is stable; device smoke is done or explicitly NOT RUN; architecture contracts and high-risk ADRs are accepted; LOC-0003/LOC-0004 acceptance evidence exists.

No product subsystem is accepted merely because it is described.

## 1 — Local kernel and durable data substrate

**Outcome:** Locco can preserve, relate, retrieve and recover user information offline.

- 1.1 Stable IDs, versioned commands and domain state transitions.
- 1.2 Canonical local store and migrations; encryption design proven before sensitive records.
- 1.3 Original evidence/blob storage, content identity, provenance and atomic writes.
- 1.4 Entities, relationships, observations and claims with conflict handling.
- 1.5 Append-only event and execution journal foundations.
- 1.6 Lexical/full-text indexes and rebuildable derived indexes.
- 1.7 Export, restore, integrity verification and migration recovery.
- 1.8 First integrated vertical slice: capture → preserve → inspect → link → search → export/restore.

Gate: deterministic schema/migration/round-trip/integrity/offline tests, data-preservation tests and one usable native workspace slice.

## 2 — Acquisition and native browser

**Outcome:** users can bring authorized information into Locco and use a real browser with reliable account/session behavior.

- 2.1 Source adapter contract and provenance lifecycle.
- 2.2 Native browser shell: address/search, tabs, navigation, history, bookmarks, downloads and page controls.
- 2.3 Browser engine decision (WebView vs GeckoView), persistent/ephemeral profiles, account sign-in, cookie/storage lifecycle and secure Auth Tab/Custom Tabs handoff.
- 2.4 Bounded, versioned DOM observation with origin/frame provenance, isolated-world injection where supported and sensitive-field redaction.
- 2.5 Inspect/Select UI, semantic locators, page mutation and observation diffs.
- 2.6 Share/import, file/document, manual, public-page and permitted API acquisition.
- 2.7 Capture pipelines, dedupe, cancellation, partial-result preservation and resume.
- 2.8 Browser authentication, session isolation and authenticated read-only device acceptance.

Gate: LOC-0005 passes; engine selection ADR is accepted from target-device evidence; authenticated sessions survive supported restarts/updates; captured evidence is preserved before parse; native browser works without AI; unsupported access is honest.

## 3 — Model ecosystem and local inference

**Outcome:** users can discover, acquire, configure and run compatible local models.

- 3.1 Model/artifact metadata contracts and catalogue-source adapters.
- 3.2 Model Hub discovery, search, filtering, comparison and offline metadata cache.
- 3.3 Download/import, pinned revision, shard/size/hash verification and atomic installation.
- 3.4 Runtime adapters: first validated LiteRT-LM and GGUF candidates; specialist runtimes only when justified.
- 3.5 Compatibility matrix, device preflight, load tests and measured benchmarks.
- 3.6 Streaming inference, cancellation, backend fallback and resource governor.
- 3.7 Per-model settings, task/role mapping and transparent routing.
- 3.8 Retrieval support models (embeddings/rerankers) only when golden-set and resource results justify them.

Gate: model is compatible/load-tested on the device before activation; download/import integrity and OOM/thermal fallback pass.

## 4 — Semantic intelligence and Capability Compiler

**Outcome:** Locco assembles new tasks from reusable components instead of hardcoded workflows.

- 4.1 Context compiler with evidence admission, redaction and budgets.
- 4.2 TaskSpec and bounded RequirementGraph.
- 4.3 Versioned capability, tool, skill, procedure, data, knowledge, constraint and evaluator library.
- 4.4 Deterministic linker, conflict resolution and readiness/capability-gap reporting.
- 4.5 ExecutionPackage and generic typed execution graph.
- 4.6 DOM Stitching: observations → validated schemas, relationships and reusable locators.
- 4.7 Evaluation, bounded repair and evidence-backed experience records.
- 4.8 Generality suite covering unrelated tasks without planner code changes.

Gate: the same compiler handles unfamiliar tasks with no use-case-specific planner branches; model proposals cannot widen authority.

## 5 — Controlled execution and integrated workspace

**Outcome:** general capabilities can run reliably and the workspace exposes the complete product state.

- 5.1 Durable async execution, checkpoint, event stream and recovery.
- 5.2 Browser and authorized native/platform capability adapters.
- 5.3 Exact approval envelopes, idempotency and uncertain-side-effect verification.
- 5.4 Pause, cancel, takeover, permission revocation and global kill.
- 5.5 Cross-surface native experience for conversations, workspace, browser, model hub, artifacts and activity.
- 5.6 Search, evidence inspection, entity views, plans and reusable capability management.
- 5.7 Background execution only where Android lifecycle/policy and device tests prove it.
- 5.8 End-to-end arbitrary-task acceptance with visible execution and postcondition evidence.

Gate: device-tested controlled actions, no blind retry, complete event/audit trace and data/session continuity.

## 6 — Product hardening and expansion

**Outcome:** dependable everyday use and explicitly gated platform/modality expansion.

- 6.1 Offline queue, process death, boot recovery and storage-pressure handling.
- 6.2 Encrypted backup/export/import, key lifecycle and deletion.
- 6.3 Security/privacy review, permissions centre and redacted diagnostics.
- 6.4 Performance, thermal, battery and storage optimization.
- 6.5 Accessibility/localization/responsive acceptance.
- 6.6 Signed release, SBOM/provenance, update and rollback/forward-recovery.
- 6.7 Additional source/model modalities and integrations.
- 6.8 iOS contract parity and implementation only after explicit approval.

Gate: release-level device and upgrade verification, security acceptance, evidence-backed release notes and no unresolved blocker silently discarded.

## Cross-phase continuity

- Keep every LOC issue visible until its actual completion criteria are met.
- Preserve original user wording and implementation history.
- Preserve package ID, signing lineage, database data, evidence IDs and versioned FFI/schema contracts.
- Every bug fix has regression proof where technically possible.
- Every feature tests adjacent existing behavior.
- Update roadmap, requirement map, issue, PR, test evidence and release evidence in the same delivery.
- Never describe a plan, screenshot, build workflow or source file as proof that a capability works.
