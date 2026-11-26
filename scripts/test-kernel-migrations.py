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
        "app_observations",
        "app_usage_events",
        "evidence",
    }
    assert expected.issubset(names), expected - names
    assert connection.execute(
        "SELECT name FROM sqlite_master WHERE type='index' AND name='note_sources_source_time'"
    ).fetchone()
    assert connection.execute(
        "SELECT name FROM sqlite_master WHERE type='index' AND name='notes_session_sort_order'"
    ).fetchone()
    note_columns = {row[1] for row in connection.execute("PRAGMA table_info(notes)")}
    assert "sort_order" in note_columns


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


def test_v2_to_v3_upgrade_preserves_kernel_data() -> None:
    connection = sqlite3.connect(":memory:")
    connection.execute("PRAGMA foreign_keys=ON")
    apply(connection, 1)
    apply(connection, 2)
    connection.execute("INSERT INTO sessions VALUES('keep-s1', 'keep me', 7)")
    connection.execute("INSERT INTO notes VALUES('keep-n1', 'keep-s1', 'keep this note', 8)")
    apply(connection, 3)
    assert connection.execute("SELECT title FROM sessions WHERE id='keep-s1'").fetchone() == ("keep me",)
    assert connection.execute("SELECT content FROM notes WHERE id='keep-n1'").fetchone() == ("keep this note",)
    connection.execute(
        "INSERT INTO app_observations(id,dedupe_key,source_package,event_type,occurred_at,received_at,acquisition_method,category) "
        "VALUES('obs1','aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa','com.example.app','notification_posted',10,11,'android_notification_listener','message')"
    )
    try:
        connection.execute(
            "INSERT INTO app_observations(id,dedupe_key,source_package,event_type,occurred_at,received_at,acquisition_method) "
            "VALUES('obs2','aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa','com.example.app','notification_posted',10,12,'android_notification_listener')"
        )
    except sqlite3.IntegrityError:
        pass
    else:
        raise AssertionError("Duplicate observation idempotency key was accepted")
    assert connection.execute("SELECT count(*) FROM app_observations").fetchone() == (1,)


def test_v3_to_v4_usage_events() -> None:
    connection = sqlite3.connect(":memory:")
    connection.execute("PRAGMA foreign_keys=ON")
    for version in range(1, 4):
        apply(connection, version)
    apply(connection, 4)
    key = "b" * 64
    connection.execute(
        "INSERT INTO app_usage_events(id,dedupe_key,source_package,event_type,occurred_at,received_at,acquisition_method) "
        "VALUES('usage1',?,'com.example.app','app_foregrounded',10,11,'android_usage_events')",
        (key,),
    )
    try:
        connection.execute(
            "INSERT INTO app_usage_events(id,dedupe_key,source_package,event_type,occurred_at,received_at,acquisition_method) "
            "VALUES('usage2',?,'com.example.app','app_foregrounded',10,12,'android_usage_events')",
            (key,),
        )
    except sqlite3.IntegrityError:
        pass
    else:
        raise AssertionError("Duplicate usage event idempotency key was accepted")
    assert connection.execute("SELECT count(*) FROM app_usage_events").fetchone() == (1,)


def test_v4_to_v5_evidence_records() -> None:
    connection = sqlite3.connect(":memory:")
    connection.execute("PRAGMA foreign_keys=ON")
    for version in range(1, 5):
        apply(connection, version)
    connection.execute("INSERT INTO sources(id,kind,captured_at) VALUES('src-e1','web',5)")
    apply(connection, 5)
    connection.execute(
        "INSERT INTO evidence(id,source_id,excerpt,content_hash,captured_at) VALUES('ev1','src-e1','quote',NULL,6)"
    )
    connection.execute(
        "INSERT INTO evidence(id,source_id,excerpt,content_hash,captured_at) VALUES('ev2','src-e1',NULL,'sha256:abc',7)"
    )
    assert connection.execute("SELECT count(*) FROM evidence WHERE source_id='src-e1'").fetchone() == (2,)
    try:
        connection.execute(
            "INSERT INTO evidence(id,source_id,excerpt,content_hash,captured_at) VALUES('ev3','missing',NULL,'sha256:def',8)"
        )
    except sqlite3.IntegrityError:
        pass
    else:
        raise AssertionError("Evidence accepted a missing source")
    connection.execute("DELETE FROM sources WHERE id='src-e1'")
    assert connection.execute("SELECT count(*) FROM evidence").fetchone() == (0,)


def test_v5_to_v6_note_order() -> None:
    connection = sqlite3.connect(":memory:")
    connection.execute("PRAGMA foreign_keys=ON")
    for version in range(1, 6):
        apply(connection, version)
    connection.execute("INSERT INTO sessions VALUES('ordered-s1', 'ordered', 1)")
    connection.executemany(
        "INSERT INTO notes VALUES(?,?,?,?)",
        [
            ("note-z", "ordered-s1", "same-time Z", 10),
            ("note-a", "ordered-s1", "same-time A", 10),
            ("note-old", "ordered-s1", "older", 9),
        ],
    )

    apply(connection, 6)
    rows = connection.execute(
        "SELECT id,sort_order FROM notes WHERE session_id='ordered-s1' ORDER BY sort_order"
    ).fetchall()
    # Existing v5 rows retain their previous deterministic (created_at,id) display order.
    assert rows == [("note-old", 0), ("note-a", 1), ("note-z", 2)], rows

    connection.execute(
        "INSERT INTO notes(id,session_id,content,created_at,sort_order) "
        "SELECT ?,?,?,?,COALESCE(MAX(sort_order),-1)+1 FROM notes WHERE session_id=?",
        ("note-new", "ordered-s1", "appended", 10, "ordered-s1"),
    )
    assert connection.execute(
        "SELECT id FROM notes WHERE session_id='ordered-s1' ORDER BY sort_order"
    ).fetchall() == [("note-old",), ("note-a",), ("note-z",), ("note-new",)]

    try:
        connection.execute(
            "INSERT INTO notes(id,session_id,content,created_at,sort_order) "
            "VALUES('duplicate-order','ordered-s1','duplicate',11,1)"
        )
    except sqlite3.IntegrityError:
        pass
    else:
        raise AssertionError("Duplicate per-session note order was accepted")


if __name__ == "__main__":
    assert ffi.locco_schema_version() == 6
    assert ffi.locco_migration_statement_count(7) == 0
    test_fresh_install()
    test_populated_v1_upgrade()
    test_v2_to_v3_upgrade_preserves_kernel_data()
    test_v3_to_v4_usage_events()
    test_v4_to_v5_evidence_records()
    test_v5_to_v6_note_order()
    print("PASS: Rust ABI migration catalogue through v6; evidence integrity, source FK/cascade and stable session-note ordering.")
