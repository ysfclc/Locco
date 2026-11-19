# ADR-0007: Native workspace, not chatbot-first UI

Status: Accepted · 2026-11-19 · Reversal cost: Medium

Context: Locco spans acquisition, knowledge, reasoning and execution. Chat alone hides evidence and operational state.

Decision: native workspace exposes capture, search, entity/evidence inspection, analysis, review and action. Conversation is one mode.

Alternatives: chatbot-first (poor inspectability); dashboard-first (static); one screen per donor project (fragmented).

Rationale: one coherent workspace over a stable ontology.

Failure: overdesign. Phase 1 ships only the smallest useful kernel and thin workspace slice.

Reversal: navigation/visual composition is reversible while domain contracts remain independent.