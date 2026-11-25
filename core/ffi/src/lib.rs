#![deny(unsafe_op_in_unsafe_fn)]

//! Versioned C ABI and Android JNI adapter. Domain and migration data live in
//! locco-kernel; this crate only translates the stable boundary.

use std::ffi::c_void;
use std::ptr;

use jni::objects::JClass;
use jni::sys::{jint, jstring};
use jni::JNIEnv;

#[no_mangle]
pub extern "C" fn locco_abi_version() -> u32 {
    locco_kernel::ABI_VERSION
}

#[no_mangle]
pub extern "C" fn locco_core_ping() -> i32 {
    locco_kernel::CORE_PING
}

#[no_mangle]
pub extern "C" fn locco_schema_version() -> u32 {
    locco_kernel::migrations::SCHEMA_VERSION
}

#[no_mangle]
pub extern "C" fn locco_migration_statement_count(version: u32) -> u32 {
    locco_kernel::migrations::migration_statement_count(version)
}

/// Returns UTF-8 byte length, or -1 when the migration statement does not exist.
#[no_mangle]
pub extern "C" fn locco_migration_statement_len(version: u32, index: u32) -> i64 {
    locco_kernel::migrations::migration_statement(version, index)
        .map(|statement| statement.len() as i64)
        .unwrap_or(-1)
}

/// Copies a statement into caller-owned memory. No NUL terminator is appended.
/// Stable return codes: 0 success, -1 unknown statement, -2 null output,
/// -3 output capacity too small.
///
/// # Safety
/// When the statement exists, output must point to capacity writable bytes.
#[no_mangle]
pub unsafe extern "C" fn locco_migration_statement_copy(
    version: u32,
    index: u32,
    output: *mut u8,
    capacity: usize,
) -> i32 {
    let Some(statement) = locco_kernel::migrations::migration_statement(version, index) else {
        return -1;
    };
    if output.is_null() {
        return -2;
    }
    if capacity < statement.len() {
        return -3;
    }
    // SAFETY: output is non-null and capacity was checked against the exact
    // number of bytes copied. The caller owns and guarantees this writable span.
    unsafe {
        ptr::copy_nonoverlapping(statement.as_ptr(), output, statement.len());
    }
    0
}

#[no_mangle]
pub extern "C" fn locco_buffer_free(_ptr: *mut u8, _len: usize) {
    // Reserved for future owned output buffers; migration SQL is copied into
    // caller-owned memory and does not cross the ABI as an allocated pointer.
}

#[no_mangle]
pub extern "system" fn Java_com_locco_app_NativeBridge_nativeAbiVersion(
    _env: *mut c_void,
    _class: *mut c_void,
) -> i32 {
    locco_abi_version() as i32
}

#[no_mangle]
pub extern "system" fn Java_com_locco_app_NativeBridge_nativePing(
    _env: *mut c_void,
    _class: *mut c_void,
) -> i32 {
    locco_core_ping()
}

#[no_mangle]
pub extern "system" fn Java_com_locco_app_NativeBridge_nativeSchemaVersion(
    _env: JNIEnv<'_>,
    _class: JClass<'_>,
) -> jint {
    locco_schema_version() as jint
}

#[no_mangle]
pub extern "system" fn Java_com_locco_app_NativeBridge_nativeMigrationStatementCount(
    _env: JNIEnv<'_>,
    _class: JClass<'_>,
    version: jint,
) -> jint {
    if version < 0 {
        return 0;
    }
    locco_migration_statement_count(version as u32) as jint
}

#[no_mangle]
pub extern "system" fn Java_com_locco_app_NativeBridge_nativeMigrationStatement(
    env: JNIEnv<'_>,
    _class: JClass<'_>,
    version: jint,
    index: jint,
) -> jstring {
    if version < 0 || index < 0 {
        return ptr::null_mut();
    }
    match locco_kernel::migrations::migration_statement(version as u32, index as u32) {
        Some(statement) => env
            .new_string(statement)
            .map(|value| value.into_raw())
            .unwrap_or(ptr::null_mut()),
        None => ptr::null_mut(),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn c_abi_exposes_versioned_migrations() {
        assert_eq!(locco_abi_version(), 2);
        assert_eq!(locco_schema_version(), 6);
        assert!(locco_migration_statement_count(1) > 0);
        assert!(locco_migration_statement_count(2) > 0);
        assert!(locco_migration_statement_count(3) > 0);
        assert!(locco_migration_statement_count(4) > 0);
        assert!(locco_migration_statement_count(5) > 0);
        assert!(locco_migration_statement_count(6) > 0);
        assert_eq!(locco_migration_statement_count(7), 0);
    }

    #[test]
    fn c_abi_copies_sql_into_caller_owned_memory() {
        let mut output = vec![0_u8; 128];
        let sql = locco_kernel::migrations::migration_statement(6, 0).unwrap();
        output.resize(sql.len(), 0);
        assert_eq!(locco_migration_statement_len(6, 0), sql.len() as i64);
        // SAFETY: output is a writable Vec whose length equals the required SQL byte length.
        unsafe {
            assert_eq!(
                locco_migration_statement_copy(6, 0, output.as_mut_ptr(), output.len()),
                0
            );
            assert_eq!(output, sql.as_bytes());
            assert_eq!(
                locco_migration_statement_copy(7, 0, output.as_mut_ptr(), output.len()),
                -1
            );
            assert_eq!(
                locco_migration_statement_copy(6, 0, output.as_mut_ptr(), 1),
                -3
            );
            assert_eq!(
                locco_migration_statement_copy(6, 0, ptr::null_mut(), output.len()),
                -2
            );
        }
    }
}
