# Skill: Rust FFI boundary

**Trigger:** Any Rust C ABI, JNI/Swift bridge, exported symbol, cross-language struct, callback, ownership, serialization or error-contract change.

## Contract inventory
Record exported functions/symbols/headers/binding generators; ABI/version and consumers; argument sizes/nullability/encoding/ownership/lifetime; threading/callback/cancellation; error codes; buffers/free functions; serialization layout.

## Implementation procedure
1. Read the canonical FFI contract and every consumer before editing. Identify behavior that must remain binary/source compatible.
2. Prefer a small versioned function surface. Do not expose Rust implementation types or compiler-specific layouts across the boundary.
3. Use explicit C-compatible representations for shared structs/enums. Define integer widths, alignment, ownership and nullability in the contract.
4. Validate pointers, lengths, bounds, enum discriminants, UTF-8/encoding and arithmetic overflow before dereference or allocation. Reject invalid values with stable error results.
5. Copy borrowed inputs before asynchronous retention. Never keep a caller-owned pointer beyond its documented lifetime.
6. Define exactly who allocates and frees each returned buffer/object. Provide one matching free function; test null, empty, repeated-free and error cleanup without double-free/use-after-free.
7. Prevent panic/unwind from crossing the ABI. Catch panics at exported boundaries where required and map them to stable error codes; do not hide internal errors as success.
8. Keep unsafe blocks small and add a safety comment explaining proof obligations. Avoid global mutable state unless synchronized and contractually justified.
9. Update header, generated bindings, Android JNI and every Swift consumer together. Version incompatible layouts/symbol semantics and add a migration path.
10. Test valid, null, empty, invalid UTF-8, oversized, malformed, overflow, concurrent, cancellation and failure inputs. Add fuzz/Miri/sanitizer checks where supported.
11. Cross-build all supported ABIs and inspect exported symbols and library load behavior.

## Required evidence
Contract diff/version; symbol list; ownership/lifetime table; error map; consumer changes; boundary-case tests; fuzz/Miri/sanitizer status; ABI and runtime evidence.

## Fail conditions
Unclear ownership, dangling pointer, double-free, integer overflow, invalid memory access, panic crossing FFI, silent ABI drift, missing consumer update or ambiguous serialization blocks merge/release.
