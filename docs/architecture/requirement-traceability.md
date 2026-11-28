# Locco product requirement traceability

**Status:** recovered requirements mapped to implementation and proof. This matrix is authoritative for delivery tracking; product requirements are defined in the [Locco product contract](../product/locco-product-contract.md).  
**Updated:** 2026-11-28.

A requirement is not complete because a screen, class or architecture document exists. Update status only when the exact source revision and named tests/device evidence support it.

| ID | Requirement | Current implementation truth | Acceptance evidence required | Status |
|---|---|---|---|---|
| LOC-R001 | One continuing native Android Locco product, Rust-cored, local-first, no web UI or replacement identity | Existing package and signed release; native Compose shell; Rust migration ABI; no replacement app | Package/signature continuity, upgrade test, offline boot, no destructive data reset | PARTIAL |
| LOC-R002 | Preserve original information and source provenance before interpretation | Manual/share capture, source/evidence records, SQLCipher persistence; generic source ingestion and immutable evidence pipeline incomplete | Atomic source/evidence writes, hashes, source offsets, duplicate handling, import failure preservation | PARTIAL |
| LOC-R003 | Stable entities, relations, observations, claims and conflicting assertions | Rust entity/relation record types exist; operational graph APIs, identity resolution and claim history do not | Identity stability, graph integrity, contradiction/supersession and provenance tests | NOT IMPLEMENTED |
| LOC-R004 | Real human-usable native browser with accounts, tabs, history, downloads and privacy controls | No Locco browser implementation | Navigation, tabs, session restore, sign-in, downloads, permissions, private-profile isolation and no-AI browsing tests | NOT IMPLEMENTED |
| LOC-R005 | Structured DOM observation and DOM Stitching with provenance and safe locators | No browser DOM observer or Stitching implementation | Bounded schema, origin/frame attribution, sensitive-field redaction, fresh locator resolution, drift and coverage tests | NOT IMPLEMENTED |
| LOC-R006 | Rich extensible Model Hub and verified local model lifecycle | No Locco model catalogue or model manager | Catalogue pagination/search, artifact manifests/digests, license/provenance, runtime compatibility, load tests and device benchmarks | NOT IMPLEMENTED |
| LOC-R007 | Replaceable local intelligence with bounded context and deterministic fallback | No local inference runtime or context compiler | Device latency/RAM/thermal benchmarks, context budget, redaction, cancellation and no-model operation | NOT IMPLEMENTED |
| LOC-R008 | Generic capability compiler; no hardcoded vertical workflows | No compiler or capability registry | Unfamiliar task fixtures, typed requirement graph, capability-gap reporting and no vertical-specific planner branches | NOT IMPLEMENTED |
| LOC-R009 | Durable action execution with approval, cancellation, recovery and verification | No durable execution engine or outbox | Crash-point recovery, approval payload binding, idempotency, uncertain-side-effect handling and postcondition evidence | NOT IMPLEMENTED |
| LOC-R010 | Native workspace for capture, search, evidence, entities, review and action | Compose shell, sessions, manual capture, timeline and Observer destination exist; full workspace/search/entity surfaces absent | Real task journeys, state restoration, accessible controls, compact/expanded layouts and offline/no-model use | PARTIAL |
| LOC-R011 | Permission-scoped Android observation of meaningful app activity | UsageEvents history-on-refresh, notification listener and opt-in content are on main. Branch `feature/locco-visible-ui-evidence-v1` adds a separately consent-gated, allowlisted AccessibilityService for window-state/click/scroll signals and bounded UI evidence; not yet merged | Exact-head migration, consent-off/on, selected-app filtering, password/editable exclusion, revocation, redaction, encrypted persistence, inspector, emulator and Redmi behavior | IN REVIEW |
| LOC-R012 | Source-neutral adapters and cross-app event correlation | Notification and UsageStats adapters only; no capability registry or cross-source entity/event correlation | Source identity/version, adapter health, typed fields, event dedupe, temporal reconstruction and evidence lineage | PARTIAL |
| LOC-R013 | Safe local persistence, migration, backup, restore and deletion | SQLCipher/Keystore key lifecycle and Rust-owned migrations v1–v8 exist in the UI evidence candidate; export/restore, key rotation and complete backup policy are not proven | v7-to-v8 upgrade preservation of all existing rows, encrypted UI evidence round-trip, key-loss fail-closed, backup/restore and deletion tests | IN REVIEW |
| LOC-R014 | Security protects credentials, page content, local data, dependencies and external actions | Security workflow, dependency checks, secret scan, signing and encrypted local store exist; full threat controls remain open | Adversarial input, prompt-injection, origin boundary, import fuzz, redaction, approval replay and backup tests | PARTIAL |
| LOC-R015 | Future iOS shell shares domain contracts without expanding current Android scope | iOS is a documented future path only | Explicit authorization, FFI contract parity and shared golden tests | GATED / NOT STARTED |
| LOC-R016 | Versioned, bounded, safe Rust/native boundary | ABI v2 exposes ping and Rust-owned schema migrations; durable command/query/event protocol is not implemented | C header compilation, buffer ownership, invalid-input bounds, panic containment, concurrency and event replay | PARTIAL |
| LOC-R017 | Inspectable events distinguish evidence, action, inference and outcome | Main has clickable notification receipts. The Accessibility candidate adds a shared inspector for source/time/method, accessible snapshot, clicked/scrolled target, hash, redactions and partial/unavailable status. It never treats a click as a successful transaction/action | Exact-head Android tests, v7-to-v8 data preservation, event callback on device, consent revocation, and proof that unsupported screens remain labelled unavailable/partial | IN REVIEW |
| LOC-R018 | Original Locco UX inspired by references without copying | Reference audit and three navigation hypotheses recovered; no comparative native prototypes or final IA | Same task journeys across prototypes, one-hand reach, accessibility, state preservation and user evaluation | NOT IMPLEMENTED |
| LOC-R019 | User-authorized observation accurately discloses Android/provider limits | Accessibility candidate has independent in-app affirmative disclosure, default-off toggle, app allowlist, persistence-time consent recheck, and no gesture/action capability; actual hierarchy coverage remains provider-dependent | Test consent-off/on, queued-event revocation, package scoping, secure/custom-rendered screens, no text-change capture, and Redmi behavior | IN REVIEW |
| LOC-R020 | Preserve every user correction and extend one existing product | Existing repository, issue and PR workflow; complete feedback register/automation not yet enforced | Trace each bug/idea to issue, decision, regression test, PR, release and upgrade evidence | PARTIAL |

