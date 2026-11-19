# Android platform policy

- Foundation baseline: minSdk 26; compileSdk/targetSdk 35; Java 17; Gradle 8.9; Android Gradle Plugin 8.7.3; Kotlin 2.0.21; NDK 27.2.12479018.
- These are pinned build baselines for the foundation APK, not a Play Store publishing promise. Revisit targetSdk before any distribution requirement and update SDK/build-tools together.
- ABIs: arm64-v8a, armeabi-v7a and x86_64. Physical device priority is arm64.
- No permissions are requested by the placeholder shell. Add permissions only with a user-visible capability, threat review, rationale and denial fallback.
- AccessibilityService, NotificationListenerService, VPN, usage access and background location each require separate product/security review and OS-policy review. Never request broad permissions just in case.
- WorkManager is the default for deferrable, resumable work; foreground service only when user-visible ongoing work and Android policy require it.
- Compose design system owns UI tokens and components. Platform adaptation respects system font scale, dark theme, reduced motion, TalkBack and window size classes.