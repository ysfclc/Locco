# Observability and offline evaluation

## Privacy-preserving telemetry
Record local diagnostics: stage durations, model load/inference latency, estimated input/output tokens, selected evidence IDs/count, retrieval rank/hit metrics, parse/schema failures, fallback reason, cancellation, memory pressure and runtime version. Do not record raw prompts, source payloads, private text, secrets or embeddings in logs.

Diagnostics remain on-device by default. Export is user-initiated and redacted.

## Evaluation
- Golden sets cover representative tasks and adversarial source text.
- Deterministic assertions test parsing, ranking, dedupe, evidence lineage, context budget and policy decisions.
- Model outputs are scored against task-specific rubrics and evidence support; no cloud calls required.
- Compare model/prompt/runtime versions against the same fixtures.
- Track retrieval precision/recall, unsupported-claim rate, schema-valid rate, fallback rate, latency percentiles and peak memory.
- User corrections become candidate evaluation examples only after explicit curation; no automatic transcript mining.

## Regression gates
A model/prompt change cannot ship if context budget overflows, evidence citations disappear, unsupported claims rise materially, deterministic fallback breaks or latency exceeds the device budget. Thresholds are set from Phase 3 baseline measurements, not invented before benchmarking.