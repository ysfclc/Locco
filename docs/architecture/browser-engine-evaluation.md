# Browser engine and authentication evaluation

**Status:** Required spike for LOC-0004/LOC-0005. No browser engine is selected.

## 1. Decision to make

Locco needs a browser engine suitable for a real Android browser and a generic DOM observation system. Evaluate:
- Android System WebView through AndroidX WebKit.
- Mozilla GeckoView.

AndroidX Browser Auth Tab/Custom Tabs and Credential Manager are authentication mechanisms, not alternative website rendering engines. They complement the browser for supported account-connection and identity-provider flows; they do not replace the user's general-purpose Locco browser.

Do not select an engine only because it is preinstalled on Android or one provider sign-in happens to work. Test the user's target sites, account/session behavior, DOM safety, modern web compatibility, privacy, memory/thermal budget and maintenance model.

## 2. Candidate comparison

| Criterion | Android System WebView + AndroidX WebKit | GeckoView |
|---|---|---|
| Distribution | Uses installed Android WebView provider | Engine/library bundled and updated with app |
| Version control | Provider differs by device; runtime feature detection required | Locco controls bundled engine, but owns engine upgrades |
| Browser suitability | Rendering surface; Locco builds all browser chrome and browser functions | Mozilla describes it as suited to embedded apps and browsers |
| Per-tab model | Locco owns tab metadata and WebView lifecycle | GeckoSession naturally represents a tab/session |
| Profile isolation | AndroidX WebKit Profile/ProfileStore API (1.9.0+) when MULTI_PROFILE is supported | GeckoSession context IDs partition cookie jars/storage; private-browsing setting is available |
| DOM integration | AndroidX WebKit 1.16 isolated-world/frame script injection and origin-scoped message listener where supported | GeckoView content/script/extension interfaces; exact safe observer bridge must be proven |
| Authentication limits | Embedded WebView may be rejected by IdPs. Google explicitly disallows embedded OAuth user agents. Auth Tab/Custom Tabs offer a separate handoff | Still an embedded browser; do not assume it bypasses provider policy |
| Session persistence | Profile data follows provider/profile lifecycle; test provider and app updates | GeckoRuntime owns persistent storage; GeckoSession has flush/restore state APIs; context deletion must be tested |
| Modern web APIs | Depend on installed provider and AndroidX feature support | Mozilla describes GeckoView as full-featured and self-contained |
| Binary footprint | No full engine bundled by Locco; measure APK/AAB impact | Bundled engine adds package/download/storage costs; measure actual artifact |
| Memory/thermal | Renderer still consumes app/system memory | Gecko runtime/processes; measure warm/cold, idle and multi-tab usage |
| Updates/security | Provider updates independently; Locco handles version drift | Locco ships GeckoView updates and tracks security cadence |
| Maintenance | Device/provider feature matrix | API, native packaging and release cadence become Locco responsibilities |

This is a research matrix, not a benchmark. Package size, RAM, login compatibility, performance and security must be measured.

