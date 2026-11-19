# Skill: Model context budget

When: inference, retrieval or memory write. Preconditions: task schema, model metadata/token estimate and policy.

Procedure: identify task/output; task-specific instruction; retrieve metadata + lexical; dedupe evidence; rank relevance/freshness/confidence; admit only output-changing context; enforce hard budget; scoped curated memory only; reserve output/safety; parse/validate; deterministic fallback; log estimates/evidence IDs, never private text.

Verify golden set, strict cap and model-offline path.

Failure: bloat, stale/conflicting memory, tokenizer mismatch or malformed output. Drop low-value context; never retry unbounded.