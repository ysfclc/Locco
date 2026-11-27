# Agent operating contract

**Mandatory authority:** GOVERNANCE.md. No agent may treat it as optional guidance. The constitution binds all tasks, phases, fixes, feature requests, PRs, releases and handoffs.

## Required reading — every task, before editing

1. GOVERNANCE.md — product continuity, authority, no-reset rule and acceptance gates.
2. docs/governance/README.md, especially feedback-lifecycle.md, change-control.md and application-continuity.md.
3. PRINCIPLES.md, CONTRIBUTING.md, product vision/scope and affected ADRs and architecture/security contracts.
4. .agent/routing.md, the selected role prompt in .agent/agents/ and each triggered skill in .agent/skills/.
5. Full linked GitHub feedback issue(s), comments, related open/closed issues, previous PRs/commits and existing implementation/tests.

A summary, memory, task prompt or recent chat excerpt does not replace the source record. If an input describes a Locco bug, regression, feature, UX problem, constraint or requested change, create or link its canonical GitHub issue (LOC-####) before implementation. Preserve the user's original wording.

**Prompt activation is not automatic:** root AGENTS.md is the repository-wide entry contract, but files under .agent/ are a prompt/skill library. The orchestrator must deliberately load the selected role and triggered skills. Do not claim an agent or independent review was activated merely because its Markdown file exists. See .agent/agents/README.md and .agent/skills/README.md.

## Non-negotiable operating rules

- **One product:** repair and evolve the existing Locco application in this repository. Never create a replacement app, sibling repository, package ID, launcher shell or clean-slate scaffold because new feedback arrived. A separate product needs explicit user direction.
- **No lost feedback:** an open bug, accepted feature, constraint or prior failure remains active until a linked record proves it resolved, or a reasoned defer/reject/supersession is recorded. New work does not erase old work.
- **No silent scope change:** a new feature idea is not automatic approval to redesign other modules. A later message supersedes an earlier decision only when the user explicitly says so.
- **Baseline first:** record base SHA, current implementation, tests, existing user flows, Android identity/data and related feedback before editing.
- **Minimal coherent change:** preserve working behavior, contracts, data, permissions and offline paths. Do not replace working code with placeholders, demos or disconnected UI.
- **Regression proof:** bug fixes add a reproducer/regression test when technically possible; feature changes test adjacent existing flows. Never delete/weaken a test to obtain green CI.
- **Upgrade safety:** do not change package/signing/database identity, wipe data or require uninstall as an ordinary fix. Breaking changes need approved ADR, migration, backup/recovery and upgrade tests.
- **Evidence only:** record actual command output, test/build results, artifact hashes, device/upgrade evidence and unknowns. No claims based on workflows that did not execute.
- **Handoff:** include feedback IDs, baseline/head/merge SHA, changed paths, tests/results, APK hash when available, unresolved items and rollback.

## Agent ownership and task control

Every task has one accountable owner, selected through .agent/routing.md. Required reviewers are distinct from the implementer. Role prompts define permitted decisions, procedure, deliverables and stop conditions. Load only relevant skills, but every triggered skill is mandatory. If independent agents/reviewers or a required tool are unavailable, state the limitation rather than simulate a second opinion.

## Agent roster

| Agent | Owns | Mandatory continuity responsibility |
|---|---|---|
| [architect](.agent/agents/architect.md) | Structure, contracts, ADRs | Reject silent contract breaks, product resets and architecture rewrites without user approval and migration. |
| [rust-core](.agent/agents/rust-core.md) | Rust domain kernel, FFI | Preserve IDs, invariants, serialized data, ABI compatibility and regression tests. |
| [android](.agent/agents/android.md) | Android shell, JNI, Gradle, APK | Preserve package/signing identity, user data, current flows and in-place upgrade behavior. |
| [ios](.agent/agents/ios.md) | Future Swift bridge; scaffold only | Preserve cross-platform contract; do not create a second product or advance iOS scope unilaterally. |
| [security](.agent/agents/security.md) | Threats, secrets, supply chain | Review new permissions/trust surfaces and protect feedback evidence from secret exposure. |
| [qa](.agent/agents/qa.md) | Independent verification | Maintain issue-to-test traceability, regression matrix, upgrade evidence and honest completion states. |
| [product](.agent/agents/product.md) | Vision, user outcomes, scope, prioritization | Preserve all user requests and make explicit accept/defer/reject decisions; newest is not automatically replacement scope. |

## Ownership and approval

Every task names one accountable owner, reviewers, inputs, outputs and measurable acceptance criteria. Relevant owners review affected contracts. Security-sensitive work requires security review and independent QA. The author cannot self-approve security acceptance.

No agent may waive the constitution, close a user issue, redefine product identity, discard a requirement, alter package/data/signing identity, or mark unverified behavior as done. Escalate a conflict to the user with the existing decision and proposed alternatives; keep the work blocked until direction is explicit.
