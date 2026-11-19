# Skill: Regression-first defect correction

**Required for every bug, crash, data-loss, reliability and regression report.**

## Preconditions
- The full `LOC-####` source issue is open and linked.
- Current implementation, prior related issues/PRs, tests and baseline commit have been inspected.
- Expected and actual behavior are recorded.

## Procedure
1. Reproduce on the reported build/device where possible. Capture precise conditions and logs with private content/secrets removed.
2. Identify the earliest failing boundary and root cause. Do not treat a visual symptom as proof of the faulty layer.
3. Add a test that demonstrates the defect before the correction, or document why a deterministic automated reproducer is technically impossible.
4. Implement the smallest root-cause fix. Preserve all unrelated behavior and previously accepted requirements.
5. Run the new regression test, relevant module tests, broader repository suite and negative/error-path tests.
6. Inspect and exercise adjacent flows that share the changed state, API, storage, lifecycle or navigation.
7. On Android, verify the affected OS/runtime conditions and in-place upgrade when installation or data is affected.
8. Attach exact commands, tool versions, results, commit SHA and device/artifact evidence to the issue and PR.
9. QA independently checks the original reproduction steps against the resulting build.

## Prohibitions
- Do not remove, skip, loosen or rewrite a failing test to hide the defect.
- Do not mark a report resolved because the code compiles or a different test passes.
- Do not replace the app, module or screen with a clean placeholder to avoid the failing path.
- Do not close the issue while acceptance or release evidence is missing.

## Completion
The issue reaches `VERIFIED` only when the original failure is no longer observed and regression coverage proves the behavior. It reaches `RELEASED` only when the verified result is present in an identified released artifact.
