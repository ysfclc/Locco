# Locco visual identity

## Launcher mark

Locco's mark is a small network of visible signal points connected through a shared center. It represents the product loop: signals arrive from separate sources, Locco preserves their evidence, and useful relationships become inspectable. It is intentionally source-neutral; it does not imply access to an app or data that Android has not actually exposed.

The mark is built as an Android VectorDrawable and registered as an adaptive launcher icon. The foreground stays inside the adaptive-icon safe region. Android 13+ themed icons use a separate single-colour monochrome vector rather than a tinted multicolour bitmap.

## Palette

The launcher uses the same restrained colours already present in the native workspace:

| Role | Value | Use |
|---|---|---|
| Ink | `#0B0E13` | Icon background and main workspace |
| Periwinkle | `#93A5FF` | Evidence paths and primary action |
| Mint | `#84DCC9` | Source points and secondary action |
| Soft white | `#F5F7FF` | Central connection point and high-contrast detail |

## Product rules

- Keep the mark legible at small launcher sizes and under circular, squircle and other adaptive masks.
- Keep the monochrome asset geometrically identical to the full-colour mark.
- Do not use a robot face, sparkle cluster, chatbot bubble or generic AI glyph as the product identity.
- Do not imply universal access, cloud synchronisation or automatic actions through the icon.
- App label, package name and signing identity remain independent from visual asset changes.

The launcher resources are checked by `scripts/verify-android-launcher-icon.py` and compiled as part of the Android build.
