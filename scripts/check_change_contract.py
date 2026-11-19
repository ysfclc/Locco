#!/usr/bin/env python3
"""Fail a PR when mandatory Locco continuity/change evidence is missing.

The checker validates structure and explicit continuity assertions. It does not
pretend to prove semantic correctness; reviewers and QA still inspect the code,
tests, data migrations and actual artifacts.
"""
from __future__ import annotations

import os
import re
import sys
from pathlib import Path

REQUIRED_HEADINGS = (
    "Change classification",
    "Baseline and current application identity",
    "User feedback records",
    "Existing behaviour inventory",
    "Product boundary and app continuity",
    "Impact analysis",
    "Regression coverage",
    "Data, migration and upgrade safety",
    "Verification evidence",
    "Known gaps and unverified claims",
    "Rollback",
)

PLACEHOLDER_PATTERNS = (
    r"\bTODO\b",
    r"\bTBD\b",
    r"\bFIXME\b",
    r"\[describe[^\]]*\]",
    r"\[fill[^\]]*\]",
    r"<!--",
)

CONTINUITY_ASSERTION = (
    "- [x] Existing Locco application is extended/repaired; no replacement application is created."
)
SEPARATE_PRODUCT_ASSERTION = (
    "- [x] Separate product explicitly instructed by user; authorization is linked below."
)


def strip_html_comments(value: str) -> str:
    return re.sub(r"<!--.*?-->", "", value, flags=re.DOTALL)


def split_sections(body: str) -> dict[str, str]:
    matches = list(re.finditer(r"^##\s+(.+?)\s*$", body, flags=re.MULTILINE))
    sections: dict[str, str] = {}
    for index, match in enumerate(matches):
        name = match.group(1).strip()
        start = match.end()
        end = matches[index + 1].start() if index + 1 < len(matches) else len(body)
        sections[name] = body[start:end].strip()
    return sections


def validate_pr_body(body: str) -> list[str]:
    errors: list[str] = []
    clean = strip_html_comments(body or "").strip()
    sections = split_sections(clean)

    for heading in REQUIRED_HEADINGS:
        if heading not in sections:
            errors.append(f"Missing required section: '## {heading}'")
        elif len(sections[heading].strip()) < 12:
            errors.append(f"Section is empty or non-substantive: '## {heading}'")

    for pattern in PLACEHOLDER_PATTERNS:
        if re.search(pattern, clean, flags=re.IGNORECASE):
            errors.append(f"Unfilled template placeholder detected: {pattern}")

    baseline = sections.get("Baseline and current application identity", "")
    if not re.search(r"\b[0-9a-f]{40}\b", baseline, re.IGNORECASE):
        errors.append("Baseline must include the exact 40-character base commit SHA.")
    for label in ("Application ID", "versionCode", "versionName"):
        if not re.search(rf"(?im)^\s*{re.escape(label)}\s*:\s*\S+", baseline):
            errors.append(f"Baseline must record {label} from the current Android build configuration.")

    feedback = sections.get("User feedback records", "")
    ids = re.findall(r"\bLOC-(\d{4,})\b", feedback)
    refs = {int(number) for number in re.findall(r"(?<!\w)#(\d+)\b", feedback)}
    if not ids:
        errors.append("User feedback records must link at least one LOC-#### record.")
    for item_id in ids:
        if int(item_id) not in refs:
            errors.append(
                f"LOC-{item_id} must be paired with its GitHub issue reference #{int(item_id)}."
            )

    product = sections.get("Product boundary and app continuity", "")
    if CONTINUITY_ASSERTION not in product and SEPARATE_PRODUCT_ASSERTION not in product:
        errors.append(
            "Explicitly confirm continuity of the existing Locco application. "
            "A separate product requires the dedicated assertion and a linked user authorization."
        )
    if SEPARATE_PRODUCT_ASSERTION in product:
        if not re.search(r"\bLOC-\d{4,}\b", product) or not re.search(r"(?<!\w)#\d+\b", product):
            errors.append("Separate-product authorization must link its LOC issue and GitHub issue number.")

    regression = sections.get("Regression coverage", "")
    if not re.search(r"\b(Automated|Manual|Not applicable|Blocked)\b", regression, re.IGNORECASE):
        errors.append("Regression coverage must state Automated, Manual, Not applicable (with reason), or Blocked.")

    verification = sections.get("Verification evidence", "")
    if not re.search(r"\b(PASS|FAIL|NOT RUN|BLOCKED)\b", verification, re.IGNORECASE):
        errors.append("Verification evidence must state actual PASS, FAIL, NOT RUN or BLOCKED results.")

    gaps = sections.get("Known gaps and unverified claims", "")
    if not re.search(r"\b(None|No known gaps|Unverified|Blocked|Not run)\b", gaps, re.IGNORECASE):
        errors.append("Known gaps must explicitly say None or list unverified/blocked items.")

    return errors


def issue_numbers(body: str) -> list[int]:
    feedback = split_sections(strip_html_comments(body or "")).get("User feedback records", "")
    return sorted({int(number) for number in re.findall(r"(?<!\w)#(\d+)\b", feedback)})


def main() -> int:
    body = os.environ.get("PR_BODY", "")
    if "--print-issues" in sys.argv:
        for number in issue_numbers(body):
            print(number)
        return 0

    errors = validate_pr_body(body)
    if errors:
        print("CHANGE CONTRACT: FAIL")
        for error in errors:
            print(f" - {error}")
        return 1

    print("CHANGE CONTRACT: PASS (structure and continuity assertions only)")
    print("Code correctness, regression behavior, migrations and device evidence still require review and tests.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
