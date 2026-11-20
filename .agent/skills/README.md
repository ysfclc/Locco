# Locco skill library

A skill is a task-triggered procedure, not a slogan or a generic checklist. Each skill defines when it applies, required inputs, ordered actions, evidence to produce and conditions that block acceptance.

## Activation rule
The task owner MUST select skills by their trigger and load the complete relevant file before performing the associated work. Merely listing a skill name in a role prompt does not mean its procedure was applied. Do not load every skill for an unrelated task; do not skip a triggered skill to save context. If a required skill is unavailable, report that blocker and follow the safest applicable governance rule.

## Trigger map

| Skill | Load when |
|---|---|
| [feedback intake](feedback-intake.md) | Any user bug, regression, feature, UX issue, correction, constraint or product decision |
| [regression-first fix](regression-first-fix.md) | Bug, crash, data loss, reliability or previous behavior stops working |
| [phase acceptance](phase-acceptance.md) | Every PR, implementation slice, phase close and release |
| [ADR writing](adr-writing.md) | Lasting product, architecture, data, platform, security or dependency decision |
| [upgrade continuity](application-upgrade-continuity.md) | Persistent data, package, signing, FFI, install or release behavior changes |
| [Rust FFI boundary](rust-ffi-boundary.md) | C ABI, JNI/Swift bridge, symbols, ownership, serialized contract or callback changes |
| [cargo-ndk cross-build](cargo-ndk-cross-build.md) | Rust/native library output for Android changes |
| [native UI acceptance](native-ui-acceptance.md) | Compose UI, navigation, lifecycle, accessibility or user-visible action changes |
| [Android signing](android-signing.md) | APK signing, package identity, artifact install/upgrade or release |
| [dependency pinning](dependency-pinning.md) | Add/update/remove code, build, native, action or model-runtime dependency |
| [secret handling](secret-handling.md) | Any credential, private key, cookie, personal export or authentication material |
| [SBOM and attestation](sbom-and-attestation.md) | Foundation/distribution artifact and every release candidate |
| [release gating](release-gating.md) | Release candidate, phase acceptance or protected contract change |
| [model context budget](model-context-budget.md) | Retrieval, prompt/context construction, local inference or memory |
| [iOS bridge gate](ios-bridge-gate.md) | Any proposed/implemented iOS shell or bridge work |
| [phase ingestion](phase-ingestion.md) | Explicitly authorized external ZIP intake only |
| [ZIP extraction safety](zip-extraction-safety.md) | Every ZIP/archive parser, extractor or intake path |

## Minimum skill handoff
For each triggered skill, the owner records:
- why its trigger applies;
- which inputs were inspected;
- which procedure steps were performed or blocked;
- exact test/build/evidence output;
- unresolved risks and next owner.

A skill does not replace code review, tests or user approval. Passing a skill checklist does not independently prove product correctness.
