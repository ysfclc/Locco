# ADR-0010: Source access is adapter-based and authorization-aware

Status: Accepted · 2026-11-19 · Reversal cost: Medium

Context: Locco's value includes native acquisition across web, apps, files, maps and other sources, with no paid API budget. Sources have different terms, stability and permission boundaries.

Decision: Every source is a replaceable adapter with declared access method, terms/authorization, rate limits, fields, provenance and failure semantics. Prefer share/import, file picker, public HTML, visible user-controlled browser extraction, documented free APIs and open datasets. No paid Google Maps API dependency. No bypass of login, CAPTCHA, paywall, anti-bot controls or app sandbox.

Alternatives: One universal scraper (false promise and fragile); official paid APIs only (violates budget); undocumented endpoints/stealth automation (unsafe and unstable).

Rationale: Adapter contracts preserve extensibility while keeping legal, technical and user-permission constraints visible.

Failure: DOM drift, throttling, access denial, data licensing or source changes. Provide manual/import fallback and stop safely.

Reversal: Individual adapters are low-cost swaps; changing evidence/provenance schema is medium/high cost.