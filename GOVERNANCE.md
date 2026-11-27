# Locco product and engineering constitution

**Status: Normative. Applies to every contributor, coding agent, reviewer, release operator and automation acting in this repository.**

This document is the operating constitution of the existing Locco product. It is not advice, a suggested checklist or a description of aspirations. Requirements using **MUST**, **MUST NOT**, **SHOULD** and **MAY** are interpreted as binding controls. A pull request that violates a MUST or omits evidence required by this constitution is not eligible for acceptance.

The user owns the product. The repository, its history, GitHub Issues, accepted ADRs, tests and release records are the durable memory of the work. A conversational turn is not permission to forget previous turns.

## 1. Product identity is continuous

Locco is one evolving product, maintained in this repository. A bug report, regression report, feature idea, UX complaint, workflow request, architecture concern or new use case is input to the existing Locco product. The default response is to understand and improve the existing codebase and application.

No contributor or agent may respond to new feedback by:
- creating a second application, repository, package, shell or replacement scaffold;
- restarting the roadmap, rebuilding an already-working subsystem from zero, or replacing the current implementation with a fresh demo;
- narrowing or redefining Locco around the most recent example or feature;
- deleting, hiding, downgrading or silently postponing previously reported bugs, accepted features, user constraints or working capabilities;
- changing the Android application ID, signing lineage, database identity, data location, Rust contracts, release channel or product name as a side effect of an ordinary feature or fix;
- asking the user to uninstall, clear application data, reset the database or abandon the installed app as a routine update step.

A separate product is allowed only when the user explicitly directs that a separate product be created. A new module, workflow, screen, adapter, capability or domain is not a new product. New domains should compose existing Locco capabilities unless a documented architectural reason proves otherwise.

## 2. Authority and conflict resolution

The following order governs work, from highest to lowest:

1. The user's explicit current direction, including an explicit statement that a prior user decision is being superseded.
2. This constitution and `PRINCIPLES.md`.
3. The product vision, product scope and accepted product decisions.
4. Accepted ADRs and versioned architecture, data, FFI, security and platform contracts.
5. Open and accepted GitHub feedback records, acceptance criteria and release commitments.
6. Implementation, tests, comments and informal proposals.

A new idea is not automatically a decision. A bug report is not permission to redesign unrelated systems. A suggestion does not supersede a previously accepted requirement merely because it arrived later. When a new request conflicts with an existing decision, the owner MUST identify the exact conflict, preserve both records, propose options and obtain an explicit user decision before making an irreversible or product-shaping change.

No agent may resolve a conflict by quietly rewriting scope, deleting history, or treating the most recent message as a complete replacement for the product brief.

## 3. Canonical record of feedback and work

