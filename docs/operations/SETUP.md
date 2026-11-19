# Locco setup

Locco is the permanent product and control repository. All normal development happens directly in this repository through GitHub branches, commits, pull requests and Actions. No ZIP handoff, separate control repository or `REPO_CREATE_TOKEN` is required.

## Start a phase

1. Read `AGENTS.md`, `.agent/routing.md`, the relevant agent and skills, `PRINCIPLES.md`, accepted ADRs, product scope and security doctrine.
2. Open a phase issue or PR with goal, base SHA, owner, reviewers, scope, acceptance criteria and rollback.
3. Create a focused branch and make changes directly in the repository.
4. Run the relevant local checks and GitHub Actions; wait for actual results.
5. Obtain independent review and merge only after required checks pass.
6. Record commit SHAs, changed paths, test evidence, gaps and rollback in the phase record/status ledger.

## Android release signing

For release signing only: create GitHub Environment `android-release`, require reviewers, and add `ANDROID_RELEASE_KEYSTORE_BASE64`, `ANDROID_RELEASE_STORE_PASSWORD`, `ANDROID_RELEASE_KEY_ALIAS`, and `ANDROID_RELEASE_KEY_PASSWORD` as environment secrets. Do not create them for debug builds. Never commit signing material.

## Repository controls

Enable branch protection or rulesets for `main`, require the relevant CI checks and independent review, enforce CODEOWNERS where supported, and configure required reviewers on the `android-release` environment.

## Optional external ZIP intake

Only use `incoming/phase-*.zip` and `.github/workflows/pipeline.yml` when a contribution genuinely arrives as an external archive and cannot be delivered as a Git commit or PR. This is an exception, not the normal development path. Follow `phase-protocol.md`; never upload ZIPs simply to move work between agents or phases.
