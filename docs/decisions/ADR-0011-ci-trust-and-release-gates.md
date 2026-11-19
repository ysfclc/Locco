# ADR-0011: Untrusted ZIPs are proposals, not executable build input

Status: Accepted · 2026-11-19 · Reversal cost: Low

Context: The initial brief asks one workflow to ingest ZIPs, publish code and build an APK. Archive contents can be hostile. A build that runs scripts from an unreviewed ZIP grants untrusted content code execution in CI.

Decision: The pipeline validates and normalizes the ZIP in an isolated job, then creates a provenance-bearing review branch/PR. It never auto-merges or executes archive scripts. Build jobs compile the trusted default-branch foundation, not unreviewed archive contents. Release signing uses a separate protected environment.

Alternatives: Auto-publish/build (unsafe); manual extraction (not reproducible); new repo per ZIP (fragments Locco's source of truth).

Rationale: One repository and a review boundary preserve identity, history and CI trust.

Failure: extra review step and phase branch conflicts. Idempotency keys include archive hash and base SHA; no-op proposals stop cleanly.

Reversal: A trusted signed-archive mode may be added after threat review, but default remains review-gated.