# ADR-0006: ZIP ingestion creates reviewed branches

**Status:** Superseded for internal delivery by ADR-0016 · 2026-11-20

## Historical decision

The original decision treated ZIP archives as proposed patches rather than authority. It required isolated validation/extraction, path and file-type restrictions, secret screening, VCS metadata removal and reviewed branches/PRs. Auto-merge, archive-script execution and creating a new repository per archive were prohibited.

## Current applicability

ZIP intake is no longer the normal internal Locco development or phase-delivery workflow. Direct GitHub branches, commits, PRs and Actions are canonical under ADR-0016.

The historical archive-safety controls remain relevant only if the user explicitly authorizes a genuinely external archive contribution. Such a contribution must be converted into a normal reviewed PR and cannot bypass the existing issue, review, CI, security or release gates.
