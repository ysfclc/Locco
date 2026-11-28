# Locco product contract

**Status:** canonical product definition; implementation status is recorded separately in the roadmap and release evidence.  
**Product:** Locco — one evolving, mobile-first, local-first Rust-cored application.  
**Primary surface:** native Android APK. iOS remains a future shell behind shared contracts.

## 1. What Locco is

Locco is a personal intelligence and action environment that makes information and capabilities scattered across the user's phone, apps, websites, files, messages, maps, devices and workflows usable as one coherent workspace.

It does not merely collect notifications, summarize webpages or answer prompts. It acquires information from user-authorized sources, preserves the evidence, understands the content, connects related people/places/things/events, helps the user reason about it, and can carry out permitted actions with approval and verification.

The product loop is:

**Acquire → Preserve → Understand → Connect → Reason → Decide → Act → Verify → Learn.**

The user remains the authority. Local-first and zero-budget operation are the baseline; no paid API, hosted backend, subscription or cloud model is mandatory.

## 2. The unit of value

The unit of value is an **inspectable, evidence-backed fact, object, event, action or change**—not an app-open counter.

Examples:
- A message arrived from a person, with the actual captured text and its source.
- A user opened a particular conversation or page, where the observation mechanism can prove it.
- A ride offer appeared with its actual visible amount, pickup, destination and source.
- The user tapped Accept, rejected an offer, sent a reply or opened navigation, when that action was actually observed.
- A later message or screen updated the same order, trip, person or task.
- Locco connected those records while preserving each original source and uncertainty.

Every claim must retain its acquisition method and evidence. If Android or the source app does not expose a fact, Locco says **unavailable**, **partial** or **inferred**; it never invents the missing detail.

## 3. Evidence and event inspection contract

Every event or captured object must be openable. Its inspector should expose:

1. **What happened** — event type and human-readable description.
2. **When** — source event time, Locco receipt/capture time and uncertainty.
3. **Where** — app/provider, package, page, conversation, screen or other source locator where available.
4. **What was actually captured** — original bounded content, structured fields, or an explicit statement that only metadata was available.
5. **What the user did** — the observed action and its evidence; never infer a tap from an app-open event.
6. **What Locco inferred** — separately labelled interpretation with confidence and model/parser provenance.
7. **What is missing or redacted** — permission gaps, inaccessible content, truncation, sensitive fields and coverage limits.
8. **What it connects to** — related events, entities and source evidence.
9. **What happened next** — verified state change or an explicitly uncertain outcome.

Raw evidence, deterministic extraction, user action, AI interpretation and external outcome are separate layers. A summary is never a substitute for the underlying evidence.

## 4. Core product environments

### 4.1 Workspace and knowledge

- Global search across explicitly named scopes: workspace, evidence, entities, files, current browser page, models and capabilities.
- Inspectable timeline, evidence library, entity pages, relationships, collections and unresolved items.
- People, places, organisations, objects, messages, files, transactions, offers, tasks and events can be connected across sources.
- Search results link back to exact source evidence.
- Captures and work survive ordinary navigation, process restart and app update.
- Export, backup, restore, deletion and retention are explicit product capabilities, not assumed from database encryption.

### 4.2 Universal app observation

Locco should understand useful activity across the apps the user explicitly permits it to observe. It uses a capability registry and multiple honest acquisition methods rather than pretending every app exposes a universal API.

Sources include:
- Android UsageStats/UsageEvents for app visibility context.
- NotificationListenerService for notification receipts and the content Android exposes, with a separate user opt-in.
- AccessibilityService for visible, accessible UI structure and selected interaction events, where the user enables it and the target app exposes useful semantics.
- Android share/import, intents, App Links and document providers.
- Official APIs/SDKs and documented provider integrations.
- Device/network metadata where useful and permissioned.
- In-process observation only inside apps Locco owns or is explicitly authorised to instrument.

Every observation identifies its source, adapter, version, method, event/receive time, fields, evidence, redactions, completeness, freshness and confidence. Source health distinguishes permission-required, active, partial, stale, unsupported, blocked, error and revoked.

