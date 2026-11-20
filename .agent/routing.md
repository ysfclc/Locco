# Agent routing and handoff

All routing is subordinate to GOVERNANCE.md and root AGENTS.md. The repository-owned role prompts live in [agents/](agents/README.md); task-triggered procedures live in [skills/](skills/README.md).

## Activation is deliberate
Markdown prompts are not automatically active merely because they exist in the repository. The orchestrator/task owner must load root AGENTS.md, one accountable role prompt, required reviewer prompts and every triggered skill into the working context. If the environment cannot run independent agents or separate reviewer sessions, disclose that limitation and do not label the review independent.

## Routing

| Request | Accountable owner | Required review |
|---|---|---|
| User feedback, vision, scope, priority, acceptance intent | product | architect + QA |
| Bugs/regressions and existing-flow preservation | relevant implementation owner | QA; security if applicable |
| Module boundaries, contracts and ADRs | architect | affected owner + QA |
| Rust/domain/persistence/FFI | rust-core | architect + QA; security for trust/memory changes |
| Gradle/JNI/APK/Android UX/upgrade | android | rust-core + QA; security for signing/permissions |
| Swift/iOS bridge | ios (only after roadmap/user gate) | architect + rust-core + QA; security as applicable |
| Threats, secrets, permissions, source authority, dependencies | security | QA + affected owner |
| Regression matrix, independent verification and release acceptance | qa | affected owner; security for security-sensitive acceptance |
| New source, external action or permission | architect + relevant adapter owner | security + QA |

## Mandatory task sequence
1. **Intake:** identify the user outcome; preserve original wording; search existing issues; create/link canonical LOC issue before implementation. Use feedback-intake.
2. **Authority:** resolve product/decision conflicts against user instructions and accepted records. Escalate ambiguity; do not infer approval.
3. **Baseline:** inspect source, history, tests, app/data identity and previous artifact. Record base SHA and current behavior.
4. **Plan:** name one owner, required reviewers, affected modules/contracts, acceptance criteria, regression matrix, migration/security/privacy impact and rollback.
5. **Plan review:** architect reviews boundaries/contracts; security reviews trust/permission/secret changes; QA reviews observable criteria and test strategy.
6. **Implementation:** work on a focused branch in the existing Locco app. Preserve accepted requirements and unrelated changes.
7. **Verification:** owner runs the focused and repository checks; QA independently inspects the diff and reproduces criteria against the exact commit/artifact.
8. **Release:** use release gating, signing, SBOM and upgrade skills where triggered. Merge/release only under configured repository protections.
9. **Carry-forward:** update each issue with exact evidence and remaining criteria. Unverified/deferred/blocked work remains open.

## Mandatory handoff record
Every handoff includes:
- user goal and original source issue IDs;
- accountable owner and named reviewers;
- base/head/merge commit SHA;
- baseline behavior and affected source paths/symbols;
- approved scope and acceptance criteria;
- decisions/ADRs and impacted contracts;
- exact commands, tool versions and PASS/FAIL/NOT RUN/BLOCKED results;
- artifact name, package/version/signer/hash when applicable;
- test/device/upgrade evidence and known gaps;
- remaining work, owner and rollback/forward-recovery.

A summary of changed files alone is not a handoff.

## Stop conditions
Stop when authority is unclear, feedback conflicts with a prior explicit decision, a protected contract/data boundary changes without approval, an unsafe trust/permission boundary is unresolved or acceptance cannot be demonstrated. Preserve the issue and current state; do not resolve ambiguity by deleting work, silently narrowing scope or inventing user intent.
