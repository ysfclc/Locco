# Contributing

Authority: user-approved direction and accepted ADRs → PRINCIPLES.md/scope → architecture/security contracts → agent roles/skills → code comments. Conflicts require an ADR.

Phase workflow: read AGENTS.md/routing/ADRs; record base SHA and scope; ingest archives only via phase-ingestion; change contracts before implementations; run skills and record evidence; security review for trust-boundary changes; independent QA; commit one coherent slice; update docs.

Done: tests pass or blockers explicit; no secrets/private data committed; contracts documented; offline/failure behavior specified; accessibility considered; Android changes produce APK + SHA-256. Distinguish built, tested, device-verified and released.