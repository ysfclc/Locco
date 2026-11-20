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
| Architecture review | Draft PR open | PR #6, latest checked head `8ec80bf342b527b4884047d73253abcd73d41f50`. Canonical product scope, machine-readable brainstorm, delivery protocol, PR template and roadmap are aligned to the full browser, model ecosystem, DOM intelligence, generic composition and account-login requirement. No independent architecture/security/QA acceptance yet. |
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


## DOM/model assurance and build alignment — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| DOM node identity and freshness | Contract specified, not implemented | LOC-R022, browser-dom-and-session.md. Node IDs are snapshot-local; locators are re-resolved against the current document; stale/ambiguous targets stop; collection completeness is evidence-backed. |
| Model artifact provenance/authenticity | Contract specified, not implemented | LOC-R021, model-ecosystem.md. Digest integrity, source pinning, publisher signature/attestation, parser safety, runtime load and task quality are separate claims. Model sources cannot supply executable runtime plugins. |
| Rust toolchain alignment | Updated in proposal | rust-toolchain.toml now pins 1.86.0, matching all inspected CI workflows. |
| Local Android build script | Updated; syntax only tested | Rebuilds all three ABIs, removes stale derived FFI libs before build, runs fmt/test/Clippy, checks outputs; APK script always performs fresh native build. `bash -n` passed. |
| Actual Rust/Android build | NOT RUN | Execution environment has no Rustup/Cargo/cargo-ndk, Gradle, Android SDK or NDK. GitHub workflows below also never reached a runner. |
| Latest PR-head CI | NOT RUN | On head `8ec80bf342b527b4884047d73253abcd73d41f50`, Rust run `44712471834`, Android run `44712471828`, Security run `44712471868` all failed before runner allocation. Their jobs each report `runner_id=0` and `steps=[]`; no command ran. |
| Account-login requirement | Open and unclassified | LOC-0005 title/body now explicitly covers multiple sign-in surfaces; exact Memo-2 APK/build, provider and failing path remain unknown. No root cause assigned. |
| Architecture PR | Open draft | PR #6 latest checked head `8ec80bf342b527b4884047d73253abcd73d41f50`; no independent acceptance or Phase 0 approval. |


## Latest architecture-head CI and account issue status — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Rust checks | NOT RUN — runner allocation failure | Run `44712728542` on `2f79f9033987023ca2bc4cb73a534191b0c20e8e`; job `136208229566` has `runner_id=0`, no runner name and no steps. No rustfmt/test/Clippy command executed. |
| Android debug APK | NOT RUN — runner allocation failure | Run `44712728655` on the same head; job `136208230275` has `runner_id=0` and no steps. No NDK, ABI or APK build/inspection executed. |
| Security and policy | NOT RUN — runner allocation failure | Run `44712728547` on the same head; job `136208230062` has `runner_id=0` and no steps. No actionlint, dependency or secret scan executed. |
| Locally available checks | Bash syntax only | `bash -n` passed on the exact read-back versions of `build-rust-android.sh` and `build-android-debug.sh`. This is not a Rust/Gradle/Android build. |
| Local build environment | BLOCKED / unavailable | This execution environment has Java but no rustup, cargo, cargo-ndk, Gradle, Android SDK/sdkmanager or NDK. |
| Account-login issue | Broadened and open | LOC-0005 title/body now explicitly require classifying embedded website login, Auth Tab/Custom Tabs, OAuth/OIDC, model/API account credentials and third-party app sign-in before diagnosis. |


## Final architecture assurance synchronization — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Architecture proposal head | Open draft | PR #6 head `48ce5884fc9a70f50664c33b322a5f1a0d0329ff`; 48 files changed. |
| Requirements traceability | 22 rows, IDs LOC-R001–LOC-R022 | LOC-R021 and LOC-R022 are now inside the requirements table and referenced by the assurance register. All remain proposed until implementation evidence exists. |
| Model catalogue trust | Specified, not implemented | Source pinning, digest integrity, publisher authenticity, registry attestations, parser safety, runtime validation and device quality remain distinct. |
| DOM observation/Stitching safety | Specified, not implemented | Snapshot-local identity, live locator re-resolution, mutation budgets, sensitive-value redaction and evidence-backed collection completeness are documented. |
| Latest full CI set | NOT RUN — no runner allocated | On `48ce5884fc9a70f50664c33b322a5f1a0d0329ff`: Security run `44712940813`, Rust run `44712940800`, Android run `44712940782`. Each raw job reports `runner_id=0`, empty runner name and `steps=[]`; none of the commands ran. |
| Build script static syntax | PASS (Bash parser only) | `bash -n` passed on read-back script contents. Rust/NDK/Gradle/APK builds are still NOT RUN. |
| Independent review / Phase 0 | OPEN | No independent architecture/security/QA acceptance; no device verification; LOC-0003/0004/0005 remain open. |



