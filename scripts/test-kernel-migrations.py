#!/usr/bin/env python3
"""Exercise the exact Rust-owned SQLite migration catalogue through ABI v2."""

from __future__ import annotations

import ctypes
import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LIBRARY = ROOT / "core" / "target" / "debug" / "liblocco_ffi.so"
if not LIBRARY.is_file():
    raise SystemExit(f"Missing migration FFI library: {LIBRARY}")

ffi = ctypes.CDLL(str(LIBRARY))
ffi.locco_schema_version.restype = ctypes.c_uint32
ffi.locco_migration_statement_count.argtypes = [ctypes.c_uint32]
ffi.locco_migration_statement_count.restype = ctypes.c_uint32
ffi.locco_migration_statement_len.argtypes = [ctypes.c_uint32, ctypes.c_uint32]
ffi.locco_migration_statement_len.restype = ctypes.c_int64
ffi.locco_migration_statement_copy.argtypes = [
    ctypes.c_uint32,
    ctypes.c_uint32,
    ctypes.POINTER(ctypes.c_uint8),
    ctypes.c_size_t,
]
ffi.locco_migration_statement_copy.restype = ctypes.c_int32


def statements(version: int) -> list[str]:
    count = ffi.locco_migration_statement_count(version)
    if count == 0:
        raise AssertionError(f"Missing migration {version}")
    result: list[str] = []
    for index in range(count):
        length = ffi.locco_migration_statement_len(version, index)
        if length <= 0:
            raise AssertionError(f"Invalid statement length at {version}:{index}")
        output = (ctypes.c_uint8 * length)()
        status = ffi.locco_migration_statement_copy(version, index, output, length)
        if status != 0:
            raise AssertionError(f"FFI copy failed at {version}:{index}: {status}")
        result.append(bytes(output).decode("utf-8"))
    return result


def apply(connection: sqlite3.Connection, version: int) -> None:
    for statement in statements(version):
        connection.execute(statement)


def test_fresh_install() -> None:
    connection = sqlite3.connect(":memory:")
    connection.execute("PRAGMA foreign_keys=ON")
    for version in range(1, ffi.locco_schema_version() + 1):
        apply(connection, version)
    names = {
        row[0]
        for row in connection.execute(
            "SELECT name FROM sqlite_master WHERE type='table'"
        )
    }
    expected = {
        "sessions",
        "notes",
        "sources",
        "entities",
        "entity_relations",
        "kernel_events",
        "note_sources",
    }
    assert expected.issubset(names), expected - names
    assert connection.execute(
        "SELECT name FROM sqlite_master WHERE type='index' AND name='note_sources_source_time'"
    ).fetchone()


def test_populated_v1_upgrade() -> None:
    connection = sqlite3.connect(":memory:")
    connection.execute("PRAGMA foreign_keys=ON")
    apply(connection, 1)
    connection.execute("INSERT INTO sessions VALUES('s1', 'preserved', 1)")
    connection.execute("INSERT INTO notes VALUES('n1', 's1', 'existing note', 2)")
    connection.execute(
        "INSERT INTO sources(id, kind, captured_at) VALUES('src1', 'web', 3)"
    )

    apply(connection, 2)

    assert connection.execute(
        "SELECT title FROM sessions WHERE id='s1'"
    ).fetchone() == ("preserved",)
    assert connection.execute(
        "SELECT content FROM notes WHERE id='n1'"
    ).fetchone() == ("existing note",)

    connection.execute(
        "INSERT INTO note_sources VALUES('n1', 'src1', 'supporting excerpt', 4)"
    )
    assert connection.execute(
        "SELECT ns.excerpt FROM note_sources ns "
        "JOIN notes n ON n.id=ns.note_id "
        "JOIN sources s ON s.id=ns.source_id"
    ).fetchone() == ("supporting excerpt",)

    try:
        connection.execute(
            "INSERT INTO note_sources VALUES('n1', 'missing', 'orphan', 5)"
        )
    except sqlite3.IntegrityError:
        pass
    else:
        raise AssertionError("Foreign key accepted a missing source")

    connection.execute("DELETE FROM notes WHERE id='n1'")
    assert connection.execute(
        "SELECT count(*) FROM note_sources"
    ).fetchone() == (0,)


if __name__ == "__main__":
    assert ffi.locco_schema_version() == 2
    assert ffi.locco_migration_statement_count(3) == 0
    test_fresh_install()
    test_populated_v1_upgrade()
    print("PASS: Rust ABI migration catalogue; fresh schema; populated v1 -> v2; evidence joins; FK and cascade integrity.")
