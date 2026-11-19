# Feedback lifecycle and product memory

**Normative companion to [GOVERNANCE.md](../../GOVERNANCE.md).** This procedure applies to bugs, regressions, crashes, data loss, UX friction, feature requests, new use cases, performance problems, security reports, product constraints and corrections to previous assistant work.

## 1. One canonical record

The canonical record is a GitHub Issue in `ysfclc/Locco`. Its durable ID is `LOC-####`, where the digits are the zero-padded GitHub issue number. Never recycle an ID. The title should name the affected area and outcome, not merely "bug" or "feature".

A user can report something in chat without opening GitHub. The person or agent receiving the report MUST create the issue as part of intake, link it in the response, and preserve:
- the original wording (verbatim where possible);
- what the user expected and what actually happened, without invented details;
- relevant product/build/version/device/OS context;
- screenshots, logs, files or reproduction steps, after removing credentials and private data;
- uncertainty, missing information and the receiver's interpretation in separate fields.

Do not ask the user to retype something already present in the conversation. Do not require a polished bug report before recording it. A brief "this is broken" message is still an intake record; reproduction details can be added later.

If a user submits several independent issues in one message, create linked records or one parent with separately numbered children. Do not merge unrelated concerns into one vague ticket. If two reports describe the same underlying requirement, link them as related/duplicate but preserve the original reports.

## 2. Classification is not disposition

Assign one primary class:
- **Bug** — current behavior violates an existing contract or expected behavior.
- **Regression** — behavior that previously worked has stopped working.
- **Feature** — new capability or workflow requested.
- **Enhancement** — improvement to an existing capability.
- **UX/accessibility** — interaction, feedback, navigation or accessibility issue.
- **Performance/reliability** — latency, memory, battery, crash or stability concern.
- **Security/privacy** — trust boundary, permission, data exposure or abuse concern.
- **Product constraint/decision** — a durable instruction or product boundary.
- **Architecture/debt** — structural issue with future correctness or change cost.

Separately assign a disposition: `NEW`, `TRIAGE`, `ACCEPTED`, `PLANNED`, `IN_PROGRESS`, `IMPLEMENTED_UNVERIFIED`, `VERIFIED`, `RELEASED`, `DEFERRED`, `REJECTED`, `DUPLICATE` or `BLOCKED`. GitHub labels/status fields should mirror these values; the issue body remains authoritative if labels are unavailable.

Never use "out of scope" as a shortcut for rejecting feedback. A disposition requires a written reason. Deferred work must name the condition or date/event that will cause reconsideration. Rejected work retains the user's request and records why it will not be done. Duplicate records point to the canonical issue and remain searchable.

## 3. Triage record

Triage must answer:
1. What exact user problem or requirement is being recorded?
2. What is the current expected behavior or desired outcome?
3. Is the issue reproducible? What evidence proves it?
4. Which existing Locco module, screen, contract, data entity, permission or workflow is involved?
5. Which previous feedback records, commits, PRs and tests are related?
6. Is it a defect, regression, new capability, constraint or a combination?
7. What is the severity/priority and why?
8. What is the smallest safe resolution that preserves the rest of the product?
9. What acceptance criteria and regression tests will prove resolution?
10. What dependencies, migration, privacy, security, offline and device constraints apply?

Priority must be reasoned, not guessed from the newest message. Data loss, security/privacy exposure, crashes and loss of a core working flow outrank cosmetic novelty. The user may explicitly reprioritize.

## 4. Acceptance criteria and status transitions

Each accepted issue must define observable acceptance criteria before implementation. Criteria should describe externally testable behavior, not merely code changes.

Allowed transitions:

`NEW → TRIAGE → ACCEPTED → PLANNED → IN_PROGRESS → IMPLEMENTED_UNVERIFIED → VERIFIED → RELEASED`

Alternative terminal/holding states:
- `DEFERRED`: retained with reason and revisit trigger.
- `REJECTED`: retained with rationale and decision source.
- `DUPLICATE`: retained with canonical issue link.
- `BLOCKED`: retained with the missing dependency/evidence and owner.

A record can return to `IN_PROGRESS` or `BLOCKED` if acceptance fails or a regression is found. Do not skip `VERIFIED` because CI is green. Do not mark `RELEASED` before the relevant build/artifact is identified. A later user confirmation may be useful, but an agent cannot pretend to have received it.

## 5. Work linkage

Every implementation PR must reference all affected `LOC-####` records and GitHub issue numbers. If one PR addresses multiple issues, each issue must identify its relevant files, commits and acceptance evidence. If one issue spans multiple PRs, the issue stays open until all required slices are verified and released.

The issue must be updated with:
- implementation PR and merge commit;
- tests added/changed and actual outcomes;
- artifact name/version/SHA-256 when produced;
- device/emulator and upgrade evidence where required;
- unresolved gaps, deferred criteria and rollback.

The PR must not close an issue automatically before release-level criteria are satisfied. Prefer `Refs #N` during implementation and explicit closure only at the final verified/released stage.

## 6. No feedback loss

No work item may disappear because:
- a new feature was requested;
- a new chat began;
- an agent/session/model changed;
- the roadmap phase changed;
- an APK was rebuilt;
- a bug was difficult to reproduce;
- an issue became old;
- a refactor changed module names;
- a task was split among agents.

When a requirement is superseded, link the replacing decision and retain the original issue. When scope is reduced, record which acceptance criteria remain unfulfilled. When implementation is abandoned, return the issue to a visible holding state; never silently close it.

## 7. Existing backlog is read before new work

At task start, search open and recently closed issues for the same area, behavior, screen, data flow and failure signature. Read related PRs and commits. Treat an earlier bug report as active until its issue demonstrates resolution. Do not rely only on a short recent-context summary or the last chat turn.

Before release, inspect all open issues linked to changed modules and explicitly classify each as fixed by this change, unaffected, or newly exposed. Do not claim "no regressions" without that review.

## 8. Privacy and sensitive reports

Keep credentials, tokens, cookies, signing keys, personal datasets and unnecessary sensitive content out of issues. Preserve diagnostic evidence with secrets redacted; explain that redaction occurred. Restrict security-sensitive disclosure to GitHub Security Advisories and follow `SECURITY.md`. Do not discard the fact or outcome of a private report; record a non-sensitive reference and its resolution state.

## 9. Current continuity anchor

- `LOC-0002`: binding product continuity and feedback preservation. This is the first explicit governance requirement created under this lifecycle.
- The repository currently contains no other discovered product-feedback issue records at the time this lifecycle was authored; do not infer that external chat history is empty. New user reports must be captured as they arrive.
