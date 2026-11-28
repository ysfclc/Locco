#!/usr/bin/env python3
"""Verify Locco's scoped, passive AccessibilityService contract."""

from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ANDROID = "{http://schemas.android.com/apk/res/android}"


def parse(path: Path) -> ET.Element:
    if not path.is_file():
        raise AssertionError(f"Required Android resource is missing: {path.relative_to(ROOT)}")
    return ET.parse(path).getroot()


def main() -> int:
    manifest_path = ROOT / "android/app/src/main/AndroidManifest.xml"
    manifest = parse(manifest_path)
    service = next(
        (
            item for item in manifest.findall("./application/service")
            if item.get(ANDROID + "name") == ".LoccoAccessibilityObserver"
        ),
        None,
    )
    assert service is not None, "LoccoAccessibilityObserver is not registered"
    assert service.get(ANDROID + "permission") == "android.permission.BIND_ACCESSIBILITY_SERVICE"
    assert service.get(ANDROID + "exported") == "true"
    actions = {
        action.get(ANDROID + "name")
        for action in service.findall("./intent-filter/action")
    }
    assert "android.accessibilityservice.AccessibilityService" in actions
    metadata = next(
        (
            item for item in service.findall("./meta-data")
            if item.get(ANDROID + "name") == "android.accessibilityservice"
        ),
        None,
    )
    assert metadata is not None
    assert metadata.get(ANDROID + "resource") == "@xml/locco_accessibility_service"
    assert "isAccessibilityTool" not in manifest_path.read_text(encoding="utf-8")

    config = parse(ROOT / "android/app/src/main/res/xml/locco_accessibility_service.xml")
    assert config.tag == "accessibility-service"
    assert config.get(ANDROID + "accessibilityEventTypes") == (
        "typeWindowStateChanged|typeViewClicked|typeViewScrolled"
    )
    assert config.get(ANDROID + "accessibilityFeedbackType") == "feedbackGeneric"
    assert config.get(ANDROID + "canRetrieveWindowContent") == "true"
    assert config.get(ANDROID + "notificationTimeout") == "150"
    assert "typeAllMask" not in ET.tostring(config, encoding="unicode")
    assert "typeViewTextChanged" not in ET.tostring(config, encoding="unicode")

    service_source = (
        ROOT / "android/app/src/main/java/com/locco/app/LoccoAccessibilityObserver.kt"
    ).read_text(encoding="utf-8")
    observer_source = (
        ROOT / "android/app/src/main/java/com/locco/app/ObserverScreen.kt"
    ).read_text(encoding="utf-8")
    migration_source = (ROOT / "core/kernel/src/migrations.rs").read_text(encoding="utf-8")

    assert "CAPTURE_UI_OBSERVATIONS_KEY, false" in service_source
    assert "UI_DISCLOSURE_VERSION_KEY" in service_source
    assert "selectedPackages(latest)" in service_source
    assert "setServiceInfo(info)" in service_source
    assert "info.packageNames = if (enabled) allowed.toTypedArray() else emptyArray()" in service_source
    assert "val permitted = visible && !password && !editable" in service_source
    assert "text = if (permitted) boundedCharSequence(node.text) else null" in service_source
    assert "contentDescription = if (permitted) boundedCharSequence(node.contentDescription) else null" in service_source
    assert "isCaptureEnabled(latest)" in service_source
    assert "rateLimiter.shouldAccept(sourcePackage, eventType, nowUptime)" in service_source
    assert "boundedCharSequence(node.text)" in service_source
    assert "android_accessibility_service" in migration_source
    assert "CREATE TABLE app_ui_observations" in migration_source
    assert "CAPTURE_UI_OBSERVATIONS_KEY" in observer_source
    assert "UI_DISCLOSURE_VERSION" in observer_source
    assert "I understand and enable capture" in observer_source
    assert "ACTION_ACCESSIBILITY_SETTINGS" in observer_source

    prohibited_calls = (
        r"\.performAction\s*\(",
        r"\.dispatchGesture\s*\(",
        r"takeScreenshot\s*\(",
        r"MediaProjection",
        r"TYPE_VIEW_TEXT_CHANGED",
        r"FLAG_INCLUDE_NOT_IMPORTANT_VIEWS\s*\|",
        r"FLAG_RETRIEVE_INTERACTIVE_WINDOWS\s*\|",
    )
    for pattern in prohibited_calls:
        assert not re.search(pattern, service_source), f"Forbidden Accessibility behavior found: {pattern}"

    print(
        "PASS: AccessibilityService is a selected-app, consent-gated, passive observer; "
        "event types are narrow, editable/password text is not read, and UI evidence uses its own v8 ledger."
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (AssertionError, ET.ParseError) as error:
        print(f"Accessibility observation contract failed: {error}", file=sys.stderr)
        raise SystemExit(1)
