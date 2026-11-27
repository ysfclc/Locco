# Locco product requirement traceability

**Status:** recovered requirements mapped to implementation and proof. This matrix is authoritative for delivery tracking; product requirements are defined in the [Locco product contract](../product/locco-product-contract.md).  
**Updated:** 2026-11-27.

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
| LOC-R011 | Permission-scoped Android observation of meaningful app activity | UsageEvents history-on-refresh and notification metadata listener exist. PR #14 adds clickable event inspection and opt-in notification content; Accessibility observation is absent | Real notification callback/content on target device, allowlist, opt-in/off behavior, redaction, restart, revocation and visible UI/action capture | PARTIAL |
| LOC-R012 | Source-neutral adapters and cross-app event correlation | Notification and UsageStats adapters only; no capability registry or cross-source entity/event correlation | Source identity/version, adapter health, typed fields, event dedupe, temporal reconstruction and evidence lineage | PARTIAL |
| LOC-R013 | Safe local persistence, migration, backup, restore and deletion | SQLCipher/Keystore key lifecycle and Rust-owned migrations v1–v7 exist; export/restore, key rotation and complete backup policy are not proven | Upgrade preservation, key-loss fail-closed, backup/restore round-trip, deletion and recovery tests | PARTIAL |
| LOC-R014 | Security protects credentials, page content, local data, dependencies and external actions | Security workflow, dependency checks, secret scan, signing and encrypted local store exist; full threat controls remain open | Adversarial input, prompt-injection, origin boundary, import fuzz, redaction, approval replay and backup tests | PARTIAL |
| LOC-R015 | Future iOS shell shares domain contracts without expanding current Android scope | iOS is a documented future path only | Explicit authorization, FFI contract parity and shared golden tests | GATED / NOT STARTED |
| LOC-R016 | Versioned, bounded, safe Rust/native boundary | ABI v2 exposes ping and Rust-owned schema migrations; durable command/query/event protocol is not implemented | C header compilation, buffer ownership, invalid-input bounds, panic containment, concurrency and event replay | PARTIAL |
| LOC-R017 | Inspectable events distinguish evidence, action, inference and outcome | Main v0.2.2 has non-clickable lifecycle/metadata cards. PR #14 adds event detail inspector and opt-in notification excerpts with schema v7; not yet part of main | Click every event, inspect source/time/method/content/redactions, verify older rows and encrypted content after update | IN REVIEW |
| LOC-R018 | Original Locco UX inspired by references without copying | Reference audit and three navigation hypotheses recovered; no comparative native prototypes or final IA | Same task journeys across prototypes, one-hand reach, accessibility, state preservation and user evaluation | NOT IMPLEMENTED |
| LOC-R019 | User-authorized observation accurately discloses Android/provider limits | Usage and notification sources labelled; no Accessibility adapter yet. Sandbox/TLS boundaries documented | Permission scope, secure/custom-rendered screen limits, revocation, source coverage and no false universal-access claims | PARTIAL |
| LOC-R020 | Preserve every user correction and extend one existing product | Existing repository, issue and PR workflow; complete feedback register/automation not yet enforced | Trace each bug/idea to issue, decision, regression test, PR, release and upgrade evidence | PARTIAL |

## Proposed event-inspector acceptance

PR #14 is not accepted until all of these are true on its exact head:
- [ ] Rust formatting, tests, Clippy and migration harness pass through schema v7.
- [ ] Android debug APK assembles and unit tests pass.
- [ ] Android instrumentation tests pass, including populated v6-to-v7 upgrade and encrypted notification-content round-trip.
- [ ] Security/policy checks pass.
- [ ] Signed release build uses the existing signing identity; package remains com.locco.app.
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
