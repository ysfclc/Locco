# Skill: SBOM and build attestation

**Trigger:** Every release candidate and any foundation/native artifact intended for distribution.

## Inputs required
Exact repository/ref/commit, build workflow/run, final artifact path/hash, dependency manifests/lockfiles and supported SBOM/attestation tooling.

## Procedure
1. Build the artifact from a clean checkout of the recorded commit. Record toolchain, SDK/NDK, build variant and workflow identity.
2. Generate a CycloneDX or other accepted SBOM covering shipped Rust, Gradle, native and bundled runtime components. Explain tool coverage gaps; a repository scan may omit dynamically fetched or embedded assets.
3. Parse the SBOM with a validator. Check component names, versions, package URLs, licenses, dependency relationships and completeness against lockfiles and APK contents.
4. Hash the final signed artifact and SBOM. Hash after signing and any post-processing; the unsigned APK hash is not the release hash.
5. Generate build provenance/attestation using the approved workflow identity. Subject digest must equal the final artifact digest.
6. Verify attestation signature/issuer/subject and source repository, ref, commit, workflow and builder identity. Do not accept a provenance file merely because it exists.
7. Compare actual APK libraries/assets to SBOM contents. Explain native/runtime binaries the tool did not detect.
8. Upload APK, SBOM, checksums and attestation as separately identifiable artifacts with retention policy. Do not include secrets or private data.
9. Link the evidence from release record and all affected LOC issues.

## Required evidence
APK SHA-256; SBOM SHA-256 and parse result; dependency coverage/limitations; source commit/ref; workflow run; provenance subject digest and verification; artifact links.

## Fail conditions
Malformed/incomplete SBOM with unexplained omissions, digest mismatch, wrong source/workflow identity, unverifiable attestation or private material in artifacts blocks release. State tooling limitations rather than claiming complete coverage.
