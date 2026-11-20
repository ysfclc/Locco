# Locco setup

Locco already exists as the permanent product repository. No separate control repo or `REPO_CREATE_TOKEN` is required; phase ZIPs create reviewed branches/PRs here, never sibling repositories.

## Foundation gate

Phase 0 must be formally accepted under LOC-0003 before the Phase 1 Local Kernel archive is ingested. Until then, continue only foundation, architecture-assurance, reproducible-build, security and verification work. Do not create a Phase 1 implementation branch early.

## Release signing

For release signing only: create GitHub Environment `android-release`, require reviewers, and add `ANDROID_RELEASE_KEYSTORE_BASE64`, `ANDROID_RELEASE_STORE_PASSWORD`, `ANDROID_RELEASE_KEY_ALIAS`, `ANDROID_RELEASE_KEY_PASSWORD` as environment secrets. Do not create them for debug builds.

## Phase ingestion

After Phase 0 acceptance, create `incoming/phase-01-local-kernel.zip`, follow `phase-protocol.md`, push it on a branch and open a PR. The pipeline validates, normalizes, hashes and proposes a review branch; it does not run archive scripts or auto-merge. Trusted repository CI builds Rust/Android and uploads artifacts. Enable branch protection, CODEOWNERS enforcement and environment reviewers in GitHub settings.
