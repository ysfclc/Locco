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
