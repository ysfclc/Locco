#!/usr/bin/env python3
"""Regression tests for Locco's explicit Android backup exclusion policy."""
from __future__ import annotations

import pathlib
import subprocess
import sys
import tempfile
import unittest
import xml.etree.ElementTree as ET

SCRIPT = pathlib.Path(__file__).resolve().parents[1] / "validate_android_backup_policy.py"
DOMAINS = [
    "file", "database", "sharedpref", "external", "root",
    "device_file", "device_database", "device_sharedpref", "device_root",
]


class AndroidBackupPolicyTests(unittest.TestCase):
    def make_project(self, root: pathlib.Path) -> None:
        manifest = root / "android/app/src/main/AndroidManifest.xml"
        xml_dir = root / "android/app/src/main/res/xml"
        xml_dir.mkdir(parents=True)
        manifest.parent.mkdir(parents=True, exist_ok=True)
        manifest.write_text(
            '<manifest xmlns:android="http://schemas.android.com/apk/res/android">'
            '<application android:allowBackup="false" '
            'android:fullBackupContent="@xml/backup_rules" '
            'android:dataExtractionRules="@xml/data_extraction_rules"/>'
            '</manifest>',
            encoding="utf-8",
        )
        excludes = "".join(f'<exclude domain="{domain}" path="."/>' for domain in DOMAINS)
        (xml_dir / "backup_rules.xml").write_text(
            f"<full-backup-content>{excludes}</full-backup-content>", encoding="utf-8"
        )
        (xml_dir / "data_extraction_rules.xml").write_text(
            "<data-extraction-rules>"
            f"<cloud-backup>{excludes}</cloud-backup>"
            f"<device-transfer>{excludes}</device-transfer>"
            "</data-extraction-rules>",
            encoding="utf-8",
        )

    def run_validator(self, root: pathlib.Path) -> subprocess.CompletedProcess:
        return subprocess.run(
            [sys.executable, str(SCRIPT), "--root", str(root)],
            text=True, capture_output=True, check=False,
        )

    def test_repository_backup_policy_is_valid(self):
        root = SCRIPT.parents[1]
        result = self.run_validator(root)
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_valid_policy_fixture_passes(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            self.make_project(root)
            result = self.run_validator(root)
            self.assertEqual(result.returncode, 0, result.stderr)

    def test_allow_backup_true_fails(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            self.make_project(root)
            manifest = root / "android/app/src/main/AndroidManifest.xml"
            manifest.write_text(
                manifest.read_text(encoding="utf-8").replace(
                    'allowBackup="false"', 'allowBackup="true"'
                ),
                encoding="utf-8",
            )
            result = self.run_validator(root)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("allowBackup", result.stderr)

    def test_missing_device_transfer_exclusion_fails(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            self.make_project(root)
            rules = root / "android/app/src/main/res/xml/data_extraction_rules.xml"
            data = ET.parse(rules)
            section = data.getroot().find("device-transfer")
            section.remove(section.find("exclude"))
            data.write(rules, encoding="unicode")
            result = self.run_validator(root)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("device-transfer", result.stderr)

    def test_missing_legacy_domain_exclusion_fails(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            self.make_project(root)
            rules = root / "android/app/src/main/res/xml/backup_rules.xml"
            data = ET.parse(rules)
            data.getroot().remove(data.getroot().find("exclude"))
            data.write(rules, encoding="unicode")
            result = self.run_validator(root)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("does not exclude every backup domain", result.stderr)

    def test_include_rule_fails_closed(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            self.make_project(root)
            rules = root / "android/app/src/main/res/xml/data_extraction_rules.xml"
            data = ET.parse(rules)
            ET.SubElement(
                data.getroot().find("device-transfer"),
                "include",
                {"domain": "file", "path": "."},
            )
            data.write(rules, encoding="unicode")
            result = self.run_validator(root)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("contains an <include>", result.stderr)


if __name__ == "__main__":
    unittest.main()
