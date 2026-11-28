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
| LOC-R011 | Permission-scoped Android observation of meaningful app activity | Main includes UsageEvents context, opt-in notification capture and a separately consent-gated, allowlisted passive AccessibilityService for window-state/click/scroll evidence with bounded snapshots | Real selected-app callback coverage, consent/revocation on device, secure/custom-rendered surface limits, process-death behavior and Redmi acceptance | PARTIAL |
| LOC-R012 | Source-neutral adapters and cross-app event correlation | Notification and UsageStats adapters only; no capability registry or cross-source entity/event correlation | Source identity/version, adapter health, typed fields, event dedupe, temporal reconstruction and evidence lineage | PARTIAL |
| LOC-R013 | Safe local persistence, migration, backup, restore and deletion | SQLCipher/Keystore key lifecycle and Rust-owned migrations v1–v8 are on main; emulator instrumentation passes populated v7-to-v8 upgrade preservation and encrypted UI evidence reopen. Export/restore, key rotation and complete backup policy remain unproven | Key-loss fail-closed, backup/restore round-trip, deletion and recovery tests | PARTIAL |
| LOC-R014 | Security protects credentials, page content, local data, dependencies and external actions | Security workflow, dependency checks, secret scan, signing and encrypted local store exist; full threat controls remain open | Adversarial input, prompt-injection, origin boundary, import fuzz, redaction, approval replay and backup tests | PARTIAL |
| LOC-R015 | Future iOS shell shares domain contracts without expanding current Android scope | iOS is a documented future path only | Explicit authorization, FFI contract parity and shared golden tests | GATED / NOT STARTED |
| LOC-R016 | Versioned, bounded, safe Rust/native boundary | ABI v2 exposes ping and Rust-owned schema migrations; durable command/query/event protocol is not implemented | C header compilation, buffer ownership, invalid-input bounds, panic containment, concurrency and event replay | PARTIAL |
| LOC-R017 | Inspectable events distinguish evidence, action, inference and outcome | Main has clickable notification and accessible UI event receipts. The inspector exposes source/time/method, hierarchy, target, hash, redactions and partial/unavailable status; it never treats a click as a successful transaction/action | Real source-app callback evidence, consent revocation on device, event detail inspection and proof unsupported screens remain partial/unavailable | PARTIAL |
| LOC-R018 | Original Locco UX inspired by references without copying | Reference audit and three navigation hypotheses recovered; no comparative native prototypes or final IA | Same task journeys across prototypes, one-hand reach, accessibility, state preservation and user evaluation | NOT IMPLEMENTED |
| LOC-R019 | User-authorized observation accurately discloses Android/provider limits | Main has independent in-app affirmative disclosure, default-off capture, package allowlist, persistence-time consent/scope recheck and no gesture/action capability; actual hierarchy coverage remains provider-dependent | Physical consent-off/on and revocation tests, package scoping, secure/custom-rendered screen limits, permission restart and distribution-policy review | PARTIAL |
| LOC-R020 | Preserve every user correction and extend one existing product | Existing repository, issue and PR workflow; complete feedback register/automation not yet enforced | Trace each bug/idea to issue, decision, regression test, PR, release and upgrade evidence | PARTIAL |

## v0.3.0 release acceptance

- [x] Rust formatting/tests/Clippy/migration harness and Android build/unit/emulator checks passed on their recorded PR head.
- [x] Security/policy and workflow validation passed.
- [x] Signed v0.3.0 release published for `com.locco.app`, versionCode 3, versionName `0.3.0-inspectable-evidence`.
- [x] Published APK package, version, signer fingerprint and SHA-256 digest verified. APK SHA-256: `556655d90f778e0b5527f4b26767793be542ad05d8b39ad7a18b22dfe32bacb7`.
- [ ] Physical Redmi Note 12 Pro 5G notification callback, opt-in/off and event-inspector behavior remain NOT RUN.

## v0.3.1 patch acceptance

- [x] Adaptive, round and Android themed monochrome launcher identity merged.
- [x] Scoped passive UI observation and additive schema v8 merged in `18125137e057dd42220d1948af3290e1e8b49970`.
- [x] Exact-head Rust, Android, security and governance checks passed; Android emulator instrumentation: 26/26, 0 failed.
- [ ] Signed v0.3.1 release verifies package `com.locco.app`, versionCode 4, versionName `0.3.1-inspectable-evidence` and existing signing certificate.
- [ ] Redmi Note 12 Pro 5G in-place upgrade, launcher mask/themed icon and real Accessibility source-app behavior are verified. Until then, physical-device behavior is NOT RUN.

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
