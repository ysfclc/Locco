# Locco repository and operating setup

Locco is the permanent product and control repository. All normal development happens directly in `ysfclc/Locco` through GitHub branches, issues, commits, pull requests and Actions. No ZIP handoff, separate control repository or `REPO_CREATE_TOKEN` is required.

## Mandatory operating authority

Before any work, read:
1. `GOVERNANCE.md`
2. `AGENTS.md` and `.agent/routing.md`
3. `PRINCIPLES.md` and the relevant agent role/skills
4. `docs/governance/feedback-lifecycle.md`, `change-control.md`, `application-continuity.md`
5. The complete source `LOC-####` issue, related issues, PRs, ADRs, contracts and tests.

A user report must be recorded in GitHub Issues before implementation. Every implementation PR links the issue and fills the continuity/evidence template. A new bug or feature must extend or repair the existing app, not create a replacement.

## Required GitHub repository controls

Committed workflow files can report a failed check, but GitHub only blocks merges when repository rules require that check. Configure a ruleset or branch protection for `main`:

- Require pull requests; disallow direct pushes and force-pushes.
- Require independent approval, including CODEOWNERS review for governed paths.
- Require the `change-contract` status check from the Change governance workflow.
- Require applicable Rust, Android and security checks before merge.
- Dismiss stale approvals when new commits change reviewed code.
- Restrict bypass actors; do not let ordinary automation bypass the gate.

After configuration, open a deliberately incomplete test PR and verify GitHub blocks it; then verify a fully compliant PR can satisfy the check. Record the ruleset and observed test in the foundation status ledger. Until both steps are complete, report branch protection as not configured/verified.

## Android release signing

For release signing only: create GitHub Environment `android-release`, require reviewers, and add `ANDROID_RELEASE_KEYSTORE_BASE64`, `ANDROID_RELEASE_STORE_PASSWORD`, `ANDROID_RELEASE_KEY_ALIAS`, and `ANDROID_RELEASE_KEY_PASSWORD` as environment secrets. Never commit signing material. Debug builds do not need release secrets.

## Local toolchain

See [local development](local-development.md). Use the pinned Rust/Gradle/JDK/Android SDK versions. Run `./scripts/verify-foundation.sh`, `./scripts/build-rust-android.sh` and `./scripts/build-android-debug.sh`; report exactly which commands ran.

## Optional external ZIP intake

Only use `incoming/phase-*.zip` and `.github/workflows/pipeline.yml` for a contribution genuinely arriving as an external archive that cannot be delivered as Git commits or a PR. This is exceptional. It is not the development path for agents or internal phases. Follow the hostile-input validator; never execute archive scripts or overwrite protected authority/security/CI files.
