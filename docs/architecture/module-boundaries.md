# Module boundaries

**Status:** Target module contract. The actual repository is currently a two-crate Rust scaffold and a single Android application module; proposed modules below are not represented as already implemented.

## 1. Rust workspace

Target responsibility:

- kernel — pure domain records, IDs, invariants, validation, deterministic transitions and versioned schemas.
- application — use cases, Capability Compiler, context/evidence services, typed ports, command/event contracts and durable execution coordination.
- ffi — stable C ABI/JNI-facing facade, handle/buffer lifecycle, serialization, panic/error containment and ABI compatibility.

The existing workspace currently contains kernel and ffi only. Adding application is a foundation task and must keep the dependency direction inward. Add other crates (storage, policy, retrieval, catalogue contracts) only when they create a meaningful test/dependency boundary; do not split the workspace for naming alone.

Dependency direction: ffi → application → kernel. Kernel never imports Android, Kotlin/JVM, UI, browser, model SDK, HTTP client, SQLite driver or platform key APIs. The application layer depends on traits/ports rather than concrete adapters.

## 2. Android shell and adapters

Current Android source is one app module. Keep it that way until actual independent build, dependency, lifecycle or test boundaries justify splitting modules.

Target responsibilities inside the Android application:

- composition/navigation — app startup, dependency graph, navigation, process lifecycle and deep links.
- native experience — Compose design system and task-specific UI state.
- browser — engine adapter, tab/session manager, profile lifecycle, account/auth handoff, downloads and DOM observation transport.
- models — catalogue transport, artifact acquisition, integrity checking, runtime adapters and resource manager.
- sources — SAF, share/import, notification and permitted Android source adapters.
- storage/platform — Keystore/key wrapping, encrypted blob/file operations and OS-owned preferences.
- execution platform — WorkManager/foreground service/notifications, app links, permission and lifecycle callbacks.
- bridge — JNI/C ABI marshalling and error conversion.

These can start as packages in :app. Split into :core:bridge, :core:designsystem or feature modules only when module ownership is genuinely independent and the split does not duplicate state or contracts.

Presentation depends on application projections/contracts, not database or model SDKs. Platform adapters implement inward-facing ports. Compose ViewModels hold restorable presentation state only; they do not become the durable domain database.

## 3. Browser engine isolation

The product-level browser contract is engine-neutral at the lifecycle level: profiles, tabs, navigation, document epochs, observations, downloads, permissions, auth handoff and failures. Engine-specific DOM APIs are encapsulated behind an adapter and explicit capability descriptor.

This is not a promise that WebView and GeckoView expose identical browser features. The chosen engine's limits are part of the capability registry and UX.

## 4. Model runtime isolation

The model ecosystem separates catalogue metadata, artifact manifests, runtime adapters and device compatibility. Android may host multiple native runtime implementations (Kotlin, Java, C/C++ or vendor libraries), but the Rust application layer sees typed runtime/capability results rather than engine-specific classes.

No model SDK is imported by the domain kernel. An unavailable/unsupported runtime is a normal capability gap.

## 5. iOS path

Future SwiftUI shell plus the stable C ABI, not a second domain implementation. Bridge generator selection remains gated until ABI and lifetime contracts are tested. No iOS code is part of current product implementation.
