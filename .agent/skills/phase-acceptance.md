# Skill: Phase and change acceptance

**Required for every PR, bug fix, feature slice, phase close and release.**

## Preconditions
- Linked `LOC-####` issues exist and retain the original user request.
- Change plan contains baseline SHA, existing behavior inventory, acceptance criteria and test matrix.
- Relevant prior issues, PRs, commits, ADRs and contracts have been inspected.
- Product continuity, data migration and rollback impact are known.

## Procedure
1. Map each user requirement/acceptance criterion to a code path, test or observable manual check.
2. Map each reported bug/regression to a reproducer and regression test, or record why automation is technically impossible.
3. Verify feature behavior and adjacent existing flows; inspect error, permission-denied, cancellation, offline and process-restoration paths where relevant.
4. Inspect tests for accidental deletion, weakened assertions, skipped coverage or changed baselines.
5. On Android, record source SHA, package/application ID, versionCode/versionName, signing lineage, APK SHA-256 and toolchain versions.
6. Test in-place upgrade from the supported baseline with representative existing data. A fresh install does not satisfy this requirement.
7. Separate build, unit test, integration test, emulator test, physical-device test, upgrade test and release evidence.
8. Record exact commands, environment, outputs, artifacts, hashes and skipped checks.
9. Obtain independent QA reproduction without relying on the implementer's verbal explanation.
10. Update every linked feedback issue. Keep incomplete, blocked, deferred and unverified criteria open.

## Pass conditions
- All required criteria have reproducible evidence.
- Existing behavior inventory is checked against the diff and related regressions.
- No previously accepted requirement or unresolved issue was silently dropped.
- No destructive data/package/signing change occurred without explicit approval and recovery proof.
- The correct existing application is tested; replacement scaffolds and unrelated demos do not count.

## Failure conditions
Unsupported claims, stale artifact, missing feedback ID, missing regression test, hidden scope reduction, unverified upgrade, destructive reset, weakened tests, or an undocumented manual step. Do not mark the phase complete.
