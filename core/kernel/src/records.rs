//! Stable, validated domain records shared by native platform adapters.
//!
//! These types deliberately contain no Android, SQLite, network, or model APIs.
//! Persistence and FFI adapters must preserve these invariants rather than
//! inventing platform-specific domain rules.

use std::fmt;

const MAX_ID_BYTES: usize = 128;
const MAX_KIND_BYTES: usize = 80;
const MAX_RELATION_BYTES: usize = 80;
const MAX_NAME_BYTES: usize = 512;

#[derive(Clone, Debug, Eq, Hash, PartialEq)]
pub struct RecordId(String);

impl RecordId {
    pub fn parse(value: impl Into<String>) -> Result<Self, RecordError> {
        let value = value.into();
        if value.is_empty()
            || value.len() > MAX_ID_BYTES
            || value.trim() != value
            || value.chars().any(char::is_control)
        {
            return Err(RecordError::InvalidId);
        }
        Ok(Self(value))
    }

    pub fn as_str(&self) -> &str {
        &self.0
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum RecordError {
    InvalidId,
    InvalidKind,
    InvalidName,
    InvalidRelation,
    InvalidTimestamp,
    MissingEvidencePayload,
}

impl fmt::Display for RecordError {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        let message = match self {
            Self::InvalidId => {
                "record ID must be non-empty, bounded, and contain no control characters"
            }
            Self::InvalidKind => "record kind must be non-empty and bounded",
            Self::InvalidName => "canonical name must be non-empty and bounded",
            Self::InvalidRelation => "relation type must be non-empty and bounded",
            Self::InvalidTimestamp => "timestamp must be non-negative",
            Self::MissingEvidencePayload => "evidence requires an excerpt or content hash",
        };
        formatter.write_str(message)
    }
}

impl std::error::Error for RecordError {}

fn validate_label(value: &str, max_bytes: usize) -> bool {
    !value.is_empty()
        && value.len() <= max_bytes
        && value.trim() == value
        && !value.chars().any(char::is_control)
}

fn validate_timestamp(timestamp: i64) -> Result<(), RecordError> {
    if timestamp < 0 {
        Err(RecordError::InvalidTimestamp)
    } else {
        Ok(())
    }
}

/// A chosen origin from which evidence was acquired. It is not itself evidence.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct SourceRecord {
    pub id: RecordId,
    pub kind: String,
    pub locator: Option<String>,
    pub title: Option<String>,
    pub captured_at: i64,
}

impl SourceRecord {
    pub fn new(
        id: RecordId,
        kind: impl Into<String>,
        locator: Option<String>,
        title: Option<String>,
        captured_at: i64,
    ) -> Result<Self, RecordError> {
        let kind = kind.into();
        if !validate_label(&kind, MAX_KIND_BYTES) {
            return Err(RecordError::InvalidKind);
        }
        validate_timestamp(captured_at)?;
        Ok(Self {
            id,
            kind,
            locator,
            title,
            captured_at,
        })
    }
}

/// An immutable excerpt/hash tied to its source. Interpretation belongs elsewhere.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct EvidenceRecord {
    pub id: RecordId,
    pub source_id: RecordId,
    pub excerpt: Option<String>,
    pub content_hash: Option<String>,
    pub captured_at: i64,
}

impl EvidenceRecord {
    pub fn new(
        id: RecordId,
        source_id: RecordId,
        excerpt: Option<String>,
        content_hash: Option<String>,
        captured_at: i64,
    ) -> Result<Self, RecordError> {
        let has_excerpt = matches!(excerpt.as_deref(), Some(value) if !value.is_empty());
        let has_content_hash = matches!(content_hash.as_deref(), Some(value) if !value.is_empty());
        if !has_excerpt && !has_content_hash {
            return Err(RecordError::MissingEvidencePayload);
        }
        validate_timestamp(captured_at)?;
        Ok(Self {
            id,
            source_id,
            excerpt,
            content_hash,
            captured_at,
        })
    }
}

/// A stable identity. Descriptions are descriptive data, not canonical identity.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct EntityRecord {
    pub id: RecordId,
    pub kind: String,
    pub canonical_name: String,
    pub description: Option<String>,
    pub created_at: i64,
}

