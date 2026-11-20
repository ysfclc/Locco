#![deny(unsafe_op_in_unsafe_fn)]

//! Minimal Android JNI and cross-platform C ABI surface. No product logic.
//! JNI pointer arguments are opaque and are never dereferenced in this scaffold.

use std::ffi::c_void;
use std::panic::catch_unwind;

fn abi_version_value() -> u32 {
    locco_kernel::ABI_VERSION
}

fn core_ping_value() -> i32 {
    locco_kernel::CORE_PING
}

#[no_mangle]
pub extern "C" fn locco_abi_version() -> u32 {
    abi_version_value()
}

#[no_mangle]
pub extern "C" fn locco_core_ping() -> i32 {
    core_ping_value()
}

#[no_mangle]
pub extern "C" fn locco_buffer_free(_ptr: *mut u8, _len: usize) {
    // Reserved contract symbol. No buffers are allocated by the foundation scaffold.
}

#[no_mangle]
pub extern "system" fn Java_com_locco_app_NativeBridge_nativeAbiVersion(
    _env: *mut c_void,
    _class: *mut c_void,
) -> i32 {
    catch_unwind(|| abi_version_value() as i32).unwrap_or(-1)
}

#[no_mangle]
pub extern "system" fn Java_com_locco_app_NativeBridge_nativePing(
    _env: *mut c_void,
    _class: *mut c_void,
) -> i32 {
    catch_unwind(core_ping_value).unwrap_or(-1)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn c_abi_returns_contract_values() {
        assert_eq!(locco_abi_version(), 1);
        assert_eq!(locco_core_ping(), 0);
    }
}
