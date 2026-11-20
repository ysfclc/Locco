# Contributing

## Authority

User-approved direction and accepted ADRs → PRINCIPLES.md and product scope → architecture/security contracts → agent roles/skills → implementation comments. Conflicts require an ADR.

## Delivery lifecycle

Every coherent delivery starts with a permanent `LOC-####` issue and follows [the issue-to-release lifecycle](docs/operations/delivery-lifecycle.md). The issue links product requirement IDs, phase, dependencies, acceptance scenarios and a baseline commit. Do not start implementation from a chat summary or an untracked TODO.

A roadmap phase is not one giant issue. Split it into small, dependency-aware vertical slices, each with a testable user outcome. Update the issue at every milestone/commit with the exact commit, work completed, verification outcome, evidence and next blocker. Update the roadmap, requirement traceability and status ledger in the same PR when their claims change.

## Engineering workflow

1. Read `AGENTS.md`, `.agent/routing.md`, relevant ADRs, product scope and acceptance criteria.
2. Inspect the actual current implementation and adjacent behavior. Record the base SHA and issue/requirement IDs.
3. Resolve high-reversal design choices with ADRs or measured spikes before hardening interfaces.
4. Implement one coherent slice in the continuing application. Preserve package identity, existing data, prior user feedback and accepted behavior.
5. Add regression, contract, integration and UI tests appropriate to the changed boundary.
6. Run checks on the exact PR head. Record full commands, environment, exit status and artifact hashes. A workflow with no assigned runner or no executed steps is **NOT RUN**, not a pass or source failure.
7. Obtain the required reviewer roles. Security-sensitive changes need security review; behavior changes need QA review. If independent reviewers are unavailable, disclose that explicitly and do not claim an independent review.
8. Update issue, PR, traceability, roadmap, status ledger and release evidence. A test passing does not prove a physical-device behavior or release artifact.

## Done

The issue is closed only when its acceptance criteria are met and evidence is attached. Distinguish `PROPOSED`, `IMPLEMENTED`, `TESTED`, `DEVICE-VERIFIED`, `EXTERNALLY-CONSTRAINED`, `BLOCKED` and `REGRESSED`. Tests pass or blockers are explicit; no secrets/private data are committed; offline/failure behavior and migration/recovery are covered; accessibility is considered; Android changes produce an inspected APK with SHA-256 and the required device evidence.

## Scope and repository continuity

Locco remains the one permanent repository. Archive ingestion uses [the hostile ZIP protocol](phase-protocol.md); archives are never trusted scripts. Do not create sibling replacements, copy donor-app identities or silently drop accepted requirements.
