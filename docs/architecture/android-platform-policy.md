# Android platform policy

**Status:** Platform baseline and review gates; browser/runtime feature availability is checked at runtime, not inferred from SDK level alone.

- Foundation baseline: minSdk 26; compileSdk/targetSdk 35; Java 17; Gradle 8.9; Android Gradle Plugin 8.7.3; Kotlin 2.0.21; NDK 27.2.12479018.
- These are pinned foundation/sideload values, not a Play Store publishing promise. Revisit targetSdk before distribution and update SDK/build tools as a reviewed set.
- Rust Android ABIs: arm64-v8a, armeabi-v7a and x86_64. Physical device priority is arm64.
- No permissions are requested by the placeholder shell. Request only at the moment a real user-visible capability requires them, with rationale and denial fallback.
- AccessibilityService, NotificationListenerService, VPN, usage access and background location require separate product/security and current Android-policy review. Never request broad permissions "just in case".
- WorkManager is the default for deferrable/resumable work; foreground services only where current Android policy and user-visible ongoing work require them.
- Compose experience must respect font scale, dark theme, reduced motion, TalkBack, keyboard/IME and window size classes.

## Browser-specific platform requirements

- Browser engine choice remains open between Android System WebView + AndroidX WebKit and GeckoView; see browser-engine-evaluation.md and ADR-0013.
- AndroidX WebKit Profile/ProfileStore, third-party cookie behavior, isolated-world JavaScript injection and message listeners are runtime feature-gated. Check WebViewFeature support on the actual installed provider.
- Bind a WebView to a non-default profile before any page operation other than attaching it to the hierarchy. When multi-profile is not supported, expose only a single browser profile; never imitate isolation by moving cookies.
- Third-party cookie policy is per WebView. Keep denied by default; any scoped opt-in has a documented test and user-visible rationale.
- OAuth/OIDC account connection uses supported system/browser authentication paths with PKCE/state/nonce validation. Do not send an IdP OAuth request inside an embedded WebView when provider policy disallows it.
- Browser renderer crash, process death, provider update and profile deletion are explicit lifecycle events. Never assume WebView page state survives.
- Isolated-world DOM observation is enabled only when the required AndroidX WebKit feature is supported; unsupported devices keep the browser but do not receive privileged DOM automation.

## Other OS integrations

Background location, media, mic/voice, notification, foreground execution and file access require individual permissions and lifecycle design. Permission revocation stops affected capabilities safely. Native platform APIs own OS UI and prompts; Rust receives typed results, not Android framework objects.

## Browser file, popup, credential and site-permission handling

- User-initiated website uploads use Android document/photo pickers. Validate every returned content URI, MIME/size policy and grant; provide only the selected URI with temporary least-privilege access. Never grant page content broad app file access or rely on insecure `file://` access.
- Implement an explicit `WebChromeClient` file chooser and popup/new-window policy. The engine's default file/new-window behavior may cancel or ignore requests; do not treat the callback's existence as a working feature. User-gesture, origin, opener, profile and frame context are checked.
- Camera/microphone requests require both an origin-scoped browser decision and Android runtime permission. Location and notifications are separately handled. Default is deny; grants can be one-time or persistent where supported and are revocable per origin/profile. Do not call `grant()` for a web permission before the Android permission succeeds.
- Use the user's selected Android Autofill/Credential Manager provider where supported. Locco does not become a password manager or credential-provider service in this product scope. Credential values never enter Rust domain objects, DOM observations, AI context, logs or backups.
- Passkey and Auth Tab capabilities are feature- and provider-gated. WebView passkeys require the supported first-party Digital Asset Links relationship; they are not general third-party IdP support. Auth Tab/Custom Tabs and embedded WebView cookies remain separate.
- TLS certificate errors are always cancelled; never expose a “continue anyway” affordance. Safe Browsing availability is checked and its interstitial/warning path is tested; lack of provider support is shown as reduced protection, not silently treated as protected.
- Site data controls operate on the correct engine/profile, close bound renderer instances before deleting a profile, and verify cookies, WebStorage and cache deletion without logging their values.

References: [Autofill Framework](https://developer.android.com/identity/autofill), [Credential Manager passkey architecture](https://developer.android.com/identity/passkeys/architecture), [WebChromeClient file chooser](https://developer.android.com/reference/android/webkit/WebChromeClient#onShowFileChooser(android.webkit.WebView,android.webkit.ValueCallback,android.webkit.WebChromeClient.FileChooserParams)), [WebView file inclusion risks](https://developer.android.com/privacy-and-security/risks/webview-unsafe-file-inclusion), [WebView permission handling in Compose](https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/wrap-webview-in-compose), [SSL error handling](https://developer.android.com/reference/android/webkit/WebViewClient#onReceivedSslError(android.webkit.WebView,android.webkit.SslErrorHandler,android.net.http.SslError)).
