# Constraint inventory — architectural skeleton

## 1. On-device resource scarcity
**Fact:** Locco targets a mid-range Android device class with 8 GB RAM. Local inference competes with the OS, UI, storage and other apps for memory, CPU/GPU/NPU and thermal headroom.

**Implication:** No model at startup; one model resident at a time; bounded generation; cancellation and unload; compact context; lexical retrieval first; model/runtime and embedding choices measured on the physical device. Core workflows must work with the model unloaded.

**Failure mode:** model load OOM, thermal throttling, long-tail latency, battery drain or poor semantic quality. **Reversal cost:** Medium for runtime adapter; High if model assumptions leak into domain contracts.

## 2. Heterogeneous sources under zero-budget and permission constraints
**Fact:** Sources differ in access method, structure, rate limits, terms, authentication and stability. Paid APIs are excluded from the baseline. Android sandboxing prevents universal app introspection.

**Implication:** Source adapter contract declares method, permission, terms, region, rate and capabilities. Prefer user-visible capture, share/import, public HTML and documented APIs. Preserve provenance and support manual fallback. Never promise universal scraping or app access.

**Failure mode:** source markup drift, access restrictions, throttling, policy changes and incomplete data. **Reversal cost:** Low for adapter swaps; Medium for changing source/evidence schema.

## 3. Trustworthy local data and controlled actions
**Fact:** Locco may collect private personal information and may eventually execute external actions. Local-first means device loss and compromised imports are meaningful threats.

**Implication:** immutable evidence, explicit derived interpretations, encrypted-at-rest storage before sensitive Phase 1 data, typed capabilities, approvals bound to exact payloads, idempotency and outcome evidence. Imported code is never executed automatically.

**Failure mode:** data loss, poisoned evidence, prompt injection, unauthorized action or signing compromise. **Reversal cost:** High for evidence identity/storage; Medium for capability adapters.

These three constraints outrank framework fashion and feature pressure.

## 4. Reference-device baseline

The first physical-device baseline is the user's Redmi Note 12 Pro 5G (MediaTek Dimensity 1080, 8 GB RAM, 256 GB storage). Record OS build, available storage, Android System WebView/Chrome versions, thermal state and RAM headroom for every benchmark. A nominal 8 GB RAM label is not an app memory budget: Android, GPU, renderer processes and other apps consume the same device resources.

Use measured headroom and Android memory/thermal signals for model admission and browser tab policy. Do not reserve a fixed percentage or promise simultaneous model residency without benchmark evidence. The device is the first acceptance target, not the only supported Android profile.
