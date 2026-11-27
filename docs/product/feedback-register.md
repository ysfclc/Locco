# Product feedback register

**The canonical feedback database is GitHub Issues, not this Markdown page.** Every product-related message or report must become a `LOC-####` issue. Do not duplicate all issue contents here; duplicated status tables become stale and are not authoritative.

## Live views

- [All open feedback and work items](https://github.com/ysfclc/Locco/issues?q=is%3Aissue+is%3Aopen)
- [Closed feedback and implementation history](https://github.com/ysfclc/Locco/issues?q=is%3Aissue+is%3Aclosed)
- [Pull requests and implementation evidence](https://github.com/ysfclc/Locco/pulls)

## First continuity record

| Durable ID | GitHub record | Subject | Status |
|---|---|---|---|
| LOC-0002 | [#2](https://github.com/ysfclc/Locco/issues/2) | Binding product continuity and feedback preservation | Open — governance controls being implemented |
| LOC-0015 | [#15](https://github.com/ysfclc/Locco/issues/15) | Evidence-backed cross-app activity, inspectable events and consolidated Locco product contract | Open — PR #14 implements the first notification-inspector slice; broader screen/action observation and product environments remain open |

## Rules

1. A chat message describing a Locco bug, regression, feature, UX issue, workflow, product constraint or correction is not "just conversation". Capture it as an issue and return the link.
2. Preserve the user's original wording and distinguish facts from interpretation.
3. Every PR and commit-level handoff references its issue IDs.
4. Keep unresolved/deferred/rejected/duplicate items visible in GitHub. Do not delete them to simplify a roadmap.
5. Update an issue with PR, merge SHA, tests, APK hash, device/upgrade evidence and gaps.
6. Search this issue database and Git history before starting adjacent work.

An item is not absent merely because it is missing from this page. Query the linked GitHub issue views.
