#![deny(unsafe_op_in_unsafe_fn)]

//! Foundation-only domain kernel. Product behavior is intentionally absent.

pub mod migrations;
pub mod records;

/// Version of the stable Locco bridge contract.
pub const ABI_VERSION: u32 = 2;

/// Foundation marker used by shell smoke tests.
pub const CORE_PING: i32 = 0;

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn foundation_contract_is_stable() {
        assert_eq!(ABI_VERSION, 1);
        assert_eq!(CORE_PING, 0);
    }
}
