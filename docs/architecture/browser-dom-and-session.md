# Native browser, account sessions and DOM intelligence

**Status:** Proposed for LOC-0004 and LOC-0005 review. Not implemented in Locco yet.

## 1. Product requirement

The browser is a first-class environment with two equal purposes:
1. A proper browser the user can operate normally.
2. A safe observation/interaction surface that exposes structured page understanding to Locco.

It is not only a capture button, provider-specific AI chat screen or hidden automation WebView. Browser chrome is native Compose; website content is rendered by Android's embedded browser engine.

## 2. Human browser experience

Required capabilities:
- Address/search bar, origin/security indicator, back/forward, reload/stop, loading/error state.
- Tabs, tab switcher, close/reopen and predictable restoration.
- History, bookmarks, recently visited and saved pages.
- Downloads with safe destination, progress and cancellation.
- Find-in-page, text selection, share and save to workspace.
- Site permissions, connection status and per-site controls.
- Normal and private/ephemeral profiles with clear data behavior.
- Inspect/Select mode for DOM elements and semantic extraction.
- Visible takeover for AI-assisted interactions.

All visible controls must work. Browser acceptance covers navigation, address parsing, tabs, state restoration, permissions, downloads and authentication.

## 3. Engine and security boundary

WebView is a rendering component, not the product browser itself. Locco owns browser chrome, tab/session metadata, history, downloads, permissions, account/session UX, DOM observation and execution integration.

Page scripts are untrusted. Locco must not expose a privileged JavaScript interface to arbitrary sites. Observation scripts are static, versioned and reviewed; model output is never evaluated as JavaScript. Navigation, popup and scheme requests are validated. HTTPS errors are not bypassed. File/content access and mixed content are denied by default.

A bounded, read-only evaluation call or origin-scoped WebMessage mechanism may return structured observations. The exact AndroidX WebKit mechanism must be feature-checked and tested on the target device; it must not be assumed available across all WebView versions.

## 4. Identity, sign-in and session profiles

Account login is a release-critical browser capability. User feedback says Memo-2 account sign-in failed while Memo-1 did not show the same problem in their experience. Root cause is unknown and must be reproduced.

Profiles to design:
- Persistent normal profile.
- Private/ephemeral profile with explicit destruction semantics.
- Additional isolated profiles only when reliable isolation is demonstrated.

A profile owns its WebView data context, cookies, DOM/local storage, cache policy, permissions, tabs and history. Accounts must not bleed across profiles. Persistent sessions should survive normal restart/update when the website permits.


### 4.1 Android WebView profiles: viable, but feature-gated

AndroidX WebKit 1.9.0 added the multi-profile API. A Profile represents one browsing session and each profile has separate data such as cookies and WebStorage. ProfileStore manages creation and deletion. WebViewCompat.setProfile binds a WebView to a named profile and must be called before doing anything else with that WebView except attaching it to the view hierarchy. Availability is runtime-gated by WebViewFeature.MULTI_PROFILE.

Implications:
- Use a named persistent profile for normal browsing.
- A private profile may use a separate ephemeral lifecycle only after deletion of cookies, WebStorage and profile data is verified.
- Tabs belong to a profile and share its identity; they are not separate account silos.
- Use the profile-specific CookieManager and WebStorage, not the default global manager, for non-default profiles.
- If MULTI_PROFILE is unavailable, expose only the default session. Do not simulate isolation by copying cookie strings or sharing global cookie state.
- Before deletion, close/destroy every WebView bound to the profile, delete it through ProfileStore, then verify removal.
- Test profile creation, switching, deletion and persistence on the target phone's actual WebView provider version.

