# Skill: Regression-first defect correction

**Trigger:** Every bug, crash, data-loss, reliability, performance-regression or previously working flow report.

## Preconditions
- Open LOC issue retains the original report and current expected/actual behavior.
- Current implementation, previous related issues/PRs, tests and baseline commit are inspected.
- Affected build/device/runtime context is recorded where available.

## Procedure
1. Reproduce on the reported build/device where possible. Preserve relevant logs/screens without secrets or unnecessary private content.
2. Search related prior fixes and failed attempts. Check whether the current symptom is a recurrence of an older issue.
3. Trace the user-visible failure to the earliest broken state/contract boundary. Do not assume the screen showing the symptom owns the defect.
4. Form a falsifiable root-cause hypothesis and identify evidence that would disprove it.
5. Add a regression test that fails on the baseline and passes after correction. If impossible, explain the technical reason and define an independent manual reproduction.
6. Implement the smallest root-cause fix in the existing owning module. Preserve unrelated behavior and all accepted requirements.
7. Run the reproducer, focused tests, affected module tests, broader suite and relevant negative/error paths.
8. Inspect neighboring flows sharing state, API, persistence, navigation, cancellation, offline behavior, permissions or lifecycle.
9. For Android install/data defects, verify the affected OS/runtime and in-place upgrade with representative data.
10. Ask QA to repeat the original steps against the exact result artifact without relying on verbal explanation.
11. Update the issue with baseline failure, root cause, fix commit, tests/results, artifact identity/hash and any remaining uncertainty.

## Required evidence
- Original reproduction steps and expected versus observed result.
- Baseline commit/build and the failing test or independent reproduction.
- Root-cause evidence and exact changed files/owning module.
- Regression test name/output, broader suite results and adjacent-flow checks.
- Candidate artifact source SHA/hash and device/runtime details where applicable.
- Unrun checks, residual uncertainty and linked LOC issue disposition.

## Fail conditions
Do not accept a fix when the original failure is not reproduced or independently explained, the new test does not protect the defect, an adjacent flow regresses, the artifact is stale, or data/identity continuity is unproven. Record the exact blocker and keep the LOC issue open.

## Completion
VERIFIED means the original failure is no longer observed and regression evidence proves the expectation. RELEASED means the verified correction is in an identified released artifact. Otherwise keep the issue open.