Android sandbox boundaries are real. Secure windows, custom-rendered surfaces, protected fields, inaccessible frames and private app processes may be unavailable. VPN metadata does not reveal HTTPS plaintext. Locco does not bypass authentication, TLS protections, anti-tamper controls, app sandboxes or provider restrictions.

### 4.3 Native browser and DOM intelligence

The browser is a real native environment, not a hidden capture form or an automation-only WebView.

Required browser capabilities:
- Address/search bar, origin/security indicator, back/forward, stop/reload and explicit loading/error states.
- Tabs, switcher, close/reopen and state restoration.
- History, bookmarks, downloads, find-in-page, selection, share and save-to-workspace.
- Site permissions and clear data controls.
- Persistent normal profile and genuinely isolated private/ephemeral profile; never fake isolation when the installed engine cannot prove it.
- Supported authentication handoffs, with honest cookie/session boundaries and human control of MFA/CAPTCHA.
- A reversible Inspect mode for selecting semantic page regions.

DOM observation is bounded, versioned and read-only. It captures semantic roles, labels, safe text, structure, state, scope, locators, origin and freshness—not executable scripts or secrets. Snapshot-local node IDs are never treated as durable identity. Locators are re-resolved against the current page before interaction.

**DOM Stitching** lets local intelligence propose relationships between repeated elements, map page structures to user-defined schemas, compare page versions, connect extracted fields to entities and evidence, and suggest reusable extraction definitions. Deterministic validation checks origin, node references, types, sample consistency, coverage, confidence and page drift. Model output never becomes executable authority.

### 4.4 Model Hub and local intelligence

The Model Hub is a first-class environment, useful even when no model is installed and while offline.

Keep separate registries for:
- model catalogue and immutable releases;
- exact artifacts/files and digests;
- runtime adapters and tested support;
- optional remote inference/provider connections.

Required lifecycle: discover, compare, inspect license/provenance, acquire/import, verify, stage, load-test, benchmark, assign, route, unload and remove safely.

Show measured device fit, memory, latency, thermal behavior, quality evidence and unknowns. A model being listed does not mean it is installable, compatible, fast, safe or good enough. A digest proves byte identity, not publisher authenticity. No untrusted model package can install executable code.

Local intelligence provides extraction, entity resolution, retrieval, semantic inference, compact context, planning assistance and evaluation. Deterministic rules remain authoritative for schemas, permissions, policy, evidence and success. Local AI is replaceable and cannot grant itself authority or certify its own result.

### 4.5 General capabilities and execution

Locco is not a set of hardcoded vertical workflows. A domain-neutral capability system composes registered tools, data sources, procedures, constraints, permissions, evaluators and user input for tasks that were not anticipated in advance.

A generic task flow is:

**Intent → TaskSpec → Requirements → Capability resolution → Readiness → Plan → Observe/Execute → Verify → Evaluate → Learn.**

Plans and executions are typed, versioned and inspectable. Missing capability or permission produces an explicit gap. The model proposes; deterministic policy validates. User/model-authored components begin untrusted and cannot authorize side effects.

Side effects require exact target/action/value approval, current evidence, expiry and expected postconditions. Non-idempotent commits are not blindly retried. Uncertain outcomes are reconciled or returned for user review. The loop is **Observe → Propose → Validate → Authorize → Act → Verify → Record**.

### 4.6 Capture and handoff

Capture is available globally: text, Android share, files, links and future voice/media paths. Each capture records its source and context. Nothing is silently archived merely because the user visited a page. User-mediated capture remains distinct from background observation.

## 5. Native and technical boundaries

- Native Android UI; no HTML, web UI, PWA or web dashboard.
- Rust owns domain truth, canonical models, deterministic invariants and schema migrations.
- Kotlin/Compose owns Android UI and platform acquisition/action adapters.
- JNI/C ABI is versioned, bounded and explicit.
- iOS is a future native shell behind the same domain contract; it is not a current release promise.
- SQLCipher/Keystore protects local data at rest, but encryption alone does not provide complete backup, restore, export or deletion semantics.
- Existing app identity, signing identity and user data must survive normal updates. No destructive reinstall or database reset by default.

## 6. Non-goals and forbidden shortcuts

