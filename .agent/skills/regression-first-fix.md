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

## Prohibitions
Never remove, skip, loosen or rewrite a failing test to hide the defect. Never mark a report resolved because code compiles or another test passes. Never replace the app/module/screen with a placeholder to avoid the failing path.

## Completion
VERIFIED means the original failure is no longer observed and regression evidence proves the expectation. RELEASED means the verified correction is in an identified released artifact. Otherwise keep the issue open.
