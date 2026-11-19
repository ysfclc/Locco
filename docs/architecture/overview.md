# Architecture overview

**Rust domain kernel + thin native shells + replaceable adapters.** Android uses Kotlin/Compose + JNI. iOS is future Swift over a versioned C ABI, gated. Rust owns domain IDs, invariants, validation, state transitions, evidence semantics and deterministic policy. Shells own lifecycle, permissions, background work, accessibility, rendering and OS integration.

Planes: acquisition adapters produce provenance-bearing observations; truth plane preserves evidence and links entities/events; intelligence compiles selective context for local inference; capability plane validates authority/approval/postconditions; native workspace projects domain state.

Dependency: shell → FFI → Rust application/domain → ports. Adapters implement ports. Domain imports no platform, UI, network client, database driver or model SDK.

UDF: intent → feature ViewModel/reducer → use case → Rust command/query → projection → UI. Reducers are feature-local; orchestration belongs in application services. Offline, denied permission, unavailable source/model, storage full and interruption are typed states. Unknown is not success.