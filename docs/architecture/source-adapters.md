# Source acquisition and adapter contract

**Status:** Target contract, not a promise of universal scraping or connector availability.

Locco acquires information without making a paid API a mandatory dependency. Every source declares access method, authorization/terms, permissions, region, rate limits, authentication, fields, retention, provenance and failures. A source is never available just because an AI can see a name or URL.

## Native acquisition methods

| Method | Use | Limits |
|---|---|---|
| Android share/import | User explicitly shares selected text, URL, image or file | Only the supplied payload; no hidden app inspection |
| Document/file picker | User-selected files | MIME/type, size and parser safety checks |
| Native HTTP + HTML parser | Public static pages where access and terms allow | No login/session bypass, CAPTCHA or anti-bot evasion |
| Native Locco browser | User browses a site; explicit capture, inspect or task operation uses the current profile | Normal browsing is not automatic archival; DOM access is scoped and may be limited by origin/frame/engine |
| Documented public API | Free/authorized endpoint | Respect quota/terms; adapter remains optional |
| Android notifications | Explicit NotificationListener permission and user-defined app scope | Sensitive payload minimization; no indiscriminate history mirror |
| Accessibility observation | A specific user-approved capability where Android policy permits | Not a universal scraper or arbitrary private app inspector |
| Open geographic datasets | OpenStreetMap and other licensed datasets | Attribution and license/instance terms apply |
| Manual/CSV/clipboard capture | Universal fallback | User controls completeness and source context |

## Browser as experience and source

Locco Browser is a first-class browser even when the user does not invoke AI. Navigation, tabs, history, bookmarks, downloads, permissions and sign-in belong to the browser subsystem.

Acquisition from a page is a distinct user-authorized operation. A capture records the smallest useful material: page origin/URL, capture time, selected visible text or bounded DOM observation, source locator and hashes. Full raw markup or full-page retention requires explicit action and sensitivity/size review. Do not silently capture cookies, hidden fields, page tokens, password/OTP/payment contents or unrelated account data.

DOM observation is read-only and origin-scoped. DOM Stitching produces derived schemas/mappings; it does not change what the browser may access. Any page action is a separate typed capability with policy and approval.

## Maps and location intelligence

No paid Google Maps SDK/API is required or assumed. Prefer licensed/open geographic datasets and user-authorized public sources. A page may be captured only within technical/terms controls. Do not depend on undocumented endpoints or anti-bot bypass. Never claim unlimited Google Maps scraping or paid-provider parity.

## Adapter contract

Each adapter declares:
- Stable source ID, source type and display name.
- Access method and user-granted permission scope.
- Authorization/terms reference and checked date.
- Rate/concurrency/backoff and maximum capture bounds.
- Supported operations and output fields.
- Authentication/session dependency and where credentials are held.
- Raw capture/observation format, provenance metadata and content hash.
- Parser/schema version and evidence locator.
- Cancellation, partial-result, resumption and typed error semantics.
- Data retention, delete/revoke behavior and offline fallback.

Persist authorized evidence before parsing/enrichment. A source failure must not erase already captured evidence. Adapter replacement must not change evidence identity or domain rules. Credentials, cookies and passkey materials remain in their dedicated platform/session/token stores and are not copied into evidence records.
