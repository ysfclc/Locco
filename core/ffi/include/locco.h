#ifndef LOCCO_FFI_H
#define LOCCO_FFI_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Incompatible native bridge contract version. Current: 2. */
uint32_t locco_abi_version(void);

/* Foundation link smoke test. */
int32_t locco_core_ping(void);

/* Canonical SQLite schema and ordered Rust-owned migrations. */
uint32_t locco_schema_version(void);
uint32_t locco_migration_statement_count(uint32_t version);
int64_t locco_migration_statement_len(uint32_t version, uint32_t index);

/*
 * Copies one UTF-8 SQL statement into caller-owned memory without a NUL byte.
 * The caller must provide at least the length returned above.
 * Return codes: 0 success, -1 unknown statement, -2 null output,
 * -3 output capacity too small.
 */
int32_t locco_migration_statement_copy(
    uint32_t version,
    uint32_t index,
    uint8_t *output,
    size_t capacity
);

/* Reserved for future Rust-owned buffers; current migration calls allocate none. */
void locco_buffer_free(uint8_t *ptr, size_t len);

#ifdef __cplusplus
}
#endif

#endif /* LOCCO_FFI_H */
