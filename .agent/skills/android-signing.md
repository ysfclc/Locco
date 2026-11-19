# Skill: Android signing

When: creating debug/release APK. Preconditions: debug JDK/SDK; release approved request and protected environment.

Procedure: debug uses local/ephemeral key. Release checks environment approval/secrets; decodes keystore to unique temp file with restrictive permissions; Gradle config via env; sign and verify with apksigner; guaranteed cleanup.

Verify correct identity and no release key in logs/artifacts.

Failure: missing secret, wrong alias, signature mismatch or retained temp file. Never fallback to debug for release.