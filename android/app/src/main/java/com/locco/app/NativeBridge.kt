package com.locco.app

/**
 * Thin platform adapter. Schema and migration statements are owned by locco-kernel.
 */
internal object NativeBridge {
    private const val EXPECTED_ABI_VERSION = 2

    init {
        System.loadLibrary("locco_ffi")
        val actual = nativeAbiVersion()
        check(actual == EXPECTED_ABI_VERSION) {
            "Locco native ABI mismatch: expected $EXPECTED_ABI_VERSION, got $actual"
        }
    }

    external fun nativeAbiVersion(): Int
    external fun nativePing(): Int
    external fun nativeSchemaVersion(): Int
    external fun nativeMigrationStatementCount(version: Int): Int
    external fun nativeMigrationStatement(version: Int, index: Int): String?

    fun databaseSchemaVersion(): Int {
        val version = nativeSchemaVersion()
        check(version > 0) { "Locco kernel returned an invalid schema version: $version" }
        return version
    }

    fun migrationStatements(version: Int): List<String> {
        val count = nativeMigrationStatementCount(version)
        check(count > 0) { "Locco kernel has no migration for schema version $version" }
        return List(count) { index ->
            nativeMigrationStatement(version, index)
                ?.takeIf(String::isNotBlank)
                ?: error("Locco kernel returned an invalid migration statement ($version, $index)")
        }
    }
}
