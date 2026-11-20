# Skill: Dependency review and pinning

**Trigger:** Adding, updating, replacing or removing Cargo, Gradle, native, GitHub Action, model-runtime or build-tool dependencies.

## Required justification
State the feature/problem that needs the dependency, why existing code or platform APIs are insufficient, and the smallest acceptable alternative. A transitive dependency is still part of the trust surface.

## Review procedure
1. Identify direct and transitive packages, exact versions, source registries, licenses, maintainers, release cadence, open advisories, native binaries and permissions/network behavior.
2. Prefer maintained upstreams and the smallest dependency surface. Inspect install/build scripts and platform-specific binaries before adoption.
3. Cargo: commit Cargo.lock for applications/workspaces; use locked CI builds; review cargo tree changes. Run available audit/deny checks and record when tooling is unavailable.
4. Gradle: avoid dynamic versions and unpinned snapshots; use dependency locking and verification metadata where configured. Inspect resolved dependency graph, repositories and plugin portal sources.
5. GitHub Actions: pin third-party actions to a reviewed full commit SHA, with a human-readable version comment. Do not pin only a mutable tag.
6. Native/model assets: record origin, hash, license, ABI, update mechanism and whether they execute code or process private data.
7. Assess license compatibility, export-control/privacy implications, CVEs, transitive footprint, binary size, offline operation and rollback.
8. Run lockfile/verification generation using the repository's toolchain; review the diff rather than accepting generated changes blindly.
9. Update SBOM/ADR/security docs when the dependency changes a trust boundary, shipped runtime or license obligation.
10. Test reproducible clean build and relevant runtime paths without network access where offline operation is promised.

## Required evidence
Reason for use; exact direct/transitive versions; source and license; advisory result; lock/verification changes; binary/permission impact; tests; rollback route; affected LOC ID.

## Fail conditions
Reject unreviewed native binaries, mutable Action references, dynamic production versions, incompatible licenses, abandoned high-risk upstreams, unexplained repository additions or a dependency that breaks the zero-budget/offline baseline without explicit approval.
