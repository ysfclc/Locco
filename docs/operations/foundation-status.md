# Foundation status ledger

As of 2026-11-19. Update only with reproducible evidence.

| Gate | Status | Evidence/blocker |
|---|---|---|
| Product identity/scope | Drafted | docs/product |
| Architecture, context contract and FFI | Drafted | docs/architecture |
| ADRs | Drafted | docs/decisions |
| 7 agent roles | Present | .agent/agents |
| 10+ skills | Present | .agent/skills (13) |
| Security doctrine | Drafted | docs/security |
| ZIP validator adversarial tests | Locally verified | 9 Python tests passed in isolated local harness |
| Foundation archive | Created and CRC-checked | incoming/foundation.zip; 104 tracked source files |
| Rust workspace compile/tests | Not verified | no Rust toolchain in current execution environment |
| Cargo.lock | Committed | core/Cargo.lock; Rust workspace currently has no third-party crate dependencies |
| Three Android Rust ABIs | Not verified | NDK/cargo-ndk build unavailable locally |
| Placeholder APK | Not verified | no Android SDK/Gradle locally; GitHub runner jobs have no assigned runner/steps |
| actionlint | Not verified | GitHub workflow job records contain no runner and no executed steps |
| Gradle dependency verification | Not enforced yet | generate and commit verification metadata from a trusted Gradle build |
| Physical device smoke | Not run | requires Android device |
| Branch protection/environment reviewers | Not configured here | owner must enable in GitHub repository settings |
| Brainstorm source archive | Transcribed | mission brief is preserved as a content transcription; original conversation remains authoritative |

Do not mark Phase 0 complete until every required gate has reproducible evidence.


## Architecture audit update — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| LOC-0004 deep architecture audit | In review | Draft PR #6: https://github.com/ysfclc/Locco/pull/6; proposed system blueprint, Capability Compiler, browser/DOM/session, model ecosystem, native experience, assurance register and revised roadmap |
| LOC-0005 browser sign-in/session continuity | Open; not reproduced | Memo-2 current main contains no BrowserSession/WebView browser implementation; Memo-1 has a visible WebView session. The user's exact affected sites/failure stage and target WebView version are still required to reproduce. No root cause assigned. |
| Browser engine decision | Proposed investigation only | ADR-0013; WebView vs GeckoView spike required; Auth Tab is separate auth handoff |
| AndroidX WebView multi-profile support | Platform API exists; target device unverified | AndroidX WebKit Profile/ProfileStore 1.9.0+; runtime MULTI_PROFILE feature check required |
| Isolated-world DOM observer | Platform API exists; target device unverified | AndroidX WebKit 1.16.0 JS_INJECTION_IN_FRAME_AND_WORLD feature check required |
| Model ecosystem architecture | Proposed | docs/architecture/model-ecosystem.md; no model runtime implemented in Locco |
| Generic Capability Compiler | Proposed | docs/architecture/capability-compiler.md; no use-case planner implementation |
| Native UX architecture | Proposed | docs/architecture/native-experience.md; no product UI beyond placeholder is implemented |
| Latest architecture PR checks | Failure | Security/policy run 44707397669 failed; job step summaries/logs unavailable, so root cause is unknown |
| Independent architecture/security/QA review | Not performed | Separate review is required; do not simulate it |

PR #6 is a design proposal only. No product feature, browser engine or model runtime is implemented or accepted by this update.


## Reference and traceability audit — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Memo-1/Memo-2 source comparison | Source audit completed | docs/architecture/reference-inspiration-audit.md; implementation and planned behavior are distinguished |
| Locco UX inspiration/iteration | Proposed; not prototyped | docs/architecture/native-experience.md; three navigation hypotheses and task-based evaluation are defined |
| Product requirement traceability | Initial mapping created | docs/architecture/requirement-traceability.md; test IDs are planned, no feature tests are claimed |
| Rich model ecosystem | Proposed | docs/architecture/model-ecosystem.md; no Locco catalogue/runtime implementation |
| Account login regression | Open; root cause unknown | LOC-0005; same-device affected-site and auth-flow reproduction not yet supplied/performed |
| Browser engine selection | No selection | ADR-0013; WebView vs GeckoView target-device spike required |


## Architecture reconciliation and current CI evidence — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| System-plane taxonomy | Reconciled in proposal | overview.md and target-system-blueprint.md now use the same five planes: Experience, Platform/adapters, Intelligence, Control and Data |
| Canonical data authority | Reconciled in proposal | Relational store is canonical; append-only journals preserve transition/side-effect evidence; blobs are referenced; indexes/embeddings are rebuildable |
| Architecture document index | Verified | README.md links both reference-inspiration-audit.md and requirement-traceability.md |
| Reference source reproducibility | Pinned | Memo-1 source commit c307f94f5d296622e9e3b2fa92b02fe9c58361f2; Memo-2 source commit 00a8a9e1244c5dba7af107b1ee0e808738d5d022 |
| Policy CI on latest audited head | Failure; no diagnosis | Run 44708789593 on commit 8f10a1082910ca2fcddc05556a81bc0036bc0a71 completed failure in ~3 seconds; job returned no step summaries, and the log endpoint returned BlobNotFound |
| Policy CI retry | Failure; no diagnosis | Run 44708513956 attempt 2 also completed failure with no step summaries/logs; this retry targeted prior head bf7f3435b52fde419da0155a4f8f148ba7402165 |
| Branch protection | Unverified | GitHub integration cannot read the repository branch-protection endpoint (403); do not infer that protections are absent or configured |
| Product UI/browser/model behavior | Not implemented in this pass | Documentation only; no UI prototype, browser engine, DOM observer or model runtime was built or tested |
| Architecture acceptance | Open | LOC-0004 review and independent architecture/security/QA reviews remain outstanding |

The system-plane and index reconciliation are documentation checks only. They do not make the architecture accepted or the foundation complete.


## FFI and durable execution contract pass — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Command/query/event FFI contract | Proposed specification | docs/architecture/ffi-command-event-protocol.md; ADR-0014; current exported ABI remains ping-only |
| Durable execution/recovery | Proposed specification | docs/architecture/durable-execution-and-recovery.md; ADR-0015; no execution engine/outbox implemented |
| Native side-effect delivery semantics | Explicitly defined in proposal | At-least-once delivery, stable effect IDs, leases/fencing, uncertain states and no blind retries |
| FFI contract verification | Not implemented | C/JNI ownership, invalid input, concurrency, replay and recovery tests remain future gate |