## v0.3.0 release and device acceptance

PR #14 is not accepted until all of these are true on its exact head:
- [x] Rust formatting, tests, Clippy and migration harness pass through schema v7 on the PR candidate.
- [x] Android debug APK assembles and unit tests pass on the PR candidate.
- [x] All 25 Android emulator instrumentation tests pass, including legacy database migration and encrypted notification-content round-trip.
- [x] Dependency policy, Rust advisory audit, workflow actionlint, foundation verification and secret scanning pass.
- [ ] Signed v0.3.0 release build verifies package com.locco.app, versionCode 3, versionName 0.3.0-inspectable-evidence and the existing signing certificate fingerprint.
- [ ] Release APK package/version/signature/digest are inspected.
- [ ] Physical Redmi Note 12 Pro 5G notification callback, opt-in/off and event-inspector behavior are verified. Until then, device behavior is NOT RUN.

## Status vocabulary

- **NOT IMPLEMENTED** — no product behavior exists.
- **PARTIAL** — some supporting code exists; the full requirement is not met.
- **IN REVIEW** — code exists in an open PR and is not part of main/release yet.
- **IMPLEMENTED** — source exists on main.
- **TESTED** — named automated test passed on the recorded commit.
- **DEVICE-VERIFIED** — target physical-device test passed with device/OS/runtime recorded.
- **GATED / NOT STARTED** — intentionally deferred pending explicit authorization or prerequisite.
- **BLOCKED** — a required decision or external dependency prevents progress.
- **REGRESSED** — previously accepted behavior no longer passes.

A green build does not make a product requirement complete. Never downgrade or delete a requirement to make delivery appear green.
