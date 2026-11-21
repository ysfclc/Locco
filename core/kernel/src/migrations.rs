//! Canonical, ordered SQLite migrations owned by the Rust kernel.
//!
//! Platform adapters execute these statements inside their database transaction.
//! They must not define or mutate the schema independently.

pub const SCHEMA_VERSION: u32 = 2;

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

/// Number of SQL statements in one migration version; zero means unsupported.
pub fn migration_statement_count(version: u32) -> u32 {
    match version {
        1 => V1.len() as u32,
        2 => V2.len() as u32,
        _ => 0,
    }
}

/// A statement is static, UTF-8, and valid for the indicated migration version.
pub fn migration_statement(version: u32, index: u32) -> Option<&'static str> {
    let statements = match version {
        1 => V1,
        2 => V2,
        _ => return None,
    };
    statements.get(index as usize).copied()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn schema_versions_are_sequential_and_supported() {
        assert_eq!(SCHEMA_VERSION, 2);
        assert!(migration_statement_count(1) > 0);
        assert!(migration_statement_count(2) > 0);
        assert_eq!(migration_statement_count(3), 0);
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
    }

    #[test]
    fn evidence_link_migration_preserves_both_parent_records() {
        let sql = migration_statement(2, 0).unwrap();
        assert!(sql.contains("REFERENCES notes(id) ON DELETE CASCADE"));
        assert!(sql.contains("REFERENCES sources(id) ON DELETE CASCADE"));
        assert!(sql.contains("PRIMARY KEY(note_id, source_id)"));
    }
}
