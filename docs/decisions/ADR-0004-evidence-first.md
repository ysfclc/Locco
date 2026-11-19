# ADR-0004: Immutable evidence before interpretation

Status: Accepted · 2026-11-19 · Reversal cost: High

Context: Source facts and model interpretations must remain distinguishable and correctable.

Decision: Immutable evidence is separate from observations, claims, interpretations and memory. Derived records link evidence IDs, offsets, hashes and parser/model version.

Alternatives: normalized-only (lossy); mutable entity record (history destroyed); transcript memory (noise/context bloat).

Rationale: Lineage supports correction, audit and model replacement.

Failure: Storage growth/conflict complexity. Compact derived indexes only; never silently mutate evidence.

Reversal: Evidence identity is high-cost; preserve stable IDs and migration/export tooling.