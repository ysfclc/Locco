# Skill: ZIP extraction safety

**Trigger:** Only when the user explicitly authorizes a genuinely external archive contribution. ZIP files are not part of normal internal Locco development or phase delivery; direct GitHub branches and PRs are canonical under ADR-0016.

## Preconditions
- A linked LOC issue records the external source, authorization, expected outcome and review owner.
- The archive is treated as hostile input, never as trusted code or repository authority.
- Extraction occurs in a fresh isolated temporary directory with no secrets, credentials or write access to the repository checkout.
- A maintained parser or Python standard-library implementation is reviewed for the supported archive format.

## Procedure
1. Record the archive filename, byte size, source, receipt time and SHA-256 before inspection.
2. Inspect the central directory before extracting. Enforce strict entry-count, total expanded-byte, per-file-size and compression-ratio limits.
3. Reject absolute paths, drive prefixes, parent traversal, backslashes, duplicate normalized paths, symlinks, hardlinks, special files, encrypted entries and unsupported compression methods.
4. Reject VCS metadata, protected workflow/agent/security/ADR files and forbidden binaries, keys, APKs, model weights or private datasets.
5. Normalize exactly one expected root layout. Resolve every destination and prove it remains inside the fresh extraction root.
6. Create files exclusively; never overwrite an existing file or follow a link. Do not preserve executable permissions from the archive.
7. Emit a manifest containing normalized path, byte size and SHA-256 for every accepted file.
8. Inspect the normalized diff against the exact repository base. Convert it into an ordinary Git branch and PR with LOC traceability, review and standard CI.
9. Never execute scripts, workflow definitions, build hooks or binaries from the archive during validation.
10. Delete the temporary extraction after the review artifact/manifest is safely recorded.

## Required evidence
Record explicit user authorization, source archive hash, parser/tool version, configured limits, accepted/rejected entry counts, normalized manifest, base commit, resulting PR and reviewer decision. Preserve rejection diagnostics without logging secrets or archive contents.

## Fail closed
Reject the entire archive on any malformed path, duplicate collision, link/special file, limit violation, parser ambiguity, protected-path modification or manifest mismatch. Do not partially extract and continue. Do not create a new repository, auto-merge, or bypass normal Locco PR/CI/review gates.
