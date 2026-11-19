# ADR-0005: Selective context compiler, not transcript memory

Status: Accepted · 2026-11-19 · Reversal cost: Medium

Context: On-device context, RAM, latency and thermal headroom are scarce.

Decision: Task-specific context, hard token admission, evidence dedupe, lexical baseline and curated memory. Transcript remains ephemeral unless explicitly distilled.

Alternatives: full transcript (unbounded/noisy); all related chunks (false relevance); always embeddings (cost without proof).

Rationale: High-signal context protects latency and reduces contradiction.

Failure: Relevant evidence omitted. Expose retrieval diagnostics and user-directed inclusion; measure false negatives.

Reversal: Retrieval changes behind compiler interface; golden-set tests required.