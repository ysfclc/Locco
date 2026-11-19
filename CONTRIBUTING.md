# Contributing

## Authority
User-approved direction and accepted ADRs → PRINCIPLES.md and product scope → architecture/security contracts → agent roles/skills → implementation comments. Conflicts require an ADR.

## Phase workflow
Read AGENTS.md, routing, relevant ADRs and acceptance. Record base SHA and scope. Ingest archives only through phase-ingestion. Change contracts before implementations. Run skills and record evidence. Security-sensitive changes require security review; all changes require QA. Commit one coherent reversible slice and update docs.

## Done
Tests pass or blockers are explicit; no secrets/private data committed; contracts documented; offline/failure behavior specified; accessibility considered; Android changes produce APK + SHA-256. Distinguish built, tested, device-verified and released.