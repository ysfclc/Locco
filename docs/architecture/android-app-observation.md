# Android App Observation and Interaction Architecture

**Status:** product-critical architecture accepted; first notification-metadata prototype implemented, real-device acceptance pending.  
**Origin:** user direction, reinforced by the App Observer and Provider Bridge research in `ysfclc/BusyBee`.  
**Product owner:** Locco.

## 1. Product intent

Locco is not being built merely to become another app. The ambitious product goal is to make Android itself more observable and composable: understand useful activity happening in other apps, interpret it into a coherent model, surface it in Locco, and offer safe ways to continue or act without repeatedly opening the source app.

The user experience should feel like a native integration/API even when the underlying acquisition mechanism is an Android system capability rather than a provider API. Locco must always disclose which mechanism supplied each fact and how complete it is.

The target loop is:

`observe → acquire → preserve → interpret → correlate → surface → decide → interact → verify`

This is a first-class Locco capability and a defining product direction, not an optional experiment or a BusyBee-only feature.

## 2. What "observe another app" means

Observation is layered. These layers are complementary; none should be mislabeled as another.

| Layer | What Locco can learn | Typical Android mechanism | Limits |
|---|---|---|---|
| App lifecycle/context | installed package, version, foreground/background transitions, usage duration | PackageManager, UsageStatsManager, UsageEvents | Usage access is user-granted and aggregate/event-limited; not app internals |
| User-visible events | notification arrival, category, actions, visible notification content where permitted | NotificationListenerService | Only what the app exposes in notifications; sensitive content must be minimised |
| Visible UI semantics | accessible window structure and, when justified and disclosed, user-visible text/state | AccessibilityService | Not a universal private API; secure windows and custom-rendered UI may be opaque; sensitive data risk |
| User-mediated handoff | shared text/files, opened links, selected content, explicit capture | Sharesheet, intents, document picker, content providers, MediaProjection with consent | User action or explicit system consent required |
| Supported integration | structured data and permitted actions | Official APIs, SDKs, app links, documented content providers | Provider-specific scope, credentials and terms |
| Device/network context | connectivity changes, VPN presence, transport metadata | ConnectivityManager, VpnService | VPN packet visibility does not mean HTTPS plaintext or per-app semantic access |
| In-process observation | app objects before encryption / after decryption, internal lifecycle events | Instrumentation/debug hooks inside an owned or explicitly authorised process | An ordinary stock Android app cannot inject into another app's private process; root or modifying/re-signing a target changes the security model and is not assumed |
| Cross-app action | open a screen, share data, invoke a documented action, or interact with visible UI | Intents, App Links, PendingIntent supplied by source, documented APIs; narrowly scoped Accessibility actions where explicitly enabled | Android has no universal cross-app action API; action availability and verification are provider-specific |

The product must distinguish **what happened**, **what was observed**, **what Locco inferred**, and **what Locco actually did**.

## 3. First-class architecture

```text
Android system + selected apps + user handoffs + supported APIs
                         |
             Observation capability registry
                         |
       Acquisition adapters (one source, many mechanisms)
                         |
          Immutable observation / evidence envelope
                         |
       Redaction + validation + source-specific parser
                         |
         Provider-neutral event and entity model
                         |
        Correlation / dedupe / temporal reconstruction
                         |
            Local evidence ledger + graph
                         |
         Context compiler + local intelligence
                         |
           Locco native workspace / notifications
                         |
          Capability policy + explicit approval
                         |
       Native action adapters (intent/share/UI/API)
                         |
              Postcondition verification
```

### 3.1 Capability registry

Each installed or connected source has a capability profile, not a binary "connected" flag.

A profile records:
- package/source identity and observed app version;
- adapter ID and version;
- supported observation surfaces and their permission state;
- supported event types and fields;
- action capabilities and their invocation method;
- freshness, last event, last successful observation and known blind spots;
- user consent, scope, retention and revocation state;
- health: `not_configured`, `permission_required`, `partial`, `active`, `stale`, `unsupported`, `blocked`, `error`, `revoked`.

Package detection or permission grant alone is never proof of a working integration.

### 3.2 Observation envelope

Every signal becomes a source-neutral record with:
- stable observation ID and idempotency key;
- source package/provider and source version when known;
- acquisition adapter/method and its version;
- event time, receive time and uncertainty;
- event type and typed, nullable fields;
- evidence reference and permitted raw-evidence digest;
- field-level completeness and confidence;
- redaction decisions and sensitivity classification;
- freshness, retention and consent context;
- parser version and interpretation lineage.

