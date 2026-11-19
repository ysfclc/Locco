# Foundation status ledger

**As of 2026-11-20.** Update only with evidence from the actual repository, Actions runs, build artifacts and device checks. A document or workflow existing is not proof that its control was executed or enforced.

| Gate | Status | Evidence/blocker |
|---|---|---|
| Product identity/scope | Drafted | `docs/product`; broad multi-domain Locco identity |
| Architecture, context contract and FFI | Drafted | `docs/architecture` |
| ADRs 0001–0011 | Present | `docs/decisions` |
| Product continuity ADR-0012 | Proposed | PR #1; must be merged and accepted |
| Binding product/engineering constitution | In review | `GOVERNANCE.md` on PR #1 branch |
| Feedback lifecycle and issue traceability | In review | `docs/governance/feedback-lifecycle.md`; LOC-0002 open |
| Change-control and baseline requirements | In review | `docs/governance/change-control.md` |
| Application/data/upgrade continuity | In review | `docs/governance/application-continuity.md` |
| Agent/role continuity requirements | In review | `AGENTS.md`, `.agent/routing.md`, role documents |
| Feedback/bug/feature issue forms | In review | `.github/ISSUE_TEMPLATE` |
| Mandatory PR evidence template | In review | `.github/pull_request_template.md` |
| Change-contract validator | Added; 11 unit tests pass in isolated source copy; GitHub run not observed | `scripts/check_change_contract.py`, `scripts/tests/test_change_contract.py`; require actual Actions result |
| GitHub change-governance workflow | Added; not yet observed | `.github/workflows/change-governance.yml`; require an executed run |
| Feedback ID automation | Added; not yet observed | `.github/workflows/feedback-register.yml`; new issues receive LOC prefix after workflow is active |
| Required-check branch protection | Not configured/verified | Repository ruleset must require `change-contract` and owner/reviewer gates |
| GitHub Issues backlog inventory | Partial | LOC-0002 is open; inspect complete issue history at every task start |
| ZIP validator adversarial tests | Locally verified | 9 Python tests passed in isolated local harness |
| Foundation archive | Created and CRC-checked | `incoming/foundation.zip`; 104 tracked source files at archive creation |
| Rust workspace compile/tests | Not verified | no Rust toolchain execution evidence yet |
| Cargo.lock | Committed | `core/Cargo.lock`; workspace has no third-party crate dependencies |
| Three Android Rust ABIs | Not verified | no successful CI artifact observed |
| Placeholder APK | Not verified | no generated APK/verified hash available |
| actionlint | Not verified | prior GitHub runs had no runner/steps; no actionlint evidence |
| Gradle dependency verification | Not enforced yet | generate and commit verification metadata from a trusted Gradle build |
| Physical device smoke | Not run | requires Android device |
| Android upgrade/data continuity | Not run | no previous supported release APK and in-place upgrade evidence yet |
| Branch/release environments | Not verified | owner must configure repository rules and `android-release` reviewers |

Do not mark Phase 0 accepted until required gates have reproducible evidence, the governance PR is merged, and GitHub merge protection is observed blocking a deliberately non-compliant PR.
