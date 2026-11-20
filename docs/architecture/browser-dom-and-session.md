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

Do not assume Android CookieManager is automatically profile-isolated. Investigate AndroidX WebKit multi-profile support, WebView data-directory constraints and process requirements. Choose the isolation strategy only after a device/API-level spike and ADR.

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
