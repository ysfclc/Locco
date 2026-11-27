# Contributing to Locco

All contributors and coding agents are governed by [GOVERNANCE.md](GOVERNANCE.md). This is a continuing application, not a series of disposable builds.

## 1. Authority

Apply this order:
1. Explicit user direction, including explicit supersession of prior user decisions.
2. `GOVERNANCE.md` and `PRINCIPLES.md`.
3. Product vision/scope and accepted product decisions.
4. Accepted ADRs and architecture, data, FFI, security and platform contracts.
5. Linked GitHub feedback records and acceptance criteria.
6. Implementation comments and proposals.

A new request does not silently override old requirements. Conflicts are recorded and escalated.

## 2. Intake before implementation

Every product-related report from chat or GitHub MUST have a canonical issue in this repository with ID `LOC-####`. Preserve the original wording, classify the report, link evidence, set an explicit disposition and write observable acceptance criteria. Do not ask the user to repeat information already supplied. Start with the [feedback lifecycle](docs/governance/feedback-lifecycle.md).

## 3. Investigate the product as it exists

Before edits, read the relevant issue history, comments, previous PRs/commits, ADRs, current implementation and tests. Record the base SHA, app/build identity, affected modules, known failures and existing behaviors to preserve. Do not assume a feature is absent or a subsystem should be rebuilt because it is inconvenient to find.

## 4. Implement the existing application

- Use a focused branch in `ysfclc/Locco`.
- Extend or repair the existing application. Do not create a second app, replacement scaffold, new package ID or repository unless the user explicitly requests a separate product.
- Preserve unrelated work and existing behavior.
- Prefer the smallest safe root-cause correction or additive feature slice.
- Version and review any contract, schema, permission, dependency or security-boundary change.
- Never reset user data, wipe a database or require uninstall as a shortcut.

See [change control](docs/governance/change-control.md) and [application continuity](docs/governance/application-continuity.md).

## 5. Verification

Bugs: reproduce, add a failing regression test when possible, fix the root cause, rerun the affected and broader suites, and verify adjacent behavior.

Features: map every acceptance criterion to evidence; test the feature plus shared existing flows, errors, permissions, cancellation, offline and process restoration where relevant.

Android releases: prove package/signing continuity, monotonically increasing versionCode, in-place upgrade, data preservation and an actual APK SHA-256. Clean install is not upgrade evidence.

Report only commands actually run. Distinguish compiled, unit-tested, emulator-tested, physical-device-tested, upgrade-tested and released. List all unverified gaps.

## 6. Pull requests and done criteria

Every PR MUST complete the repository [PR template](.github/pull_request_template.md), include linked `LOC-####` records, baseline, change inventory, product-boundary assertion, impact analysis, regression evidence, data/upgrade analysis, verification results, gaps and rollback. It must pass the `change-contract` check. Review routing and CODEOWNERS apply.

Do not close feedback issues at implementation time. Close only when the recorded acceptance criteria have been independently verified and release requirements are satisfied. Update linked issues with PR/merge SHA, test results, artifact/hash, device/upgrade evidence and remaining work.

## 7. Review and exceptions

All changes need QA review; affected owners review their contracts. Security/privacy, signing, permissions, source access, FFI, migrations and dependency changes require their specialist review. No author self-approves a security-sensitive change.

A constitution exception requires a written reason, risk, alternatives, affected feedback IDs, reversibility and explicit user approval where product/data/security identity changes. Urgency is not approval.
