# Skill: Android signing and artifact identity

**Trigger:** Any debug/release APK, signer change, signing configuration, installation/upgrade test or release artifact handoff.

## Inputs required
- Exact source commit and build variant.
- Application ID, versionCode/name and expected signer/lineage from the current supported baseline.
- Android SDK/build-tools versions and release authorization.
- For release, approved protected environment and externally provisioned signing secrets.

## Procedure
1. Read the Android Gradle signing configuration and manifest; never infer package/version/signer from chat memory.
2. Separate debug from release. Debug signing is for development only. A debug APK does not prove update compatibility with a release-signed install.
3. Never generate, replace, rotate, print or commit the user's release key as part of ordinary work. Missing release secrets block release; they do not authorize a debug fallback.
4. Build the requested variant from a clean checkout at the recorded commit. Record Gradle/JDK/SDK/build-tools versions and the exact output path.
5. For protected release signing, use only the repository's approved release workflow/environment. Decode the supplied keystore to a unique temporary file with restrictive permissions; disable shell tracing; use environment-backed passwords; remove the temporary file in a guaranteed cleanup step.
6. Verify APK signature with apksigner verify --verbose. Obtain the signer certificate digest with apksigner certificate inspection or the documented Android build-tools equivalent. Compare it to the approved baseline fingerprint/lineage without exposing private material.
7. Inspect package name, versionCode, versionName, manifest entry points, native ABIs and APK contents. Record SHA-256 after signing; do not reuse a hash from the unsigned input.
8. Install/upgrade only with the expected signer. A signature mismatch is a hard stop, not a reason to uninstall the existing app.
9. Check workflow logs, temp paths, artifacts and diagnostics for passwords, keystore contents or private key material.

## Required evidence
Source SHA; build variant; app ID/version; signer certificate fingerprint/lineage reference; SDK/tool versions; signature verification output; APK SHA-256; artifact path; install/upgrade result; sanitized log reference.

## Fail closed
Missing approval, absent secret, wrong alias, wrong package, non-monotonic versionCode, signer mismatch, unverifiable signature, leaked secret or retained temporary keystore blocks release. Do not substitute a debug key.
