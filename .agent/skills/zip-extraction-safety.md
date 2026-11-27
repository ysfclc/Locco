# Skill: Hostile ZIP extraction safety

**Trigger:** Any archive enters a repository, CI runner or local ingestion workflow. For Locco, direct GitHub branches/commits are the normal path; use ZIP intake only for a genuinely external contribution explicitly supplied for review.

## Validation limits
Define conservative maximums for archive bytes, entry count, total expanded bytes, per-file bytes, compression ratio and path length before parsing. Enforce limits on actual streamed bytes, not trusted only from ZIP metadata.

## Procedure
1. Record archive SHA-256, source, byte size, base commit and validator version.
2. Inspect central-directory entries without extracting. Reject encrypted/unsupported entries, duplicate raw or normalized paths and inconsistent metadata.
3. Normalize separators and Unicode/path representation. Reject absolute paths, drive/UNC prefixes, parent traversal, empty/dot paths, control characters and paths outside the allowed root.
4. Reject symbolic links, hard links, devices, sockets and all special files. Extract only regular files/directories.
5. Reject VCS metadata, credential files, model weights, binaries and protected policy/workflow files unless explicitly authorized and independently reviewed.
6. Enforce decompressed size, entry count, per-file size and compression ratio while streaming. Abort immediately on limit violation.
7. Extract into a fresh private temporary directory. Resolve destination paths and verify containment before each exclusive-create write. Never overwrite the working tree.
8. Normalize exactly one repository root; reject ambiguous or nested competing roots.
9. Emit a manifest of relative path, type, actual size and SHA-256. Compare paths/content with the trusted base and highlight deletions/protected-path edits.
10. Create only a review branch/PR. Do not execute archive scripts/binaries, auto-merge, or treat validation as code correctness.
11. Keep identical archive/base pairs idempotent and retain provenance without storing private archive content in logs.

## Required tests
Traversal, absolute path, drive path, Unicode normalization collision, duplicate path, symlink escape, special file, entry bomb, high compression ratio, oversized file, truncated/corrupt archive and ambiguous root.

## Fail closed
Any path escape/collision, resource-limit breach, unsupported file type, secret, protected-control overwrite or parser uncertainty rejects the archive. Preserve a sanitized rejection report and do not extract partial contents into the repository.
