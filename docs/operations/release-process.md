# Release process

A release is an upgrade of the existing Locco application, not publication of a parallel app.

## Before build
- Confirm all linked `LOC-####` issues and acceptance criteria.
- Review `GOVERNANCE.md` and [application continuity](../governance/application-continuity.md).
- Confirm package/application ID, signing lineage, versionName, monotonically increasing versionCode, database schema and migration sequence.
- Confirm all required CI checks and independent reviews passed on the exact source commit.
- Confirm no required issue is silently closed, deferred or omitted from release notes.

## Build and verification
- Build from a clean checkout at the recorded commit.
- Record toolchain versions, APK path, package metadata and SHA-256.
- Verify native libraries and supported ABIs are present.
- Install as an in-place upgrade over the previous supported build using the expected signing identity.
- Verify representative existing records, files, preferences, provenance and settings survive.
- Test migrations, launch, key existing flows, the reported fixes/features, permission behavior and recovery.
- Test clean install separately; never treat it as a substitute for upgrade.
- Do not ask users to uninstall/clear data as a routine workaround.

## Release record
Record commit SHA, version metadata, APK SHA-256, signing certificate fingerprint/reference, SBOM/attestation, CI run, tests, upgrade source/target, data-integrity outcome, known gaps, release notes and rollback/forward-recovery instructions.

## Completion
Update each linked feedback issue. A bug is fixed only when the reported behavior is verified; it is released only when that verified correction is in the identified release artifact. Keep unshipped work open. Do not claim physical-device or upgrade verification unless it was actually performed.
