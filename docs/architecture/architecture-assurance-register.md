# Architecture assurance register

**Status:** Open; working register for LOC-0004.

Checklist semantics: `[x]` means a proposed document/contract exists and is linked. It does **not** mean the architecture is accepted, code is implemented, tests pass or a physical device has been verified. Those are tracked as separate evidence states.

## Evidence labels

PROPOSED, IMPLEMENTED, TESTED, DEVICE-VERIFIED, EXTERNALLY-CONSTRAINED, UNVERIFIED and BLOCKED are distinct. A document or control in a screen does not prove runtime behavior.

## Initial findings

| Subsystem | Current evidence | Initial state | Required assurance |
|---|---|---|---|
| Product vision | Vision, scope and original brief | PROPOSED | Trace requirements/non-goals to architecture/tests |
| Rust domain | Kernel crate is a stub | Scaffold only | Records, commands, transitions, invariants |
| Rust application layer | Described but no application crate in workspace | PROPOSED | Crate/port boundaries and service ownership |
| FFI/JNI | ABI version and ping only | Link scaffold | Typed command/query envelopes, engine handles, durable event replay, effect lease/fencing, buffer ownership, panic/size/thread safety |
| Canonical persistence | ADR-0008 plus storage-security-and-recovery.md contract; physical schema/driver not implemented | PROPOSED | Technology spike, schema, WAL/crash behavior, key-loss recovery, backup/restore, rotation and deletion |
| Evidence/provenance | Data-model contract only | PROPOSED | Stable identity, source offsets, immutable preservation |
| Source adapters | Adapter contract only | PROPOSED | Lifecycle, authorization, partial results and cancellation |
| Browser UI | No Locco browser implementation | Not implemented | Native browser chrome, tabs, downloads and sessions |
| Browser authentication | Explicit proposed identity/session contract in browser-dom-and-session.md; no runtime session subsystem yet | PROPOSED; device acceptance blocked | LOC-0005 flow classification/reproduction, engine choice and target-device acceptance |
| DOM observation | No implementation | PROPOSED | Versioned bounded observation and fixtures |
| DOM Stitching | No implementation | PROPOSED | Schema/relationship proposal, validation and drift tests |
| Model catalogue | No Locco model hub implementation | PROPOSED | Metadata schema, sources, cache and trust model |
| Model runtimes | No inference runtime in Locco scaffold | PROPOSED | Runtime matrix and load/device spikes |
| Model device fit | No measurements | UNVERIFIED | RAM/thermal/storage/latency on target hardware |
| Capability Compiler | No implementation | PROPOSED | TaskSpec, graph, library, readiness and package |
| Execution coordinator | No durable engine | PROPOSED | Transactional outbox, leases/fencing, cancellation, crash-point recovery, idempotency and uncertain-side-effect verification |
| Policy/approval | Design rules only | PROPOSED | Exact target/payload/authority/expiry tests |
| Context/memory | Contracts only | PROPOSED | Admission, redaction, conflicts and curation |
| Native UX | One placeholder Compose Activity | Scaffold only | Original IA, design system and real flows |
| Android integrations | Build/package baseline only | UNVERIFIED | permissions, WebView lifecycle, foreground and storage |
| iOS | Gated path documented | PROPOSED | Shared contract parity; no implementation yet |
| Security | Threat model and doctrine | PROPOSED | Threat boundaries and adversarial tests |
| Release continuity | Package identity documented | UNVERIFIED | APK, signing lineage, upgrade and data preservation |

## Mandatory work packages

### A. Product boundary
- [ ] Audit original mission brief, structured brainstorm, vision, scope, personas and ADRs.
- [ ] Map each capability and non-goal to one architecture element and acceptance test.
- [ ] Interpret “no HTML/web UI” as no Locco web app; it does not prohibit rendering websites in the native browser.
- [ ] Keep Memo separate; reuse concepts only.

