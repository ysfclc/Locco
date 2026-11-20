# Skill: Existing application and upgrade continuity

**Trigger:** Any Android package/signing/storage/database/migration/FFI/release change, or any change that can alter persisted user state.

## Baseline capture
Inspect source and, when available, the installed/released APK. Record:
- applicationId, namespace, versionCode, versionName, manifest entry points and supported Android versions;
- signing certificate fingerprint/lineage reference without exposing private keys;
- database names/schema versions/migrations, preferences, files, cache and export locations;
- Rust stable IDs, serialization formats, event versions and FFI/JNI contract versions;
- last supported APK commit, version and SHA-256;
- representative data shape and current integrity checks.

Mark missing information unknown. Never fill gaps from a prior assistant summary.

## Change design
1. List all data owners and every read/write path affected by the change.
2. Preserve package identity, signer lineage, storage locations and public contracts by default.
3. Prefer additive/backward-compatible schema and contract changes. Give each migration a monotonic version and deterministic, idempotent behavior where feasible.
4. Define transaction boundaries, interruption/retry behavior, duplicate handling, referential integrity and corruption detection.
5. Specify backup/export and restore implications before changing durable storage. A backup must be readable and actually restorable, not merely copied.
6. Preserve stable IDs, provenance, timestamps, user preferences and imported files.
7. Test old app/new data and new app/old data compatibility when rollback is part of the promise. State honestly when downgrade is impossible and provide a forward-recovery route.
8. Require explicit user approval and an accepted ADR for destructive data operations, package/signer identity changes or breaking migrations.

## Verification procedure
1. Preserve a representative pre-upgrade fixture or test database with known records, relations, files and preferences.
2. Install the previous supported build with that data using the expected signing identity.
3. Capture counts, IDs, relationships, checksums and settings before upgrade.
4. Install the candidate in place without uninstall/clear-data. Record actual install output and APK identity.
5. Exercise migrations, app relaunch, interrupted migration recovery, export/restore and relevant user flows.
6. Compare post-upgrade records, relationships, file hashes, preferences, provenance and settings against expected results.
7. Test clean install separately. It does not count as upgrade evidence.
8. Attach commands, source/APK hashes, device/API level, before/after integrity report and gaps to every linked LOC issue.

## Fail conditions
Any unexplained missing/duplicated data, changed stable ID, broken relation, signer/package mismatch, incomplete migration, destructive reset or untested recovery path blocks acceptance. Never tell the user to uninstall or clear data as the normal fix.
