# ADR-0014: Android app observation and interaction as a core capability

**Status:** Accepted product direction; implementation and platform feasibility gates pending  
**Date:** 2026-11-22  
**Owners:** Product + Android  
**Review:** Architect, Security, QA

## Context

The user's central ambition for Locco is not merely to ship a note-taking or capture app. Locco should make Android more observable and composable: learn useful activity occurring in other selected apps, interpret it into a structured local model, surface it in Locco while the source app is not open, and provide safe ways to hand off or perform supported interactions.

BusyBee contains substantial prior research: App Observer Lab, Platform Activity Integration, Native Provider Bridge and the owned-app Plaintext Observer POC. This work distinguishes Android system signals, visible UI, provider APIs and in-process plaintext observation. It also documents the limits of ordinary Android app sandboxing.

The existing Locco roadmap mentions notifications and source adapters but does not elevate cross-app observation and interaction to a defining capability. That underspecifies the user's intent.

## Decision

1. Android app observation and interaction is a first-class Locco product capability, not a side experiment.
2. Locco will use a layered acquisition model: UsageStats/UsageEvents, opt-in notifications, scoped Accessibility observation, user-mediated intents/share/import, documented provider APIs/SDKs and controlled in-process research.
3. Each source exposes a capability profile with per-signal permissions, field coverage, action support, health, freshness and blind spots.
4. Every acquired signal becomes a provenance-bearing observation/evidence envelope and is interpreted into provider-neutral typed events before presentation.
5. Locco's UI displays interpreted source activity locally without requiring the source app to be foregrounded, when Android delivers a supported signal.
6. Observation and action remain separate. Supported actions use documented intents, App Links, share/PendingIntent, APIs or explicitly enabled narrow Accessibility interaction; they require policy checks and postcondition verification.
7. BusyBee research is an input to audit and adapt, not an automatic code import or dependency.
8. The first product proof is a real Android system-observation slice end-to-end. A distinct owned-app test harness proves pre-TLS/post-TLS observation. No claim is made that this enables inspection of arbitrary third-party processes.
9. No root, APK repackaging, authentication bypass, certificate-pinning bypass, anti-tamper evasion, private endpoint replay or covert surveillance is a production dependency.

## Consequences

- Phase 2 Acquisition is expanded into an explicit Android Observation subsystem and capability registry.
- Phase 5 Capabilities must support source-specific handoff/action adapters and verification, not just generic workflow plans.
- Evidence records must retain acquisition method, source version, event/receive time, permission/consent state, completeness, redaction and parser lineage.
- The native workspace needs Connected Sources/Apps, live observation timeline, capability coverage and source health.
- Permissions are requested just-in-time with clear rationale, per-source allowlists and pause/revoke controls.
- Device acceptance must include background delivery, process restart, battery, thermal, latency and missed-event testing on real hardware.
- The product must never label notification or UI observation as a provider API or in-process observation.

## Alternatives considered

- **Only notifications and CSV import:** rejected as too narrow; it cannot represent the intended Android observation ambition.
- **VPN capture as universal observer:** rejected; VPN transport visibility does not ordinarily expose HTTPS plaintext or app semantic state.
- **Accessibility as universal app API:** rejected; it is incomplete, user-sensitive and not equivalent to private app internals.
- **Root or modified third-party APKs as default:** rejected; changes the device trust model, can break provider integrity and is not a viable assumed daily-device path.
- **Only official APIs:** rejected as the sole architecture because many apps expose no public API; retain as the preferred structured provider adapter where available.

## Verification required

- Audit BusyBee App Observer Lab and design docs; record exact donor commit, file list, licences and test/build status before porting.
- Implement a Locco-owned system-observation slice with at least one background Android signal and a real typed event persisted in the local evidence store.
- Demonstrate Locco presentation while the source app is not foregrounded.
- Verify source provenance, redaction, deduplication, permission revocation, process restart and degraded-state reporting.
- Demonstrate one safe supported handoff and verify its postcondition.
- Separately demonstrate outbound/inbound plaintext observation in an owned test app; no third-party compatibility claim without independent evidence.
- Security and QA review the permission, data minimisation, retention and action model before broader rollout.
