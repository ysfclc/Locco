#!/usr/bin/env python3
"""Validate and safely normalize a phase ZIP. Never executes archive content."""
from __future__ import annotations

import argparse
import hashlib
import json
import pathlib
import shutil
import stat
import zipfile

MAX_ARCHIVE = 100 * 1024 * 1024
MAX_ENTRIES = 5000
MAX_TOTAL = 200 * 1024 * 1024
MAX_FILE = 40 * 1024 * 1024
MAX_RATIO = 100

ALLOWED_SUFFIXES = {
    ".md", ".txt", ".json", ".toml", ".yaml", ".yml", ".xml", ".kt", ".kts",
    ".rs", ".swift", ".m", ".h", ".c", ".cpp", ".java", ".properties",
    ".gradle", ".sh", ".py", ".mmd", ".sql", ".csv", ".svg", ".png",
    ".jpg", ".jpeg", ".webp", ".ttf", ".otf", ".pro", ".lock", ".cfg",
    ".conf", ".ini", ".plist", ".entitlements", ".pbxproj", ".xcconfig",
    ".strings", ".storyboard", ".xib", ".cs", ".csproj", ".dart"
}
ALLOWED_BASENAMES = {
    "LICENSE", "LICENSE-MIT", "LICENSE-APACHE", "NOTICE", "Makefile",
    "Dockerfile", ".gitignore", ".gitattributes", ".editorconfig"
}
FORBIDDEN_NAMES = {
    ".env", "id_rsa", "id_ed25519", "credentials.json", "keystore.properties",
    "local.properties"
}
FORBIDDEN_SUFFIXES = {
    ".apk", ".aab", ".aar", ".jar", ".class", ".dex", ".so", ".dll",
    ".dylib", ".exe", ".bin", ".dat", ".keystore", ".jks", ".pem",
    ".p12", ".pfx", ".sqlite", ".db", ".gguf", ".litertlm", ".zip",
    ".7z", ".tar", ".gz"
}
PROTECTED = {
    ".github", ".agent", "docs/security", "docs/decisions",
    "docs/brainstorm/raw", "docs/operations/provenance", "incoming", "PRINCIPLES.md", "SECURITY.md",
    "CODEOWNERS", "CONTRIBUTING.md", "scripts/validate_phase_zip.py"
}


def fail(message: str) -> None:
    raise SystemExit(f"ZIP REJECTED: {message}")


def normalized_name(raw: str) -> str:
    if "\x00" in raw or "\\" in raw:
        fail(f"invalid path separator: {raw!r}")
    trimmed = raw[:-1] if raw.endswith("/") else raw
    parts = trimmed.split("/")
    if not trimmed or trimmed.startswith("/") or any(p in ("", ".", "..") for p in parts):
        fail(f"unsafe path: {raw!r}")
    if parts[0].endswith(":"):
        fail(f"drive path: {raw!r}")
    return "/".join(parts)


def is_protected(name: str) -> bool:
    return any(name == item or name.startswith(item + "/") for item in PROTECTED)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--zip", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    archive = pathlib.Path(args.zip)
    if not archive.is_file():
        fail("archive does not exist")
    if archive.stat().st_size > MAX_ARCHIVE:
        fail("compressed archive exceeds size limit")

    digest = hashlib.sha256()
    with archive.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    archive_hash = digest.hexdigest()

    output = pathlib.Path(args.output).resolve()
    if output.exists():
        shutil.rmtree(output)
    output.mkdir(parents=True)
    extracted = output / "normalized"
    extracted.mkdir()
    manifest = []

    try:
        with zipfile.ZipFile(archive) as zf:
            infos = zf.infolist()
            if not infos or len(infos) > MAX_ENTRIES:
                fail(f"entry count outside allowed range: {len(infos)}")

            seen = set()
            candidates = []
            total = 0
            for info in infos:
                name = normalized_name(info.filename)
                if name in seen:
                    fail(f"duplicate normalized path: {name}")
                seen.add(name)

                mode = (info.external_attr >> 16) & 0xFFFF
                file_type = stat.S_IFMT(mode)
                if stat.S_ISLNK(mode) or file_type not in (0, stat.S_IFREG, stat.S_IFDIR):
                    fail(f"links/special files are forbidden: {name}")
                if info.is_dir():
                    continue
                if info.file_size > MAX_FILE:
                    fail(f"file too large: {name}")
                total += info.file_size
                if total > MAX_TOTAL:
                    fail("expanded archive exceeds total size limit")
                if info.file_size and info.compress_size == 0:
                    fail(f"invalid compressed size: {name}")
                if info.compress_size and info.file_size / info.compress_size > MAX_RATIO:
                    fail(f"compression ratio exceeds limit: {name}")
                if ".git" in pathlib.PurePosixPath(name).parts:
                    continue
                candidates.append((info, name))

            if not candidates:
                fail("archive contains no accepted files")

            parts = [pathlib.PurePosixPath(name).parts for _, name in candidates]
            root = parts[0][0] if all(len(p) > 1 and p[0] == parts[0][0] for p in parts) else None

            for info, name in candidates:
                relative = pathlib.PurePosixPath(name)
                if root:
                    relative = pathlib.PurePosixPath(*relative.parts[1:])
                rel_name = relative.as_posix()
                if not rel_name or is_protected(rel_name):
                    fail(f"protected or empty normalized path: {rel_name!r}")

                basename = relative.name
                suffix = relative.suffix.lower()
                if basename in FORBIDDEN_NAMES or basename.startswith(".env."):
                    fail(f"secret-bearing filename is forbidden: {rel_name}")
                if suffix in FORBIDDEN_SUFFIXES:
                    fail(f"forbidden file type: {rel_name}")
                if suffix and suffix not in ALLOWED_SUFFIXES:
                    fail(f"file extension is not allowlisted: {rel_name}")
                if not suffix and basename not in ALLOWED_BASENAMES and not basename.startswith("."):
                    fail(f"extensionless filename is not allowlisted: {rel_name}")

                target = (extracted / pathlib.Path(*relative.parts)).resolve()
                if extracted not in target.parents:
                    fail(f"path escapes extraction root: {name}")
                target.parent.mkdir(parents=True, exist_ok=True)

                file_hash = hashlib.sha256()
                written = 0
                with zf.open(info, "r") as source, target.open("xb") as destination:
                    while True:
                        chunk = source.read(1024 * 1024)
                        if not chunk:
                            break
                        written += len(chunk)
                        if written > MAX_FILE:
                            fail(f"expanded file exceeded limit while extracting: {rel_name}")
                        file_hash.update(chunk)
                        destination.write(chunk)
                if written != info.file_size:
                    fail(f"declared and extracted sizes differ: {rel_name}")
                manifest.append({
                    "path": rel_name,
                    "size": written,
                    "sha256": file_hash.hexdigest()
                })
    except zipfile.BadZipFile:
        fail("not a valid ZIP archive")

    (output / "manifest.json").write_text(
        json.dumps({
            "archive": archive.name,
            "archive_sha256": archive_hash,
            "entries": manifest
        }, indent=2) + "\n",
        encoding="utf-8"
    )
    print(json.dumps({
        "archive_sha256": archive_hash,
        "file_count": len(manifest),
        "output": str(extracted)
    }))


if __name__ == "__main__":
    main()
