# Locco brainstorming recovery record

**Prepared:** 2026-11-27  
**Purpose:** distinguish original user requirements, reconstructed summaries, donor research and later architecture proposals. This record prevents a normalized summary from being misrepresented as a verbatim brainstorm archive.

## 1. Sources actually inspected

- docs/brainstorm/raw/KEYSTONE-MISSION-BRIEF.md — a reconstructed/normalized transcription of the 2026-11-19 founding instruction. Its own README says it is not byte-for-byte. It primarily specifies repository foundation, agent/skill structure, security and the original ZIP pipeline. It is not the full product brainstorm.
- docs/brainstorm/structured.json — a 1.0 structured interpretation of the product, constraints, loop, capabilities, donors and exclusions. It is distilled data, not a raw transcript.
- docs/brainstorm/raw/BUSYBEE-APP-OBSERVER-DIRECTION.md — a 2026-11-22 donor research synopsis, not original Locco brainstorming and not proof that BusyBee code was ported.
- Product vision/scope/roadmap and the Android observation architecture.
- The deep architecture proposal on branch architecture/deep-product-audit, including browser/DOM, model ecosystem, Capability Compiler, UX, storage, execution, security and requirement traceability.
- Recovered prior-conversation user constraints: Locco is the permanent mobile-first Rust-cored product; Android APK is primary; iOS is anticipated; no HTML/web UI; the repository is the source of truth; archive raw brainstorm verbatim plus structured data; and keep one evolving application rather than resetting or replacing it.

## 2. Recovered product direction

Locco is a personal local-first intelligence and action environment. It unifies acquisition, evidence preservation, understanding, entity relationships, reasoning, capabilities, controlled execution and a native workspace.

The recovered user direction identifies these as selective donor inspirations:
- Outbound Sourcer — source adapters and discovery.
- EBO — event/signal ledger, qualification and approvals.
- BOS — entities, capabilities, workflows and policy.
- Troy — source-aware extraction, interpretation and identity resolution.
- ME — immutable evidence and derived observations.
- Memo — selected Android automation, approvals and visible-browser patterns; Locco is not a Memo clone.
- BusyBee — App Observer Lab, provider bridge and owned-app plaintext research.
- VaultOS — local import, indexing, backup and integrity.

The product is broader than any donor. It must not become a sales/trade-only product, a business OS, a chatbot shell or a hardcoded set of use-case workflows.

Recovered capability families:
1. **Acquisition:** public/user-authorized sources, browser, apps, files, Android share/import, notifications, supported APIs and manual capture.
2. **Evidence and knowledge:** immutable source lineage, facts, claims, entities, relationships, deduplication, searchable records and event reconstruction.
3. **Intelligence:** deterministic extraction, local semantic inference, compact context, retrieval, evaluation and learning from outcomes.
4. **Capabilities and execution:** typed tools, generic plans, policy, approvals, native adapters, action verification and recovery.
5. **Workspace:** capture, search, evidence inspection, entity views, review, analysis and action.
6. **Android observation:** understand useful activity across apps the user permits; inspect actual captured data and distinguish observation from inference and action.

The user's later explicit clarification makes the Observer requirement especially concrete: events should be clickable and answer what actually happened, what was opened, received, closed, what action was taken and what actual data was involved. App-open/app-left counters alone are not useful product events.

## 3. What the current archive does not contain

The founding brief file is not the original complete brainstorming transcript. It contains no complete verbatim record of all earlier product examples, workflows or corrections. The structured JSON compresses the product into a small set of categories and donor summaries. The BusyBee file records donor research, not the original Locco vision.

The full original user-authored brainstorm from the originating conversation was not recoverable in the accessible conversation archive during this audit. Therefore:
- Do not claim that every original brainstorm sentence has been preserved.
- Do not silently fabricate or backfill missing wording as if it were verbatim.
- Treat the recovered constraints and the product contract as an explicit reconstruction with provenance.
- If the original export is later supplied, add it as a new immutable raw source and reconcile differences without overwriting this record.

## 4. Architecture proposal versus implementation

The detailed architecture branch is a proposal and requirements source, not evidence that the features exist in the APK. In particular, the native browser, profile isolation, DOM Stitching, rich Model Hub, local runtime, generic Capability Compiler, durable execution/outbox, Accessibility observation and cross-app semantic graph are not implemented merely because their designs exist.

The current implementation state and delivery gates belong in docs/product/roadmap.md, release evidence and the product contract. A proposed architecture requirement must remain OPEN until code, tests, release and device evidence prove it.

## 5. Preservation rule

New user brainstorming must be archived verbatim before normalization, with date, conversation/source provenance and an immutable filename. Structured JSON and architecture documents are derived interpretations and must link back to the raw source. Corrections preserve the earlier wording and add a dated decision; they do not erase history.
