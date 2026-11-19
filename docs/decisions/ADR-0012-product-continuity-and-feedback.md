# ADR-0012: Preserve product continuity and user feedback

- **Status:** Proposed in PR #1; becomes accepted only when merged to `main`.
- **Date:** 2026-11-20
- **Owners:** Product, Architect, QA
- **Related requirement:** [LOC-0002](https://github.com/ysfclc/Locco/issues/2)
- **Supersedes:** No prior ADR. This decision adds an operational constraint to all roadmap phases.

## Context

Locco is intended to be one continuing product. Prior development failures can recur when each bug report or new feature idea is treated as an isolated prompt, causing fresh scaffolds, broad rewrites, lost bug reports, overwritten functionality, incomplete regressions and replacement APKs that do not preserve installed app state.

A short branch/PR instruction is insufficient. The system needs permanent records and explicit gates connecting the original user feedback to triage, code changes, regression evidence, release artifacts and data continuity.

## Decision

1. The existing `ysfclc/Locco` repository and Android application are the default and continuing product.
2. Ordinary bug reports, regressions, feature requests and improvements MUST be implemented as repairs or additive evolution of that product. They MUST NOT create a new app, product identity, package ID, replacement shell, repository or reset scaffold.
3. Separate product creation requires explicit user direction that a separate product is wanted.
4. Every user feedback item MUST be stored as a GitHub Issue with immutable ID `LOC-####`, original wording, classification, status, acceptance criteria and evidence.
5. Every implementation PR MUST link all relevant feedback IDs and demonstrate pre-existing behavior inventory, impact analysis, regression coverage, data/upgrade safety, verification results and rollback.
6. Unresolved feedback survives phase boundaries, assistant/session/model changes, refactors and releases. It may only change disposition with a recorded reason and traceable decision.
7. Existing Android package/signing identity, stored data, database schema history, Rust IDs and FFI contracts are compatibility surfaces. Destructive or breaking changes require explicit user approval and tested migration/recovery.
8. The GitHub `change-contract` workflow validates PR structure; the `main` ruleset must require this check and the designated reviews before the gate is described as merge-blocking.

## Alternatives considered

- **Treat each chat as a disposable task:** rejected; conversational context is not durable and prior requirements are lost.
- **Keep a separate Markdown spreadsheet of feedback:** rejected; it duplicates GitHub issue state and can drift.
- **Generate a fresh app for each feature:** rejected; breaks product, data, package and test continuity.
- **Allow unrestricted rewrites with a completion checklist:** rejected; checklists do not prove preserved behavior or justify destructive changes.
- **Use GitHub Issues + PR traceability + executable PR validation:** selected; it gives durable history, cross-links and a checkable merge contract.

## Consequences

- Intake creates or links issues before implementation.
- Product triage becomes explicit; newest does not mean highest priority.
- Bug fixes require regression proof; feature work must test shared existing flows.
- Android releases require in-place upgrade evidence and data checks.
- Agents must inspect prior issues, commits, ADRs and implementation before acting.
- The repository owner must configure branch protection to require the validator and reviews. Without that setting, CI can report failure but GitHub may still permit merging.

## Verification

The acceptance evidence for this ADR is the existence and validation of:
- `GOVERNANCE.md`;
- `docs/governance/feedback-lifecycle.md`;
- `docs/governance/change-control.md`;
- `docs/governance/application-continuity.md`;
- GitHub issue #2;
- issue and PR templates;
- `scripts/check_change_contract.py` and its unit tests;
- `.github/workflows/change-governance.yml`;
- observed green/red Actions results and required-check branch protection.

The decision is not operationally complete until those controls run on GitHub and required-check enforcement is configured.
