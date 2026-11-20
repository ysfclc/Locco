# Threat model

**Status:** Initial system-level threat model; expand per subsystem and verify controls through adversarial tests.

## Assets and boundaries

Assets: original captures/evidence, entity/claim graph, curated memory, database and blob store, model artifacts/catalogue metadata, model prompts/outputs, browser profiles/cookies/storage, OAuth tokens, passkey and credential flows, execution plans, approval envelopes, side-effect receipts, signing keys, repository credentials and release artifacts.

Trust boundaries: websites/frames, browser engine and DOM observer, identity providers/authentication callbacks, source adapters, imported files/archives, model/catalogue publishers, local runtime/native libraries, Rust FFI, Android OS permissions, CI and release signing.

| Threat | Required controls | Residual risk |
|---|---|---|
| ZIP traversal/bomb/link | Isolated extraction; reject absolute/parent paths, links/special files; cap entries/bytes/ratio | Parser bugs |
| Prompt injection from web/file/model | Source is untrusted data; context separation; model has no authority; typed capability allowlist | Semantic manipulation |
| Cross-profile cookie/session leak | Engine-managed isolated profiles; runtime feature checks; no cookie copying; cross-profile tests | Browser engine/provider defect |
| OAuth redirect interception | Auth Tab/Custom Tabs fallback handling; PKCE, state and OIDC nonce; verified redirect where supported | User/browser/provider misconfiguration |
| Embedded browser policy rejection | Detect/communicate supported handoff; no user-agent spoofing or bypass | Site functionality may be unavailable inside embedded session |
| DOM bridge/origin abuse | No privileged page-world bridge; static origin-scoped isolated-world observer; frame/origin/schema/size checks | Engine/runtime vulnerabilities |
| Sensitive field leakage | DOM redaction for password/OTP/payment/recovery fields; no cookies or auth headers in context/logs | Site-specific field misclassification |
| Stale/ambiguous DOM action | Fresh snapshot, unique locator resolution, action envelope, pause/takeover, no silent commit retry | Dynamic page race |
| Malicious/compromised model artifact | Source revision, exact manifest, hash/signature where available, file/parser bounds, runtime sandbox boundary | Upstream publisher compromise |
| Incompatible/OOM model | Runtime compatibility matrix, storage/RAM preflight, load test, cancellation/unload, typed OOM | Device/vendor variability |
| Compromised dependency/action | SHA pins, lockfiles, cargo-deny/audit, Gradle verification, SBOM and provenance | Upstream zero-day |
| Secret leakage | Dedicated encrypted secret/token store, no echo, short-lived CI credentials, redacted diagnostics | Compromised runtime/runner |
| Signing theft | Protected release environment, reviewer gate, ephemeral keystore file, cleanup | Malicious approved release |
| FFI memory bug | Minimal unsafe, pointer/length validation, panic containment, fuzz and ABI tests | Native memory bug |
| Data loss/corruption | Atomic writes, backup/migration, integrity checks, restore and crash tests | Device loss/hardware failure |
| Unauthorized side effect | Capability manifest, exact target/payload/authority approval, idempotency and postcondition proof | External source drift |

Imported data/code is never executed automatically. AI cannot widen authority. Approval is immutable and expires. Private content and secrets are excluded from telemetry. No bypass scraping, hidden surveillance or covert account use.


## Model authenticity refinement

A matching digest verifies byte integrity against a particular manifest; it does not authenticate the publisher or prove model safety. Keep source pinning, digest verification, signature/attestation verification, parser validation, runtime load, device fit and task quality as separate evidence. Never permit a model catalogue to install native/runtime code. See docs/architecture/model-ecosystem.md, section 14.
