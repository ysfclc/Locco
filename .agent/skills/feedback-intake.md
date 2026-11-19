# Skill: Feedback intake and preservation

**Required for every user-facing bug, regression, feature, UX or product-direction input.**

## Preconditions
- Read `GOVERNANCE.md` and `docs/governance/feedback-lifecycle.md`.
- Know the existing Locco repository and issue history.
- Keep original wording separate from interpretation.

## Procedure
1. Determine whether the message contains a product requirement, defect, correction, constraint or decision. Do not ignore concise or emotional feedback; extract the underlying report without sanitizing away meaning.
2. Search GitHub Issues for an exact or related existing record. Read the full issue and comments.
3. Reuse the canonical issue only when it represents the same underlying requirement. Otherwise create an issue in `ysfclc/Locco` and assign its permanent `LOC-####` ID.
4. Preserve the original user text, context, current expected/actual behavior, evidence and unknowns. Record interpretation as interpretation, not fact.
5. Classify the type and disposition independently. Set reasoned priority, related issues, current implementation area and acceptance criteria.
6. Link earlier reports and failed attempts. Never mark an item duplicate without linking the canonical record.
7. Return the issue link in the handoff. Keep it open until verified/released or explicitly deferred/rejected with rationale.

## Required issue fields
- source and original wording;
- problem/desired outcome;
- current behavior and what already exists;
- product/build/device context where relevant;
- classification, severity/priority and disposition;
- acceptance criteria;
- affected modules, dependencies and related feedback IDs;
- owner, next action and blockers.

## Failure conditions
Stop and record the blocker if the source cannot be preserved, issue creation fails, the product boundary is unclear, or the request conflicts with a prior explicit decision. Never proceed by silently dropping or rewriting the requirement.
