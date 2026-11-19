# Agent routing and handoff

All routing is subordinate to [GOVERNANCE.md](../GOVERNANCE.md) and the mandatory reading/continuity rules in [AGENTS.md](../AGENTS.md). Read the linked `LOC-####` issue and history before assigning work.

| Request | Owner | Required review |
|---|---|---|
| User feedback intake, vision, scope, priority, acceptance | product | architect + QA |
| Bugs/regressions and existing-flow preservation | relevant implementation owner | QA; security if applicable |
| Modules/contracts/ADRs | architect | affected owner + QA |
| Rust/FFI/persistence | rust-core | architect + QA |
| Gradle/JNI/APK/Android UX/upgrade | android | rust-core + QA; security for signing/permissions |
| Swift/iOS bridge | ios | architect + rust-core + QA |
| Threats/secrets/signing/dependencies | security | QA + affected owner |
| Regression matrix, release/upgrade evidence and acceptance | qa | affected owner |
| New source, external action or permissions | architect + relevant adapter owner | security + QA |

## Mandatory handoff protocol

1. **Issue:** link/create the canonical feedback record and preserve original user wording. Include every affected ID, not only the newest one.
2. **Baseline:** record base SHA, app/build/data identity, affected flows, tests, prior bugs/PRs/ADRs and the known-good artifact where one exists.
3. **Plan:** accountable owner states scope, acceptance criteria, impact, reviewers, regression matrix, data/upgrade/security analysis and rollback.
4. **Review plan:** architect reviews contract/scope changes; security reviews trust/permission/data changes; QA reviews criteria and regression plan.
5. **Implement:** focused branch and incremental change to the existing app. No replacement app, reset, hidden scope reduction or destructive update.
6. **Verify:** run reproducible tests and actual runtime/device/upgrade checks. Distinguish what ran from what remains unknown.
7. **Independent QA:** compare the result against the original issue, existing behavior inventory and all adjacent open feedback.
8. **Return:** changed paths, issue IDs, base/head/merge SHAs, commands/results, APK/hash, device/upgrade evidence, risks, remaining work and rollback.

Stop when authority is unclear, feedback conflicts with a prior explicit decision, a protected contract/data boundary changes without approval, or acceptance cannot be proven. Do not resolve ambiguity by deleting work or inventing user intent.