GitHub Issues in this repository are the canonical durable record for user feedback and product work. Each record has an immutable identifier `LOC-####`, derived from its GitHub issue number (for example, issue #2 is `LOC-0002`). Pull requests, commits, tests and releases MUST link back to these records.

Every product-related report received in conversation or GitHub MUST be captured as an issue before implementation begins. Preserve the user's original words, meaningful context, screenshots/logs where safe, and the reported app/build/device version. Do not silently paraphrase away a constraint or uncertainty. If a statement is incomplete or ambiguous, preserve it as written and label the interpretation as provisional.

The issue is the source record; do not create a competing spreadsheet or manually maintained status list that can drift. A dashboard may summarize issue links but MUST NOT replace them. Duplicate reports are linked to the canonical record and remain discoverable. Deferred and rejected requests remain recorded with rationale; they are not deleted or represented as fixed.

See [feedback lifecycle](docs/governance/feedback-lifecycle.md).

## 4. Mandatory pre-change investigation

Before touching implementation, the accountable owner MUST:

1. Read this constitution, `AGENTS.md`, routing, relevant role and skills, `PRINCIPLES.md`, product vision/scope, relevant ADRs and affected contracts.
2. Read the linked issue in full, including comments and attachments, and search related open/closed issues, prior PRs, commits and regression history.
3. Inspect the current implementation and tests for the affected behavior. Identify what already works; do not infer absence from an incomplete search.
4. Establish a baseline commit SHA, current branch, app/package identity, current build/version where available, affected data schema and the last known-good artifact/device state.
5. Identify all related open bugs, accepted feature records, existing user workflows and previous failed attempts. State explicitly which records are in scope and which are unaffected.
6. Write a change plan with owner, reviewers, files/modules, acceptance criteria, regression coverage, compatibility/data impact, risks and rollback.

If the repository does not contain the evidence needed to understand a previous commitment, do not invent it. Mark the item unknown, inspect GitHub history, and request the missing source only when it materially blocks safe work.

See [change control](docs/governance/change-control.md).

## 5. Bug and regression policy

A reported defect is not closed by changing code, making a build green, or saying that it should work. The exact reported behavior must be reproduced or the inability to reproduce it must be recorded.

For each bug:
- retain the original expected behavior and actual behavior;
- record reproduction steps, affected versions/devices, severity, frequency and data-loss/security risk;
- identify the failing layer and root cause, not merely the visible symptom;
- add a failing automated regression test before or with the fix whenever technically possible;
- make the smallest coherent correction that fixes the cause without removing unrelated behavior;
- run the focused test, affected-module suite and the existing regression suite;
- verify previously working paths adjacent to the change;
- state what was not tested and why.

A test MUST NOT be deleted, weakened, skipped, or re-baselined merely to make a change pass. Any necessary test-contract change requires a written reason, an ADR or explicit acceptance where behavior changes, and independent review.

## 6. Feature and enhancement policy

A feature request is first a durable requirement, not an instruction to immediately generate another app or replace the current architecture. Product triage records the user problem, intended outcome, existing capability reuse, affected user journey, priority, dependencies, cost, risks, and acceptance criteria.

Accepted features are implemented as additive, coherent changes to Locco. Existing screens, workflows, data, preferences, permissions, integrations and offline paths remain working unless an explicitly approved product decision changes them. A feature must not be called complete when only a placeholder, mock, disconnected screen, dead button, hard-coded demo or unintegrated scaffold exists.

The requester may change priority or direction. That does not erase other accepted requirements. If work must be reordered, the displaced records remain open and their new status/reason is recorded.

## 7. Application, data and release continuity

Ordinary changes MUST preserve the installed application identity and user data. Android package ID, signing identity, persistent storage location, database identity, schema history, deep links and upgrade path are controlled compatibility surfaces.

Database changes require explicit versioned migrations, migration tests and a backup/restore or recovery strategy. Existing data must be preserved. Destructive reset, data deletion, package identity change, signing-key rotation or breaking migration requires an explicit user-approved ADR and a tested migration/recovery plan.

A release candidate must be tested as an upgrade from the previous supported build with representative existing data, not only as a fresh install. Version codes increase monotonically. Release records include source commit, version, APK hash, signing identity/version, test evidence, migration outcome and rollback route.

See [application continuity](docs/governance/application-continuity.md).

## 8. Change and acceptance gates

Every pull request MUST:
- identify its change class and linked `LOC-####` issue(s);
- state the baseline commit and files/modules changed;
- inventory existing behavior that must remain intact;
- confirm it extends/fixes the existing Locco application, or link the explicit user authorization for a separate product;
- identify affected ADRs, contracts, permissions, data, dependencies and offline behavior;
- provide regression and feature acceptance evidence;
- report actual build/test/device/artifact evidence and all gaps;
- include a safe rollback or migration plan.

The committed validator `scripts/check_change_contract.py` checks required PR sections and continuity assertions. `.github/workflows/change-governance.yml` runs it on pull requests. Repository rules MUST require the resulting `change-contract` check on `main`; until that rule is configured and independently observed, the governance gate is not considered platform-enforced.

A workflow file existing in GitHub is not evidence that it ran. A green build is not evidence of behavior, upgrade safety, physical-device correctness or release readiness.

## 9. Completion is evidence-based

A feedback record may move through intake, triage, accepted, planned, in progress, implemented-unverified, verified and released. "Implemented" is not "verified"; "verified" is not "released". The issue may be closed only when the exact acceptance criteria are demonstrated against the correct commit/build and the user-facing change is recorded.

QA must independently inspect the evidence. If a device is unavailable, say so. If only a unit test passed, say so. Never claim a produced APK, successful installation, data preservation, or user-visible fix without the corresponding artifact or observation.

## 10. Exceptions

An exception to this constitution requires:
- the exact rule being waived;
- why the rule cannot be met;
- the risk and affected feedback records;
- reversible alternatives considered;
- explicit user approval for product/data/security/identity changes;
- a time-bounded remediation plan and ADR when architecture or contracts change.

No agent may grant its own exception. Silence, urgency, token limits, a new feature idea, or a green CI run are not exceptions.

## 11. Required reading for every task

Start with this file, then `AGENTS.md`, `.agent/routing.md`, `PRINCIPLES.md`, the linked feedback issue, the relevant role/skills, product scope, affected ADRs and contracts. This order is mandatory. When a task spans modules, read every affected owner contract. A handoff without issue IDs, baseline SHA, evidence and unresolved risks is incomplete.
