"""Unit tests for the Locco pull-request change contract."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from check_change_contract import issue_numbers, validate_pr_body  # noqa: E402


VALID_BODY = """## Change classification
Bug fix — prevent data loss on interrupted import.

## Baseline and current application identity
Base commit: a8e6763e384c3fe1f1f508ea216210d7213652b8
Application ID: com.locco.app
versionCode: 1
versionName: 0.1.0-foundation

## User feedback records
- LOC-0002 — #2 — continuity policy and required evidence.

## Existing behaviour inventory
Reviewed import, resume, cancellation, search and restore flows. These must remain intact.

## Product boundary and app continuity
- [x] Existing Locco application is extended/repaired; no replacement application is created.
Package identity and stored data remain unchanged.

## Impact analysis
Rust import state machine and Android lifecycle adapter; no new permission or dependency.

## Regression coverage
Automated: regression test reproduces interrupted import and verifies resume and integrity.

## Data, migration and upgrade safety
No schema change. Existing records and preferences remain untouched; upgrade path inspected.

## Verification evidence
PASS — cargo test import suite; PASS — Android debug assembly; exact commit recorded.

## Known gaps and unverified claims
None. Physical-device smoke is tracked separately and is not claimed by this PR.

## Rollback
Revert this commit; no persistent schema changes or destructive operations are introduced.
"""


class ChangeContractTests(unittest.TestCase):
    def test_valid_contract_passes(self):
        self.assertEqual(validate_pr_body(VALID_BODY), [])

    def test_missing_required_section_fails(self):
        body = VALID_BODY.replace("## Rollback\nRevert this commit; no persistent schema changes or destructive operations are introduced.\n", "")
        self.assertTrue(any("Missing required section" in error for error in validate_pr_body(body)))

    def test_baseline_sha_is_required(self):
        body = VALID_BODY.replace("a8e6763e384c3fe1f1f508ea216210d7213652b8", "unknown")
        self.assertTrue(any("40-character base commit SHA" in error for error in validate_pr_body(body)))

    def test_android_identity_fields_are_required(self):
        body = VALID_BODY.replace("Application ID: com.locco.app", "Package: Locco")
        self.assertTrue(any("Application ID" in error for error in validate_pr_body(body)))

    def test_feedback_id_must_match_issue_number(self):
        body = VALID_BODY.replace("LOC-0002 — #2", "LOC-0003 — #2")
        self.assertTrue(any("LOC-0003" in error for error in validate_pr_body(body)))

    def test_no_feedback_id_fails(self):
        body = VALID_BODY.replace("LOC-0002 — #2 — continuity policy and required evidence.", "No linked feedback.")
        self.assertTrue(any("LOC-####" in error for error in validate_pr_body(body)))

    def test_replacement_app_is_not_accepted(self):
        body = VALID_BODY.replace(
            "- [x] Existing Locco application is extended/repaired; no replacement application is created.",
            "- [ ] Existing Locco application is extended/repaired; no replacement application is created.",
        )
        self.assertTrue(any("continuity of the existing Locco application" in error for error in validate_pr_body(body)))

    def test_unfilled_placeholder_fails(self):
        body = VALID_BODY.replace("Revert this commit;", "TODO: Revert this commit;")
        self.assertTrue(any("placeholder" in error for error in validate_pr_body(body)))

    def test_separate_product_requires_explicit_user_authorization(self):
        body = VALID_BODY.replace(
            "- [x] Existing Locco application is extended/repaired; no replacement application is created.\nPackage identity and stored data remain unchanged.",
            "- [x] Separate product explicitly instructed by user; authorization is linked below.\nLOC-0002 — #2 contains the explicit authorization.",
        )
        self.assertFalse(any("Separate-product authorization" in error for error in validate_pr_body(body)))
        body = body.replace("LOC-0002 — #2 contains the explicit authorization.", "No authorization link.")
        self.assertTrue(any("Separate-product authorization" in error for error in validate_pr_body(body)))

    def test_feedback_ids_support_issue_numbers_above_9999(self):
        body = VALID_BODY.replace("LOC-0002 — #2", "LOC-10000 — #10000")
        self.assertFalse(any("LOC-10000" in error for error in validate_pr_body(body)))

    def test_issue_references_are_extracted(self):
        self.assertEqual(issue_numbers(VALID_BODY), [2])


if __name__ == "__main__":
    unittest.main()
