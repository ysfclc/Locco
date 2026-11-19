# Skill: Rust FFI boundary

When: adding/changing ABI or Kotlin/Swift bridge. Preconditions: accepted FFI contract and ownership rules.

Procedure: minimal versioned surface; validate pointers/length/encoding; copy inputs before async use; catch panics; stable errors; explicit buffer ownership/free; small unsafe blocks with safety comments; ABI/invalid-input tests; fuzz where feasible.

Verify no unwind crosses boundary, symbols match contract, Miri/fuzz where supported.

Failure: use-after-free, double-free, overflow, invalid UTF-8 or ABI drift. Unexplained crash blocks release.