### B. Contracts
- [ ] Approve system context, trust boundaries and module responsibilities.
- [x] Specify proposed canonical identity, evidence, provenance, entities, claims, memory, events and artifacts in data-model.md (physical schema, identity algorithm and encryption driver remain open).
- [x] Specify proposed command/event/FFI protocol in ffi-command-event-protocol.md (contract tests still absent).
- [x] Specify persistence security, key lifecycle, migration, backup, restore and deletion contract in storage-security-and-recovery.md (technology spike and implementation remain open).
- [x] Specify the normative model context admission budget, output reserve, safety margin and insufficient-budget fallback in context-feeding-contract.md (tokenizer/runtime and multimodal cost tests remain open).

### C. Browser/DOM
- [x] Specify the proposed full native browser, account/session and DOM observation architecture in browser-dom-and-session.md (engine/runtime implementation and device acceptance remain open).
- [x] Specify everyday browser completeness (address/search, safe popups, tabs, upload/download, external links, site permissions/data controls, page controls, recovery) and OS credential/autofill boundaries in browser-dom-and-session.md section 12 (all runtime behavior remains unimplemented and engine-dependent).
- [x] Specify the mandatory simultaneous normal/private profile baseline and engine ineligibility when actual profile isolation is unavailable (browser-dom-and-session.md §§4.1/4.5; device isolation tests remain open).
- [ ] Reproduce LOC-0005 login/session problem.
- [ ] Compare AndroidX WebView multi-profile with GeckoView session contexts on the target phone.
- [ ] Verify AndroidX WebKit MULTI_PROFILE and JS_INJECTION_IN_FRAME_AND_WORLD support on target WebView.
- [x] Specify Auth Tab/Custom Tabs/Credential Manager as distinct authentication paths and document cookie/session boundaries; target-device compatibility and LOC-0005 reproduction remain open.
- [x] Specify Android Autofill/Credential Manager interoperability without Locco-owned password storage, plus WebView first-party passkey limits and no-secret DOM/log boundary (provider/engine/device acceptance remains open).
- [ ] Reproduce embedded identity-provider restrictions using supported handoff.
- [ ] Accept ADR-0013 only after same-device engine/auth spike results.
- [x] Specify bounded DOM observations, script ownership and origin boundaries in browser-dom-and-session.md (observer implementation and adversarial fixtures remain open).
- [x] Specify proposed DOM Stitching and deterministic schema/locator validation in browser-dom-and-session.md and capability-compiler.md (implementation/tests remain open).
- [x] Specify typed capabilities, exact approval binding, action uncertainty and verification in capability-compiler.md and durable-execution-and-recovery.md (implementation/tests remain open).
- [ ] Test SPA, frames, shadow roots, virtualized lists, stale nodes and prompt injection.

### D. Model ecosystem
- [x] Specify proposed model, release and artifact metadata contracts in model-ecosystem.md (schema implementation and validation remain open).
- [x] Specify catalogue-source adapters, provenance and trust boundaries in model-ecosystem.md (source adapters remain unimplemented).
- [x] Specify runtime capability and compatibility descriptors in model-ecosystem.md (runtime matrix must be measured on device).
- [x] Specify model import/download/verification/staging/install/load-test lifecycle in model-ecosystem.md (implementation/tests remain open).
- [x] Specify device-fit levels, benchmark dimensions, resource scheduling and a conservative resource governor in model-ecosystem.md (target-device measurements remain open).
- [ ] Validate candidate LiteRT-LM/GGUF/specialist runtimes on target hardware.

### E. Compiler/execution
- [x] Specify TaskSpec and RequirementGraph in capability-compiler.md (serialization and contract tests remain open).
- [x] Specify versioned capability-library component kinds and trust promotion in capability-compiler.md (implementation/tests remain open).
- [x] Specify deterministic linking, conflict checks and readiness/capability-gap reporting in capability-compiler.md (implementation/tests remain open).
- [x] Specify immutable ExecutionPackage and generic execution graph in capability-compiler.md (implementation/tests remain open).
- [x] Specify cancellation, checkpoint, retry, process death and uncertain effects in durable-execution-and-recovery.md (implementation/tests still absent).
- [ ] Test unrelated task shapes without planner code branches.

