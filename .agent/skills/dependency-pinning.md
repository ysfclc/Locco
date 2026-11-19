# Skill: Dependency pinning

When: adding/updating Cargo, Gradle, Action or native dependency. Preconditions: demonstrated need.

Procedure: record exact version/license/maintenance/transitive footprint; pin Actions full SHA; commit Cargo.lock; enable Gradle verification; run cargo-deny/audit and dependency review; update SBOM/ADR if trust boundary changes.

Verify locked repeatable build, license/advisory review, no dynamic versions.

Failure: abandoned upstream, compromised release, incompatible license or unreviewed native binary. Reject/isolate.