# Canonical data model contract

**Status:** Domain contract proposal; physical schema and encryption driver remain open under ADR-0008 and LOC-0004.

## 1. Canonical ownership

One local relational store is the canonical source of truth. Rust owns schema, identifiers, validation and migrations. Android provides the platform storage/key services through explicit ports. Original binary/content payloads may live in an encrypted blob store referenced by canonical rows; they must not become a second competing database.

The append-only event/execution journals preserve state-transition evidence and side-effect history. They are not a claim that the entire application uses event sourcing. Canonical record state is stored in the relational store; journals make meaningful transitions, attempts and audit outcomes traceable.

Search indexes, embeddings, DOM-derived acceleration structures and caches are rebuildable projections. They never become canonical truth.

## 2. Shared invariants

Every durable domain record has:
- Opaque stable ID, never derived from display text or array position.
- Schema version and explicit migration path.
- Creation time and last meaningful update time.
- Monotonic record version for optimistic concurrency where mutation is allowed.
- Explicit lifecycle/status; deletion/archive is not represented as silent absence.
- Provenance references when the record asserts facts or derives from external material.
- Stable operation/event/correlation IDs where state changes are asynchronous.

Canonical timestamps are stored in a platform-neutral UTC representation; source-local timezone and original date string are preserved when they affect meaning. The precise ID algorithm and serialization/canonical hashing format remain ADR decisions.

Content hashes identify immutable payload bytes or canonicalized structured artifacts. A hash is not a substitute for a stable record ID, source origin or semantic identity.

## 3. Source and evidence

| Record | Meaning | Mutability |
|---|---|---|
| Source | Origin and access contract (site, file collection, user input, API, app/notification scope) | Versioned metadata; credentials are outside the record |
| Capture | One acquisition attempt and its lifecycle, authorization, timestamp, result and errors | Append updates/state history |
| Evidence | Immutable original or explicitly scoped capture; exact content/blob reference, hash and provenance | Immutable; correction is a new evidence item |
| Observation | Parser/browser/model/rule-produced candidate facts with offsets and source evidence | Derived and versioned |
| ExtractionSchema | Typed reusable mapping used to turn observations into records | Versioned; trust and last verification recorded |

A normal web visit does not automatically archive all page content. A capture action stores the bounded, consented material selected by the user or required by a specific authorized task: URL/origin, timestamp, selected text or DOM observation, source locators and hashes. Full-page/raw markup retention is a separate explicit operation with sensitivity, size and retention checks. Passwords, OTPs, payment secrets, hidden tokens, cookies, authorization headers and unrelated page state are never evidence.

## 4. Knowledge graph and identity

| Record | Meaning | Mutability |
|---|---|---|
| Entity | Stable person, organization, place, object or user-defined type | Versioned projection |
| EntityAlias | Alternate name/identifier/source-specific key for an entity | Versioned |
| Relationship | Typed, directed or undirected link between entities/records | Versioned with provenance |
| Claim | A proposition that may be supported, contradicted or unresolved | Append/retire; never silently overwrite |
| Interpretation | Derived conclusion from rules or model version | Replaceable/versioned |

Identity resolution is a proposal, not an irreversible deduplication side effect. Matching signals, candidate entities and evidence are recorded. A merge requires deterministic validation and an explicit policy/user decision where ambiguity or destructive consolidation exists. Original IDs remain resolvable through aliases/redirects and history. No data is dropped merely because two names look similar.

Claims preserve conflicting evidence. Each claim links to supporting/contradicting evidence IDs, source spans, asserted/observed times, confidence/provenance and current status. Model-generated claims remain interpretations until validated and promoted under an explicit rule.

## 5. Memory and conversation

Memory is not the chat transcript and not a copy of the evidence store.

A MemoryItem records:
- User-confirmed content or explicit preference.
- Scope (global, project, task or other user-defined boundary).
- Source/evidence and confirmation method.
- Confidence/validity and last-confirmed time where applicable.
- Supersession/correction history.
- Sensitivity classification and retrieval policy.

