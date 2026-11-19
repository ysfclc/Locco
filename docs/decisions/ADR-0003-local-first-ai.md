# ADR-0003: Local-first data and on-device AI

Status: Accepted · 2026-11-19 · Reversal cost: Medium

Context: Personal use, zero budget and on-device AI are hard constraints.

Decision: Canonical data, evidence, memory and inference remain on-device. Network sources are optional adapters. Model runtime is selected after target-device benchmarks.

Alternatives: Cloud-first (cost/privacy/connectivity); hard-code runtime now (device incompatibility); no abstraction (SDK coupling).

Rationale: One runtime seam preserves choice without speculative router complexity.

Failure: Small models may be slow/weak; thermal/storage limits. Deterministic/manual paths remain useful.

Reversal: Cloud adapter only explicit opt-in with disclosure and local fallback. Moving canonical data is high-cost.