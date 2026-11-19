# Local AI and context

Model is a bounded local semantic worker, never truth or authority. Code handles parsing, normalization, validation, filtering, ranking, dedupe, policy and state transitions. `ModelRuntime` abstracts metadata, load/unload, capabilities, token estimate, bounded generation, cancellation and telemetry. Runtime selection waits for device benchmarks.

Static context: task instruction/schema. Dynamic: only relevant user state/evidence. Ephemeral: current request + minimum relevant turns. Initial budget: 10% instruction/schema, 15% task/current turn, 55% evidence, 10% curated memory pointers, 10% output/safety. Input hard cap 70% of model context. Never fill capacity by default.

Start lexical retrieval + metadata filters. Add embeddings only if golden-set tests justify RAM/latency/storage. Chunk by semantic records, preserve offsets/lineage, retrieve excerpts not whole docs. Admit only relevant, provenance-valid, nonduplicate candidates likely to change output. No speculative memory.

Memory is a separate curated store with scope/source/confidence/last-confirmed/supersession. Chat is not auto-indexed. Pipeline: sanitize → retrieve → budget → infer with deadline/cancel → parse schema → validate → deterministic fallback/uncertain → show evidence. Model output is untrusted. Session handoff carries unresolved state and evidence IDs, not transcript narrative. No model at startup; unload on memory pressure. Exact latency/RAM budgets are Phase 3 measurements.