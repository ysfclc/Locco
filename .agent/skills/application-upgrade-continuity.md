# Skill: Existing application and upgrade continuity

**Required for every Android package, signing, storage, database, migration, FFI or release change.**

## Baseline capture
Inspect and record:
- applicationId, namespace, versionCode, versionName and manifest entry points;
- signing lineage/certificate identity without exposing keys;
- database filename, schema version, migrations, preferences and user file locations;
- Rust stable IDs, serialization formats and FFI/JNI contract versions;
- previous supported APK and its SHA-256, where available.

Never infer these values from a previous assistant summary when the source or artifact can be inspected.

## Change design
- Preserve the package/application identity and signing lineage.
- Use additive/backward-compatible schema and contract changes by default.
- Give each migration an ordered version and deterministic behavior.
- Preserve records, provenance, preferences and imported files.
- Define transaction boundaries, interruption handling, backup/export, integrity checks and recovery.
- Version FFI/serialized contracts explicitly; test all supported ABIs and consumers.
- Require explicit user approval and an ADR for destructive data operations, package/signing identity changes or breaking migrations.

## Verification
1. Build the APK and record source commit, package, version and SHA-256.
2. Install the previous supported build with representative data.
3. Upgrade in place using the intended signing identity.
4. Verify database migration, record counts/relationships, files, settings, permissions and user-visible workflows.
5. Exercise interruption/failure handling and the documented recovery route.
6. Test clean install separately.
7. Record results and limitations on every linked feedback issue.

A fresh install is not proof of upgrade safety. A debug key is not proof of compatibility with a release-signed installation. Never tell the user to uninstall or clear data as the normal fix.
