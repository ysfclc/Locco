# Phase ZIP protocol

Name: `incoming/phase-<NN>-<short-name>.zip`. One top-level folder or archive-root files. Never include .git, credentials, model weights, APKs, keys, private datasets or backups.

Every ZIP is hostile. Inspect central directory; cap entries, expanded bytes, per-file size and compression ratio; reject absolute/parent paths, drive prefixes, duplicate normalized paths, symlinks, hardlinks and special files; strip VCS metadata; normalize one root; extract only to fresh temp dir; emit path/size/SHA-256 manifest.

Locco is the single permanent repository. ZIPs create reviewed branches/PRs here, not sibling repos. They cannot replace protected workflows, .agent, security docs, principles, ADRs or validator. Never auto-merge or execute archive scripts. Identical archive/base pairs are idempotent.

Provenance includes archive hash/name, run, source ref, actor, UTC timestamp, phase ID and manifest. Never log archive content or secrets.