# BusyBee donor research — Android app observer and provider bridge

**Ingested into Locco:** 2026-11-22  
**Donor repository:** `ysfclc/BusyBee`  
**Reviewed donor tree:** default branch `main` at the App Observer Lab source lineage (research results referenced commit `05d335794498516b7b7f2c5c3f32ecc9c6b89094`).  
**Rule:** this is a research synopsis, not a claim that donor code has been ported or that every experiment works on the user's phone.

## Donor documents and code

- [App Observer Lab README](https://github.com/ysfclc/BusyBee/blob/main/research/app-observer/README.md)
- [App Observer Lab recovery record](https://github.com/ysfclc/BusyBee/blob/main/research/app-observer/RECOVERY.md)
- [Platform Activity Integration](https://github.com/ysfclc/BusyBee/blob/main/docs/PLATFORM_ACTIVITY_INTEGRATION.md)
- [Native Provider Bridge](https://github.com/ysfclc/BusyBee/blob/main/docs/NATIVE_PROVIDER_BRIDGE.md)
- [Plaintext Observer POC](https://github.com/ysfclc/BusyBee/blob/main/docs/PLAINTEXT_OBSERVER_POC.md)
- [Source Register](https://github.com/ysfclc/BusyBee/blob/main/docs/SOURCE_REGISTER.md)
- [Architecture Decisions](https://github.com/ysfclc/BusyBee/blob/main/docs/DECISIONS.md)
- Observer implementation: `research/app-observer/android/app/src/main/java/com/ysfclc/appobserver/`

## App Observer Lab baseline

The separate diagnostic app was designed to measure public Android observation surfaces for Uber Driver, Bolt Driver, Waze, Google Maps, WhatsApp and WhatsApp Business.

Its documented baseline measures:
- installed app identity, version, target SDK and selected permissions;
- Usage Access summaries;
- notification metadata after explicit Notification Access;
- Accessibility event counts and aggregate window structure after opt-in;
- device-level active network transports and VPN presence.

The preserved baseline intentionally does **not** read notification title/body or screen node text, does not capture packets or per-app flows, has no INTERNET permission, no telemetry, and exports an on-device JSON report. It is a measurement tool, not yet the Locco product experience.

## Provider Activity / Native Provider Bridge direction

BusyBee's design says the product outcome should feel like a first-party provider integration, while honestly reporting the acquisition mechanism and coverage. Its proposed architecture separates:
- source adapters;
- evidence envelopes and provenance;
- provider-neutral normalisation;
- offer/trip/earnings lifecycle events;
- local ledger and UI;
- observation from provider-side action.

It calls out that notifications, screen capture/OCR and accessibility-visible UI are partial user-visible sources, not in-process API access.

## Plaintext observation research

The research goal is to understand outbound application data before TLS encryption and inbound data after TLS decryption. It explicitly proposes proving the method first in an Android app and HTTPS service we own, with synthetic lifecycle data, redaction, correlation and instrumentation health tests.

The donor analysis states:
- an ordinary non-root Android app cannot simply inject into an untouched third-party app process;
- Frida Server generally relies on privileged/root injection;
- Frida Gadget must be loaded by the target process, normally requiring an owned or modified/re-signed APK;
- modifying provider APKs can break signature/integrity, updates, pinning, login and other protections;
- VPN packet observation is not equivalent to HTTPS plaintext observation.

No evidence from this research proves current Uber/Bolt compatibility or authorises modifying those apps. Locco must preserve this distinction and separately assess every source.

## Locco adaptation

The donor research is now represented in:
- [Android App Observation and Interaction architecture](../../architecture/android-app-observation.md)
- [ADR-0014](../../decisions/ADR-0014-android-observation-and-interaction.md)
- [Product vision](../../product/vision.md)
- [Product roadmap](../../product/roadmap.md)

Next action is to audit the exact donor files, tests, package/permission declarations and licence, then implement Locco's own source-neutral observation subsystem. Do not copy the standalone diagnostic UI as the product and do not add a BusyBee runtime dependency.
