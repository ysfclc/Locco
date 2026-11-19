# Locco setup

Locco already exists as the permanent product repository. No separate control repo or `REPO_CREATE_TOKEN` is required; phase ZIPs create reviewed branches/PRs here, never sibling repositories.

For release signing only: create GitHub Environment `android-release`, require reviewers, and add `ANDROID_RELEASE_KEYSTORE_BASE64`, `ANDROID_RELEASE_STORE_PASSWORD`, `ANDROID_RELEASE_KEY_ALIAS`, `ANDROID_RELEASE_KEY_PASSWORD` as environment secrets. Do not create them for debug builds.

First phase: create `incoming/phase-01-local-kernel.zip`, follow phase-protocol.md, push it on a branch and open a PR. Pipeline validates, normalizes, hashes and proposes a review branch; it does not run archive scripts or auto-merge. Trusted repository CI builds Rust/Android and uploads artifacts. Enable branch protection, CODEOWNERS enforcement and environment reviewers in GitHub settings.