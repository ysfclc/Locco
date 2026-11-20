# Skill: Feedback intake and preservation

**Trigger:** Any user statement that may express a bug, regression, data-loss concern, feature, enhancement, UX/accessibility issue, product constraint, correction or decision. Short or emotional messages still qualify.

## Preconditions
Read GOVERNANCE.md and the feedback lifecycle. Search the Locco issue/PR history before creating a record. Preserve the user's original words separately from interpretation.

## Procedure
1. Decide whether the input is feedback, a decision, a question only, or unrelated conversation. Do not turn a question into an accepted feature without evidence of intent.
2. Copy the original wording verbatim where possible. Keep punctuation, uncertainty and constraints; do not rewrite it into a stronger claim.
3. Search exact phrases, affected behavior/screens/modules, previous failure signatures and linked issues. Read full matching issues/comments before choosing reuse vs new issue.
4. Reuse a canonical issue only for the same underlying requirement. Otherwise create one issue in ysfclc/Locco. Assign permanent LOC-ID from its issue number using the feedback-register workflow or manual prefix if automation is not active.
5. Separate: original wording; normalized problem; intended outcome; facts; interpretation; unknowns; actual/current behavior; and evidence. Never invent device/build/reproduction context.
6. Classify the item independently from disposition. A valid request can be deferred; a duplicate remains discoverable; rejection requires a rationale and decision source.
7. Define observable acceptance criteria or record what must be clarified before they can be written. Identify current capabilities, previous attempts, affected modules, related LOC IDs, owner, reviewer and next action.
8. Link duplicate/related issues without closing or deleting the original record. Preserve prior failed attempts and user corrections.
9. Return the canonical issue link and a faithful summary to the user. Do not ask the user to repeat context already present.
10. Keep the issue open while work is planned, in progress, implemented-unverified or blocked. Update status only when the evidence in the issue justifies the transition.

## Required issue content
Original source wording; outcome; current vs expected behavior; context/evidence/unknowns; classification and disposition; priority rationale; acceptance criteria; affected flows/modules; related LOC IDs; owner/reviewers; blockers and next action.

## Fail conditions
If issue creation/search fails, state the failure and preserve the input in the current handoff. Do not implement from a silently rewritten or untraceable requirement. Never expose tokens, signing keys or unnecessary sensitive data in the issue.
