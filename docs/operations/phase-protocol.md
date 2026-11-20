# Phase delivery protocol

A phase is a dependency-aware collection of permanent LOC issues, not an archive upload or a generated branch. Direct GitHub delivery is canonical under ADR-0016.

## Before implementation

1. Confirm Phase 0 and prior-phase gates are accepted.
2. Read the product vision, scope, roadmap, accepted ADRs, assurance register and all related open LOC issues.
3. Decompose the phase into reviewable outcomes with explicit dependencies and acceptance criteria.
4. Record the exact base commit and current application/data baseline.

## Delivery

1. Implement one coherent outcome per focused branch/PR in the existing Locco repository.
2. Link every PR to its permanent LOC issue(s) and requirement IDs.
3. Preserve all unresolved feedback and accepted behavior.
4. Run governance, Rust, Android and security/policy workflows applicable to the changed files.
5. Verify artifacts against the exact source commit; do not accept stale APKs or native libraries.
6. Obtain specialist and independent review. Do not simulate unavailable reviewers.
7. Merge only when required checks and repository-level protection are active.

## Completion

Update the LOC issue, requirement traceability, roadmap and foundation/assurance ledger with exact commit, run, test, device and artifact evidence. Mark unavailable checks NOT RUN or BLOCKED. A phase never resets the app, repository, package identity or user data.

## External archive exception

No ZIP is required for internal delivery. If an external archive is explicitly authorized, treat it as hostile under ADR-0006, validate/extract it in isolation, and convert the resulting changes into an ordinary reviewed PR. Archive intake never bypasses normal controls.