Sources:
- [AndroidX Profile](https://developer.android.com/reference/androidx/webkit/Profile)
- [AndroidX ProfileStore](https://developer.android.com/reference/androidx/webkit/ProfileStore)
- [AndroidX WebKit release notes](https://developer.android.com/jetpack/androidx/releases/webkit)
- [AndroidX WebViewCompat](https://developer.android.com/reference/androidx/webkit/WebViewCompat)
- [Android Auth Tab/passkey architecture](https://developer.android.com/identity/passkeys/architecture)
- [Google OAuth embedded-browser restriction](https://developers.google.com/identity/protocols/oauth2/resources/best-practices)
- [Mozilla GeckoView](https://mozilla.github.io/geckoview/)
- [GeckoSession](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession)
- [GeckoSessionSettings.Builder](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSessionSettings.Builder)
- [GeckoRuntime](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoRuntime)

## 3. Authentication is separate from engine choice

Authentication has three contexts:
1. Website login held inside the browser engine's own profile.
2. Native account authorization to Locco through OAuth/OIDC or Credential Manager.
3. External website login that the provider refuses to perform inside embedded browsers.

Important platform facts:
- AndroidX WebKit 1.9.0 added Profile/ProfileStore with separate WebView profile data when runtime MULTI_PROFILE support exists. setProfile must happen before page operations other than attaching the view.
- Third-party cookie acceptance is per WebView. Apps targeting Lollipop or later default to rejecting third-party cookies.
- AndroidX Browser Auth Tab (Browser 1.9.0+, Chrome 137+ where supported) is designed for authentication. It shares Chrome cookie/credential state by default or can use ephemeral browsing. Unsupported default browsers fall back to Custom Tabs.
- Auth Tab returns an authentication result to Locco; it does not copy the authenticated Chrome cookie jar into Locco's WebView profile.
- OAuth/OIDC requires PKCE, state validation and (for OIDC) nonce validation. Auth Tab does not perform the protocol's application-side validation.
- Google prohibits OAuth authorization requests inside embedded WebViews. A user-agent change is not an acceptable workaround.
- WebView passkeys are a first-party-domain option using Digital Asset Links and Credential Manager; they are not general external-IdP support. WebView sessions are isolated from Chrome.

A provider may permit ordinary username/password login inside an embedded browser but disallow an external identity-provider redirect. Those are different cases. The browser UI must show the actual session context and must not claim account linkage when only an OAuth callback was completed.

## 4. Required spike plan

### S0 — Inventory device and reported failures
- Record device build, Android release, Android System WebView package/version and Chrome version.
- Record the affected sites/accounts and exact failure stage without requesting passwords, OTPs or recovery codes.
- Reproduce Memo-1 and Memo-2 behavior on the same device and provider where possible.
- Save redacted evidence: redirect host sequence, error codes, popup events, cookie policy, profile mode and navigation result.
- Never collect cookie values, request headers, form contents or account secrets.

### S1 — WebView profile/auth capability
- Check MULTI_PROFILE and JS_INJECTION_IN_FRAME_AND_WORLD on the target WebView.
- Create default and two named profiles; verify cookies, localStorage and service-worker/storage data do not cross profiles.
- Bind each profile before page operations; close a test profile, delete it, recreate it and confirm data is gone.
- Test third-party cookie default and per-WebView opt-in with controlled fixtures. Opt-in is explicit and narrowly scoped.
- Test same-site, cross-site redirect, popup and MFA flows.
- Test app process restart, app update and WebView provider update.

### S2 — Auth Tab and Credential Manager
- Test Auth Tab support detection, Chrome 137 path, older-Chrome/third-party browser fallback and redirect callback.
- Use a sample OAuth/OIDC client; validate PKCE, state and nonce.
- Confirm Auth Tab's Chrome session is not exposed as a WebView profile session.
- Test ephemeral Auth Tab and cancel/deny.
- Verify Google OAuth through a supported browser/native path, not embedded WebView.
- Test WebView WebAuthn only on a Locco-controlled first-party fixture with Digital Asset Links.

### S3 — Isolated DOM observer
- Check AndroidX WebKit 1.16 isolated-world feature support.
- Inject a static DOM reader in an isolated world with exact origin rules.
- Register a read-only message listener in the same world; reject wrong origin, disallowed frame, oversized payload, stale document epoch and invalid schema.
- Attempt page-world forgery of bridge names; verify no privilege is exposed.
- Cover main frame, same-origin frame, cross-origin frame and dynamic navigation.
- Unsupported path must keep the browser usable but not offer privileged DOM automation.

### S4 — GeckoView proof of concept
- Pin a stable GeckoView artifact and record exact size, checksum and license.
- Implement one GeckoRuntime, multiple GeckoSessions, normal/private context, context deletion and history/session restore.
- Test the same account/login fixtures from S0-S2.
- Test a read-only DOM observer using documented content interfaces. Do not ship a privileged WebExtension/bridge without review.
- Measure APK download increase, installed size, cold/warm startup, idle/one-tab/four-tab RAM, CPU, scrolling, media, battery and thermal behavior.
- Define engine update cadence, CVE response and rollback behavior.

## 5. Evaluation scorecard

| Dimension | Weight | Evidence |
|---|---:|---|
| Login success and session continuity | 25% | User-reported sites, ordinary login, OAuth/IdP and restart |
| Web compatibility | 15% | Same fixtures, SPA, media, downloads and modern page behavior |
| DOM safety and coverage | 15% | Isolated observation, allowed origins/frames, redaction and locator tests |
| Session/profile isolation | 10% | Cookie/storage cross-contamination and deletion |
| Performance/resource usage | 15% | APK size, RAM, CPU, startup, battery and thermal |
| Security/update support | 10% | Sandbox, security architecture, patch cadence and rollback |
| Lifecycle/recovery | 5% | Renderer crash, process death, session restore |
| Engineering/maintenance | 5% | API stability, integration complexity, test burden |

Weights can be revised before testing, not after seeing which candidate wins. Record raw measurements and failure cases, not only aggregate scores.

## 6. Decision criteria

Choose WebView if the multi-profile and isolated-world APIs exist on target/support devices, the user's actual account sign-in/session needs pass where providers permit embedded browsing, and DOM, performance and lifecycle gates pass.

Choose GeckoView if WebView cannot meet required browser/DOM/login compatibility after safe platform-supported fixes, and GeckoView passes the same login/profile/DOM/device suite within the storage, RAM, thermal and maintenance budget.

Do not choose GeckoView to evade embedded identity-provider policy. It is still an embedded browser component.

If neither meets a user journey, state the limitation and offer a supported handoff. Never simulate cookie transfer.

## 7. Spike output

The spike produces device/version inventory, redacted test evidence, package/resource measurements, feature matrix, limitations, security review and recommendation. A selected-engine ADR supersedes ADR-0013 only after tests and independent review.
