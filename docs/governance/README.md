# Governance index

This directory contains operationally binding procedures. The root [GOVERNANCE.md](../../GOVERNANCE.md) is the authority; these documents define how to execute it.

| Document | Controls |
|---|---|
| [Feedback lifecycle](feedback-lifecycle.md) | Intake, durable issue IDs, preservation, triage, status transitions, closure |
| [Change control](change-control.md) | Baseline, history review, impact analysis, implementation, regression tests, review and closeout |
| [Application continuity](application-continuity.md) | Existing application identity, stored data, migrations, Android signing and upgrades |

## Required sequence

Every task: constitution → AGENTS/routing → original feedback issue and history → relevant product/ADR/contracts → change plan → implementation → regression proof → independent review → release/upgrade proof → issue closeout.

These documents are not optional reading and are not replaced by a short handoff summary.
