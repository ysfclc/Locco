# Skill: ZIP extraction safety

When: any archive enters repo/CI. Preconditions: Python stdlib or reviewed extractor; isolated temp dir.

Procedure: inspect central directory; cap entries/expanded bytes/per-file size/ratio; reject absolute/drive/parent paths, duplicate normalized paths, symlinks, hardlinks and special files; reject forbidden extensions/VCS metadata; normalize one root; resolve target under extraction root; exclusive-create; emit path/size/SHA-256 manifest.

Verify traversal, absolute path, symlink escape, duplicate, entry bomb and zip bomb fixtures fail.

Failure: metadata lies/parser bugs; use conservative limits and maintained parser.