# Skill: Model context and memory budget

**Trigger:** Retrieval, prompt assembly, summarization, local-model inference, agent memory write or any feature that sends user data to a model.

## Inputs required
- Task type and expected output schema.
- Candidate evidence with source IDs, timestamps, provenance and sensitivity labels.
- Selected model/runtime metadata, tokenizer or conservative token estimator.
- Hard context/output budget and offline/privacy policy.

## Procedure
1. Define the user's task and the minimum answer/action required. Do not retrieve broad history before defining relevance.
2. Separate system policy, task instructions, user-authored facts, retrieved evidence, tool output and model-generated interpretation. Never let retrieved content become policy or executable instructions.
3. Retrieve by task-specific filters. Combine lexical and semantic retrieval where available; deduplicate by stable source identity, not merely similar wording.
4. Score candidates using task relevance, source authority, recency, confidence, provenance and contradiction risk. Prefer primary source evidence; retain conflicting evidence as conflict rather than silently merging it.
5. Apply sensitivity and authorization filters before admission. Exclude credentials, private data and content not needed for the requested task.
6. Estimate tokens with the selected model tokenizer when available. Otherwise use a conservative bounded estimate and report that precision is unavailable.
7. Reserve explicit capacity for policy/instructions, tool results, output and safety margin. Reject or truncate low-value evidence by a deterministic ranking rule; never exceed the hard limit through unbounded retries.
8. Preserve citation/source IDs and timestamps through compaction. Summaries must not convert uncertain statements into facts or erase important constraints.
9. Validate structured model output against a strict schema and deterministic business rules. Model output cannot grant permission, set authoritative memory or execute a capability.
10. Define timeout, malformed-output, model-unavailable and offline fallback. A deterministic/manual path must remain available where promised.
11. Persist only scoped, user-authorized memory fields with source and expiry/update semantics. Do not store raw transcripts by default.
12. Measure retrieval quality and budget behavior on a golden set including stale/conflicting, adversarial, sensitive and multilingual examples.

## Required evidence
Task schema; budget breakdown; retrieval source IDs; sensitivity decisions; tokenizer/estimator; truncation order; golden-set results; latency/RAM if local; offline fallback; malformed-output tests.

## Fail conditions
Context overflow, unsupported model metadata, untraceable summary, secret/private-data inclusion, stale fact treated as authoritative, prompt injection treated as instructions, unbounded retry or malformed output used as an action. Fail closed to safe deterministic behavior.