Raw payloads are not the canonical application model. They are evidence with bounded retention and explicit access rules. Credentials, session tokens, cookies, authorisation headers, payment data and unrelated personal content must never enter ordinary event records.

### 3.3 Interpretation pipeline

1. Receive a signal without blocking Android system callbacks.
2. Persist a minimal durable receipt / queue entry.
3. Resolve source identity and adapter version.
4. Apply source-specific redaction before durable raw retention.
5. Parse into typed candidate events.
6. Validate fields, timestamps, identifiers and size limits.
7. Correlate duplicates and lifecycle updates.
8. Preserve original evidence lineage and parser version.
9. Publish committed events to Locco's local event stream.
10. Let deterministic rules and local AI propose interpretations; AI never silently upgrades an inference into source fact.

The pipeline must tolerate process death, app updates, delayed and out-of-order events, duplicate callbacks, revoked permissions, malformed payloads and unavailable adapters.

### 3.4 Interaction model

Locco should expose possible actions alongside observations, but actions are a separate capability set:
- open source app or a documented deep link;
- share or hand off selected, user-approved data;
- invoke a documented provider API / SDK action;
- interact with visible UI only through a clearly enabled, user-authorised Accessibility capability where appropriate;
- verify the expected postcondition using a fresh observation.

Never infer that observing an action request authorises replaying it. Never fabricate private endpoints or replay captured requests. No automatic high-impact action (payments, accepting work, sending messages, account changes) without an explicitly approved and verified supported action path.

## 4. Implementation strategy: capability ladder

Build the broadest useful system on ordinary, supported Android first, while keeping the architecture open to deeper authorised research.

### Level 1 — system observation foundation
- Usage events and app lifecycle/context.
- Opt-in notification listener with privacy-aware content handling.
- User-visible accessibility event/structure observer with narrow package allowlist and explicit controls.
- Share/import, App Links and document handoffs.
- Permission/connection health and on-device event timeline.

### Level 2 — semantic observation and native handoffs
- Per-source parsers and lifecycle state machines.
- Structured extraction from notifications and accessible visible UI where permitted.
- Supported deep links, intents, share targets and PendingIntent actions.
- Source-specific capability coverage, event freshness and failure diagnostics.
- Locco surfaces interpreted events without requiring the source app to be foregrounded.

### Level 3 — supported provider integrations
- Official APIs, SDKs, partner access, documented content providers and export formats.
- OAuth/account linking only where provider-supported and securely implemented.
- Incremental sync, pagination, rate limits, revocation and reconciliation.

### Level 4 — controlled in-process research
- Owned sample app + local HTTPS fixture proving outbound pre-TLS and inbound post-TLS observation.
- Runtime-specific instrumentation and extraction tests in processes we own or are explicitly authorised to instrument.
- Compare observation points, overhead, version drift, redaction and failure behaviour.
- Treat each third-party provider's compatibility and permission as a separate unproven question.

This ladder is not a promise that every app can be fully inspected on stock Android. Android's app sandbox is a security boundary. If a capability is not exposed by the OS, provider or authorised target process, Locco must show the limitation rather than pretend it can see through it.

## 5. BusyBee research to ingest

The following BusyBee work is a donor research source and must be treated as engineering input, not as code automatically copied into Locco:

- `research/app-observer/`: standalone App Observer Lab measuring installed app identity/version, Usage Access summaries, notification metadata, aggregate Accessibility window signals and device network transports. It deliberately does not read notification body or screen text and has no network permission or telemetry.
- `docs/PLATFORM_ACTIVITY_INTEGRATION.md`: distinguishes VPN packet observation, user-visible observation and in-process plaintext observation; specifies owned-app proof first and warns that ordinary non-root Android cannot inject into an untouched app process.
- `docs/NATIVE_PROVIDER_BRIDGE.md`: defines a provider-neutral internal API, capability discovery, source health, canonical events, provenance, and the product experience as a first-party integration.
- `docs/PLAINTEXT_OBSERVER_POC.md`: owned Android app and local HTTPS fixture for outbound/inbound plaintext-boundary proof, synthetic lifecycle events, redaction and health tests.
- `docs/SOURCE_REGISTER.md` and `docs/DECISIONS.md`: document limits and separation of observation, normalisation, explanation and action.

Locco must re-evaluate exact source, tests, licences, package IDs and build state before porting any implementation. Preserve BusyBee as the research origin and do not create a hard runtime dependency on it.

