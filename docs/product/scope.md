# Product scope

## In scope
Native Android; future iOS shell sharing Rust contracts; local data/evidence/entities/relationships/search/export; user-authorized public sources, visible browser capture, Android share/import, files, notifications, supported APIs and manual capture; a first-class Android app observation subsystem using UsageStats/UsageEvents, opt-in NotificationListenerService, scoped Accessibility observation, intents/App Links and supported provider interfaces; a capability registry and source-neutral observation/event pipeline; safe native handoffs and verified actions; on-device model adapter and compact context; capability registry, approvals, execution and verification; native multi-mode workspace.

## Explicit non-goals
- No HTML, web UI, web app, PWA or browser dashboard.
- Not Memo 2; Memo is separate and contributes selected engineering patterns only.
- Not an Outbound Sourcer clone, sales-only, trade-only, CRM-only or lead-generation product.
- Not a generic business OS or multi-tenant SaaS.
- No mandatory cloud, paid model/API/maps/scraping provider, subscription or monetization.
- No unrestricted scraping, hidden surveillance, credential harvesting or bypass of CAPTCHA/paywall/anti-bot/auth controls.
- No claim of universal visibility into third-party app processes; Android sandbox boundaries remain real. No root, modified/re-signed third-party APK, TLS pinning bypass, anti-tamper evasion, private endpoint replay or covert monitoring as a production dependency.
- Notifications, UsageStats, Accessibility, VPN metadata and in-process plaintext observation must remain distinct, accurately labelled acquisition methods.
- No high-impact unattended action without policy/approval; no assumption AccessibilityService can inspect arbitrary app internals.
- No private donor-repo data import; no model weights in Git; iOS is not a Phase 0 build.

New domains may compose existing capabilities. New foundational subsystems, permissions or external dependencies require ADR/product review.