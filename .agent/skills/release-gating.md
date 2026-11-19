# Skill: Release gating

When: phase acceptance, APK release or protected contract change. Preconditions: criteria/test matrix committed.

Procedure: clean base/accepted ADRs; unit/integration/static/security/build checks; three ABIs and APK contents; permissions/dependencies/SBOM/provenance; QA/security sign-off; protected environment for release; record hash/tool versions/limits.

Verify every gate passed with evidence or explicitly blocked; skipped is not pass.

Failure: skipped tests, stale artifact, wrong key or missing approval. Block release.