## Shared Android build and artifact verification path — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| Local/CI build-path unification | Implemented in scripts; execution unverified | scripts/build-android-debug.sh is now the common local and CI entrypoint |
| Rust preflight | Implemented in script; execution unverified | Repository-pinned 1.86.0 is checked after entering core; fmt/test/Clippy included |
| Three-ABI native build inspection | Implemented in script; execution unverified | ELF class/machine and exported C ABI symbols checked per ABI |
| APK inspection | Implemented in script; execution unverified | Package/version/SDK, required ABI libraries and signature checked; SHA-256 printed |
| Script invocation permissions | Corrected in Git mode | Build and verifier scripts are executable |
| Actions workflow execution | Blocked/unverified | Latest three workflows failed in ~3 seconds with no job steps or log URLs; no runner/test result is available |
| Gradle wrapper | Still absent | CI and local use explicit Gradle 8.9; wrapper is a remaining reproducibility improvement |
| Device install/runtime smoke | Not run | Requires a target Android device and built APK |

The build scripts' presence and static checks do not constitute a successful build. Preserve the exact workflow-run result and artifact digest once a runner executes.

## Shared Android build and artifact-verifier regression correction — 2026-11-20

| Gate | Status | Evidence/blocker |
|---|---|---|
| APK verifier source read-back | Corrected | A malformed intermediate verifier version was detected during read-back and replaced in full. The current script includes its complete obsolete-library conditional, signing check, hash output and exact packaged-vs-built native binary comparison. |
| Bash syntax | PASS | `bash -n` passed for the current `build-android-debug.sh`, `build-rust-android.sh` and `verify-android-apk.sh` read-back content. |
| ELF export validation | Tightened in proposal | The Rust build now requires each C ABI symbol to be GLOBAL/DEFAULT and defined (not merely present as an undefined dynamic import). Execution remains unverified without an Android NDK runner. |
| Android workflow path filters | Updated | Both push and pull_request now include `rust-toolchain.toml`, build scripts and APK verifier changes. |
| Latest workflow execution | NOT RUN | Runs `44714368594` (Rust), `44714368545` (Android) and `44714368566` (Security) on `30cf15b38127c53c27eb146d58ad73444c57d34b` each failed before runner assignment; raw jobs have `runner_id=0` and `steps=[]`. |


## Current browser/model/UX and build-verification audit — 2026-11-20

| Gate | State | Evidence / remaining work |
|---|---|---|
| Top-level product vision | Updated | Explicitly includes a human-usable native browser, DOM Stitching, rich Model Hub, generic use-case-neutral Capability Compiler, user authority and no donor-app copying. |
| Browser / DOM product requirements | Specified, not implemented | Full tabs/navigation/history/bookmarks/downloads/session profiles, isolated bounded DOM observations, snapshot-local node IDs, live locator re-resolution, provenance, coverage semantics and hostile-page boundaries. |
| Model Hub requirements | Specified, not implemented | Multi-modal catalogue, immutable artifacts, separate source/publisher trust, replaceable runtime adapters, device compatibility, benchmark evidence and resource governance. No specific model/runtime is assumed compatible before test evidence. |
| Navigation iteration | Research plan specified; prototypes NOT STARTED | Three competing navigation hypotheses and twelve user journeys; build and test at least three native prototypes before selecting primary navigation or copying any visual pattern. |
| Memo-2 account-login issue | Open; unclassified | LOC-0005 covers embedded website login, Auth Tab/Custom Tabs, OAuth/OIDC connections, model/API credentials and third-party app sign-in. Exact account/provider/APK/build and failure stage still required for reproduction. No cause inferred. |
| Rust exported-symbol validation | Tightened in proposal | Each ABI must export defined GLOBAL/DEFAULT symbols for `locco_abi_version`, `locco_core_ping`, `locco_buffer_free`; undefined imports do not satisfy the check. Positive and negative awk fixtures passed. |
| APK payload verification | Tightened in proposal | Extracts each of the three FFI .so files from APK and byte-compares it with the exact native build output before accepting the APK. Package/version/SDK/signature checks remain in the same verifier. |
| Build script shell syntax | PASS | `bash -n` passed for build-android-debug.sh, build-rust-android.sh and verify-android-apk.sh. This is syntax/fixture validation only; no native build performed. |
| Android workflow filters | Corrected | Both push and pull_request path filters include core, Android, rust-toolchain.toml, build scripts, APK verifier and workflow definition. |
| Latest CI on head `2b38baa4c1c0a2d425071a9d2d858e4a50d08808` | NOT RUN — GitHub runner not allocated | Rust run `44714587728` (job `136214602444`), Android run `44714587722` (job `136214601927`), Security run `44714587717` (job `136214602199`). Every job reports `runner_id=0`, empty runner name and `steps=[]`; all ended within ~2 seconds. No checkout, source command, test or build ran. |
| Actual Rust/Android/APK build | NOT RUN | Current execution environment lacks Rustup/Cargo, cargo-ndk, Gradle, Android SDK and NDK. |
| Phase 0 / Phase 1 | Phase 0 OPEN; Phase 1 NOT STARTED | No independent architecture/security/QA acceptance, branch-protection confirmation, runner execution or physical-device smoke test. Preserve LOC-0003 gate. |



