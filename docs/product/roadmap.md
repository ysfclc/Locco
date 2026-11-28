# Roadmap

## 0 — Foundation
Product, principles, architecture, FFI, Rust workspace, Android shell, iOS plan, security, agents, skills, direct GitHub issue/branch/PR delivery and CI. The legacy ZIP-ingestion workflow is retired. Gate: three Rust Android ABIs + APK; docs agree.

## 1 — Local kernel
**IN PROGRESS:** native session workspace, pure capture kernel, Kotlin unit tests, local SQLite session persistence and legacy preference import. Rust defines platform-neutral Source, Evidence, Entity, Relationship and Event records.

**Implemented in this slice:** Rust owns schema versions 1–8 and ordered SQLite DDL. ABI v2 exposes a bounded caller-buffer C API and Android JNI adapter. Android SQLiteOpenHelper executes those migrations. Version 2 adds note-to-source provenance links; version 5 adds source-linked evidence; version 6 adds a per-session note ordinal. Existing v5 notes are backfilled in the prior deterministic `(created_at,id)` display order, while new writes append transactionally so equal-millisecond captures retain insertion order. SQLCipher-backed storage and Android Keystore key wrapping are implemented.

**Acceptance still required:** prove actual session plaintext is absent from DB/WAL/journal while records reopen correctly; implement a safe, recoverable migration for existing plaintext databases; complete source/evidence CRUD, entity relation operations, append-only event APIs, lexical retrieval and verified export/restore. Sensitive-data readiness remains blocked until migration and security review pass.

## 2 — Acquisition and Android Observation (IN PROGRESS — first vertical slice)
This is a defining Locco capability, not a generic file-import phase. Build the Android observation subsystem and capability registry: app identity/version, UsageStats/UsageEvents, opt-in NotificationListenerService, scoped Accessibility observation, share/import, intents/App Links and documented provider interfaces. Signals must be persisted as provenance-bearing evidence, interpreted into typed source-neutral events, correlated and surfaced in Locco while the source app is not foregrounded.

**First usable workspace slice:** the Android Activity destination now presents separate Usage Access and notification permissions, selected-app management, a refreshable captured-event timeline. UsageEvents transitions are stored only as context, not shown as standalone events. Build and real-device acceptance are still required; this UI does not imply continuous monitoring or access to app screen contents.

**Implemented on main in PR #14 / commit 64938d7 (signed release and device verification pending):** opt-in NotificationListenerService, launcher-app discovery with display names and installed versions, explicit per-app observation switches, metadata-only receipts by default, SHA-256 idempotency, SQLCipher persistence and a native Observer feed. A separate off-by-default toggle now permits bounded notification title/text capture for selected apps, with obvious code/credential/card-like values filtered before storage. Rust schema v7 adds nullable content fields without changing older rows. Each event is clickable and exposes its source, timestamps, acquisition method, captured content, content hash and redaction metadata. Android's notification-listener permission remains separately user-granted. Unit and instrumentation tests cover filtering, bounds, encrypted persistence and migration; the listener callback and UX still require physical-device acceptance.

**Implemented next slice (device verification pending):** Rust-owned schema v4 adds a separate encrypted ledger for UsageEvents package lifecycle transitions. Observer exposes a separately user-granted Usage Access permission and imports selected-app foreground/background events from the last 24 hours when refreshed. These are stored as contextual signals and deliberately excluded from the user-facing event feed. This is history-on-refresh, not continuous background monitoring; no screen contents or in-app actions are exposed. The lifecycle reducer aggregates Android Q+ activity resume/pause callbacks into package-level visibility boundaries, so switching between activities in one app does not create a false background transition. Legacy package-level events are used only when modern activity events are absent for that package.

**Merged on main — PR #21 / commit `18125137e057dd42220d1948af3290e1e8b49970` (automated tests passed; physical-device acceptance pending):** a passive `AccessibilityService` observes only window-state, accessible-control click and scroll events. Android dynamically restricts event delivery to the existing selected-app allowlist. Capture defaults OFF and requires a separate affirmative in-app disclosure/consent as well as Android service enablement. Consent and app scope are rechecked when queued events are persisted. Password/editable/invisible node text and descriptions are not read; callback data is bounded and throttled. The service does not perform gestures/actions, take screenshots, read text-change events or access app-private data. A separate Rust-owned schema v8 `app_ui_observations` ledger leaves the v3/v7 notification table and rows unchanged. Observer cards and the existing inspector expose the accessible hierarchy, target, timestamps, adapter, hashes, redactions and partial/unavailable status. It never treats a click as proof of a completed business action.

**Automated acceptance:** Rust format/tests/Clippy/migration harness, security/policy and change governance passed; Android debug build and Kotlin unit tests passed; all 26 emulator instrumentation tests passed (0 failed), including encrypted v7-to-v8 upgrade preservation and the legacy plaintext migration path. Exact evidence is recorded in [PR #21](https://github.com/ysfclc/Locco/pull/21). The prior failed emulator run exposed an old hard-coded expected schema version 7; the test was updated to 8, and the complete rerun passed.

**Next acceptance:** publish the signed v0.3.1 patch, install over the existing app on the Redmi Note 12 Pro 5G, and verify launcher mask/themed monochrome rendering, Accessibility service grant, disclosure-off/on, selected-app scoping, revocation during queued activity, real screen/click/scroll callbacks, inspector detail, process restart, encrypted persistence and battery/thermal behavior. This is the first accessible-UI observation source, not universal access or completion of cross-app reconstruction/correlation. See [Android App Observation and Interaction](../architecture/android-app-observation.md).

**Separate research gate:** prove outbound pre-TLS and inbound post-TLS observation in an app and HTTPS endpoint we own. This does not imply that third-party app processes can be observed or modified on stock Android.

## 3 — Local intelligence
Benchmark local model/embedding candidates; context compiler, retrieval, schema validation, event interpretation and golden set. Gate: measured latency/RAM, provenance-aware outputs and deterministic fallback.

## 4 — Workspace
Native Connected Sources, observation timeline, search, entity/evidence views, source health and review. Gate: accessibility, adaptive layout, background/process restoration and no false "connected" states.

## 5 — Capabilities
Typed plans, policy, approvals, idempotency, source-specific native handoffs/actions and postcondition verification. Prefer intents, App Links, share/PendingIntent and documented APIs; narrowly scoped user-enabled UI interaction only where appropriate. Gate: threat review and end-to-end proof.

## 6 — Expansion/iOS
New sources/platform only after contract parity, permission/privacy review and migration plan. No phase starts without prior acceptance evidence.
