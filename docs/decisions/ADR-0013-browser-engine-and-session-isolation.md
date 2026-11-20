# ADR-0013: Browser engine and session isolation evaluation

**Status:** Proposed investigation; no engine selected · 2026-11-20 · Reversal cost: High

## Context

Locco requires a real Android browser with dependable account sessions, DOM observation/Stitching, site permissions and safe interaction. The user's Memo-2 login failure makes account authentication a critical acceptance requirement.

The initial architecture assumed Android WebView as renderer. Current Android APIs materially improve that option: AndroidX WebKit 1.9.0 added profile isolation, and 1.16.0 adds frame/origin-scoped JavaScript injection in separate execution worlds. GeckoView is also designed for browser embedding and has session/context controls, but adds a bundled engine and its own update/resource costs.

Authentication is separate from rendering-engine selection. Google prohibits OAuth authorization inside embedded WebViews. Android Auth Tab/Custom Tabs and Credential Manager provide supported identity flows; their sessions are not automatically shared with an embedded WebView.

## Decision

Do not lock WebView or GeckoView in the foundation architecture.

Run the same target-device spike suite for:
- Android System WebView + AndroidX WebKit.
- Mozilla GeckoView.
- AndroidX Browser Auth Tab/Custom Tabs and Credential Manager as authentication integrations, not competing rendering engines.

Select only after the scorecard in docs/architecture/browser-engine-evaluation.md has evidence from the target device, affected sign-in scenarios, DOM isolation, profile lifecycle, package/RAM/thermal measurements and security/update review.

## Consequences

- Browser implementation does not begin with an irreversible engine dependency.
- Stable product contracts cover tabs, profile identity, navigation, observations, downloads, permissions and lifecycle; engine-specific DOM APIs are not assumed to be drop-in portable.
- WebView multi-profile is used only when MULTI_PROFILE is runtime-supported. setProfile must occur before page operations except attaching the view.
- Third-party cookies remain rejected by default. Any per-WebView exception requires a documented compatibility reason and test.
- DOM observation prefers origin-allowlisted scripts in isolated JavaScript worlds. If unavailable, privileged DOM automation is unavailable; page-world fallback cannot authorize actions.
- OAuth/OIDC connection uses supported auth surfaces with PKCE/state/nonce validation. It does not grant Locco access to the Auth Tab/Chrome cookie jar from embedded WebView.
- Google and other providers that reject embedded OAuth require supported handoff; no user-agent spoofing or cookie transplant.
- GeckoView is not an identity-provider policy bypass.
- If neither engine meets the account/session requirement, supported product capability is constrained honestly and requires visible handoff.

## Alternatives considered

1. WebView by default without a spike — rejected; device provider differences and account/login needs are too consequential.
2. GeckoView by default — rejected pending actual package/RAM/thermal, compatibility and security measurements.
3. Auth Tab as the entire Locco browser — rejected; it is an authentication surface, not a full multi-tab browser.
4. Import Chrome/Auth Tab cookies into WebView — rejected; unsupported and unsafe.
5. Spoof user agent to bypass embedded OAuth policy — rejected; policy evasion and unreliable security.

## Acceptance evidence

See docs/architecture/browser-engine-evaluation.md and LOC-0005. Supersede this investigation ADR with a selected-engine ADR only after spike results and independent review.
