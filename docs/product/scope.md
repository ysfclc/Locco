# Product scope

## In scope
Native Android; future iOS shell sharing Rust contracts; local data/evidence/entities/relationships/search/export; user-authorized public sources, visible browser capture, Android share/import, files, notifications, supported APIs and manual capture; on-device model adapter and compact context; capability registry, approvals, execution and verification; native multi-mode workspace.

## Non-goals
- No HTML, web UI, web app, PWA or browser dashboard.
- Not Memo 2; Memo is separate and contributes selected engineering patterns only.
- Not an Outbound Sourcer clone, sales-only/trade-only/CRM-only/lead-gen product.
- Not a generic business OS or multi-tenant SaaS.
- No mandatory cloud, paid model/API/maps/scraping provider, subscription or monetization.
- No unrestricted scraping, hidden surveillance, credential harvesting or bypass of CAPTCHA/paywall/anti-bot/auth controls.
- No high-impact unattended action without policy/approval; no assumption AccessibilityService can inspect arbitrary app internals.
- No private donor-repo data import; no model weights in Git; iOS not a Phase 0 build.

New domains may compose existing capabilities. New foundational subsystems, permissions or external dependencies require ADR/product review.