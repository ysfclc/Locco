//! Canonical, ordered SQLite migrations owned by the Rust kernel.
//!
//! Platform adapters execute these statements inside their database transaction.
//! They must not define or mutate the schema independently.

pub const SCHEMA_VERSION: u32 = 4;

const V1: &[&str] = &[
    "CREATE TABLE sessions (id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, created_at INTEGER NOT NULL)",
    "CREATE TABLE notes (id TEXT PRIMARY KEY NOT NULL, session_id TEXT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE, content TEXT NOT NULL, created_at INTEGER NOT NULL)",
    "CREATE INDEX notes_session_time ON notes(session_id, created_at)",
    "CREATE TABLE sources (id TEXT PRIMARY KEY NOT NULL, kind TEXT NOT NULL, uri TEXT, title TEXT, captured_at INTEGER NOT NULL, metadata_json TEXT NOT NULL DEFAULT '{}')",
    "CREATE TABLE entities (id TEXT PRIMARY KEY NOT NULL, kind TEXT NOT NULL, canonical_name TEXT NOT NULL, description TEXT, created_at INTEGER NOT NULL, metadata_json TEXT NOT NULL DEFAULT '{}')",
    "CREATE TABLE entity_relations (id TEXT PRIMARY KEY NOT NULL, from_entity_id TEXT NOT NULL REFERENCES entities(id) ON DELETE CASCADE, relation_type TEXT NOT NULL, to_entity_id TEXT NOT NULL REFERENCES entities(id) ON DELETE CASCADE, created_at INTEGER NOT NULL, metadata_json TEXT NOT NULL DEFAULT '{}')",
    "CREATE INDEX entity_relations_from ON entity_relations(from_entity_id)",
    "CREATE INDEX entity_relations_to ON entity_relations(to_entity_id)",
    "CREATE TABLE kernel_events (id TEXT PRIMARY KEY NOT NULL, event_type TEXT NOT NULL, occurred_at INTEGER NOT NULL, payload_json TEXT NOT NULL DEFAULT '{}')",
];

const V2: &[&str] = &[
    "CREATE TABLE note_sources (note_id TEXT NOT NULL REFERENCES notes(id) ON DELETE CASCADE, source_id TEXT NOT NULL REFERENCES sources(id) ON DELETE CASCADE, excerpt TEXT, attached_at INTEGER NOT NULL, PRIMARY KEY(note_id, source_id), CHECK(excerpt IS NOT NULL OR length(source_id) > 0))",
    "CREATE INDEX note_sources_source_time ON note_sources(source_id, attached_at)",
];

/// Minimal, privacy-preserving Android observation receipts. Notification text,
/// title, actions, extras and raw Notification payloads are never persisted.
const V3: &[&str] = &[
    "CREATE TABLE app_observations (id TEXT PRIMARY KEY NOT NULL CHECK(length(id) BETWEEN 1 AND 128), dedupe_key TEXT NOT NULL UNIQUE CHECK(length(dedupe_key)=64), source_package TEXT NOT NULL CHECK(length(source_package) BETWEEN 3 AND 255), event_type TEXT NOT NULL CHECK(event_type IN ('notification_posted')), occurred_at INTEGER NOT NULL CHECK(occurred_at >= 0), received_at INTEGER NOT NULL CHECK(received_at >= 0), acquisition_method TEXT NOT NULL CHECK(acquisition_method='android_notification_listener'), category TEXT CHECK(category IS NULL OR length(category) <= 80), redacted_fields_json TEXT NOT NULL DEFAULT '[\"notification_text\",\"notification_title\",\"notification_extras\"]')",
    "CREATE INDEX app_observations_received ON app_observations(received_at DESC)",
    "CREATE INDEX app_observations_source_time ON app_observations(source_package, occurred_at DESC)",
];

const V4: &[&str] = &[
    "CREATE TABLE app_usage_events (id TEXT PRIMARY KEY NOT NULL CHECK(length(id) BETWEEN 1 AND 128), dedupe_key TEXT NOT NULL UNIQUE CHECK(length(dedupe_key)=64), source_package TEXT NOT NULL CHECK(length(source_package) BETWEEN 3 AND 255), event_type TEXT NOT NULL CHECK(event_type IN ('app_foregrounded','app_backgrounded')), occurred_at INTEGER NOT NULL CHECK(occurred_at >= 0), received_at INTEGER NOT NULL CHECK(received_at >= 0), acquisition_method TEXT NOT NULL CHECK(acquisition_method='android_usage_events'))",
    "CREATE INDEX app_usage_events_received ON app_usage_events(received_at DESC)",
    "CREATE INDEX app_usage_events_source_time ON app_usage_events(source_package, occurred_at DESC)",
];

/// Number of SQL statements in one migration version; zero means unsupported.
pub fn migration_statement_count(version: u32) -> u32 {
    match version {
        1 => V1.len() as u32,
        2 => V2.len() as u32,
        3 => V3.len() as u32,
        4 => V4.len() as u32,
        _ => 0,
    }
}

/// A statement is static, UTF-8, and valid for the indicated migration version.
pub fn migration_statement(version: u32, index: u32) -> Option<&'static str> {
    let statements = match version {
        1 => V1,
        2 => V2,
        3 => V3,
        4 => V4,
        _ => return None,
    };
    statements.get(index as usize).copied()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn schema_versions_are_sequential_and_supported() {
        assert_eq!(SCHEMA_VERSION, 3);
        assert!(migration_statement_count(1) > 0);
        assert!(migration_statement_count(2) > 0);
        assert!(migration_statement_count(3) > 0);
        assert_eq!(migration_statement_count(4), 0);
    }

    #[test]
    fn every_migration_statement_is_available_and_nonempty() {
        for version in 1..=SCHEMA_VERSION {
            for index in 0..migration_statement_count(version) {
                let statement = migration_statement(version, index).unwrap();
                assert!(!statement.trim().is_empty());
            }
        }
        assert!(migration_statement(2, migration_statement_count(2)).is_none());
        assert!(migration_statement(3, 0)
            .unwrap()
            .contains("dedupe_key TEXT NOT NULL UNIQUE"));
    }

    #[test]
    fn evidence_link_migration_preserves_both_parent_records() {
        let sql = migration_statement(2, 0).unwrap();
        assert!(sql.contains("REFERENCES notes(id) ON DELETE CASCADE"));
        assert!(sql.contains("REFERENCES sources(id) ON DELETE CASCADE"));
        assert!(sql.contains("PRIMARY KEY(note_id, source_id)"));
    }
}