### F. Native UX
- [x] Inspect Memo-1 and Memo-2 source and separate working behavior from plans; see reference-inspiration-audit.md.
- [ ] Produce and test Locco-specific navigation alternatives; initial hypotheses are recorded.
- [ ] Define original tokens, components, motion and accessibility.
- [x] Specify proposed Browser and Model Hub UX in browser-dom-and-session.md, model-ecosystem.md and native-experience.md (interactive prototypes and device acceptance remain open).
- [ ] Verify each control and state.

### G. Security/quality
- [x] Specify initial trust boundaries and attack surfaces in target-system-blueprint.md and security/threat-model.md (independent review and adversarial tests remain open).
- [ ] Define contract, property, fuzz, emulator, device and upgrade tests.
- [x] Create initial requirement-to-architecture/test mapping in requirement-traceability.md.
- [ ] Convert planned test IDs into implemented tests with passing evidence.
- [ ] Record high-reversal-cost ADRs and spike results.
- [ ] Update roadmap against dependencies.
- [x] Define issue-to-release work item, acceptance, verification vocabulary and status-update lifecycle in docs/operations/delivery-lifecycle.md; issue template, PR template and README roadmap now point to it. (No GitHub Project board or issue-status synchronization automation is configured or verified.)
- [ ] Keep unknowns visible.

## Explicit architectural risks

1. Rust domain ownership plus Android-owned WebView/model/OS runtimes requires a durable async event/command contract; ping-only FFI is insufficient.
2. WebView profile isolation is available through AndroidX WebKit 1.9.0+ only when MULTI_PROFILE is supported by the installed provider; fallback must not pretend to isolate sessions.
2a. AndroidX WebKit 1.16.0 isolated-world DOM APIs require runtime JS_INJECTION_IN_FRAME_AND_WORLD support; older providers must not receive a privileged page-world bridge.
2b. Auth Tab shares Chrome session by default but does not share Chrome cookies with WebView; an OAuth callback must not be mislabeled as embedded browser sign-in.
2c. GeckoView offers session/context controls but requires separate engine, resource and maintenance evaluation.
3. Identity providers can reject embedded browsers; external login does not imply embedded cookie transfer.
4. Background WebView execution cannot be assumed from invisible overlay windows.
5. DOM snapshots are observations, not stable element identities; SPAs and virtualized lists invalidate locators.
6. A model file extension/repository tag does not prove runtime compatibility or device fitness.
7. The Capability Compiler can regress into a hardcoded workflow engine if domain labels leak into planner branches.
8. Local model availability does not guarantee quality or latency; deterministic fallbacks are mandatory.
9. Database pages and large encrypted evidence/model assets have distinct storage, key and backup requirements.
10. FFI, evidence identity, schema, approval and app identity are costly to change after user data exists.

## Architecture acceptance

LOC-0004 closes only when:
- Every requirement has architecture mapping, state and test.
- High-reversal decisions have accepted ADRs or measured spikes.
- Document contradictions are resolved or explicitly recorded as open.
- Browser/auth/DOM, model ecosystem, compiler, storage, execution and UX specifications agree.
- Independent architecture/security/QA reviewers inspect the same revision.
- Roadmap has dependency-aware deliveries and evidence gates.

Update this register in the same PR as material architecture changes.


## Reference audit tasks (LOC-0004)

