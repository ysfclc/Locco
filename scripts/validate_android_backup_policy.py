#!/usr/bin/env python3
"""Fail closed unless Android cloud backup and device transfer are explicitly excluded."""
from __future__ import annotations

import argparse
import pathlib
import sys
import xml.etree.ElementTree as ET

ANDROID_NS = "{http://schemas.android.com/apk/res/android}"
REQUIRED_DOMAINS = {
    "file", "database", "sharedpref", "external", "root",
    "device_file", "device_database", "device_sharedpref", "device_root",
}


class PolicyError(RuntimeError):
    pass


def parse_xml(path: pathlib.Path) -> ET.Element:
    if not path.is_file():
        raise PolicyError(f"Required backup policy XML is missing: {path}")
    try:
        return ET.parse(path).getroot()
    except ET.ParseError as exc:
        raise PolicyError(f"Malformed XML in {path}: {exc}") from exc


def resource_name(value: str | None, attribute: str) -> str:
    if not value or not value.startswith("@xml/") or not value[5:]:
        raise PolicyError(f"Manifest {attribute} must reference an @xml resource; got {value!r}")
    return value[5:]


def verify_exclusions(section: ET.Element, label: str) -> None:
    if any(child.tag == "include" for child in section):
        raise PolicyError(f"{label} contains an <include>; automatic app-data backup must be disabled")
    found = {
        (child.attrib.get("domain"), child.attrib.get("path"))
        for child in section
        if child.tag == "exclude"
    }
    missing = sorted(domain for domain in REQUIRED_DOMAINS if (domain, ".") not in found)
    if missing:
        raise PolicyError(f"{label} does not exclude every backup domain: {', '.join(missing)}")


def validate(root: pathlib.Path) -> None:
    manifest = parse_xml(root / "android/app/src/main/AndroidManifest.xml")
    app = manifest.find("application")
    if app is None:
        raise PolicyError("AndroidManifest.xml has no application element")

    if app.attrib.get(ANDROID_NS + "allowBackup") != "false":
        raise PolicyError('Android application must set android:allowBackup="false"')

    legacy_name = resource_name(
        app.attrib.get(ANDROID_NS + "fullBackupContent"), "fullBackupContent"
    )
    modern_name = resource_name(
        app.attrib.get(ANDROID_NS + "dataExtractionRules"), "dataExtractionRules"
    )
    xml_dir = root / "android/app/src/main/res/xml"

    legacy = parse_xml(xml_dir / f"{legacy_name}.xml")
    if legacy.tag != "full-backup-content":
        raise PolicyError(f"{legacy_name}.xml must have a <full-backup-content> root")
    verify_exclusions(legacy, "Android 11 and lower full-backup-content")

    modern = parse_xml(xml_dir / f"{modern_name}.xml")
    if modern.tag != "data-extraction-rules":
        raise PolicyError(f"{modern_name}.xml must have a <data-extraction-rules> root")

    sections: dict[str, list[ET.Element]] = {}
    for child in modern:
        sections.setdefault(child.tag, []).append(child)
    required_sections = {"cloud-backup", "device-transfer"}
    missing_sections = sorted(required_sections - set(sections))
    if missing_sections:
        raise PolicyError(
            "Modern Android backup rules are missing sections: " + ", ".join(missing_sections)
        )

    for section_name, section_nodes in sections.items():
        if len(section_nodes) != 1:
            raise PolicyError(f"Modern Android backup rules contain duplicate {section_name} sections")
        verify_exclusions(section_nodes[0], f"Android 12+ {section_name}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--root",
        type=pathlib.Path,
        default=pathlib.Path(__file__).resolve().parents[1],
        help="repository root (defaults to the parent of scripts/)",
    )
    args = parser.parse_args()
    try:
        validate(args.root.resolve())
    except PolicyError as exc:
        print(f"Android backup policy check failed: {exc}", file=sys.stderr)
        return 1
    print(
        "Android backup policy passed: legacy backup, modern cloud backup and "
        "device transfer exclude all app-data domains."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
