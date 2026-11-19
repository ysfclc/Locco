# Source acquisition and adapter contract

Locco must acquire useful information without a paid API becoming a dependency. "Scrape it" is not a blanket permission or a guarantee that every source is extractable. Each adapter declares source, access method, authorization/terms, permitted regions, rate limits, authentication requirement, fields collected, retention, failure modes and provenance.

## Native acquisition methods

| Method | Use | Limits |
|---|---|---|
| Android share/import intent | User explicitly sends selected text, URL, image or file to Locco | Only the shared payload; no hidden app inspection |
| Document/file picker | User-selected local files | Validate MIME, size and parser safety |
| Native HTTP + HTML parser | Public static pages where access/terms allow | No login/session bypass, CAPTCHA or anti-bot evasion |
| Visible Android WebView/browser surface | User-controlled page interaction and extraction of visible DOM/text | Visible, pausable, user can take over; no hidden background scraping |
| Documented public API | Only where free/authorized and no paid dependency | Respect quotas and terms; adapter remains optional |
| Android notifications | Explicit notification-listener permission and user-defined app scope | Notification content may be sensitive; minimize and retain only selected signals |
| Accessibility observation | Only a specific user-approved capability where policy permits | Not a universal scraper; no arbitrary private app internals |
| Open geographic datasets | OpenStreetMap and other licensed public datasets | Attribute, obey ODbL/other dataset terms, use public instances responsibly |
| Manual/CSV/clipboard capture | Universal fallback | User controls completeness and source context |

## Maps and location intelligence
No paid Google Maps SDK/API is required or assumed. Prefer OpenStreetMap-derived datasets and licensed public geodata for canonical map/search features. A user-visible map page may be captured only within its terms and technical controls; DOM extraction is fragile and must not depend on undocumented endpoints or anti-bot bypass. Never claim unlimited Google Maps scraping or route/geocoding parity with a paid provider.

## Adapter contract
Each adapter declares:
- stable source ID and display name;
- access method and permission scope;
- authorization/terms reference and checked date;
- rate/concurrency/backoff limits;
- supported operations and fields;
- raw capture format and provenance metadata;
- parser version and content hash;
- cancellation, partial-result and error semantics.

Persist raw evidence before parsing/enrichment. A source failure must not erase already captured evidence. Adapter replacement must not change evidence identity or domain rules.