Conversation messages are session data. They may reference durable records/evidence, but are not automatically promoted into long-term memory. Distillation requires explicit user curation or an approved confirmation flow. Sensitive/inferred attributes are not silently stored.

## 6. Goals, tasks and compilation

| Record | Meaning |
|---|---|
| Goal | User outcome with scope and success definition |
| Task | Durable user-facing unit with status, due/priority and completion evidence |
| TaskSpec | Compiled request contract: objective, deliverables, constraints, risk, budget, language and evaluator |
| RequirementGraph | Bounded typed graph of data, knowledge, capabilities, procedures, constraints and evaluators |
| CapabilityComponent | Versioned tool, capability, skill, procedure, data binding, knowledge pack, constraint, persona or evaluator |
| ExecutionPackage | Immutable manifest pinning task, components, evidence, budgets, authority and evaluators |
| Execution | Durable run of an execution graph |
| ExecutionNode | Typed operation, dependencies, inputs/outputs and postconditions |
| ExecutionAttempt | One attempt with idempotency key, checkpoint, timing and result |
| Approval | Human/policy decision bound to exact target, operation, payload hash, scope, expiry and postcondition |
| Outcome | Verified, failed, partial or uncertain result with evidence/receipt |
| Experience | Evaluated result referencing component versions and conditions; no automatic authority promotion |

The engine does not require domain-specific records such as CRM or job application to understand task execution. Such types can be user-defined schemas or versioned domain packs that compose the same generic contracts.

## 7. Artifacts and files

Artifact represents a document, structured export, generated image/media, report or other deliverable. It includes stable ID, type/schema, revision, content hash/blob reference, producing execution, source evidence, generator/model/runtime versions, license/provenance and lifecycle state.

Large files are blob-managed and content-addressed where safe. Blob encryption/key lifecycle, deduplication and deletion semantics must not leak content through filenames, hash side channels or diagnostics. No model weights are persisted as user evidence merely because they are installed.

## 8. Events and audit

Events are append-only records of meaningful facts and state transitions, with event ID, aggregate/record ID, schema version, sequence/correlation/causation IDs, UTC timestamp, actor/authority and bounded metadata. Sensitive payloads are referenced, not duplicated into logs.

External action audit records include target, operation summary, approval reference, payload hash, before/after observation/evidence references, idempotency key, executor/runtime version and postcondition result. The audit must not contain secrets or raw credential fields.

The event and execution journals support reconstruction of activity and safe recovery. They do not replace canonical relational state unless a separate ADR explicitly adopts event sourcing.

## 9. Migrations and deletion

- Schema changes are additive/versioned where possible; migration tests preserve IDs, evidence lineage and user data.
- Failed migrations leave recoverable source state and enter a safe read-only/recovery state rather than reset data.
- Export/restore covers canonical rows, referenced encrypted blobs, capability/version metadata and recovery checks.
- Deleting a record creates an explicit tombstone or deletion event and applies reference policy. Evidence retention/deletion must be visible; no orphan or cascade deletion is silently performed.
- User-requested secure deletion must include referenced blobs, derived indexes, profile/session stores and backup semantics as applicable.
- Restore never automatically re-executes side effects.

## 10. Open decisions

- SQLite driver and encryption strategy (SQLCipher vs encrypted fields/containers), WAL and crash semantics.
- Binary blob encryption and key wrapping/rotation.
- Canonical serializer and content-hash rules for structured records.
- ID format and merge/alias policy.
- Event journal compaction and retention.
- Per-record conflict, archive, tombstone and secure-delete behavior.

Resolve these through ADRs and test spikes before schema is implemented with real user data. See [storage-security-and-recovery.md](storage-security-and-recovery.md) for the required key, backup, restore, browser-profile and deletion contract.
