# ADR index

ADRs are append-only. Supersede decisions with a new ADR; never erase rejected alternatives or original rationale. A proposed ADR is not accepted until merged to `main`.

| ADR | Decision | Status |
|---|---|---|
| 0001 | Rust core/native shells | Accepted |
| 0002 | Single Locco repository | Accepted |
| 0003 | Local-first data/on-device AI | Accepted |
| 0004 | Evidence before interpretation | Accepted |
| 0005 | Selective context compiler | Accepted |
| 0006 | ZIP review gate | Accepted |
| 0007 | Native workspace | Accepted |
| 0008 | Local relational store and derived indexes | Accepted for foundation; implementation deferred |
| 0009 | Android native stack and build baseline | Accepted |
| 0010 | Authorization-aware source adapters | Accepted |
| 0011 | CI trust boundary and release gates | Accepted |
| 0012 | Product continuity and user-feedback preservation | Proposed in PR #1; acceptance requires merge and CI/repository-gate evidence |

[ADR-0012 — Product continuity and feedback](ADR-0012-product-continuity-and-feedback.md) defines the permanent rule that user input evolves the existing Locco application, preserves all reported work, and requires traceable regression and upgrade evidence.
