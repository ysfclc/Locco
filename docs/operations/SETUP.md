# Locco repository and operating setup

Locco is the permanent product and control repository. All normal development happens directly in `ysfclc/Locco` through GitHub branches, issues, commits, pull requests and Actions. No ZIP handoff, separate control repository or `REPO_CREATE_TOKEN` is required.

## Mandatory operating authority

Before any work, read:
1. `GOVERNANCE.md`
2. `AGENTS.md` and `.agent/routing.md`
3. `PRINCIPLES.md` and the relevant agent role/skills
4. `docs/governance/feedback-lifecycle.md`, `change-control.md`, `application-continuity.md`
5. The complete source `LOC-####` issue, related issues, PRs, ADRs, contracts and tests.

A user report must be recorded in GitHub Issues before implementation. Once `.github/workflows/feedback-register.yml` is merged and enabled, new issue titles are automatically prefixed with their permanent `LOC-####` ID; until then, the intake owner applies the prefix manually. Every implementation PR links the issue and fills the continuity/evidence template. A new bug or feature must extend or repair the existing app, not create a replacement.

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

The current protected release workflow reads exactly two secrets from the `android-release` environment: `LOCCO_KEYSTORE_BASE64` and `LOCCO_KEYSTORE_PASSWORD`. These names are already used by the successful v0.2.2 release workflow; do not rename them or create aliases. The workflow decodes the single-key keystore and uses the same password for keystore and key access. Never commit signing material. Debug builds do not need release secrets.

To publish without a browser session, open the repository's **Actions → Protected Android release → Run workflow**, select branch `main`, and enter the semantic tag (for example `v0.3.0`). The workflow creates the tag/release if needed, verifies package/version/signing identity, and uploads the APK, checksum, signing certificate, metadata and SBOM. A tag push also triggers the same release workflow.

## Local toolchain

See [local development](local-development.md). Use the pinned Rust/Gradle/JDK/Android SDK versions. Run `./scripts/verify-foundation.sh`, `./scripts/build-rust-android.sh` and `./scripts/build-android-debug.sh`; report exactly which commands ran.

## Optional external ZIP intake

External ZIP intake is currently disabled: the legacy `.github/workflows/pipeline.yml` has been retired and must not be referenced as an active workflow. Normal work must arrive as Git commits and pull requests. If a genuine external archive is ever unavoidable, obtain explicit approval and add a separately reviewed, security-tested intake workflow before accepting it.
