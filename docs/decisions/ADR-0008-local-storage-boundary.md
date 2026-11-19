# ADR-0008: Local relational store with separate rebuildable indexes

Status: Accepted for foundation direction; implementation deferred to Phase 1 · 2026-11-19 · Reversal cost: Medium

Context: Locco needs durable evidence, entities, events, relationships and memory without mandatory cloud. Vector search and encryption have nontrivial device/build costs.

Decision: Rust owns schema and migrations. Use one local relational database as canonical source of truth (SQLite is the Phase 1 candidate). Key-value preferences remain platform-owned and non-authoritative. Search indexes and embeddings are derived/rebuildable, not a second canonical store. Start lexical retrieval; add vector index only after measured value. Sensitive user data is not persisted until encryption-at-rest design is implemented and tested; Android key material is protected by Keystore, with iOS Keychain equivalent later.

Alternatives: Room as canonical DB (splits ownership across platforms); separate vector DB (storage/maintenance overhead); cloud DB (violates local-first); plaintext DB (unacceptable for private data).

Rationale: One canonical store reduces sync/conflict complexity; deferring vectors protects resource budget. Encryption implementation needs a Phase 1 spike comparing SQLCipher vs encrypted-field design and backup semantics.

Failure: SQLite locking/migration/corruption, encryption integration cost, index rebuild time. Reversal cost is medium while schema is early; high after user data exists.

Reversal: storage driver can change behind Rust ports. Any migration must preserve stable IDs, evidence lineage and verified export/restore.