- No Memo clone; Memo, Memo-2 and other projects are selective references only.
- No sales-only, trade-only, CRM-only, lead-generation or generic business-OS identity.
- No BusyBee or VaultOS runtime dependency; donor projects remain separate.
- No mandatory cloud, paid API, subscription or hosted backend.
- No HTML/web UI.
- No hardcoded workflows for named verticals.
- No hidden surveillance, credential harvesting, unrestricted scraping or unsupported third-party process access.
- No fabricated source facts, inferred taps presented as observed actions, or AI summaries presented as raw evidence.
- No automatic high-impact action without exact authority, approval and verification.
- No project reset, replacement scaffold, second app, package-ID change or lost feedback without explicit user instruction.

## 7. Donor-project boundaries

The original brainstorm identifies these projects as sources of selective ideas, not product identities:

| Donor | Concepts to evaluate |
|---|---|
| Outbound Sourcer | Source adapters and discovery pipelines |
| EBO | Event/signal ledger, qualification and approval concepts |
| BOS | Entity, capability, workflow and policy modelling |
| Troy | Source-aware extraction, interpretation and identity resolution |
| ME | Immutable evidence and derived-observation concepts |
| Memo | Selected Android automation, approvals and visible-browser patterns |
| BusyBee | App Observer Lab, Provider Bridge and owned-app plaintext research |
| VaultOS | Local import, indexing, backup and data integrity |

No donor's current implementation is assumed complete or copied without source, licence, test and security review. Locco remains one independent application.

## 8. Implementation truth at the 2026-11-28 v0.3.1 main candidate

The v0.2.2 release is the foundation baseline. Main includes the original notification-evidence implementation (PR #14), the native launcher identity (PR #18 / commit `9bb3907`) and the first visible-UI evidence adapter (PR #21 / commit `18125137e057dd42220d1948af3290e1e8b49970`). Current Android configuration is applicationId `com.locco.app`, versionCode 4 and versionName `0.3.1-inspectable-evidence`. The established signing identity is unchanged.

Main now includes clickable notification event details, off-by-default notification title/text capture for selected apps, bounded content filtering and content hashes, contextual-only UsageEvents, and a passive AccessibilityService for selected apps. Visible UI capture has its own in-app disclosure and default-off consent switch, separate from Android's system accessibility grant. It records only window-state, accessible-control click and accessible-container scroll signals; callback content is bounded, filtered and stored in additive Rust schema v8. The existing v7 notification table remains unchanged. The shared Observer timeline and inspector display source, time, acquisition method, exposed labels, hashes, redactions and partial/unavailable status. It does not claim a click completed a transaction or that all app screens are visible. The adaptive, round and themed monochrome launcher assets are registered in the Android manifest. The legacy ZIP phase pipeline is retired, and product/feedback governance is in the repository.

On PR #21's exact head, Rust format/tests/Clippy/migration harness passed; Android debug build and Kotlin unit tests passed; all 26 emulator instrumentation tests passed (0 failed), including the populated v7-to-v8 encrypted upgrade and legacy plaintext migration tests; security/policy and change-governance checks passed. The signed v0.3.0 APK is published and its package/version/certificate fingerprint/SHA-256 were verified. The signed v0.3.1 release is still pending, as is physical-device verification.

Locco still does **not** implement the complete app-observation system, cross-app event correlation, operational entity graph, native browser, DOM Stitching, Model Hub, local model runtime, general capability compiler or durable verified action engine. The new Accessibility adapter is best-effort: secure or custom-rendered surfaces may expose no useful hierarchy, real source-app callbacks and revocation behavior must still be verified on device, and a green emulator suite does not establish universal coverage or Play Store approval. This is an incremental product capability, not completion of the full product contract.

## 9. Definition of done

A capability is not complete because a screen or class exists. It is complete only when:
- its user-visible path works end to end;
- source and evidence are inspectable;
- permission, scope, limitations and failure states are explicit;
- data survives process death, restart and app update;
- schema migration preserves existing user data;
- duplicates, delayed events, revocation and malformed input are tested;
- security/privacy boundaries are tested;
- the same source revision passes Rust, Android, security and signed-release workflows;
- the release APK's package, version, signing certificate and digest are verified;
- physical-device-only behavior is labelled NOT RUN until actually tested.

A green build proves the build, not the full product vision. Product completion is measured against this contract and the traceability matrix, not the number of screens or commits.
