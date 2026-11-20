# Product scope

## In scope

- **Native Android product:** one continuing Locco application, with Rust-owned domain contracts and a Kotlin/Compose native experience. iOS is a future native shell sharing those contracts and remains gated.
- **Canonical local workspace:** local records, immutable evidence, provenance, entities, relationships, claims, search, derived indexes, export, encrypted backup/restore and recovery.
- **First-class native browser:** ordinary human browsing independent of AI, including address/search, navigation, tabs and safe pop-ups, history/bookmarks, downloads and system-picker uploads, validated external links, find/zoom/desktop mode/save-PDF, per-site permission/data controls, OS credential/autofill interoperability, and explicit persistent/private session profiles. Account sign-in and session continuity are first-class acceptance requirements; provider restrictions must be handled through honest supported handoff. Extensions, cloud sync, VPN/proxy, translation and advanced content blocking are deferred and separately gated.
- **DOM intelligence:** bounded, versioned, provenance-bearing page observations, element/relationship inspection, semantic locators and local-AI-assisted DOM Stitching into validated reusable schemas, entity mappings and generic plans. The page remains untrusted input; observations never expose credentials or grant action authority.
- **Acquisition adapters:** user-authorized websites and public sources, browser navigation/capture, Android share/import, files, supported notifications, documented APIs and manual capture. Access is permission-, terms-, rate- and platform-constrained; partial results and provenance are preserved.
- **Model ecosystem:** a rich, extensible Model Hub covering language, embedding, reranking, vision, speech/audio, image and multimodal/specialist models where compatible runtimes and device resources support them. It includes catalogue discovery, comparison, artifact manifests, import/download, integrity verification, runtime management, device-fit assessment, benchmarks, routing and resource scheduling. Optional remote inference/provider adapters may exist only with explicit user configuration/consent; no cloud provider is mandatory.
- **General-purpose intelligence and composition:** deterministic analysis, evidence retrieval, selective context, local semantic inference, a typed capability registry and a generic TaskSpec/RequirementGraph/Capability Compiler. New tasks are composed from reusable versioned components; core planner branches must not be hardcoded for individual use cases.
- **Controlled execution:** reusable platform/native adapters, inspectable plans, permission checks, exact approvals, durable execution, cancellation, pause/takeover, idempotency and postcondition verification.
- **Native workspace:** intent/conversation, knowledge/evidence, browser, model management, activity/control, capabilities and artifacts. These are product domains, not a fixed navigation layout; information architecture and visual design must be iterated against real tasks.

## Explicit non-goals

- No HTML-based Locco application, web UI, web app, PWA or browser dashboard. This does **not** prohibit the native Android browser from rendering ordinary remote websites.
- Not Memo 1 or Memo 2; both remain separate products and contribute selectively audited interaction and engineering ideas only. No screen, navigation, theme, package, workflow or source-structure cloning.
- Not an Outbound Sourcer clone, sales-only, trade-only, CRM-only or lead-generation product.
- Not a generic business OS or multi-tenant SaaS.
- No mandatory cloud, paid model/API/maps/scraping provider, subscription or monetization.
- No unrestricted scraping, hidden surveillance, credential harvesting, or bypass of CAPTCHA, paywall, anti-bot or authentication controls.
- No high-impact unattended action without policy/approval; no assumption that AccessibilityService can inspect arbitrary app internals.
- No private donor-repository data import and no model weights committed to Git.
- iOS is not a Phase 0 or current Android implementation target.

New domains may compose existing capabilities. New foundational subsystems, permissions or external dependencies require product/architecture review and a recorded ADR.