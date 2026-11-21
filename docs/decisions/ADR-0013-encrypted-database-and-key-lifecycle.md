# ADR-0013: Full-database encryption and key lifecycle

Status: Proposed · 2026-11-21 · Owners: Architect + Security · Review: Rust Core, Android, QA

## Context

Locco is local-first and will retain user-authored notes, captured evidence, source metadata, entity relationships, and future private memory. ADR-0008 explicitly prohibits treating plaintext persistence as acceptable for sensitive content. Encrypting selected columns alone would leave schema/data fragments, SQLite journal/WAL pages, indexes, and future records inconsistently protected. The first Android slice currently uses framework SQLite and plaintext storage; it is not safe for sensitive notes.

## Decision direction

Use full-database encryption (SQLCipher-compatible encrypted SQLite) as the target storage model rather than application-managed field encryption. The database, indexes, page metadata, journal/WAL, and temporary database artifacts must be covered by the encryption design. Rust remains the owner of canonical schema, migrations, domain rules, and storage-facing contracts; platform adapters own encrypted database opening, transactions, lifecycle, and platform key protection. This ADR is Proposed until the dependency/compatibility spike is reviewed.

### Key architecture

- Generate a random database encryption key; never derive it from user identity, device identifiers, a hardcoded secret, or a constant.
- Android: protect the database key using Android Keystore-backed key material. The database key is not persisted in plaintext, included in backups, logs, crash reports, or source control.
- Opening the database must fail closed when key access or encryption verification fails. Do not silently create a replacement key and open a new empty database over existing user data.
- Keep key access and database lifecycle in the Android storage adapter. Avoid exposing the raw database key through Rust FFI or general application state.
- Future iOS shell: use Keychain-backed protection for equivalent key lifecycle; validate parity before shipping cross-platform storage.
- Rust owns canonical migration definitions and record semantics; encrypted storage must not cause Kotlin-only schema drift.
- Rebuildable search indexes are still private data. They must remain inside the encrypted store or be separately encrypted and covered by the same lifecycle policy. No plaintext search cache.
- Exports are plaintext only when the user explicitly requests an export and is informed of the destination/format. Restore must validate input and re-encrypt into the Locco store.

## Backup, recovery, and device lifecycle

- Exclude the encrypted database and key envelope from cloud/device backup by default until a reviewed backup/restore protocol exists; a backup policy must never copy the key in a way that defeats encryption.
- Device transfer, backup restore, app reinstall, Keystore key invalidation, database corruption, and key loss are explicit states with user-facing recovery behavior.
- Key loss must not trigger silent data deletion, silent reset, or an automatic new-key fallback. Preserve the encrypted file and explain that without recovery material the content may be unrecoverable.
- Recovery material/rotation are not promised in this slice. They require a separate threat-reviewed design with UX and test cases before implementation.
- Do not make a sensitive-data safety claim based solely on a dependency being installed or the database file appearing unreadable.

## Threats and limits

Protects against offline inspection of copied database files and database sidecars when key material is unavailable. Does not protect data while the app is unlocked and running, rooted/compromised devices, malicious code in the app process, screenshots, user-exported plaintext, or someone who can access the device while Locco is unlocked. Metadata such as app presence, file sizes, and timing may remain observable.

## Spike acceptance gates

Before implementation is accepted:
1. Compare SQLCipher-compatible library choices (maintenance, Android API support, licensing, ABI footprint, backup/WAL behavior, JNI/FFI compatibility, and future iOS availability).
2. Prove encrypted database, WAL/journal, temp artifacts and indexes contain no plaintext test sentinel after writes/checkpoint/close.
3. Verify wrong key and missing/invalidated Keystore key fail closed without replacing or deleting the existing database.
4. Verify key lifecycle: creation, app process restart, lock/unlock policy, reinstall, backup exclusion, key loss and failure UX.
5. Test migration from the current v2 plaintext schema. Migration must be explicit, transactional where possible, preserve rows/IDs/relationships, and never delete the source until encrypted destination verification and integrity checks pass. Handle insufficient storage and interruption with a resumable/recoverable process.
6. Verify Android CI build and automated tests. Device tests are required before claims of verified Android key lifecycle.
7. Review data leakage through logs, crash reporting, caches, notifications, backups, and export paths.

## Alternatives

- Per-field encryption: rejected as the primary store because it complicates query/index behavior and leaves structural metadata and temporary artifacts exposed.
- Framework SQLite plus ad hoc file encryption: rejected; easy to create unsafe plaintext sidecars and incomplete coverage.
- User-entered passphrase only: deferred; UX and recovery costs need a dedicated design.
- Plaintext storage: unacceptable for private Locco data.

## Rollout

The existing first-slice local notes remain plaintext and must not contain sensitive content. A security gate in the product and release documentation must remain until encryption and migration tests are complete. This proposal does not authorize shipping or handling sensitive data.

## Reversal

The storage adapter can be replaced behind platform storage ports. Once encrypted databases ship, format/key versioning and migration compatibility become critical. Never change cipher configuration or key derivation without a versioned migration and verified restore path.
