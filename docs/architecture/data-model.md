# Data model contract

Canonical concepts and Rust-owned record types:

- **Source**: chosen origin and acquisition metadata. A source is not itself evidence.
- **Evidence**: immutable excerpt or content-hash reference tied to a source.
- **Entity**: stable identity with a type and canonical name.
- **Relationship**: typed, directed link between entity IDs.
- **Event**: append-only record of a local kernel occurrence.
- **Observation / Claim / Interpretation**: derived facts and propositions must retain links to supporting or contradicting evidence; interpretations remain replaceable.
- **Capability / Proposal / Approval / Execution / Outcome**: future action records, with approval bound to an exact payload hash and expiry.
- **Memory item**: curated stable context, superseded rather than silently overwritten.

core/kernel/src/records.rs is the current platform-neutral source of truth for record identity and constructor invariants. IDs are opaque, bounded strings; timestamps are non-negative; source/entity/relation/event types are validated; evidence must retain a source ID and an excerpt or content-hash reference.

Every record has an opaque stable ID. Derived records must link evidence IDs, source offsets, hashes, parser/model version and timestamp. Conflicts coexist with provenance. Rust owns domain invariants and migrations. Search indexes and embeddings are rebuildable derived data.

**Implementation boundary:** the current Android SQLite store is still a first-slice persistence adapter. Its schema/migration ownership has not yet been moved behind the Rust kernel/FFI contract. Do not treat the existing Kotlin SQLiteOpenHelper as the final cross-platform storage architecture or add feature migrations there.
