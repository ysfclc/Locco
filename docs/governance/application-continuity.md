# Application identity, data and upgrade continuity

**Normative companion to [GOVERNANCE.md](../../GOVERNANCE.md).** The objective is to improve the application the user already has, not ship a parallel application every time the product evolves.

## 1. Stable identity

Ordinary work MUST retain:
- the Locco repository and product identity;
- the Android `applicationId` and namespace unless an approved migration explicitly changes them;
- the installed application's signing lineage;
- persistent database/file/preferences locations;
- deep-link, intent and share-target contracts;
- the Rust domain identity, stable IDs and versioned FFI contract;
- user-visible settings, saved work and imported evidence;
- monotonically increasing Android `versionCode` for distributable builds.

Before changing any of these, inspect the current values from the source, manifest, build configuration and prior release artifact. Do not guess them from memory.

## 2. No replacement-app workflow

A bug fix or feature request MUST NOT result in:
- a new package/application ID;
- a second launcher app or duplicate shell;
- a fresh standalone demo presented as the product;
- replacing the main app with a parallel scaffold;
- resetting the repository, database, user storage or preferences;
- instructions to uninstall/clear data as the default update procedure;
- dropping existing modules or screens to make the new feature easier.

A separate product is an exceptional product-level decision and requires explicit user wording that a separate application is wanted. A feature branch, feature module, adapter or internal process is not a separate product.

## 3. Database and user data

Every persistent schema change must:
1. have a monotonically versioned migration;
2. define behavior for existing rows, files and relationships;
3. be tested from each supported prior schema that can reasonably occur;
4. be transactional or have a documented recovery strategy;
5. preserve provenance and source evidence;
6. avoid deleting or rewriting user content without explicit consent;
7. include backup/export and restore implications.

Use additive or backward-compatible changes by default. Renames are implemented as copy/migrate/verify before old fields are retired. Destructive changes require explicit approval, an export/backup, integrity check, rollback plan and tests with representative user data. Never use "reset database" as a substitute for fixing a migration.

## 4. Android install/upgrade contract

A release candidate must be signed with the expected lineage and tested as an in-place upgrade over the previous supported build. The test record includes:
- source commit and APK SHA-256;
- package ID, versionCode and versionName;
- signing certificate fingerprint or signing lineage reference (never the private key);
- source build and target build;
- representative pre-existing records/files/preferences;
- migration logs/outcome and post-upgrade integrity checks;
- launch, navigation, permissions, background/restore and affected feature checks;
- install/upgrade failure behavior and recovery route.

A clean install is a separate test. It cannot stand in for an upgrade test. A debug APK signed with a different local key cannot prove compatibility with an installed release-signed APK.

## 5. Rust, FFI and storage compatibility

The Rust core owns domain invariants and stable IDs. Cross-boundary structs/functions require explicit versioning and safe error handling. A FFI change must be tested for every supported ABI and every shell consuming that contract. Never change ownership, pointer lifetime, serialization layout or error semantics implicitly.

Persisted Rust data and events are versioned. Migrations are deterministic and idempotent where possible. When a new feature adds data, specify schema, indexes, identity, provenance, deletion/export behavior and offline behavior before implementation.

## 6. Rollback and forward recovery

For each release, document:
- what can be reverted by reinstalling the prior build;
- whether the old build can read the migrated data;
- whether a forward-fix is required after migration;
- how to export/backup before upgrade;
- what happens if the process is killed halfway through;
- how the user can recover without silently losing data.

If database migration is irreversible, the release needs a tested forward-recovery path and explicit user approval. Do not promise rollback that is technically impossible.

## 7. Continuity acceptance gate

No user-facing release is complete until:
- the package and signing lineage are checked against the baseline;
- versionCode advances correctly;
- an in-place upgrade is exercised;
- pre-existing data survives and remains usable;
- new and adjacent existing workflows work;
- migration, failure and recovery paths are recorded;
- APK hash and commit are attached to linked feedback issues.

If no previous supported APK exists yet, state that limitation honestly and define the first baseline artifact. Do not fabricate upgrade evidence.
