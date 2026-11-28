#!/usr/bin/env python3
"""Fail CI when Locco's launcher icon is missing or loses adaptive/themed assets."""

from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ANDROID = "{http://schemas.android.com/apk/res/android}"


def parse(path: Path) -> ET.Element:
    if not path.is_file():
        raise AssertionError(f"Required launcher resource is missing: {path.relative_to(ROOT)}")
    return ET.parse(path).getroot()


def verify_vector(path: Path, minimum_paths: int = 5) -> None:
    root = parse(path)
    assert root.tag == "vector", f"{path.name} must be a VectorDrawable"
    assert root.get(ANDROID + "viewportWidth") == "108"
    assert root.get(ANDROID + "viewportHeight") == "108"
    paths = root.findall("path")
    assert len(paths) >= minimum_paths, f"{path.name} is missing the complete Locco mark"


def main() -> int:
    manifest = parse(ROOT / "android/app/src/main/AndroidManifest.xml")
    application = manifest.find("application")
    assert application is not None, "Android manifest has no application element"
    assert application.get(ANDROID + "icon") == "@mipmap/ic_launcher", "Launcher icon is not registered"
    assert application.get(ANDROID + "roundIcon") == "@mipmap/ic_launcher_round", "Round launcher icon is not registered"
    assert application.get(ANDROID + "label") == "@string/app_name", "Application label must use the shared string resource"

    for filename in ("ic_launcher.xml", "ic_launcher_round.xml"):
        adaptive = parse(ROOT / "android/app/src/main/res/mipmap-anydpi-v26" / filename)
        assert adaptive.tag == "adaptive-icon", f"{filename} must be an adaptive icon"
        assert adaptive.find("background") is not None
        assert adaptive.find("background").get(ANDROID + "drawable") == "@color/locco_icon_background"
        assert adaptive.find("foreground") is not None
        assert adaptive.find("foreground").get(ANDROID + "drawable") == "@drawable/ic_locco_launcher_foreground"
        assert adaptive.find("monochrome") is not None
        assert adaptive.find("monochrome").get(ANDROID + "drawable") == "@drawable/ic_locco_launcher_monochrome"

    verify_vector(ROOT / "android/app/src/main/res/drawable/ic_locco_launcher_foreground.xml")
    verify_vector(ROOT / "android/app/src/main/res/drawable/ic_locco_launcher_monochrome.xml")
    print("Locco launcher identity verified: adaptive, round, and themed monochrome assets are registered.")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (AssertionError, ET.ParseError) as error:
        print(f"Launcher identity verification failed: {error}", file=sys.stderr)
        raise SystemExit(1)
