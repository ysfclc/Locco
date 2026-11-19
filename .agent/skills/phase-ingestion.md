# Skill: Phase ingestion

When: phase ZIP arrives in incoming/. Preconditions: clean checkout, base SHA and archive hash known, validator available, no secrets in env.

Procedure: validate dry-run; reject unsafe/protected paths; extract only to fresh temp; normalize one root; create manifest/provenance; compare to base; create review branch/PR; never auto-merge or execute archive scripts.

Verify archive hash, manifest, accepted/rejected entries and base SHA; identical archive/base is idempotent.

Failure: traversal, zip bomb, symlink, duplicate path, secret, ambiguous root or protected overwrite. Fail closed.