#!/usr/bin/env python3
"""Validate that Locco's role prompts and skills remain usable repository artifacts."""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path
from urllib.parse import urlsplit

ROLE_HEADINGS = (
    "Mission",
    "Authority and limits",
    "Load before work",
    "Execution procedure",
    "Required outputs",
    "Stop conditions",
    "Done",
)
SKILL_PROCEDURE_HEADINGS = (
    "Procedure",
    "Implementation procedure",
    "Gate sequence",
    "Rules",
    "Verification procedure",
)
SKILL_EVIDENCE_HEADINGS = (
    "Required evidence",
    "Required artifact",
    "Required issue content",
    "Required tests",
    "Pass conditions",
)
SKILL_FAILURE_HEADINGS = (
    "Fail conditions",
    "Fail closed",
)
LINK_RE = re.compile(r"\[[^\]]*\]\(([^)]+)\)")
WORD_RE = re.compile(r"\b[\w-]+\b")


def read_text(path: Path, errors: list[str]) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except (OSError, UnicodeError) as exc:
        errors.append(f"{path}: cannot read UTF-8 content: {exc}")
        return ""


def markdown_targets(path: Path, content: str, root: Path, errors: list[str]) -> set[Path]:
    targets: set[Path] = set()
    for raw in LINK_RE.findall(content):
        target = raw.strip().split()[0].split("#", 1)[0].split("?", 1)[0]
        if not target or target.startswith(("https://", "http://", "mailto:", "data:")):
            continue
        if target.startswith("/"):
            errors.append(f"{path}: repository-local link must be relative: {target}")
            continue
        candidate = (path.parent / target).resolve()
        try:
            candidate.relative_to(root)
        except ValueError:
            errors.append(f"{path}: link escapes repository root: {target}")
            continue
        if candidate.is_dir():
            candidate = candidate / "README.md"
        if not candidate.is_file():
            errors.append(f"{path}: broken local link: {target}")
        else:
            targets.add(candidate)
    return targets


def validate_agent_library(root: Path) -> list[str]:
    root = root.resolve()
    errors: list[str] = []
    agent_dir = root / ".agent" / "agents"
    skill_dir = root / ".agent" / "skills"
    routing = root / ".agent" / "routing.md"
    agents_readme = agent_dir / "README.md"
    skills_readme = skill_dir / "README.md"
    root_contract = root / "AGENTS.md"

    for required in (agent_dir, skill_dir, routing, agents_readme, skills_readme, root_contract):
        if not required.exists():
            errors.append(f"Missing required agent-library entry: {required.relative_to(root)}")
    if errors:
        return errors

    agent_files = sorted(p for p in agent_dir.glob("*.md") if p.name != "README.md")
    skill_files = sorted(p for p in skill_dir.glob("*.md") if p.name != "README.md")
    if not agent_files:
        errors.append("No role prompts found in .agent/agents.")
    if not skill_files:
        errors.append("No skills found in .agent/skills.")

    agent_index = read_text(agents_readme, errors)
    skill_index = read_text(skills_readme, errors)
    routing_text = read_text(routing, errors)
    root_text = read_text(root_contract, errors)

    for path in agent_files:
        content = read_text(path, errors)
        if not content.startswith("# Agent role:"):
            errors.append(f"{path.relative_to(root)}: title must start with '# Agent role:'.")
        for heading in ROLE_HEADINGS:
            if not re.search(rf"^##\s+{re.escape(heading)}\s*$", content, re.MULTILINE):
                errors.append(f"{path.relative_to(root)}: missing required section '{heading}'.")
        if len(WORD_RE.findall(content)) < 140:
            errors.append(f"{path.relative_to(root)}: role prompt is too short to be operational (<140 words).")
        role = path.stem
        if role not in routing_text:
            errors.append(f"{path.relative_to(root)}: role is not present in .agent/routing.md.")
        if f".agent/agents/{path.name}" not in root_text:
            errors.append(f"{path.relative_to(root)}: role is not linked from root AGENTS.md.")

    for path in skill_files:
        content = read_text(path, errors)
        if not content.startswith("# Skill:"):
            errors.append(f"{path.relative_to(root)}: title must start with '# Skill:'.")
        if not re.search(r"\*\*(Trigger|When)\s*:", content[:600], re.IGNORECASE):
            errors.append(f"{path.relative_to(root)}: missing an explicit trigger near the top.")
        if not any(re.search(rf"^##\s+{re.escape(h)}\s*$", content, re.MULTILINE) for h in SKILL_PROCEDURE_HEADINGS):
            errors.append(f"{path.relative_to(root)}: missing a concrete procedure section.")
        if not any(re.search(rf"^##\s+{re.escape(h)}\s*$", content, re.MULTILINE) for h in SKILL_EVIDENCE_HEADINGS):
            errors.append(f"{path.relative_to(root)}: missing required evidence/acceptance output section.")
        if not any(re.search(rf"^##\s+{re.escape(h)}\s*$", content, re.MULTILINE) for h in SKILL_FAILURE_HEADINGS):
            errors.append(f"{path.relative_to(root)}: missing explicit fail-closed criteria.")
        if len(WORD_RE.findall(content)) < 120:
            errors.append(f"{path.relative_to(root)}: skill is too short to be operational (<120 words).")

    # Every skill must be discoverable from the skill index; every role must be
    # selectable from the root operating contract. Link targets are checked below.
    indexed_skills = {p.name for p in markdown_targets(skills_readme, skill_index, root, errors)}
    expected_skills = {p.name for p in skill_files}
    if expected_skills != indexed_skills:
        errors.append(
            "Skill index mismatch: "
            f"unindexed={sorted(expected_skills - indexed_skills)}, "
            f"missing_files={sorted(indexed_skills - expected_skills)}"
        )

    # Validate internal Markdown links in the entire prompt library and its root entrypoint.
    for path in [root_contract, routing, agents_readme, skills_readme, *agent_files, *skill_files]:
        content = read_text(path, errors)
        markdown_targets(path, content, root, errors)

    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path.cwd(), help="candidate repository root")
    args = parser.parse_args()
    errors = validate_agent_library(args.root)
    if errors:
        print("AGENT LIBRARY: FAIL")
        for error in errors:
            print(f" - {error}")
        return 1
    print("AGENT LIBRARY: PASS (structure, trigger, procedure, evidence, fail criteria and local links)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