## Everyday browser completeness and credential interop — 2026-11-20

| Gate | State | Evidence |
|---|---|---|
| Browser daily-use baseline | SPECIFIED / NOT IMPLEMENTED | Browser contract §12 now includes omnibox/search, navigation, popups, tabs/session, history/bookmarks, safe downloads/uploads, external app links, origin permissions, privacy/site-data clearing, find/zoom/desktop mode/PDF and recovery. |
| Credential interoperability | SPECIFIED / NOT DEVICE-VERIFIED | Use OS-selected Autofill/Credential Manager pathways where supported; do not create a Locco password vault or expose values to DOM/AI/logs. WebView passkey limits and Auth Tab session boundaries are explicit. |
| Browser requirements traceability | 24 proposed rows | LOC-R023 covers daily browser behavior; LOC-R024 covers credential/autofill/passkey boundaries. Both rows are inside the matrix before its legend. |
| Engine feature parity | NOT TESTED | WebView and GeckoView must run the same popup/file upload/download/external-intent/permission/page-control/credential/provider/session suite on the reference phone before selection. |
| Login regression | OPEN / UNCLASSIFIED | LOC-0005; exact Memo-2 installed build, affected provider, entry surface and failure stage still unknown. |


## Context budget invariant correction — 2026-11-20

| Gate | State | Evidence |
|---|---|---|
| Context input/output budget contradiction | Corrected in architecture proposal | `context-feeding-contract.md` now defines input_limit = min(70% of actual usable context, context minus task-specific output reserve minus 10% safety margin). |
| Allocation weights | Corrected | The 10/15/10/55/10 weights now total 100% of available input and explicitly cover static instructions, task envelope, current state, evidence and curated memory. They are not percentages of the full context window. |
| Regression coverage | Specified, not implemented | LOC-R025 requires hard-ceiling, output reserve, safety margin, weight-sum, tokenizer/multimodal cost and insufficient-budget fallback tests. |


## Browser profile eligibility clarification — 2026-11-20

| Gate | State | Evidence |
|---|---|---|
| Normal/private profile contract | SPECIFIED / NOT DEVICE-VERIFIED | Full browser requires simultaneous persistent normal and isolated ephemeral contexts. On process death, private context is discarded rather than restored; whole-profile deletion must verify actual engine data removal. |
| WebView eligibility | NOT DECIDED | A WebView provider lacking MULTI_PROFILE cannot pass the full browser profile gate for that device. Evaluate GeckoView or another reviewed engine; no cookie-copy shim. |
| Site-data deletion claims | ENGINE-DEPENDENT / UNVERIFIED | The UI may report only the data classes/scope the selected engine can actually clear and verify. |


## Android backup and device-transfer policy — 2026-11-20

| Gate | State | Evidence |
|---|---|---|
| Manifest backup defaults | IMPLEMENTED IN PROPOSAL | `allowBackup=false` plus explicit legacy and Android 12+ backup-rule references. |
| Legacy cloud backup | EXCLUDED BY POLICY | `backup_rules.xml` excludes all nine app-data domains. |
| Android 12+ cloud backup and D2D transfer | EXCLUDED BY POLICY | `data_extraction_rules.xml` includes both sections and excludes all nine app-data domains in each. |
| Static policy validator | ADDED; test execution pending | `scripts/validate_android_backup_policy.py` fails closed if a resource, domain exclusion or transfer section is missing or an include rule appears. |
| OEM/device-transfer behavior | NOT DEVICE-VERIFIED | Must be verified on the reference phone and declared OEM support matrix; `allowBackup=false` alone is not sufficient on all OEMs. |
| Explicit workspace export | NOT IMPLEMENTED | Future encrypted, user-initiated export excludes browser sessions, credentials and private profile data. |


## Android backup policy validator execution — 2026-11-20

| Check | Result | Evidence / limit |
|---|---|---|
| Read-back validator | PASS | Committed `validate_android_backup_policy.py` was executed from a clean temporary project with the current manifest and both backup rule files. |
| Regression suite | PASS — 6 tests | Valid policy, valid fixture, allowBackup=true rejection, missing device-transfer exclusion rejection, missing legacy exclusion rejection and accidental <include> rejection. |
| Python compilation | PASS | Validator and test module compile with `py_compile`. |
| Android resource/manifest compilation | NOT RUN | No Android SDK/build-tools/Gradle in this environment; GitHub Android workflow had no runner. |
| OEM cloud backup/device transfer | NOT DEVICE-VERIFIED | Must be tested on reference Redmi and other declared OEM support targets. |
