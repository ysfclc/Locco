# Local AI and context

**Status:** Proposed system contract. Runtime/device selections remain deferred until model ecosystem benchmarks.

Models are bounded semantic workers, never truth or authority. Deterministic code handles identity, parsing, normalization, validation, deduplication, permissions, policy and state transitions.

## Model ecosystem

The model catalogue is not a hardcoded list of chat models. It describes versioned model releases, artifact manifests, task/modality capabilities, licenses, source provenance and runtime compatibility claims. Separate registries describe runtime adapters and target-device compatibility/quality results. A listed model is not automatically loadable or fit for this device.

Tasks may need language, embedding, reranking, vision, speech, audio, image-generation or multimodal models. Only measured and supported combinations are exposed as available capabilities. LiteRT-LM, llama.cpp/GGUF and specialist runtimes are candidates, not assumed universal support.

## Context compiler

The context supplied to inference is not a mirror of the database. It includes:
- Task envelope and output schema.
- Minimal static instructions.
- Scoped, confirmed user memory.
- Explicitly referenced current state.
- Retrieved evidence excerpts with IDs/source locators.
- Reserved output/safety budget.

Admission requires task relevance, provenance, non-duplication, freshness/confidence policy, sensitivity clearance and expected contribution. Record admission/rejection reasons without copying private content into telemetry.

The existing context-feeding contract's initial 10/15/55/10/10 percent budget is a starting heuristic, not a permanent constant. Actual tokenizer, context window, multimodal token cost and task budget determine admission. Hard input ceiling and output reserve remain enforced.

## Retrieval and memory

Begin with lexical/full-text retrieval and metadata filters. Add embeddings/rerankers only when golden-set tests demonstrate value under storage, RAM, latency and thermal budgets. Chunk semantic records and retain offsets/lineage. Retrieve excerpts, not whole documents.

Memory is separate, curated, scope/source/confidence/last-confirmed/supersession-aware. Chat is not auto-indexed. No speculative or inferred sensitive memory. Conflicting facts remain separate claims with evidence.

## Runtime orchestration

A runtime descriptor advertises supported formats, architectures, modalities, operations, tokenization, context, streaming, cancellation, load/unload, concurrency, backend and resource limits. A task router chooses among only verified capabilities and returns a rationale and rejected alternatives.

No model at startup. Conservative single large model residency until actual device benchmarks justify more. Unload or downgrade on memory/thermal pressure. Heavy inference, model load, indexing and file parsing never block the UI.

Inference pipeline:
sanitize → retrieve → budget → infer with deadline/cancel → parse → schema/evidence validate → deterministic fallback/uncertain → expose evidence to user.

Model output is untrusted. It cannot mutate canonical records directly; mutations require typed domain commands and policy validation.

## Evaluation

Compare candidate models/runtimes against task-specific golden sets. Track retrieval precision/recall, evidence support/unsupported-claim rate, schema validity, fallback rate, latency percentiles, peak memory and thermal changes. Device thresholds are set from physical measurement, not invented before benchmarking.

Diagnostics are on-device by default. Never log raw prompts, source payloads, private text, secrets or embeddings. User corrections become evaluation fixtures only after explicit curation.
