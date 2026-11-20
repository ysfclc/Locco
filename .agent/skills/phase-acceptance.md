# Skill: Phase and change acceptance

**Trigger:** Every PR, bug fix, feature slice, phase close and release. This is the final traceability procedure, not a substitute for specialist skills.

## Preconditions
- Canonical LOC issue(s) exist and preserve the original user wording.
- Base SHA, current behavior inventory, user-visible criteria and regression plan are recorded.
- Relevant issues, comments, previous PRs/commits, ADRs, tests and current artifacts were inspected.
- Owner and independent reviewers are identified.

## Procedure
1. Create a traceability row for every original user requirement and every acceptance criterion.
2. For bugs, map expected/actual behavior to a reproducer and regression test; explain technically impossible automation.
3. For features, map each criterion to code path, automated test or observable manual check.
4. Map changed modules/state/contracts to adjacent existing journeys that share navigation, storage, permissions, lifecycle, offline behavior or adapters.
5. Inspect the diff for deleted/weakened tests, skipped coverage, placeholders, mock-only paths, dead controls and unrelated rewrites.
6. Run focused tests, affected module suite and all relevant repository checks. Record exact commands, versions and result; a command not run is NOT RUN.
7. Android: record source SHA, application ID, versionCode/name, signer fingerprint/lineage, ABI list and APK SHA-256. Test package/signature and in-place upgrade with representative existing data when relevant.
8. Separate static analysis, host tests, cross-build, unit/integration/UI tests, emulator/device tests, upgrade tests and release evidence.
9. Have QA independently reproduce the original outcome from the source/artifact. The implementer's verbal explanation is not evidence.
10. Update every linked issue individually with the evidence, unresolved criteria, artifact and next action. Preserve deferred/blocked items.

## Pass conditions
Every required criterion has evidence or an explicitly accepted blocker; adjacent flows are checked; no unresolved critical regression/data-loss/security issue remains; identity and user-data continuity are proved where relevant; issue status matches evidence.

## Fail conditions
Missing issue, stale artifact, hidden scope reduction, absent regression test without reason, weakened assertions, destructive reset, unverified migration/upgrade or unsupported completion claim. Do not mark phase complete.
