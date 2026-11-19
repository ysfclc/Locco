# Module boundaries

Rust workspace: `kernel` (domain types/invariants/pure transitions), `application` (use cases and ports), `ffi` (stable ABI and panic/error boundary). Add storage/retrieval/policy crates only when test isolation or dependency boundaries justify them.

Android: `:app` composition root/navigation/lifecycle; `:core:bridge` JNI/ABI/error mapping; `:core:designsystem` tokens/components. Add feature modules only for independent state/navigation/test boundaries. iOS: future SwiftUI shell + bridge, not built now.

Presentation depends on application contracts, not storage/model SDKs. Platform adapters depend inward. Domain imports no platform/UI/network/model types. CI architecture tests forbid cycles.