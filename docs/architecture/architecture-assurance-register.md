# Architecture assurance register

**Status:** Open; working register for LOC-0004.

## Evidence labels

PROPOSED, IMPLEMENTED, TESTED, DEVICE-VERIFIED, EXTERNALLY-CONSTRAINED, UNVERIFIED and BLOCKED are distinct. A document or control in a screen does not prove runtime behavior.

## Initial findings

| Subsystem | Current evidence | Initial state | Required assurance |
|---|---|---|---|
| Product vision | Vision, scope and original brief | PROPOSED | Trace requirements/non-goals to architecture/tests |
| Rust domain | Kernel crate is a stub | Scaffold only | Records, commands, transitions, invariants |
| Rust application layer | Described but no application crate in workspace | PROPOSED | Crate/port boundaries and service ownership |
| FFI/JNI | ABI version and ping only | Link scaffold | Typed command/query envelopes, engine handles, durable event replay, effect lease/fencing, buffer ownership, panic/size/thread safety |
| Canonical persistence | ADR-0008 direction only | PROPOSED | Encryption/storage spike, schema, migrations, recovery |
| Evidence/provenance | Data-model contract only | PROPOSED | Stable identity, source offsets, immutable preservation |
| Source adapters | Adapter contract only | PROPOSED | Lifecycle, authorization, partial results and cancellation |
| Browser UI | No Locco browser implementation | Not implemented | Native browser chrome, tabs, downloads and sessions |
| Browser authentication | No session subsystem | BLOCKED by design gap | LOC-0005 reproduction and device acceptance |
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
- [ ] Specify canonical identity, evidence, provenance, entities, claims, memory, events and artifacts.
- [x] Specify proposed command/event/FFI protocol in ffi-command-event-protocol.md (contract tests still absent).
- [ ] Specify persistence, encryption, migration, backup, restore and deletion.
- [ ] Specify offline behavior and resource budgets.

### C. Browser/DOM
- [ ] Design full native browser.
- [ ] Reproduce LOC-0005 login/session problem.
- [ ] Compare AndroidX WebView multi-profile with GeckoView session contexts on the target phone.
- [ ] Verify AndroidX WebKit MULTI_PROFILE and JS_INJECTION_IN_FRAME_AND_WORLD support on target WebView.
- [ ] Evaluate Auth Tab/Custom Tabs/Credential Manager as separate authentication paths; prove no cookie-sharing assumption is made.
- [ ] Reproduce embedded identity-provider restrictions using supported handoff.
- [ ] Accept ADR-0013 only after same-device engine/auth spike results.
- [ ] Specify DOM snapshot, script ownership and origin boundaries.
- [ ] Specify DOM Stitching and deterministic schema/locator validation.
- [ ] Specify typed actions, approval and uncertainty.
- [ ] Test SPA, frames, shadow roots, virtualized lists, stale nodes and prompt injection.

### D. Model ecosystem
- [ ] Define model/artifact metadata schemas.
- [ ] Define catalogue sources, provenance and trust.
- [ ] Define runtime compatibility descriptors.
- [ ] Define import/download/verification/install lifecycle.
- [ ] Define device fit, benchmarking, scheduling and resource governor.
- [ ] Validate candidate LiteRT-LM/GGUF/specialist runtimes on target hardware.

### E. Compiler/execution
- [ ] Specify TaskSpec and RequirementGraph.
- [ ] Specify component kinds, versions and trust.
- [ ] Specify deterministic assembly and readiness.
- [ ] Specify ExecutionPackage and generic DAG.
- [x] Specify cancellation, checkpoint, retry, process death and uncertain effects in durable-execution-and-recovery.md (implementation/tests still absent).
- [ ] Test unrelated task shapes without planner code branches.

### F. Native UX
- [x] Inspect Memo-1 and Memo-2 source and separate working behavior from plans; see reference-inspiration-audit.md.
- [ ] Produce and test Locco-specific navigation alternatives; initial hypotheses are recorded.
- [ ] Define original tokens, components, motion and accessibility.
- [ ] Define Browser and Model Hub UX.
- [ ] Verify each control and state.

### G. Security/quality
- [ ] Map trust boundaries and attack surfaces.
- [ ] Define contract, property, fuzz, emulator, device and upgrade tests.
- [x] Create initial requirement-to-architecture/test mapping in requirement-traceability.md.
- [ ] Convert planned test IDs into implemented tests with passing evidence.
- [ ] Record high-reversal-cost ADRs and spike results.
- [ ] Update roadmap against dependencies.
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

