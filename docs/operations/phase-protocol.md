# Direct GitHub phase protocol

GitHub is the canonical development interface for Locco. Work directly against this repository; ZIP files are not required for normal phases.

## Standard phase

1. Read `AGENTS.md`, `.agent/routing.md`, the relevant agent role and skills, `PRINCIPLES.md`, accepted ADRs, product scope and security doctrine.
2. Record the goal, base commit SHA, accountable owner, reviewers, scope, acceptance criteria and rollback approach in the phase issue or pull request.
3. Create a focused branch from the current trusted base. Keep the change set small and within the assigned agent's authority.
4. Implement directly in the repository. Preserve existing contracts and update ADRs/docs when a decision or contract changes.
5. Run the relevant tests and CI. Never claim a check passed unless its result is observable.
6. Obtain independent review from the required owners, including security review for trust-boundary, secrets, signing or dependency changes.
7. Merge only after required checks and reviews pass. Record the merge commit, changed paths, test evidence, known gaps and rollback in the phase record/status ledger.

## GitHub-native change rules

- Locco is the permanent source of truth; do not create sibling product repositories for phases.
- Prefer direct GitHub branch, commit, pull-request, review and Actions workflows. Do not package repository work into ZIPs merely to move it between agents or phases.
- Never commit credentials, signing keys, model weights, private datasets, backups, build outputs or generated APKs.
- Do not bypass branch protection, required reviews, CI gates or release environments.
- No self-approval for security-sensitive work. Stop on unclear source permission, secret exposure, protected-path changes, authority widening or unprovable acceptance.
- Each handoff returns the goal, base/head/merge SHAs, changed paths, owner/reviewers, commands and results, evidence, gaps and rollback.

## Optional external ZIP intake

The legacy ZIP phase pipeline has been retired. The validator may be retained as a defensive library for tests or future review, but there is no active archive-ingestion workflow. If a genuine external archive is ever unavoidable, obtain explicit approval and design a new isolated intake workflow with hostile-input limits before processing it.

Never use ZIP intake for ordinary Locco development. Do not create or invoke a replacement archive workflow without explicit approval and security review. Never execute archive scripts or let an archive replace protected workflows, `.agent`, security docs, principles, ADRs or validators.
