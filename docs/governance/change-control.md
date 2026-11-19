# Change control: from feedback to verified implementation

**Normative companion to [GOVERNANCE.md](../../GOVERNANCE.md).** This is the required execution procedure. A coding task is not authorized merely because an agent can imagine a solution.

## Stage 0 — Intake and identity

- Confirm the existing Locco repository and product. The default target is `ysfclc/Locco`, Android application already defined by the project, and the existing Rust core/Android shell contracts.
- Find or create the canonical GitHub Issue (`LOC-####`). Preserve the source request verbatim and link evidence.
- Classify the request without converting an observation into a broader feature or treating a feature idea as permission for a rewrite.
- If user instruction explicitly supersedes an old decision, record that link and the exact supersession. Otherwise existing commitments remain active.

**Exit gate:** issue exists, source and context retained, and desired outcome is understood well enough to investigate.

## Stage 1 — Baseline and history

Record in the issue/PR:
- `base_sha` from the branch being changed;
- current product/version and, for Android, application ID, versionCode, versionName and signing lineage where available;
- current build artifact and SHA-256, if one exists;
- relevant modules, Rust crates, Kotlin packages, JNI/FFI contracts, DB schemas, permissions, intents and external adapters;
- current tests and known failures;
- related open/closed issues, PRs, commits, ADRs and previous attempts.

Inspect code, tests and actual behavior. Build or install the baseline when feasible. If it is not feasible, state exactly which parts are unknown. A new implementation must not be assumed necessary because a previous response failed to find an existing implementation.

**Exit gate:** written baseline and a list of existing behavior that must remain unchanged.

## Stage 2 — Impact analysis and solution choice

For every affected behavior, produce a change-impact matrix:

| Surface | Questions that must be answered |
|---|---|
| Product | Does this remain within Locco's identity and explicit scope? |
| Existing UX | Which screens, gestures, routes, states and workflows must still work? |
| Rust/domain | Which invariants, IDs, events, state transitions or core APIs change? |
| FFI/JNI | Is the ABI unchanged? If not, is it versioned and tested across shells? |
| Data | Are schemas, stored files, preferences, cache, encryption or migrations affected? |
| Android | Are package ID, permissions, lifecycle, background behavior, intents or target APIs affected? |
| Local AI/context | Does it change model routing, memory, context budgets, retrieval or offline fallback? |
| Sources/actions | Are access authority, provenance, approval, exact payload or postcondition changed? |
| Security/privacy | Does this add trust, permission, exposure, retention or dependency risk? |
| Compatibility | Will the current installed app and existing data upgrade safely? |
| Operations | Can this be tested, diagnosed, rolled back and reproduced? |

Choose the least disruptive solution that satisfies the requirement. Reuse existing components and contracts. A rewrite requires evidence that incremental correction is not viable, an alternatives analysis, migration/rollback plan, and explicit user approval. A new application requires explicit user direction.

**Exit gate:** accepted approach, measurable acceptance criteria, reviewers, risks and rollback.

## Stage 3 — Implementation

- Create a focused branch from the recorded base SHA.
- Implement only the approved slice. Do not opportunistically rewrite unrelated modules.
- Keep Rust as the owner of domain truth and invariants. Shell code remains platform adaptation and presentation.
- Preserve stable public contracts. If a contract must change, update/version the contract and ADR before or together with dependent implementations.
- Do not replace working files with placeholder scaffolds or demo implementations.
- Do not remove existing capabilities, tests, persistence, migrations, build steps or documentation to make the new work easier.
- Preserve unrelated uncommitted or in-flight work. Never reset, force-push, clean or delete another contributor's changes without explicit authorization.
- Record deviations from the accepted plan in the PR before they become hidden scope.

## Stage 4 — Verification

### For a bug or regression
1. Reproduce the report on the affected baseline, or document exact reproduction blockers.
2. Add a test that fails for the reported defect, whenever technically possible.
3. Apply the smallest root-cause fix.
4. Run the new test, affected module tests and the full available regression suite.
5. Exercise adjacent previously working flows and relevant negative/error paths.
6. Re-test on the affected Android version/device when the defect is platform/runtime dependent.
7. Verify existing user data and upgrade behavior when persistence or installation paths are involved.

### For a feature or enhancement
1. Map every accepted criterion to a test or observable manual check.
2. Test the new path and existing paths that share state, navigation, permissions, data or adapters.
3. Test offline, cancellation, permission denial, malformed input and process death where relevant.
4. Verify no dead button, mock-only state, hard-coded demo data or disconnected UI remains.
5. Verify the feature works inside the existing application and preserves existing settings/data.

### For a refactor or infrastructure change
Prove behavioral equivalence for the affected contracts. Add migration/compatibility tests if serialized data, FFI, API or build artifacts change.

All evidence must state command, environment/tool versions, exact result and commit SHA. A command that was not run must not appear as a pass.

**Exit gate:** all required checks pass or each gap is explicit and accepted as a blocker; no unresolved critical regression.

## Stage 5 — Review and merge

The PR must follow the repository PR template and pass `change-contract`. Required reviewers are selected by `.agent/routing.md` and CODEOWNERS. Security-sensitive changes require security review and independent QA; the author cannot self-approve the security review.

Reviewers inspect the diff against the baseline, related issues and existing behavior. A green check does not substitute for review. Merge only through the protected branch path. Do not bypass missing required checks.

## Stage 6 — Release and upgrade

For a user-facing Android change, assemble the actual APK, record its SHA-256, install it over the previous supported build with representative existing data, and verify the reported behavior and adjacent workflows. Test clean install separately; it does not replace upgrade testing.

Do not ask the user to uninstall/clear data as a normal remedy. If a destructive migration or signer/package identity change is unavoidable, stop until the user approves the explicit decision and the tested recovery path exists.

**Exit gate:** issue updated with merge SHA, APK identity/hash, tests, upgrade/device results, known gaps and rollback. Only then may acceptance be marked released.

## Stage 7 — Closeout and carry-forward

- Update all linked issues individually.
- Keep deferred/unverified criteria open.
- Record the next remaining work explicitly.
- Update architecture, ADR, roadmap, runbooks and tests when the implementation changes their truth.
- Produce a handoff containing baseline/head/merge SHAs, changed paths, commands/results, artifacts/hashes, issue IDs, risks, open work and rollback.

A handoff that says only "done" or lists changed files is not acceptable.
