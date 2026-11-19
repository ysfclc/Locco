# ADR-0006: ZIP ingestion creates reviewed branches

Status: Accepted · 2026-11-19 · Reversal cost: Low

Context: ZIP may contain hostile paths, secrets or executable code. Running it in CI before review is unsafe.

Decision: validate/extract in isolation, strip VCS metadata, reject links/special files, enforce limits and create review branch/PR. Never auto-merge or execute archive scripts.

Alternatives: auto-publish/build (unsafe); manual extraction (unrepeatable); new repo per ZIP (fragments product).

Rationale: archive is a proposed patch, not authority.

Failure: review overhead. Provide useful dry-run and clear rejection reasons.

Reversal: relax only for signed trusted sources after threat review.