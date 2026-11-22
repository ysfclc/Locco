# Roadmap

## 0 — Foundation
Product, principles, architecture, FFI, Rust workspace, Android shell, iOS plan, security, agents, skills, ZIP protocol and CI. Gate: three Rust Android ABIs + APK; docs agree.

## 1 — Local kernel
**IN PROGRESS:** native session workspace, pure capture kernel, Kotlin unit tests, local SQLite session persistence and legacy preference import. Rust defines platform-neutral Source, Evidence, Entity, Relationship and Event records.

**Implemented in this slice:** Rust owns schema versions 1–4 and ordered SQLite DDL. ABI v2 exposes a bounded caller-buffer C API and Android JNI adapter. Android SQLiteOpenHelper executes those migrations. Version 2 adds note-to-source provenance links. SQLCipher-backed storage and Android Keystore key wrapping are implemented.

**Acceptance still required:** prove actual session plaintext is absent from DB/WAL/journal while records reopen correctly; implement a safe, recoverable migration for existing plaintext databases; complete source/evidence CRUD, entity relation operations, append-only event APIs, lexical retrieval and verified export/restore. Sensitive-data readiness remains blocked until migration and security review pass.

## 2 — Acquisition and Android Observation (IN PROGRESS — first vertical slice)
This is a defining Locco capability, not a generic file-import phase. Build the Android observation subsystem and capability registry: app identity/version, UsageStats/UsageEvents, opt-in NotificationListenerService, scoped Accessibility observation, share/import, intents/App Links and documented provider interfaces. Signals must be persisted as provenance-bearing evidence, interpreted into typed source-neutral events, correlated and surfaced in Locco while the source app is not foregrounded.

**Implemented prototype (device verification pending):** opt-in NotificationListenerService, launcher-app discovery with display names and installed versions, explicit per-app observation switches, metadata-only notification receipts, SHA-256 idempotency key, Rust-owned schema v3, SQLCipher persistence and a native Observer feed. Android's notification-listener permission remains separately user-granted. The adapter intentionally ignores notification title/text/actions/extras. Instrumentation tests cover the ledger boundary; the end-to-end listener callback still needs real-device proof.

**Implemented next slice (device verification pending):** Rust-owned schema v4 adds a separate encrypted ledger for UsageEvents package lifecycle transitions. Observer exposes a separately user-granted Usage Access permission and imports selected-app foreground/background events from the last 24 hours when refreshed. This is history-on-refresh, not continuous background monitoring; no screen contents or in-app actions are exposed.

**Next acceptance:** verify app discovery and version display, grant Usage Access, then prove real foreground/background transitions for an explicitly selected app become deduplicated typed events in the encrypted local ledger. Verify permission revocation, process restart, history window, event ordering and Android-version differences. Continue real-device notification callback acceptance separately. Verify source/method/time/coverage, permission revocation, duplicate delivery, process restart, redaction, health and device battery/latency. See [Android App Observation and Interaction](../architecture/android-app-observation.md).

**Separate research gate:** prove outbound pre-TLS and inbound post-TLS observation in an app and HTTPS endpoint we own. This does not imply that third-party app processes can be observed or modified on stock Android.

## 3 — Local intelligence
Benchmark local model/embedding candidates; context compiler, retrieval, schema validation, event interpretation and golden set. Gate: measured latency/RAM, provenance-aware outputs and deterministic fallback.

## 4 — Workspace
Native Connected Sources, observation timeline, search, entity/evidence views, source health and review. Gate: accessibility, adaptive layout, background/process restoration and no false "connected" states.

## 5 — Capabilities
Typed plans, policy, approvals, idempotency, source-specific native handoffs/actions and postcondition verification. Prefer intents, App Links, share/PendingIntent and documented APIs; narrowly scoped user-enabled UI interaction only where appropriate. Gate: threat review and end-to-end proof.

## 6 — Expansion/iOS
New sources/platform only after contract parity, permission/privacy review and migration plan. No phase starts without prior acceptance evidence.
