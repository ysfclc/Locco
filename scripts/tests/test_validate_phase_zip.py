#!/usr/bin/env python3
"""Adversarial contract tests for validate_phase_zip.py."""
import json
import pathlib
import stat
import subprocess
import sys
import tempfile
import unittest
import zipfile

SCRIPT = pathlib.Path(__file__).resolve().parents[1] / "validate_phase_zip.py"


class ZipValidatorTests(unittest.TestCase):
    def run_validator(self, archive: pathlib.Path, output: pathlib.Path):
        return subprocess.run(
            [sys.executable, str(SCRIPT), "--zip", str(archive), "--output", str(output)],
            text=True, capture_output=True, check=False
        )

    def make_zip(self, root: pathlib.Path, name: str, entries):
        path = root / name
        with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
            for entry_name, data, mode in entries:
                info = zipfile.ZipInfo(entry_name)
                info.compress_type = zipfile.ZIP_DEFLATED
                if mode is not None:
                    info.external_attr = mode << 16
                zf.writestr(info, data)
        return path

    def test_valid_single_root_is_normalized_and_manifested(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            archive = self.make_zip(root, "valid.zip", [
                ("phase-01/README.md", "# phase", None),
                ("phase-01/src/lib.rs", "pub fn foundation() {}", None),
            ])
            output = root / "out"
            result = self.run_validator(archive, output)
            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertTrue((output / "normalized/README.md").is_file())
            self.assertTrue((output / "normalized/src/lib.rs").is_file())
            manifest = json.loads((output / "manifest.json").read_text())
            self.assertEqual(len(manifest["entries"]), 2)
            self.assertEqual(len(manifest["archive_sha256"]), 64)

    def test_parent_traversal_is_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            archive = self.make_zip(root, "bad.zip", [("root/../escape.txt", "x", None)])
            result = self.run_validator(archive, root / "out")
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("unsafe path", result.stderr)

    def test_absolute_path_is_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            archive = self.make_zip(root, "bad.zip", [("/etc/passwd", "x", None)])
            result = self.run_validator(archive, root / "out")
            self.assertNotEqual(result.returncode, 0)

    def test_symlink_is_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            mode = stat.S_IFLNK | 0o777
            archive = self.make_zip(root, "bad.zip", [("root/link", "../../outside", mode)])
            result = self.run_validator(archive, root / "out")
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("links/special", result.stderr)

    def test_protected_path_after_root_normalization_is_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            archive = self.make_zip(root, "bad.zip", [("phase/.github/workflows/evil.yml", "run: evil", None)])
            result = self.run_validator(archive, root / "out")
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("protected", result.stderr)

    def test_forbidden_binary_is_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            archive = self.make_zip(root, "bad.zip", [("phase/app.apk", b"not really apk", None)])
            result = self.run_validator(archive, root / "out")
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("forbidden", result.stderr)

    def test_compression_ratio_bomb_is_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = pathlib.Path(tmp)
            archive = self.make_zip(root, "bomb.zip", [("phase/readme.txt", "A" * 200_000, None)])
            result = self.run_validator(archive, root / "out")
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("compression ratio", result.stderr)


if __name__ == "__main__":
    unittest.main()
