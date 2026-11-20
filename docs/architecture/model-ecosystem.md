# Local model ecosystem and runtime management

**Status:** Proposed for LOC-0004 review. This defines an extensible model platform, not a hardcoded list of recommendations.

## 1. Separate four concepts

1. Catalogue entry — metadata describing a model/release and source.
2. Artifact manifest — exact immutable revision, file/shard list, sizes and digests.
3. Runtime adapter — engine support for formats, architectures, tasks, backends and limits.
4. Device compatibility result — measured outcome for a model + artifact + runtime + device/OS/backend combination.

A model being listed does not mean it is compatible, downloadable, loadable or useful on this device.

Memo-2's current source has three fixed Qwen LiteRT-LM entries and basic download/import/discovery. Its GGUF/LiteRT runtime interface and router are useful references. Its broader multi-family Hugging Face catalogue is described in a build plan, not proven implemented. Locco should improve and extend the pattern.

## 2. Catalogue task taxonomy

Extendable categories:
- Language: chat, instruction following, planning, reasoning, coding, structured extraction.
- Embedding: text/document/image vectors.
- Reranking: retrieval candidate ordering.
- Vision: image/screenshot understanding and classification.
- Speech recognition: streaming/offline ASR and language identification.
- Speech generation: natural TTS and supported voice tasks.
- Audio: audio understanding and sound classification.
- Image generation: text-to-image and transformations.
- Multimodal: combinations of text, image and audio.
- Specialist: OCR, layout, segmentation and other typed tasks.

These categories are possible capabilities, not a promise that every model runs on Android.

## 3. Metadata contract

A catalogue release describes stable model/family ID, publisher, repository/provider, immutable revision, name/description/tags, modalities/tasks, architecture, parameter count, quantization/precision/format, context/tokenizer/chat template, structured output/tool calling, license/restrictions, gated status, exact file/shard manifest with size/SHA-256, required auxiliary files, runtime compatibility claims, source provenance and metadata freshness.

Remote metadata is untrusted until validated. Floating revisions or missing digests cannot become VERIFIED. Gated sources require a user-controlled upstream authentication flow; tokens never appear in metadata or logs.

## 4. Discovery sources

Provider-neutral catalogue-source adapters may include:
- Curated bundled seed.
- Hugging Face.
- Official runtime/vendor catalogues.
- User-added catalogue sources with trust labels.
- Local filesystem discovery.
- User-selected imports.
- Offline cache.

Adapters normalize, paginate and preserve provenance; they do not decide device fitness. User catalogues cannot inject executable adapters. Search/filter by task, modality, format, architecture, quantization, license, publisher, size, runtime and device-fit status. Unknown metadata stays visibly unknown.

## 5. Runtime ecosystem

Initial candidates to evaluate:
- LiteRT-LM for explicitly supported artifacts.
- llama.cpp for supported GGUF models and architectures.
- ONNX Runtime or another specialist runtime where mobile support and artifact justify it.
- Android-native speech/OCR/image libraries where verified.

Each adapter advertises formats, architectures, tasks/modalities, quantization, CPU/GPU/NPU backend, context/batch/concurrency/memory limits, streaming, cancellation, unload, tokenizer/processor requirements, unsupported features, runtime version and ABI compatibility.

An extension is not proof of compatibility. No runtime is promised to load every model in a format.

## 6. Compatibility levels

1. Metadata-compatible.
2. Artifact-valid.
3. Runtime-loadable.
4. Feature-compatible.
5. Device-fit.
6. Quality-usable.

Only later stages justify activation for a role. Device fit and quality require tests, not parameter-count guesses.

The resource manager observes storage, memory headroom, thermal state, battery and app foreground/background status. It bounds concurrent residency and switches intentionally. Initial policy should be conservative until target-device benchmarks prove multiple large models can coexist. OOM is a typed failure.

## 7. Acquisition lifecycle

Discover → Inspect → Compare → Assess compatibility → Confirm license/access → Storage preflight → Download/import → Verify → Stage → Load-test → Benchmark → Activate.

Downloads require resumable bounded transfer, correct Range/ETag handling, immutable revision, complete file manifest, exact size/digest checks, atomic staging, rollback, cancellation/restart recovery, storage preflight and real progress. Never delete a previous working model until replacement is fully verified and load-tested.

Unknown imports may be preserved as untrusted artifacts but cannot become active without integrity/runtime assessment. Do not install executable plugins from model repositories.

## 8. Runtime lifecycle and request contract

A model runtime exposes discovery/capabilities, load, health, info, streaming inference, cancellation, unload and close.

Each request binds model/artifact/runtime versions, task role, schema, bounded context references, supported sampling parameters, deadline/resource budget, privacy class, cancellation and correlation ID. Unsupported settings are rejected or clearly unavailable, never silently ignored. Partial output remains partial until the runtime completes.

## 9. Routing

Routing considers task/modality, runtime compatibility, quality evidence, privacy/consent, user pin/preferences, current resources, health/failures, latency, cost and network limits. The result includes rationale and rejected alternatives.

Local-first is default when a local model is eligible and meets readiness/quality requirements. Otherwise report the capability gap or ask the user. Never route private material to remote services without permission.

## 10. Model Hub experience

Native Model Hub capabilities:
- Featured/recommended models with reasons.
- Full searchable/filterable normalized catalogue.
- Detail pages for task, size, license, revision, files and compatibility.
- Side-by-side comparison.
- Durable download queue and per-file progress.
- Import/local discovery.
- Installed library with integrity/health.
- Runtime/backend diagnostics.
- Per-model parameters and role/task assignments.
- Benchmark history and device-fit explanation.
- Safe storage removal with dependency warnings.
- Offline cached catalogue/manual import.

Do not show a download action unless the artifact and acquisition path exist. Do not show a compatibility badge without a test/result or explicit unknown state.

## 11. Security and verification

Weights, tokenizer/config files and metadata are untrusted. Enforce path/size/shard/parser limits and checksum verification. Keep runtime libraries separate from model artifacts. Display license and gated status. Catalogue browsing must not transmit workspace content.

Tests cover source pagination/cache, malformed metadata, floating revisions, missing/tampered shards, Range changes, cancellation/process death, suspicious imports, compatibility matrices, OOM, streaming, cancellation, tokenizer/template mismatch, CPU fallback, routing privacy and golden-set quality.

## 12. Open decisions

Resolve by spike/ADR: minimum runtime set, catalogue providers/trust model, runtime implementation language/boundary, cache/storage limits, benchmark thresholds and whether specialist modalities fit the actual device's memory/thermal envelope.
