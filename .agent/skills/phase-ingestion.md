# Skill: External phase archive ingestion

**Trigger:** Only when a genuinely external contributor supplies a ZIP that cannot be delivered as a Git branch/commit/PR. This is not the normal Locco workflow; use direct GitHub branches for internal phases.

## Preconditions
- User/maintainer explicitly requests archive intake.
- Clean checkout and trusted base SHA are recorded.
- The validator is from the trusted base, not the archive.
- A private temporary extraction directory is available; secrets are not present in the extraction environment.

## Procedure
1. Record archive name, byte size, source, uploader, receipt time and SHA-256 before parsing.
2. Inspect the central directory and validate entry count, expanded size, per-file size, compression ratio, paths, normalized duplicates, file types and root structure.
3. Reject absolute paths, drive prefixes, parent traversal, symlinks, hardlinks, special files, duplicate normalized names, VCS metadata, secrets, protected workflows/policies and unapproved binary artifacts.
4. Run the validator in dry-run mode. Never execute scripts, build hooks or binaries from the archive.
5. Extract only into a fresh temporary directory using exclusive file creation and path-containment checks. Never extract over the working tree.
6. Normalize one repository root and generate a manifest containing relative path, size and SHA-256 for every accepted file.
7. Compare the normalized proposal against the exact base. Report added/changed/deleted paths; deletions and protected-path changes require explicit review.
8. Create a focused review branch/PR from the recorded base. Include archive hash, manifest, rejected entries and provenance. Never auto-merge.
9. Build/tests must use trusted repository workflows. A passing archive validator proves safe intake only—not product correctness.
10. Repeating the same archive/base pair must be idempotent; detect an existing proposal by both hashes.

## Required evidence
Archive/base hashes; validator version; entry/size limits; accepted/rejected manifest; normalized diff; branch/PR link; build/test outcomes and known gaps.

## Fail conditions
Traversal, zip bomb, path collision, symlink escape, secret, VCS metadata, ambiguous root, protected control-file overwrite or parser uncertainty. Fail closed and preserve a sanitized rejection report.
