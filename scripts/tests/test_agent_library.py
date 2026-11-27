"""Tests for the agent-role and skill-library quality gate."""
from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

sys_path_added = False
import sys
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from validate_agent_library import validate_agent_library  # noqa: E402


ROLE = """# Agent role: qa

## Mission
""" + ("The role owns independent verification and protects existing user behavior. " * 18) + """
## Authority and limits
The role cannot self-approve or waive product requirements.
## Load before work
Read the source issue and affected code.
## Execution procedure
Inspect the baseline and verify every criterion.
## Required outputs
Return evidence and remaining risks.
## Stop conditions
Stop when data or authority is unclear.
## Done
Another reviewer can reproduce the result.
"""

SKILL = """# Skill: fixture skill

**Trigger:** Use whenever this test fixture is required.

## Inputs
Record the source issue and relevant files.

## Procedure
""" + ("Inspect the existing behavior, perform the documented steps and record the exact result. " * 12) + """
## Required evidence
Return the command, output, artifact and remaining risks.
## Fail conditions
Fail closed when a prerequisite or result is missing.
"""


def make_repo(root: Path) -> None:
    (root / ".agent/agents").mkdir(parents=True)
    (root / ".agent/skills").mkdir(parents=True)
    (root / "AGENTS.md").write_text(
        "[qa](.agent/agents/qa.md)\n", encoding="utf-8"
    )
    (root / ".agent/routing.md").write_text(
        "[agents](agents/README.md)\nqa owns verification\n", encoding="utf-8"
    )
    (root / ".agent/agents/README.md").write_text(
        "[routing](../routing.md)\n", encoding="utf-8"
    )
    (root / ".agent/agents/qa.md").write_text(ROLE, encoding="utf-8")
    (root / ".agent/skills/README.md").write_text(
        "[fixture skill](fixture.md)\n", encoding="utf-8"
    )
    (root / ".agent/skills/fixture.md").write_text(SKILL, encoding="utf-8")


class AgentLibraryTests(unittest.TestCase):
    def test_valid_library_passes(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            make_repo(root)
            self.assertEqual(validate_agent_library(root), [])

    def test_short_skill_fails(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            make_repo(root)
            (root / ".agent/skills/fixture.md").write_text(
                "# Skill: tiny\n**Trigger:** now\n## Procedure\nDo it.\n"
                "## Required evidence\nNone.\n## Fail conditions\nStop.\n",
                encoding="utf-8",
            )
            self.assertTrue(any("too short" in e for e in validate_agent_library(root)))

    def test_broken_local_link_fails(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            make_repo(root)
            (root / ".agent/agents/qa.md").write_text(
                ROLE + "\n[missing](../skills/no-such-skill.md)\n",
                encoding="utf-8",
            )
            self.assertTrue(any("broken local link" in e for e in validate_agent_library(root)))

    def test_skill_must_have_trigger_and_fail_gate(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            make_repo(root)
            altered = SKILL.replace("**Trigger:** Use whenever this test fixture is required.", "Applicable.")
            altered = altered.replace("## Fail conditions", "## Notes")
            (root / ".agent/skills/fixture.md").write_text(altered, encoding="utf-8")
            errors = validate_agent_library(root)
            self.assertTrue(any("explicit trigger" in e for e in errors))
            self.assertTrue(any("fail-closed criteria" in e for e in errors))

    def test_skill_missing_from_index_fails(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            make_repo(root)
            (root / ".agent/skills/other.md").write_text(SKILL, encoding="utf-8")
            self.assertTrue(any("Skill index mismatch" in e for e in validate_agent_library(root)))


if __name__ == "__main__":
    unittest.main()
