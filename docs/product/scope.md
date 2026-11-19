# Product scope

## Product continuity

Locco is one continuing product in `ysfclc/Locco`. All ordinary product feedback is evaluated against and implemented in the existing application. A new bug, feature, source, workflow, domain or screen does not create a new app or replace the current product. A separate product requires explicit user direction.

The current implementation, open feedback, accepted requirements, data and working user flows are cumulative assets. They must be carried forward from phase to phase. A new roadmap phase is not a reset and a new request is not a replacement brief.

## In scope

Native Android; future iOS shell sharing Rust contracts; local data/evidence/entities/relationships/search/export; user-authorized public sources, visible browser capture, Android share/import, files, notifications, supported APIs and manual capture; on-device model adapter and compact context; capability registry, approvals, execution and verification; native multi-mode workspace. Additive domains compose these primitives.

## Explicit non-goals

- No HTML, web UI, web app, PWA or browser dashboard.
- Not Memo 2; Memo is separate and contributes selected engineering patterns only.
- Not an Outbound Sourcer clone, sales-only, trade-only, CRM-only or lead-generation product.
- Not a generic business OS or multi-tenant SaaS.
- No mandatory cloud, paid model/API/maps/scraping provider, subscription or monetization.
- No unrestricted scraping, hidden surveillance, credential harvesting or bypass of CAPTCHA/paywall/anti-bot/auth controls.
- No high-impact unattended action without policy/approval; no assumption AccessibilityService can inspect arbitrary app internals.
- No private donor-repo data import; no model weights in Git; iOS is not a Phase 0 build.
- No replacement application, second launcher app, new package ID or clean-slate scaffold for ordinary bug fixes and feature requests.

New domains may compose existing capabilities. New foundational subsystems, permissions, external dependencies, breaking data/FFI contracts or changes to product identity require an ADR and the reviews/explicit user approval defined in `GOVERNANCE.md`.
