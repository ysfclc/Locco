# ADR-0016: Direct GitHub delivery is the canonical internal workflow

**Status:** Proposed for Phase 0 acceptance · 2026-11-20 · Reversibility: High

## Context

Locco development is performed directly in its permanent GitHub repository using focused branches, commits, pull requests and GitHub Actions. A ZIP-upload pipeline duplicates that workflow, introduces an unnecessary archive trust boundary for ordinary internal work, and risks making archive intake look like the required route for phase delivery.

The repository's Rust, Android, security and governance workflows already validate pull requests and build artifacts from reviewed Git commits. The user's explicit operating direction is direct GitHub collaboration; ZIP handoff is not required.

## Decision

Use direct GitHub branch/commit/PR delivery as the only canonical internal workflow.

1. Capture each bug, feature or phase outcome in a permanent LOC-#### issue linked to its LOC-R### requirements.
2. Create a focused branch from the current reviewed base commit.
3. Implement in the existing Locco repository and app identity. Preserve unresolved feedback and prior accepted behavior.
4. Open/update a PR using the mandatory evidence template. Keep it draft while design, implementation or acceptance is incomplete.
5. Run the relevant GitHub Actions checks against the exact PR commit: governance, Rust, Android and security/policy.
6. Upload build artifacts from the same commit; verify package identity, ABI payloads, signatures and hashes.
7. Obtain required specialist/independent review and repository merge protection before merge.
8. Update the LOC issue, roadmap and assurance ledger with exact commit/run/artifact evidence.
9. Use protected GitHub Environments for release signing and release-only credentials.

ZIP intake is not part of internal development or phase delivery. If a genuinely external contribution arrives as an archive, it must first be converted into a normal reviewed Git branch/PR through a separately authorized, isolated process. That exception must not publish files, execute archive scripts or bypass the normal review/CI gates.

## Consequences

- One auditable Git history and one normal PR path.
- No routine archive upload, extraction or generated phase branch.
- GitHub Actions runs directly against the proposed source commit and produces traceable artifacts.
- Existing ZIP extraction-safety tests may remain as defensive regression tests for the exceptional external-archive boundary; they do not imply that ZIP intake is a supported internal workflow.
- The old ZIP-triggered phase pipeline is removed to prevent competing workflows.
- Existing ADR-0006 is superseded for internal delivery. Its archive threat controls remain applicable only to any future explicitly authorized external archive intake.

## Alternatives considered

- Keep ZIP ingestion as the normal phase pipeline: rejected because it duplicates direct GitHub delivery and adds no value for the current workflow.
- Manual ZIP extraction into the repository: rejected because it retains the unnecessary archive handoff and is less reproducible.
- Direct commits to main without PRs: rejected because it weakens review, CI evidence, traceability and rollback.

## Acceptance evidence

- README and setup/protocol documentation describe direct GitHub delivery as canonical.
- The ZIP-triggered pipeline is absent.
- PRs link LOC issues, preserve app identity and carry explicit regression/data/security/verification evidence.
- Governance, Rust, Android and security workflows execute on PR commits and their exact results are recorded.
- Repository rules require the relevant checks and independent approval before merge.
- A debug APK can be traced to its source SHA, workflow run and artifact digest.

## Fail closed

Do not treat this ADR as Accepted until the documentation is reconciled, the obsolete pipeline is removed, PR checks are observed on the current branch, and repository-level merge protection is configured and tested. A workflow file alone is not merge protection.
