# Skill: SBOM and attestation

When: every release candidate and foundation artifact. Preconditions: artifact and source/workflow identity.

Procedure: generate CycloneDX SBOM for Rust/Android; record toolchain/SDK/NDK; hash APK/SBOM; attest provenance where supported; upload separately; verify subject digest equals APK digest.

Verify SBOM parses and attestation points to expected repo/ref/workflow.

Failure: incomplete dependency graph or digest mismatch. Mark untrusted and block release.