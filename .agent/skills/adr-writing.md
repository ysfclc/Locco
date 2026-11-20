# Skill: ADR writing

**Trigger:** A decision changes or constrains product architecture, module ownership, public APIs, persistence, security/privacy, platform support, dependencies, build/release design or a hard-to-reverse implementation choice.

## Inputs required
- Linked LOC issue and original user wording.
- Current source/contracts and prior accepted/proposed ADRs.
- Constraints, current failure/evidence and affected modules.
- At least two plausible options for consequential choices.

## Procedure
1. Confirm an ADR is warranted. Do not write one for a routine local refactor with no durable contract impact.
2. Search the ADR index and history. Extend an existing decision or propose a superseding ADR; never edit history to make an old decision disappear.
3. Give the ADR a stable sequential ID and status: Proposed, Accepted, Superseded or Rejected. A PR draft is not an accepted decision.
4. Record the problem, user outcome, scope and explicit non-goals. Link affected LOC issues.
5. Describe the current system with real file, symbol, schema and workflow references. Separate verified facts from assumptions.
6. Compare alternatives against constraints: compatibility, security/privacy, user data, offline operation, dependency risk, implementation/maintenance cost, failure modes and reversibility.
7. State one decision precisely. Explain what remains unchanged and what is intentionally not being decided.
8. Define consequences, rejected trade-offs, migration/versioning, rollout, observability, rollback or forward recovery.
9. Add measurable validation: contract tests, migration tests, threat review, platform parity or build evidence as appropriate.
10. Update the ADR index and affected contracts/docs in the same change. Search for contradictory text and update it without rewriting unrelated history.

## Required artifact
A reviewer must be able to identify: decision owner, date, status, source issue, context, decision, alternatives, rationale, consequences, tests, migration/rollback and superseded decision (if any).

## Fail closed
Reject the ADR when it has no evidence, meaningful alternative, explicit boundary, compatibility plan or accountable decision source. Keep the ADR Proposed until its acceptance gate is met. Do not treat a draft PR, unchecked box or passing documentation validator as acceptance of the underlying architecture.
