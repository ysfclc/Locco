# Context feeding contract

This is the normative admission contract for every model inference call. The model context is not a mirror of the database.

## Assembly order
1. **Task envelope:** task ID, user request, output schema, deadline, risk class.
2. **Minimal static instruction:** only rules needed for this task and output format.
3. **Scoped user memory:** only confirmed preferences/decisions whose scope matches this task.
4. **Current state:** explicitly referenced entities, selected workspace and unresolved task state.
5. **Retrieved evidence:** lexical/metadata candidates, deduplicated by evidence ID/hash, ranked by relevance, freshness and confidence.
6. **Output reserve:** reserve tokens for the required response, formatting/schema closure and safety margin before admitting evidence.

## Token-budget calculation

Let `C` be the model's actual context-token limit and `O` the output reserve required by the task, including response/schema completion and a safety allowance.

```text
input_limit = min(floor(0.70 * C), C - O - floor(0.10 * C))
available_input = max(0, input_limit)
```

- Input may never exceed 70% of the model context.
- At least 10% of the full context remains unallocated safety margin, in addition to the output reserve.
- `O` is task-specific and must satisfy the output contract; it is not assumed to be a fixed 10% for every task.
- If the model has a smaller provider/runtime usable context than the advertised context, use the smaller measured limit.
- If `available_input` is insufficient, reduce evidence, ask for clarification, use a smaller task or select a verified model with a suitable context. Never silently violate the ceiling.

### Initial input allocation

The following weights are shares of `available_input`, not percentages of the full model context:
- Static instruction/schema: 10%.
- User request/task envelope: 15%.
- Explicit current state: 10%.
- Retrieved evidence: 55%.
- Curated memory pointers: 10%.

These are starting weights, not rigid quotas. Unused capacity may be reassigned to relevant evidence or task state under the same hard input limit. Evidence does not get admitted merely because its allocation is unused.

Use the actual tokenizer for the selected model/runtime. When exact tokenization is unavailable, estimate conservatively, account for multimodal payload cost and reject/truncate before the hard limit. Record exact budget inputs, output reserve and admission decisions without copying private content into telemetry.

## Admission test
A chunk enters only if it is relevant to the task, provenance-valid, not a duplicate, within freshness/confidence policy, sensitivity-cleared and likely to change the answer or action. Every chunk carries evidence ID and source locator. Retrieval records admission/rejection reason without copying private content into telemetry.

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
Timeout, model unavailable, malformed output, invalid schema, insufficient context budget or low confidence returns a typed failure/uncertain result and the deterministic alternative. No inference result mutates canonical data without validation and an explicit domain command.