## 6. Privacy and safety invariants

- Observation is opt-in, visible, scoped and revocable.
- Show the user which apps and signal types are observed.
- Default to local processing and storage; no telemetry or automatic upload.
- Minimise notification/UI content; do not collect unrelated conversations, passwords, payment details or secure-window content.
- Prefer event metadata and task-relevant fields over indiscriminate screen dumps.
- Keep package allowlists and per-source pause/disable controls.
- Expose a clear last-observed time and degraded state; do not imply continuous monitoring when Android may suspend it.
- Never bypass app authentication, TLS pinning, integrity checks, anti-tamper controls, OS sandboxing or provider access controls.
- Do not use Accessibility as a covert general-purpose surveillance or universal control channel.
- Provide retention, deletion, export and audit visibility.
- High-impact actions require explicit approval and independent postcondition verification.

## 7. First engineering proof and acceptance

### 7.1 Implemented prototype (device verification pending)

The first Locco-owned observation adapter is now present in the Android shell:

- A system-bound `NotificationListenerService` receives notification lifecycle callbacks while Locco is not foregrounded.
- The Observer screen discovers launchable installed apps through Android's package visibility query and shows each app's display name, package name and installed version.
- The user explicitly enables observation per app with a switch; the selected package allowlist is persisted locally. No app is observed by default.
- Android's notification-listener permission remains a separate, explicit system setting.
- The adapter copies only package name, post timestamp and standard notification category. It never reads notification title, text, actions or extras.
- A SHA-256 digest of the notification identity is used as a unique idempotency key; the raw notification key is not persisted.
- Rust migration schema v3 adds an indexed `app_observations` ledger table with acquisition method, source package, event/receive timestamps and explicit redaction metadata.
- The event is stored through the SQLCipher database and displayed in Locco's Observer feed. The feed refreshes when the Observer destination is opened.
- Instrumentation coverage verifies encrypted-at-rest observation metadata, reopen behavior and duplicate suppression.
- Rust migration schema v4 adds a separate `app_usage_events` ledger for package-level foreground/background transitions.
- When Usage Access is granted, Observer refresh queries Android `UsageEvents` for the selected package allowlist (last 24 hours), persists deduplicated transitions, and merges them into the local feed. This is history-on-refresh, not a continuous background monitor; no screen contents or in-app actions are exposed.
- Android Q+ activity-level resume/pause callbacks with a nonblank activity class name are reduced into package-level foreground/background boundaries using an in-memory active-activity set. This avoids treating an internal activity switch as the app leaving the foreground. Because deprecated MOVE_TO_* event constants alias modern ACTIVITY_* values, class-bearing records identify modern activity events; when no class-bearing records exist, classless records use the legacy package-level interpretation. Activity class names are not persisted.

This is an initial system-signal adapter, not semantic understanding of the source app. It proves only that Android exposed a notification event for an explicitly selected package. App discovery and version display are not evidence that a source integration is active.

### 7.2 Remaining acceptance work

The first implementation milestone is not an attractive dashboard. It is an end-to-end native observation slice.

1. Verify app discovery and package/version display across supported Android versions.
2. Receive at least one real Android system signal while Locco is not foregrounded.
3. Persist the signal through Locco's encrypted local ledger.
4. Convert it into a typed event with source, method, timestamps, permission, confidence and freshness.
5. Show the interpreted event in Locco's native workspace without opening the source app.
6. Demonstrate a safe supported handoff (open app/deep link or share) and verify the resulting foreground/package or expected callback.
7. Test process death/restart, duplicate delivery, permission revocation, malformed input, sensitive-data redaction and source app update.
8. Record battery, memory, latency and missed-event limitations on the target Android device.

A separate controlled proof must show pre-TLS outbound and post-TLS inbound visibility in an owned app. It must not be conflated with the system-observation slice or claimed as working against Uber, Bolt or any other third-party app until separately proven and authorised.

## 8. Product acceptance language

Use precise statements:
- "Observed via notification" rather than "API connected" when notification data is the source.
- "Visible UI signal" rather than "internal app state" for Accessibility-derived data.
- "Usage event" rather than "app activity feed" when only foreground transitions are known.
- "In-process plaintext observed in owned test app" rather than "provider traffic intercepted".
- "Action handed to official app" rather than "Locco controlled provider action" when using an intent/deep link.

The ambition is to make more of Android's capabilities useful and composable, not to claim impossible universal access. A trustworthy partial integration is better than a false promise of full visibility.
