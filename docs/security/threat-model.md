# Threat model

Assets: personal source content, entity graph, memory, DB, prompts/outputs, cookies, signing keys, GitHub credentials and artifacts. Boundaries: external sources, ZIP ingestion, model output, FFI, CI/secrets and signing.

| Threat | Control | Residual risk |
|---|---|---|
| ZIP traversal/bomb/link | isolated extraction; reject absolute/parent paths, links/special files; cap entries/bytes/ratio | parser bugs |
| Prompt injection | source is data; model has no authority; tool allowlist | semantic manipulation |
| Compromised dependency/action | SHA pins, lockfiles, cargo-deny/audit, Gradle verification, SBOM | upstream zero-day |
| Secret leakage | GitHub secrets/environment, no echo, short-lived OIDC | compromised runner |
| Signing theft | protected environment, ephemeral key file, cleanup | malicious approved release |
| FFI memory bug | minimal unsafe, pointer checks, panic containment, fuzz | native bug |
| Data loss | atomic writes, migration backup, export/restore tests | device loss |
| Unauthorized action | capability manifest, exact approval, postcondition | source drift |

Imported data is not executable. AI cannot widen authority. Approval is immutable and expires. Private content/secrets excluded from telemetry. No bypass scraping or hidden surveillance.