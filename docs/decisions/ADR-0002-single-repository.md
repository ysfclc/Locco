# ADR-0002: Locco is the permanent product repository

Status: Accepted · 2026-11-19 · Reversal cost: Medium

Context: Mission describes ZIP pipeline creating repos by project name; user designated Locco as foundation for all future build phases.

Decision: Locco is single source of truth. ZIPs create review branches/PRs here, never sibling repos. No REPO_CREATE_TOKEN required.

Alternatives: Control repo + child repos (fragments identity); keep Locco empty and create another (contradicts user); commit ZIP directly to main (unsafe).

Rationale: One architecture, ADR history, security policy and pipeline; review branches preserve provenance.

Failure: ZIP conflicts with protected files or contains unrelated content. Reject protected paths; require review.

Reversal: Split only after ADR, stable contracts and migration plan.