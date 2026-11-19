#!/usr/bin/env python3
"""Validate and safely normalize a phase ZIP. Never executes archive content."""
from __future__ import annotations
import argparse, hashlib, json, os, pathlib, shutil, stat, tempfile, zipfile

MAX_ENTRIES = 5000
MAX_TOTAL = 200 * 1024 * 1024
MAX_FILE = 40 * 1024 * 1024
MAX_RATIO = 100
FORBIDDEN_SUFFIXES = {".apk", ".aab", ".keystore", ".jks", ".pem", ".p12", ".pfx", ".sqlite", ".db", ".gguf", ".litertlm"}
PROTECTED = {".github", ".agent", "docs/security", "docs/decisions", "PRINCIPLES.md", "SECURITY.md", "CODEOWNERS", "scripts/validate_phase_zip.py"}

def fail(message: str) -> None:
    raise SystemExit(f"ZIP REJECTED: {message}")

def normalized_name(raw: str) -> str:
    if "\x00" in raw or "\\" in raw:
        fail(f"invalid path separator: {raw!r}")
    p = pathlib.PurePosixPath(raw)
    if p.is_absolute() or not p.parts or any(x in ("", ".", "..") for x in p.parts):
        fail(f"unsafe path: {raw!r}")
    if p.parts[0].endswith(":"):
        fail(f"drive path: {raw!r}")
    return p.as_posix()

def is_protected(name: str) -> bool:
    return any(name == x or name.startswith(x + "/") for x in PROTECTED)

def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--zip", required=True)
    ap.add_argument("--output", required=True)
    args = ap.parse_args()
    archive = pathlib.Path(args.zip)
    if not archive.is_file():
        fail("archive does not exist")
    archive_hash = hashlib.sha256(archive.read_bytes()).hexdigest()
    out = pathlib.Path(args.output).resolve()
    if out.exists():
        shutil.rmtree(out)
    out.mkdir(parents=True)
    extracted = out / "normalized"
    extracted.mkdir()
    manifest = []
    try:
        with zipfile.ZipFile(archive) as zf:
            infos = zf.infolist()
            if not infos or len(infos) > MAX_ENTRIES:
                fail(f"entry count outside allowed range: {len(infos)}")
            total = 0
            seen = set()
            files = []
            for info in infos:
                name = normalized_name(info.filename.rstrip("/")) if info.filename.rstrip("/") else ""
                if not name:
                    continue
                if name in seen:
                    fail(f"duplicate normalized path: {name}")
                seen.add(name)
                mode = (info.external_attr >> 16) & 0xFFFF
                if stat.S_ISLNK(mode) or stat.S_ISCHR(mode) or stat.S_ISBLK(mode) or stat.S_ISFIFO(mode):
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
                if pathlib.PurePosixPath(name).suffix.lower() in FORBIDDEN_SUFFIXES:
                    fail(f"forbidden file type: {name}")
                if ".git" in pathlib.PurePosixPath(name).parts:
                    continue
                if is_protected(name):
                    fail(f"protected path cannot be supplied by phase ZIP: {name}")
                files.append((info, name))
            if not files:
                fail("archive contains no accepted files")
            # Normalize a single common top-level directory.
            first_parts = [pathlib.PurePosixPath(n).parts for _, n in files]
            root = first_parts[0][0] if all(len(p) > 1 and p[0] == first_parts[0][0] for p in first_parts) else None
            for info, name in files:
                rel = pathlib.PurePosixPath(name)
                if root:
                    rel = pathlib.PurePosixPath(*rel.parts[1:])
                target = (extracted / pathlib.Path(*rel.parts)).resolve()
                if extracted not in target.parents:
                    fail(f"path escapes extraction root: {name}")
                target.parent.mkdir(parents=True, exist_ok=True)
                digest = hashlib.sha256()
                with zf.open(info, "r") as src, open(target, "xb") as dst:
                    while True:
                        chunk = src.read(1024 * 1024)
                        if not chunk:
                            break
                        digest.update(chunk)
                        dst.write(chunk)
                manifest.append({"path": rel.as_posix(), "size": info.file_size, "sha256": digest.hexdigest()})
    except zipfile.BadZipFile:
        fail("not a valid ZIP archive")
    (out / "manifest.json").write_text(json.dumps({
        "archive": archive.name, "archive_sha256": archive_hash,
        "entries": manifest
    }, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"archive_sha256": archive_hash, "file_count": len(manifest), "output": str(extracted)}))

if __name__ == "__main__":
    main()
