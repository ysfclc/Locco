# Skill: Release candidate gating

**Trigger:** Foundation/phase acceptance that emits a build artifact, Android release candidate, protected contract change or production release.

## Inputs required
- Approved release scope and linked LOC issues.
- Exact source ref/SHA, version metadata and previous supported release.
- Completed tests, security review, SBOM/provenance and upgrade plan.
- Required reviewer/environment settings and signing identity.

## Gate sequence
1. **Source integrity:** verify the release ref points to the reviewed commit; inspect uncommitted/generated inputs and lockfiles. Do not release from an unreviewed working tree.
2. **Change acceptance:** confirm every issue criterion has a disposition and all required change-contract/build/test checks ran on this source. A workflow file existing is not evidence of a run.
3. **Code quality:** run formatting, compiler/linter, unit, integration, UI and static-analysis checks applicable to changed surfaces. Record skipped checks individually.
4. **Native compatibility:** cross-build all supported ABIs; inspect ELF architecture, exported symbols and packaged libraries.
5. **Security:** obtain required security review; inspect permissions, secrets, dependency/advisory results, source authorization and threat-model changes.
6. **Artifact identity:** verify application ID, monotonically increasing versionCode, versionName, signer fingerprint/lineage, APK signature and SHA-256.
7. **Supply chain:** generate and parse SBOM; verify artifact digest and provenance/attestation subject point to the exact APK and source/workflow.
8. **Upgrade/data:** install over the previous supported build with representative records/files/preferences and expected signing identity. Verify migrations, integrity, interrupted recovery and core flows. Test clean install separately.
9. **Independent acceptance:** QA reviews source diff, linked requirements and artifact evidence; specialist owners sign off their contracts. Implementer cannot self-approve independent QA/security.
10. **Release record:** record source/merge SHA, workflow run, package/version/signer, APK/SBOM hashes, checks, device/API, upgrade source/target, gaps, release notes and rollback/forward-recovery.

## Required evidence
- Exact source/merge SHA, workflow IDs and job conclusions for every required gate.
- Test summaries and named skipped/not-run checks.
- APK SHA-256, package/version, signer fingerprint and supported ABI inspection.
- SBOM and provenance/attestation subject digests.
- Upgrade baseline/candidate versions, device/API, before/after integrity evidence and recovery results.
- Required environment/reviewer approvals, unresolved risks and final release decision.

## Decision states
- PASS: required gate ran and evidence supports the requirement.
- FAIL: gate ran and found a defect.
- NOT RUN: not executed; never counts as pass.
- BLOCKED: cannot run due to missing authority, environment, artifact or prerequisite.
- NOT APPLICABLE: explain the technical reason and who accepted it.

## Fail conditions
Any skipped required test, stale artifact, wrong signer/package, missing approval, unresolved critical/high risk, missing ABI, SBOM/hash mismatch, untested required upgrade or unsupported completion claim blocks release. Do not bypass a gate to meet a date.
