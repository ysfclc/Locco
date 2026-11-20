# Storage security, key lifecycle and recovery contract

**Status:** Proposed for LOC-0004/LOC-0003. No sensitive user payload may be persisted until the selected design passes the storage security spike and release tests.

## 1. Data classes and ownership

The canonical relational store is the source of truth. Referenced blobs are protected assets governed by the same retention and key policy. Derived indexes/caches are rebuildable; browser engine profiles have a separate lifecycle; public model weights are replaceable downloads rather than user evidence.

Classify at minimum:
- Public metadata and public model artifacts.
- Private workspace records and user-provided content.
- Original evidence and extracted observations.
- Credentials, cookies, passkeys, OTPs and recovery material.
- Execution/approval records and external action receipts.
- Temporary imports, partial downloads and diagnostic bundles.

Credentials and authentication secrets are never placed in canonical evidence, model prompts, FFI payload logs, ordinary backups or support bundles. Browser profile session data is managed by the selected engine and is not silently copied into workspace exports.

## 2. Encryption boundaries

The design must protect:
- Canonical relational pages, indexes that reveal private content, WAL/journal files and temporary copies.
- Evidence and artifact blobs, thumbnails, extracted text and generated previews.
- Backup/export archives and temporary staging files.
- Any local search/embedding projection containing user content.

Platform file-based encryption is useful defense in depth but is not accepted as the only Locco storage protection without a threat review.

Use vetted authenticated-encryption and key-management libraries. Do not design custom cryptographic primitives or reuse a nonce with the same key. The exact database encryption driver and AEAD suite remain an implementation decision after the spike.

## 3. Key hierarchy proposal

Use envelope encryption rather than hardcoding a password or embedding a static key:
1. Generate random data-encryption key material when initializing a workspace.
2. Wrap the data key with a non-exportable Android Keystore key (or the future platform key provider).
3. Keep unwrapped data keys in memory only for the minimum necessary lifetime; zeroize where the implementation/runtime permits and never log or serialize them.
4. Use separately scoped keys or derived subkeys for database, blobs and backup artifacts so a format/rotation change can be isolated.
5. A user-initiated portable backup may wrap the workspace data key with a key derived from a user passphrase using a vetted, parameterized password KDF. Never use the passphrase directly as an encryption key and never send it to a service.

A hardware-backed Keystore key is preferred where available, but availability and behavior vary by device. Record actual security level; do not claim hardware protection merely because Android Keystore is used.

Biometric/device-credential gating is a UX and threat-model choice. It must not make unattended recovery impossible or create an unannounced key-loss path.

## 4. Key loss, rotation and recovery

- If the Keystore key is invalidated, missing or unusable, Locco enters an explicit LOCKED/RECOVERY_REQUIRED state. It never creates a new empty key and overwrites the old workspace.
- If a user recovery passphrase/key exists, allow verified recovery from an encrypted export on a new installation/device.
- Without a recovery secret or valid key, encrypted data may be unrecoverable. Explain this before the user relies on the workspace; do not imply account login can restore local encryption keys.
- Key rotation is versioned and crash-safe. Keep old key material until every canonical row/blob/backup needed for the migration has been re-encrypted or deliberately retired and verified.
- Revoke/erase old key wrappers only after integrity and restore checks pass.
- Authentication cookies/tokens are not the workspace recovery key and are never used to derive it.

## 5. Backup and export policy

A backup manifest identifies format/schema versions, workspace generation, included records/blobs, sizes, digests, encryption parameters and creation time. Export is user-initiated, staged atomically and verified before it is presented as complete.

Default backup includes canonical records, referenced user evidence/blobs, user-authored capabilities, permitted settings and recovery metadata. It excludes by default:
- Browser cookies, OAuth tokens, passkeys and active authenticated sessions.
- Temporary cache/partial downloads and derived indexes (rebuild them).
- Public model weight files (re-download where available; preserve model preferences and exact revisions).
- Raw diagnostics, prompt traces and temporary page state.

Browser bookmarks/history may be included only under an explicit profile-data choice; session credentials remain excluded. An imported backup is untrusted input and must pass path, size, schema, digest and migration checks before activation.

Restore is a staged transaction: inspect → verify archive → unlock/decrypt → validate schema → stage data → run integrity checks → commit workspace switch. A failed restore keeps the previous workspace intact. Restore never replays execution attempts or external side effects.

## 6. Browser and account continuity

Normal app updates must preserve the chosen persistent browser profile through the engine-supported data lifecycle, subject to provider policy and engine behavior. Private profiles are explicitly ephemeral and are destroyed on the defined privacy lifecycle.

Do not promise portable account login or import browser cookies between engines/devices. If a provider requires re-authentication after restore, tell the user and provide the supported sign-in flow. Browser profile deletion must close its WebViews/sessions first, remove cookies/storage/history according to the selected engine API, and verify the profile is gone where the engine permits.

## 7. Deletion and retention

Deleting a record follows explicit reference policy; it must not silently cascade into unrelated evidence. User-initiated deletion identifies related blobs, indexes, thumbnails and backup copies.

Flash storage does not generally guarantee physical overwrite. The product must not promise forensic erasure by overwriting file bytes. Where the key scope permits, cryptographic erasure can make remaining ciphertext inaccessible; backups and external exports have separate retention and deletion behavior that must be disclosed.

Deletion tests inspect canonical rows, blob references, caches, indexes, profile storage and backup manifests. Tombstones preserve integrity/audit where required without retaining deleted sensitive payload.

## 8. Technology spike and acceptance

Compare candidate whole-database encryption (including SQLCipher) with authenticated record/blob encryption using the same workload and device:
- Query, insert, full-text search and migration latency.
- WAL/checkpoint/crash behavior and concurrent readers.
- APK/native ABI size, dependency/license obligations and update cadence.
- Key wrapping, rotation, backup/restore and corrupted-key recovery.
- Blob streaming, range access, atomic replacement and deduplication leakage.
- Memory pressure and thermal cost on target Android versions/devices.

Required tests:
- No sensitive write before encryption is ready.
- Key unavailable/invalidation never resets or overwrites the workspace.
- Backup passphrase wrong/corrupt/tampered archive is rejected without replacing current data.
- Export/import round-trip preserves stable IDs, evidence lineage and capabilities.
- Crash at every restore/rotation stage leaves either the old or new verified workspace.
- Restore does not replay attempts or effects.
- Key rotation verifies all rows/blobs and only then retires old wrappers.
- Deletion covers indexes, blobs, browser profile choice and backup disclosure.
- Diagnostics contain no key, passphrase, cookie or private content.

No encryption approach is accepted until this matrix is measured and independently reviewed.
