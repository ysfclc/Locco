# Context feeding contract

This is the normative admission contract for every local inference call. The model context is not a mirror of the database.

## Assembly order
1. **Task envelope:** task ID, user request, output schema, deadline, risk class.
2. **Minimal static instruction:** only rules needed for this task and output format.
3. **Scoped user memory:** only confirmed preferences/decisions whose scope matches this task.
4. **Current state:** explicitly referenced entities, selected workspace and unresolved task state.
5. **Retrieved evidence:** lexical/metadata candidates, deduplicated by evidence ID/hash, ranked by relevance, freshness and confidence.
6. **Output reserve:** reserve tokens for response and safety margin before admitting evidence.

## Initial token budget
- Static instruction/schema: 10%.
- Task/current request: 15%.
- Retrieved evidence: 55%.
- Curated memory pointers: 10%.
- Output/safety reserve: 10%.
- Hard input ceiling: 70% of model context. Use actual tokenizer when available; otherwise conservative estimate. Smaller task budgets are preferred.

## Admission test
A chunk enters only if it is relevant to the task, provenance-valid, not a duplicate, within freshness/confidence policy, and likely to change the answer or action. Every chunk carries evidence ID and source locator. Retrieval records admission/rejection reason without copying private content into telemetry.

## Explicit exclusions
- Full chat transcripts.
- Unrelated project memories or other domains.
- Entire source documents when a short excerpt suffices.
- Duplicate evidence or repeated prompt instructions.
- Unverified model-generated claims as if they were source facts.
- Speculative preferences, inferred sensitive attributes or memories without user value.
- Tool descriptions for capabilities unavailable to this task.
- Secrets, cookies, access tokens and raw credentials.

## Conflict handling
Conflicting facts remain separate claims linked to evidence. A newer user correction supersedes the older memory assertion; the old assertion is retired from retrieval and retained only as provenance/history where needed. Never present two contradictory memories as equally current.

## Session rotation
When token budget, topic shift, uncertainty or turn count crosses policy threshold, produce a compact handoff containing task objective, decisions, unresolved questions, relevant evidence IDs and current state. Do not carry narrative transcript. Start a fresh inference context.

## Deterministic fallback
Timeout, model unavailable, malformed output, invalid schema or low confidence returns a typed failure/uncertain result and the deterministic alternative. No inference result mutates canonical data without validation and an explicit domain command.