- [x] Inspect Memo-1 and Memo-2 current source and separate working behavior from future plans.
- [x] Record useful UI/model/browser patterns and limitations in reference-inspiration-audit.md.
- [x] Preserve the user's account-login comparison and no-root-cause-without-reproduction rule in LOC-0005.
- [ ] Prototype and test Locco navigation alternatives; no IA selected yet.
- [ ] Run LOC-0005 authentication/profile/DOM/device spike; target device evidence is still absent.
- [ ] Convert planned requirement/test IDs in requirement-traceability.md into actual test suites as implementations are built.
- [ ] Obtain independent architecture, security and QA reviews.



- [x] Specify model artifact provenance/authenticity as separate from digest integrity (LOC-R021); signature/attestation trust roots and no-executable-plugin boundary are documented (validation and parser tests remain open).
- [x] Specify snapshot-local DOM identity, fresh locator resolution, mutation/backpressure and coverage/completeness semantics (LOC-R022; observer implementation and adversarial tests remain open).


## Reproducible build alignment pass — 2026-11-20

| Change | Evidence | Acceptance state |
|---|---|---|
| Rust version pin | `rust-toolchain.toml` now pins 1.86.0, matching the explicit version in Rust, Android, Security and Release workflows | IMPLEMENTED IN PROPOSAL; CI not executed |
| Native build freshness | `build-rust-android.sh` removes only the three derived `liblocco_ffi.so` outputs before compiling, runs fmt/test/Clippy and checks each ABI output | IMPLEMENTED IN PROPOSAL; no NDK/build execution |
| APK build reuse guard | `build-android-debug.sh` always invokes the Rust build for the current source and then checks all three libraries before Gradle | IMPLEMENTED IN PROPOSAL; no APK execution |
| Gradle contract | Local script requires Gradle 8.9 to match CI; repository has no Gradle wrapper | DOCUMENTED; toolchain unavailable locally |
| Native library identity | Local-development docs now name `liblocco_ffi.so`, consistent with Cargo package `locco-ffi` and JNI loader | DOCUMENTED |
| Script syntax | Bash `-n` passed against the exact read-back script contents | TESTED (syntax only) |
| Physical build | Available environment has no rustup, cargo, cargo-ndk, gradle, Android SDK/sdkmanager or NDK | NOT RUN; no build/ABI/APK claim |



## Browser completeness additions — 2026-11-20

The proposed daily-use browser contract is recorded in browser-dom-and-session.md §12 and evaluated in browser-engine-evaluation.md §8. The additional matrix requirements are LOC-R023 (everyday browser controls and safe file/permission/window behavior) and LOC-R024 (OS credential/autofill interoperability without owning or exposing secrets). These checkmarks mean the requirements are specified only; engine comparison, UI implementation, provider/device tests and acceptance remain open.


## Profile completeness gate — 2026-11-20

The daily-browser contract requires a real persistent normal profile and a simultaneously usable isolated private/ephemeral profile. WebView without runtime MULTI_PROFILE support fails the full-browser profile gate on that device; no cookie copying or fake privacy mode. Private data is destroyed on final private-tab close or process death and is not restored from persistent history/backup. This is specified but not implemented or device-verified.


## Android backup and transfer policy

- [x] Specify explicit legacy full-backup and modern cloud/device-transfer exclusions for every app-data domain; current manifest/XML policy is guarded by `scripts/validate_android_backup_policy.py` (OEM/device-transfer tests and future custom export remain open).
- [x] Add and locally exercise six regression cases for Android backup policy (valid manifest/policy, positive fixture, allowBackup=true, missing D2D exclusion, missing legacy exclusion and include-rule rejection); all six passed in an isolated project, while device/OEM behavior remains unverified (LOC-R026).


## R026 backup policy validation evidence

The committed backup XML and validator read back from PR #6 were assembled in an isolated temporary project and exercised with six regression scenarios. All six passed, including the valid policy and fail-closed cases. This validates the Python policy check and XML structure in isolation; it does not replace Android resource compilation, BackupManager/Auto Backup behavior, OEM device-transfer tests or a signed APK test.
