# Issue-to-release delivery lifecycle

**Status:** Operational policy proposal for Phase 0 governance review. The issue/PR conventions can be followed now; Project-board automation is not yet configured or verified.

## 1. One source of truth per concern

- Product intent and non-goals: `docs/product/vision.md`, `scope.md` and the preserved founding brief.
- Dependency order and outcomes: `docs/product/roadmap.md`.
- Individual work item and progress: one permanent GitHub `LOC-####` issue.
- Implementation/change set: a focused pull request linked to its issue(s).
- What is architecturally permitted: accepted ADRs and normative contracts.
- What is proven: named test/device/release evidence attached to the exact commit or artifact.
- Current phase gates: `docs/architecture/requirement-traceability.md`, `architecture-assurance-register.md` and `docs/operations/foundation-status.md`.

Do not use the roadmap as a list of guessed percentages. It describes sequence; LOC issues describe work and status; test and artifact records describe proof.

## 2. Work-item granularity

A LOC issue should deliver one coherent, reviewable outcome. A phase is decomposed into multiple issues; an issue may contain multiple implementation commits only when they build toward one acceptance outcome. Split work when it has independent prerequisites, reviewers, rollback, risk or acceptance evidence.

Every issue records:
- Original request/feedback and the requirement IDs it satisfies.
- Roadmap phase and dependency links.
- Baseline commit and current observed behavior.
- User outcome and explicit non-goals.
- Acceptance scenarios: normal, edge, denial, offline, interruption, recovery and adjacent regression as applicable.
- Data/API/FFI/browser session/model artifact compatibility and migration implications.
- Threat/risk classification and permissions/network effects.
- Named automated, integration, emulator and physical-device evidence.
- Owner, accountable reviewer roles, rollback and next action.

Never close or rewrite a user-feedback issue merely because implementation was split into smaller tasks. Link delivery issues back to the original feedback issue and preserve its original wording.

## 3. Delivery states

Use one explicit state per issue:

| State | Entry criteria |
|---|---|
| **Backlog** | Captured and linked to requirements; scope or prerequisites may still be incomplete |
| **Ready** | Acceptance criteria, dependencies, design decision, baseline and verification plan are sufficiently clear |
| **In Progress** | A named owner is implementing a focused slice on a branch |
| **Review** | PR exists; diff, tests, contracts, rollback and reviewer assignments are ready |
| **Verified** | Required acceptance tests actually executed on the exact commit and passed; unresolved device-only criteria remain separately visible and prevent claiming device verification |
| **Released** | Approved signed artifact/source release exists, hashes/provenance are recorded and required install/upgrade smoke tests pass |

**Blocked** is an overlay with a reason, impact, owner and next unblock action; it does not erase the underlying stage.

Keep issue status distinct from requirement evidence status. A requirement progresses through `PROPOSED → IMPLEMENTED → TESTED → DEVICE-VERIFIED` (or an honest externally constrained/blocked/regressed state). Closing a work issue does not automatically mark every linked requirement device-verified.

## 4. Milestone progress updates

Update the issue body or add a concise progress comment whenever a meaningful milestone is committed or its state changes. Use this structure:

```text
State:
Commit:
Completed:
Verification: PASS / FAIL / NOT RUN
Evidence:
Open gaps:
Blocker and next action:
```

A progress update without a commit/test/evidence or a concrete next action is not useful. Preserve failed results and regressions; never overwrite them with a later green check.

## 5. PR and review contract

Every PR must link its LOC issue(s), LOC-R requirement IDs and phase step. It must include:
- one focused outcome; no opportunistic unrelated refactor;
- contract/ADR impact and backwards-compatibility statement;
- tests for changed behavior plus adjacent accepted behavior;
- data migration, upgrade and rollback/recovery plan;
- security, privacy, permissions and network effects;
- exact verification commands, tool versions, exit codes, commit SHA and artifacts;
- screenshots only as visual evidence, never as proof of behavior;
- required role reviews, with unavailable independent review disclosed.

Keep PRs draft while design, acceptance or CI is incomplete. No merge based solely on compile success, documentation completeness or the absence of visible failures.

## 6. Verification vocabulary

- **PASS:** the named command/test ran on the stated commit/environment and returned success.
- **FAIL:** it ran and failed; preserve output and regression link.
- **NOT RUN:** no runner, device, permission, fixture or tool was available; no result is inferred.
- **UNVERIFIED:** implementation or evidence exists but does not satisfy the acceptance test.
- **DEVICE-VERIFIED:** physical device, exact OS/provider/runtime version, test steps and result are recorded.
- **RELEASED:** exact signed artifact and integrity/provenance evidence are recorded.

A GitHub Actions run that fails before a runner is assigned, has no steps, or has no actual test execution is **NOT RUN** for source validation. It must not be described as a code failure or as a pass.

## 7. Roadmap and status synchronization

When a delivery changes, update these records in the same PR:
1. The LOC issue: state, commit, evidence and next action.
2. The linked LOC-R rows: implementation/test/device state.
3. The phase roadmap: delivery step status only if the step's evidence changed.
4. The architecture assurance/foundation ledger when a foundation gate or unresolved risk changes.
5. Release records when an artifact is signed, installed or promoted.

The same commit should update docs that would otherwise contradict one another. Link the issue, PR, tests and exact artifact; avoid duplicating full narratives across documents.

### Project-board / automation status

There is currently no verified GitHub Project board or working issue-status synchronization automation for Locco. Do not imply this is already live. Until it is deliberately configured and tested, the permanent LOC issue is the status source of truth, and the delivery checklist/progress update is maintained in the issue. After repository Actions runner dispatch is restored, a project board can be introduced with fields for Phase, Requirement IDs, State, Risk, Owner, Linked PR, Last verified SHA and Release. Any automation must be idempotent, least-privilege and must not mark work Verified merely because a PR was merged.

## 8. Definition of complete

A feature is complete only when:
- its user outcome and exclusions are met;
- acceptance scenarios and regression cases pass;
- state, data and session continuity survive relevant lifecycle events;
- failure/denial/offline/interruption paths are handled;
- security and accessibility reviews are complete for the risk;
- exact artifacts and device evidence exist where required;
- traceability and roadmap status are synchronized;
- no blocker/uncertainty is silently recast as success.
