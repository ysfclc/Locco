# Foundation status ledger

As of 2026-11-20. Update only with reproducible evidence.

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


## Storage security and recovery contract pass — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Storage/key/backup architecture | Proposed specification | docs/architecture/storage-security-and-recovery.md; ADR-0008 implementation choice still open |
| Database encryption selection | Not selected | SQLCipher vs authenticated record/blob encryption must be benchmarked and threat-reviewed |
| Key-loss/recovery behavior | Contract defined; not implemented | Must enter RECOVERY_REQUIRED and never silently initialize a replacement workspace |
| Backup/restore continuity | Contract defined; not implemented | Encrypted, staged, verified restore; sessions/secrets excluded by default; no action replay |


## Latest user-direction and reference audit — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Memo-2 UI inspiration, second-pass source analysis | Source analysis completed | `docs/architecture/reference-inspiration-audit.md` now records what is useful in the actual six-area Compose UI and Model Hub, where its fixed navigation/static catalogue stop short, and how Locco must improve rather than copy. No final Locco IA or visual language selected. |
| Locco navigation prototypes | Not started | Three hypotheses and twelve representative journeys are documented in `native-experience.md` and the reference audit. They still need comparative interactive prototypes and task-based evaluation. |
| Memo-2 account-login report | Unclassified; open in LOC-0005 | User reports sign-in failure in Memo-2 and no equivalent issue in Memo-1. Exact provider, installed APK/build, entry surface and failure stage are not yet identified. The inspected Memo-2 main source has no dedicated browser/session subsystem; this does not explain an installed APK or other account connector. Classify the flow before diagnosis. |
| LOC-0005 reproduction guard | Updated | Issue #5 distinguishes website sign-in, Auth Tab/Custom Tabs, OAuth/OIDC app connection, remote model/API credentials and third-party app integration. No credentials or session secrets may enter diagnostics. |
| Browser/account architecture | Proposed, not implemented | `browser-dom-and-session.md`; WebView/Auth Tab/profile and DOM observer features remain runtime-gated and require target-device testing. |
| Storage security and recovery | Proposed; technology spike open | `storage-security-and-recovery.md` defines data classes, envelope-key direction, key-loss behavior, backup exclusion of session secrets, restore, rotation and deletion tests. No encryption implementation is accepted. |
| Architecture review | Draft PR open | PR #6, latest checked head `6816f91e51adf2083017a1c8df6af66ee7ababd7`. Canonical product scope, machine-readable brainstorm, delivery protocol, PR template and roadmap are aligned to the full browser, model ecosystem, DOM intelligence, generic composition and account-login requirement. No independent architecture/security/QA acceptance yet. |
| Latest Security and policy CI | Pre-runner failure; source checks NOT RUN | Run `44710944012` on the checked head failed. Raw job record: `runner_id=0`, empty runner name, `steps=[]`; the job ended in about one second. No validator or source command ran. This is not evidence of a source-check failure or success; restore runner dispatch or use a controlled runner before accepting CI. |
| Product UI/browser/model runtime | Not implemented in this pass | The work remains architecture and requirement clarification. No product UI, browser engine, DOM observer or model runtime is claimed. |
| Phase 0 / Phase 1 gate | Open / Phase 1 not started | LOC-0003 and LOC-0004 still require independent acceptance. Preserve this gate. |

## Canonical product scope clarification — 2026-11-20

The founding/raw brainstorm remains preserved unchanged. The reviewed product scope and machine-readable structured brief now clarify the approved direction:
- Locco includes a first-class native browser, not only browser capture.
- The browser is usable independently of AI and provides a structured, safe DOM observation/Inspect surface for later DOM Stitching.
- The Model Hub is a rich, extensible model ecosystem across modalities/tasks where runtime and device fit are proven; it is not one static list or one runtime.
- Website authentication/session state, OAuth account connections and model/API credentials are distinct surfaces. The user's Memo-2 login issue remains unresolved until the exact surface and failing flow are classified and reproduced.
- Memo UI is inspiration only; Locco's own IA/design must be iterated and tested.
- General-purpose task composition uses reusable typed capabilities; no per-use-case planner branches.
- “No HTML/web UI” prohibits a Locco web application, not the native browser rendering remote websites.



## Latest CI dispatch evidence — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Security/policy Actions on architecture head | Pre-runner failure; checks NOT RUN | Run `44711219969` for commit `dd67e67ce995d2bd335f1d88578bcc4c8f0c8806` failed. Raw job `136203133179` has `runner_id=0`, empty runner name and `steps=[]`; it completed about two seconds after creation. No repository command, policy validator or security scan executed. Do not treat this as a source-level test failure or a pass. |


## Canonical scope and CI synchronization — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Product scope alignment | Updated in proposal branch | `docs/product/scope.md` and `docs/brainstorm/structured.json` now explicitly define the first-class native browser and its account/session/DOM responsibilities, rich model ecosystem, DOM Stitching, general-purpose capability compiler and non-copying UI iteration. The raw founding brief remains unchanged as history. |
| Latest Security/policy CI on canonical-scope head | Pre-runner failure; source checks NOT RUN | Run `44711515524` for commit `ad6d9fc28c89f132b1744ad192f6775e280f4623` failed. Raw job `136204109242` had `runner_id=0`, empty runner name and `steps=[]`, ending in about two seconds. No source or validator command ran. |
| Architecture PR | Open draft | PR #6 at `ad6d9fc28c89f132b1744ad192f6775e280f4623`; specification work only. Phase 0 and independent architecture/security/QA acceptance remain open. |


## Issue-to-release workflow definition — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Delivery lifecycle policy | Specified | `docs/operations/delivery-lifecycle.md` defines LOC issue granularity, readiness, lifecycle states, exact-commit evidence, reviewer duties, continuity, and roadmap/traceability updates. |
| Delivery intake template | Added | `.github/ISSUE_TEMPLATE/delivery.yml` captures user outcome, phase, LOC-R requirements, base SHA, acceptance, dependencies, compatibility, verification and risk. |
| PR evidence contract | Updated | `.github/pull_request_template.md` requires exact commit, test result (PASS/FAIL/NOT RUN), artifact hash, device versions, review and rollback evidence. |
| Current progress tracking | Manual issue source of truth | Each LOC issue must be updated at every meaningful milestone. The roadmap is a phase dependency map, not a percent-complete dashboard. |
| Project board / status automation | Not configured or verified | No live GitHub Project board or issue-status synchronizer is claimed. Introduce only after Actions dispatch is restored and its idempotency/permissions are tested. |

## Latest CI dispatch evidence after delivery protocol — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Security/policy Actions on delivery-protocol commit | Pre-runner failure; source checks NOT RUN | Run `44711967239` on commit `6816f91e51adf2083017a1c8df6af66ee7ababd7` failed. Raw job `136205622332`: `runner_id=0`, empty runner name, `steps=[]`; no repository command executed. |


## Model provenance and DOM freshness audit — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Model artifact authenticity | Contract specified, not implemented | model-ecosystem.md separates source pinning, digest integrity, publisher signatures/attestations, parser validation, runtime compatibility and device quality; no model artifact verifier exists |
| DOM observation identity and freshness | Contract specified, not implemented | browser-dom-and-session.md defines snapshot-local node IDs, document/mutation epochs, live locator re-resolution, bounded mutation and evidence-backed collection coverage; no observer exists |
| DOM Stitching acceptance | Requirements expanded | LOC-R022 adds epoch invalidation, stale/ambiguous locator stop, virtualized completeness and evidence provenance test criteria |
