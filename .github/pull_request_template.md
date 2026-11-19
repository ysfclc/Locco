## Change classification
<!-- Choose one: Bug fix / Regression / Feature / Enhancement / Refactor / Documentation-governance / Infrastructure / Release. State the user outcome. -->

## User feedback records
<!-- REQUIRED: list every affected durable ID and matching GitHub issue, e.g. LOC-0002 — #2. Do not omit earlier linked bugs/features. -->

## Existing behaviour inventory
<!-- What current code, screens, flows, data, settings, permissions, adapters and tests were inspected? What must remain working? Include related open issues and previous attempts. -->

## Product boundary and app continuity
<!-- Keep the first assertion checked unless the user explicitly ordered a separate product. -->
- [x] Existing Locco application is extended/repaired; no replacement application is created.
<!-- If and only if the user explicitly instructed a separate product, replace the line above with the statement below and cite the actual user-authorization issue. -->
<!-- - [x] Separate product explicitly instructed by user; authorization is linked below. -->
<!-- State package ID, signing, database and stored-data impacts. -->

## Impact analysis
<!-- List changed modules/files, affected ADRs/contracts, Rust/FFI/JNI, Android lifecycle/permissions, local AI/context, source adapters, security/privacy, dependencies, offline behavior and compatibility. Write "none" with a reason where applicable. -->

## Regression coverage
<!-- Bug: reproduce and add a failing regression test first/with fix where possible. Feature: test new behavior and adjacent existing flows. State Automated / Manual / Not applicable with reason / Blocked. Include exact tests and outcomes. -->

## Data, migration and upgrade safety
<!-- State schema/storage changes, migration versions, backup/restore, in-place upgrade test and pre-existing data checks. If no data impact, explain what was inspected. -->

## Verification evidence
<!-- Exact commands, environment/tool versions, commit SHA, PASS/FAIL/NOT RUN/BLOCKED result, artifact name and SHA-256, emulator/device evidence. Do not report unexecuted checks as passed. -->

## Known gaps and unverified claims
<!-- Explicitly state None or list each gap, consequence, owner and follow-up issue. Distinguish compiled / unit-tested / emulator-tested / physical-device-tested / upgrade-tested / released. -->

## Rollback
<!-- Exact revert/forward-recovery steps. State whether data migration is reversible and what happens to user data. -->

## Review checklist
- [ ] Linked every affected LOC record and preserved unresolved feedback.
- [ ] Inspected current implementation, prior issues, PRs, commits, ADRs and tests.
- [ ] Existing Locco application continues; no replacement/reset without explicit user authorization.
- [ ] Added/updated regression and acceptance tests; did not weaken/delete tests to make CI pass.
- [ ] Reviewed permissions, privacy, data, migration, offline and compatibility effects.
- [ ] CI checks ran and their actual results are recorded.
- [ ] QA and affected specialist owners reviewed the change.
- [ ] Feedback issues updated with merge/release evidence and remaining work.