impl EntityRecord {
    pub fn new(
        id: RecordId,
        kind: impl Into<String>,
        canonical_name: impl Into<String>,
        description: Option<String>,
        created_at: i64,
    ) -> Result<Self, RecordError> {
        let kind = kind.into();
        let canonical_name = canonical_name.into();
        if !validate_label(&kind, MAX_KIND_BYTES) {
            return Err(RecordError::InvalidKind);
        }
        if !validate_label(&canonical_name, MAX_NAME_BYTES) {
            return Err(RecordError::InvalidName);
        }
        validate_timestamp(created_at)?;
        Ok(Self {
            id,
            kind,
            canonical_name,
            description,
            created_at,
        })
    }
}

/// A typed directed edge. Duplicate detection is a storage/index concern.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct EntityRelation {
    pub id: RecordId,
    pub from_entity_id: RecordId,
    pub relation_type: String,
    pub to_entity_id: RecordId,
    pub created_at: i64,
}

impl EntityRelation {
    pub fn new(
        id: RecordId,
        from_entity_id: RecordId,
        relation_type: impl Into<String>,
        to_entity_id: RecordId,
        created_at: i64,
    ) -> Result<Self, RecordError> {
        let relation_type = relation_type.into();
        if !validate_label(&relation_type, MAX_RELATION_BYTES) {
            return Err(RecordError::InvalidRelation);
        }
        validate_timestamp(created_at)?;
        Ok(Self {
            id,
            from_entity_id,
            relation_type,
            to_entity_id,
            created_at,
        })
    }
}

/// Append-only fact that something happened in the local kernel.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct KernelEvent {
    pub id: RecordId,
    pub event_type: String,
    pub occurred_at: i64,
}

impl KernelEvent {
    pub fn new(
        id: RecordId,
        event_type: impl Into<String>,
        occurred_at: i64,
    ) -> Result<Self, RecordError> {
        let event_type = event_type.into();
        if !validate_label(&event_type, MAX_KIND_BYTES) {
            return Err(RecordError::InvalidKind);
        }
        validate_timestamp(occurred_at)?;
        Ok(Self {
            id,
            event_type,
            occurred_at,
        })
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn id(value: &str) -> RecordId {
        RecordId::parse(value).expect("valid test ID")
    }

    #[test]
    fn rejects_empty_unbounded_and_control_character_ids() {
        assert_eq!(RecordId::parse(""), Err(RecordError::InvalidId));
        assert_eq!(RecordId::parse("bad\nid"), Err(RecordError::InvalidId));
        assert_eq!(
            RecordId::parse("x".repeat(MAX_ID_BYTES + 1)),
            Err(RecordError::InvalidId)
        );
    }

    #[test]
    fn source_requires_a_valid_kind_and_timestamp() {
        assert_eq!(
            SourceRecord::new(id("s1"), " ", None, None, 1).unwrap_err(),
            RecordError::InvalidKind
        );
        assert_eq!(
            SourceRecord::new(id("s1"), "web", None, None, -1).unwrap_err(),
            RecordError::InvalidTimestamp
        );
        let source = SourceRecord::new(
            id("s1"),
            "web",
            Some("https://example.test".into()),
            None,
            1,
        )
        .unwrap();
        assert_eq!(source.id.as_str(), "s1");
    }

    #[test]
    fn evidence_must_preserve_a_source_and_payload_reference() {
        assert_eq!(
            EvidenceRecord::new(id("ev1"), id("src1"), None, None, 1).unwrap_err(),
            RecordError::MissingEvidencePayload
        );
        let evidence = EvidenceRecord::new(
            id("ev1"),
            id("src1"),
            Some("original excerpt".into()),
            None,
            2,
        )
        .unwrap();
        assert_eq!(evidence.source_id.as_str(), "src1");
    }

    #[test]
    fn entity_identity_requires_kind_and_canonical_name() {
        assert_eq!(
            EntityRecord::new(id("e1"), "person", " ", None, 1).unwrap_err(),
            RecordError::InvalidName
        );
        let entity = EntityRecord::new(id("e1"), "person", "Ada Lovelace", None, 1).unwrap();
        assert_eq!(entity.canonical_name, "Ada Lovelace");
    }

    #[test]
    fn relationships_are_typed_and_events_are_timestamped() {
        let relation = EntityRelation::new(id("r1"), id("e1"), "works_at", id("e2"), 3).unwrap();
        assert_eq!(relation.relation_type, "works_at");
        let event = KernelEvent::new(id("ev1"), "source_captured", 4).unwrap();
        assert_eq!(event.occurred_at, 4);
        assert_eq!(
            KernelEvent::new(id("ev2"), " ", 4).unwrap_err(),
            RecordError::InvalidKind
        );
    }
}