References: [AndroidX Profile](https://developer.android.com/reference/androidx/webkit/Profile), [ProfileStore](https://developer.android.com/reference/androidx/webkit/ProfileStore), [WebViewCompat.setProfile](https://developer.android.com/reference/androidx/webkit/WebViewCompat), [AndroidX WebKit release notes](https://developer.android.com/jetpack/androidx/releases/webkit).

### 4.2 Third-party cookies

Android's CookieManager controls third-party cookies per WebView. Apps targeting Lollipop/API 21 or later default to rejecting third-party cookies. This setting can differ across WebView instances.

Do not enable third-party cookies globally as a speculative login fix. Locco needs a conservative default, per-profile/site compatibility decisions with visible rationale, SameSite/Secure and federated-login fixtures, and no cookie-value diagnostics. Any compatibility exception must have a reproducible reason and test.

Reference: [CookieManager.setAcceptThirdPartyCookies](https://developer.android.com/reference/android/webkit/CookieManager).

### 4.3 Three distinct sign-in paths

Do not collapse these into one "login works" checkbox.

**A. Ordinary website browsing inside Locco.** The user signs into a site in the actual browser profile when that site permits embedded browsers. Its cookies/storage remain in that profile. Verify the authenticated destination after sign-in and app restart.

**B. Connecting an account to Locco.** When Locco needs delegated authorization (OAuth/OIDC), prefer a supported browser authentication flow such as AndroidX Browser Auth Tab where available, or the Custom Tabs fallback. Use authorization code + PKCE, validate state and OIDC nonce, and verify redirect ownership. Auth Tab is an authentication surface, not the Locco browser engine.

Auth Tab shares cookie/credential state with Chrome by default and can be made ephemeral. It does not make the authenticated Chrome session available as cookies in an embedded WebView. A successful OAuth callback may give Locco a scoped token for an authorized API; it does not mean Locco can inspect the provider's logged-in website DOM.

**C. Provider rejects embedded authentication.** Google explicitly disallows OAuth authorization inside embedded WebViews. Do not spoof a user agent, hide the WebView or bypass provider policy. Use supported native sign-in/credential or Auth Tab/Custom Tabs flows. If a user wants to browse a website that requires that provider's login, explain whether the handoff can return a usable page session; do not claim a Chrome/Auth Tab cookie is transferred into Locco's WebView.

WebView passkey support is limited: Android documents it for first-party domains associated with the app through Digital Asset Links; it is not general external-identity-provider support. WebView does not share its cookies/session with Chrome and lacks WebAuthn Conditional UI. Use Credential Manager or Auth Tab for supported native/IdP flows.

References: [Android passkey architecture: Credential Manager vs Auth Tab vs WebView](https://developer.android.com/identity/passkeys/architecture), [Google OAuth best practices](https://developers.google.com/identity/protocols/oauth2/resources/best-practices).

### 4.4 DOM observation scripts in isolated worlds

AndroidX WebKit 1.16.0 adds JS_INJECTION_IN_FRAME_AND_WORLD, including origin-scoped addJavaScriptOnEvent and world-specific addWebMessageListener. Both APIs require runtime feature detection.

Preferred path when supported:
- Register a static, reviewed observer script in a dedicated isolated JavaScript world.
- Restrict injected frames to exact allowed-origin rules and intended frames.
- Register a minimal read-only message listener in the same isolated world.
- Validate message origin, source frame, schema version, size, profile/tab/document epoch and rate limits in native code.
- Return only bounded PageObservation data; no native actions, credentials, filesystem, tokens or arbitrary command calls are exposed to page content.
- Remove the script/listener when the tab/profile is destroyed or observation is disabled.

The older page-world addWebMessageListener API injects an object into the page's global scope; it is not the preferred trust boundary for arbitrary websites. Do not silently substitute it when isolated-world support is missing.

Fallback policy:
- The browser remains usable.
- DOM AI features are marked unsupported/degraded for that WebView instance, or limited to a one-shot read-only observation whose page-world result is treated as untrusted and cannot authorize an action.
- No page-world fallback receives privileged native methods.
- Never evaluate model-generated JavaScript.

References: [WebViewCompat.addJavaScriptOnEvent](https://developer.android.com/reference/androidx/webkit/WebViewCompat), [WebViewCompat.addWebMessageListener](https://developer.android.com/reference/androidx/webkit/WebViewCompat), [WebViewFeature.JS_INJECTION_IN_FRAME_AND_WORLD](https://developer.android.com/reference/androidx/webkit/WebViewFeature).


Authentication scenarios:
- Username/password form login where allowed.
- Redirect/OAuth/OIDC authorization-code + PKCE handoffs where applicable.
- Popup/new-window login and provider consent.
- SameSite/Secure/third-party cookie behavior.
- HTTP auth challenges.
- Custom schemes and verified app links.
- Passkeys/WebAuthn where supported.
- MFA and human challenge handoff.
- Return to originating tab after handoff.
- Process death, renderer crash, app update and WebView update.

No password, OTP, recovery secret, cookie or passkey assertion enters DOM observations, model context, workflow artifacts or logs. CAPTCHA/2FA remain human-controlled.

Some identity providers intentionally reject embedded WebViews. In those cases provide a secure external/system-auth handoff and state honestly whether the resulting session is available inside Locco. Do not claim that external-browser cookies can be imported when the platform does not provide that ability.

Generic authentication is the default. Provider-specific compatibility rules must remain small, reviewed and justified; core must not contain one login workflow per provider.

## 5. DOM observation protocol

The observation is a bounded, versioned data object, not live DOM or executable code. Suggested fields:
- observation ID/schema version.
- browser profile/tab and navigation/document epoch.
- origin, sanitized URL, title and timestamp.
- viewport, scroll and page readiness.
- main document plus separately attributed accessible frame/shadow regions.
- node records, relationships, locator candidates and truncation metadata.
- content hash and provenance.

Node fields:
- snapshot-local ID, parent/ordered children.
- semantic role and element kind.
- accessible name, labels and bounded text excerpt.
- safe allowlisted attributes (name, type, placeholder, aria-label, stable test ID).
- visibility, enabled, selected, checked, required, editable and focus states.
- bounds, viewport and frame/shadow scope.
- locator candidates/stability and sensitivity classification.

Never capture password, OTP, card, recovery or protected field values. Exclude hidden fields, script text, cookies, authorization headers and token-bearing URL fragments. Represent protected fields by marker and structure only.

Apply strict node-count/depth/text/byte/time budgets. Truncation is explicit. Page content remains untrusted evidence.

## 6. DOM Stitching

DOM Stitching connects observations into reusable semantic models:
- Relate repeated rows/cards into collections.
- Identify candidate fields and labels.
- Map different page structures to a user-defined schema.
- Link fields to entities/claims and source evidence.
- Infer pagination/repeated sections.
- Compare page structure over time and detect drift.
- Propose locator sets and extraction transforms.

The model proposes; deterministic validators check node references, origins, uniqueness, field types, sample consistency, confidence, unsupported assumptions and evidence coverage. Model output contains no executable JS or authority.

A stitched schema is versioned with observation IDs, transforms, model/runtime version, samples, confidence, trust and last verification. It begins UNTRUSTED/TRIAL. Page drift triggers re-observation and validation.

## 7. DOM/browser limits

Explicitly handle SPA route changes, virtualized lists, open/closed shadow roots, same/cross-origin frames, canvas/WebGL, responsive layouts, renderer death, stale nodes, downloads and source restrictions.

Cross-origin frames that cannot be read are marked inaccessible. OCR/screenshot fallback may assist human selection but is not equivalent to DOM access and cannot justify ambiguous side effects. Do not bypass login, paywalls, CAPTCHA, anti-bot controls or app sandbox boundaries.

## 8. Generic interaction safety

Expose a closed, typed action vocabulary (navigation, wait, scroll, extract, select, fill, toggle, click, upload a user-selected file, download, focus and commit). Every action references a current observation and locator. Resolve immediately before action; stale or non-unique resolution pauses.

Side-effecting actions require an envelope binding origin, exact sequence and values, capability, expected postcondition, expiry and payload hash. Final commits are not retried. Uncertain outcomes require receipt/postcondition verification or user review.

Loop: OBSERVE → PROPOSE → VALIDATE → AUTHORIZE → ACT → VERIFY → RECORD.

## 9. Android execution lifecycle

Do not assume invisible overlay-hosted WebViews run indefinitely. Android may throttle, pause or terminate browser/app processes. Prioritize visible sessions and reliable authentication. Background continuation is a separate gated capability with user-visible notification, pause/takeover and honest failure semantics. When a website requires foreground interaction, ask for takeover.

## 10. Privacy-safe diagnostics

A redacted support bundle may include Android/WebView/Locco versions, profile mode, feature-availability checks, redirect host sequence without secrets, blocked scheme categories, popup/cookie/permission decisions, renderer crashes and DOM schema/failure codes.

Never log cookie values, passwords, OTPs, recovery secrets, passkey material, sensitive form values or raw private page text.

## 11. Testing and acceptance

Fixture tests: form sign-in, redirect login, popup login, SameSite/third-party cookies, normal/private isolation, return-to-origin, SPA mutation, stale locator, open shadow root, same-origin frame, cross-origin exclusion, sensitive-field redaction, budget/truncation, renderer crash and challenge cancellation.

Device acceptance:
- Reproduce the Memo-1 versus Memo-2 user observation on the same device/browser engine where possible.
- Record website, login method, WebView version, profile and failure stage.
- Verify authenticated navigation after restart and normal update where provider allows.
- Verify external handoff honestly for sites that reject embedded browsers.
- Do not accept DOM automation until sign-in, authenticated read-only browsing, profile cleanup and credential redaction pass.

LOC-0005 is the feedback record and detailed acceptance issue.


## DOM identity, freshness and completeness contract

A DOM node identifier is **snapshot-local**, not a durable identity. A locator is a recipe that must be resolved against the live page immediately before use. These two concepts must never be conflated.

Each observation records:
- observation ID and schema version.
- profile ID, tab ID, navigation epoch, document epoch and monotonically increasing per-document mutation revision.
- canonical origin and sanitized URL (remove credentials and secret-bearing fragments/query values).
- frame route with frame identity, origin and access classification.
- root scope (document/open shadow root/other supported root).
- capture timestamp, observer version, bounded viewport/scroll/readiness and completeness/truncation state.

Each node has:
- snapshot-local node ID.
- Parent and ordered child IDs for this observation only.
- Role/kind, accessible name, allowlisted attributes, safe text excerpt and sensitivity classification.
- Visible/enabled/selected/checked/editable/focused state.
- Frame/root scope and bounds.
- One or more locator candidates with strategy, scope, expected semantic fingerprint and stability evidence.
- Explicit value state: not_read, redacted, user_selected, or safe_observed. Sensitive form values default to redacted; the observer never reads credential/payment/OTP/recovery input values.

Locator candidates are typed strategies, not arbitrary model-generated JavaScript. They may include accessible role/name, stable allowlisted attributes, constrained CSS within a known root, and frame/root traversal where supported. Avoid selectors based solely on volatile classes, array position or generated IDs. Do not expose arbitrary XPath/JS execution as a capability.

### Freshness and resolution

Before any interaction:
1. Verify the same profile, tab, origin, document epoch and permitted frame/root still exist.
2. If the document epoch changed, discard all node IDs and re-observe.
3. If mutation revision changed, re-resolve the locator against the current live tree.
4. Require exactly one matching element with the expected role/name/semantic fingerprint and permitted state.
5. Re-check visibility, enabled state, sensitivity, origin, frame and expected preconditions.
6. If zero or multiple candidates match, or material semantics changed, stop and request a fresh observation or user choice.

A snapshot node ID is never reused across document epochs. A locator candidate may survive a mutation only after fresh uniqueness and semantic checks. DOM Stitching cannot override this rule. Every action binds the observation ID, document epoch, frame/root scope, locator recipe/fingerprint and current target origin; commit approvals additionally bind exact normalized values and postcondition.

### Mutation observation and incremental capture

Mutation observers are on-demand, origin-scoped and resource-bounded. They are not installed permanently on every visited site. Use coalesced mutation batches, rate limits, node/depth/text/byte/time ceilings and backpressure. A change stream is an optimization; when the observer loses sequence continuity, the page navigates, the renderer restarts or the diff cannot be applied deterministically, take a fresh bounded snapshot.

Never infer a complete dataset from the currently rendered DOM. Virtualized lists may contain only visible rows. Record coverage as visible_slice, observed_collection, pagination_incomplete, user_scrolled_range, known_total_matches or unknown; the value is supported by evidence, not guessed. Pagination/scroll exploration is a separate typed action, and every captured range has its own provenance/observation. A complete result requires a site-provided/observed total or an explicit bounded traversal result with all expected pages/records accounted for.

### Stitching output contract

A stitched semantic model contains:
- Stable schema ID and schema version.
- Scope/origin policy and supported page families.
- Field definitions and types, required/optional status, normalization as declarative allowlisted transforms, and evidence links.
- Collection/entity/relation definitions with source observation/node references.
- Sample set, coverage and excluded/inaccessible regions.
- Locator recipes and expected semantic fingerprints—not stale node IDs.
- Validation report: field coverage, parse/type errors, duplicate/ambiguity rate, cross-page consistency, confidence calibration, drift status and known limitations.
- Provenance: source observations, user corrections, model/runtime version, validator version and timestamp.

Models may propose mappings and relationships only. Transform execution uses a small deterministic declarative language with bounded operations; no generated scripts, arbitrary regex backtracking, eval, network calls or native capabilities. A schema starts TRIAL; it can be promoted only after deterministic fixture/live-sample validation and explicit user acceptance where required. Any material origin/page-family drift moves it back to STALE or REVALIDATION_REQUIRED.

### Required adversarial and property tests

- Snapshot-local node IDs cannot resolve in another profile/tab/document epoch.
- SPA navigation, full navigation and renderer restart invalidate previous document node IDs.
- Attribute/label/role mutation, duplicate elements and hidden/disabled targets cause safe re-resolution or stop.
- Cross-origin frames cannot impersonate the top frame in messages; frame origin and scope are native-validated.
- Open and closed shadow-root behavior is accurately represented; unavailable roots never become fabricated children.
- Virtualized list tests prove no false total/completeness claim.
- Mutation overload, lost diff, node budget, depth, byte budget and time budget force explicit truncation/resnapshot.
- Password, OTP, passkey, card, recovery and sensitive values are redacted before serialization/model context/logging.
- Model-generated selector/transform payloads cannot execute code or exceed the declarative operation budget.
- Cross-page Stitching preserves per-field observation provenance and rejects conflicting/unmapped samples.
- Drift tests demote a previously valid schema before any action is authorized.
