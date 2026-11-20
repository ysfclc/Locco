# Locco setup

Locco is developed directly in its permanent GitHub repository. Use focused branches, commits, pull requests and GitHub Actions. Do not create phase ZIPs or use archive upload as an internal handoff.

## Foundation gate

Phase 0 must be formally accepted under LOC-0003 before Phase 1 implementation begins. Until then, continue only foundation, architecture-assurance, reproducible-build, security and verification work. Do not create a Phase 1 implementation branch early.

## Standard delivery path

1. Create/update the permanent LOC issue and link the relevant LOC-R requirements.
2. Branch from the current reviewed base commit.
3. Implement one coherent outcome in the existing Locco app/repository.
4. Open a PR with the complete evidence template and keep it draft until ready.
5. Run the applicable governance, Rust, Android and security/policy workflows.
6. Review the exact commit, test results and artifact hashes.
7. Merge only after required checks and independent approvals are enforced.
8. Update the LOC issue, roadmap and foundation/assurance ledger.

## Release signing

For release signing only: create GitHub Environment android-release, require reviewers, and add ANDROID_RELEASE_KEYSTORE_BASE64, ANDROID_RELEASE_STORE_PASSWORD, ANDROID_RELEASE_KEY_ALIAS, ANDROID_RELEASE_KEY_PASSWORD as environment secrets. Do not create them for debug builds.

## External archive exception

ZIPs are not part of normal internal development. If the user explicitly authorizes an external archive contribution, it must be handled as untrusted input under ADR-0006 and the archive-safety skill, then converted into a normal reviewed Git branch/PR. Never execute archive scripts or auto-merge extracted content.
