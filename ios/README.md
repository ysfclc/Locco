# iOS scaffold — gated, not built

The future iOS app is a native Swift/SwiftUI shell over the same versioned Rust C ABI. No Xcode project or iOS build is included in Phase 0.

Before implementation: stabilize ABI; pass shared contract fixtures on Android; approve iOS phase; choose UniFFI vs thin C wrapper via spike; define XCFramework slices; use macOS runner + xcodebuild; review signing, provisioning and privacy declarations. Domain rules remain in Rust. See docs/architecture/ios-path.md and docs/architecture/ffi-contract.md.