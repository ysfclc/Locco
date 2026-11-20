# Locco native UX prototype brief

**Status:** Comparative proposal only. No navigation concept is selected or implemented.

## 1. Design principles from the reference audit

Keep the useful Memo-2 lessons: product breadth is visible, Model Hub has a clear home, and actions sit close to the objects they change. Keep the useful Memo-1 lessons: semantic tokens, consistent native components, explicit states and visible approval/takeover. Do not copy either product's palette, typography, labels, tab structure, screen composition, component hierarchy or code.

Locco should feel like a capable personal instrument: calm for browsing/reading, precise for inspecting evidence, information-dense when comparing models, and unmistakable when an action needs approval. Not a generic analytics dashboard or a chatbot with unrelated mini-apps.

## 2. Three navigation concepts to prototype

All options expose Intent, Workspace, Browser, Activity/Control, Model Hub, Capability Library and Settings. They differ in their primary anchors and contextual organization.

### A — Workspace-led
**Hypothesis:** Users return to information and ongoing work more often than to a blank prompt.
- Compact phone: primary anchors **Workspace · Browser · Activity**; global capture everywhere.
- Workspace home: Today/Recent, captures, resumable work, pinned collections, unresolved items and search; no empty dashboard.
- Models is a clearly visible global destination/switcher entry, not buried in Settings; active model/runtime status stays visible.
- Capability Library is reachable from workspace tools and a plan's missing-capability state.
- Expanded: Workspace/Browser/Activity/Models navigation rail, main content and evidence inspector.
- Strength: continuity and retrieval. Risk: Intent/Models may feel secondary.

### B — Intent-led
**Hypothesis:** Locco's defining behavior is turning intent into evidence-backed work.
- Compact phone: **Now · Browser · Library · Models**.
- Now combines compose, clarification and resumable work; not a chat-only screen.
- Library holds evidence, entities, claims, collections and artifacts.
- Models is a direct destination; runtime health is visible from Now.
- Activity/approvals is a global queue and per-task timeline, not another crowded tab.
- Expanded: task rail, central task/workspace pane, plan/evidence inspector; Browser and Models open as full environments while retaining task context.
- Strength: makes task composition tangible. Risk: over-emphasizes AI and subordinates browsing/retrieval.

### C — Environment-led adaptive shell
**Hypothesis:** Browser, Workspace and Model Hub are substantial environments and navigation should adapt to window size.
- Compact phone: switcher for **Workspace · Browser · Library**; Model Hub is a first-class full-screen environment from the global switcher; capture is global; Activity/approvals use a labelled persistent status entry.
- Expanded: rail for Workspace/Browser/Models/Activity; multi-pane content with contextual inspector and optional activity queue.
- Strength: distinct environments scale to wider screens. Risk: environment switching may feel less immediate on phones.

**Selection rule:** Build comparable native/clickable prototypes. Evaluate all 12 journeys in reference-inspiration-audit.md with identical task instructions and data. Record completion, time, wrong turns, exits, state loss, one-hand reach, accessibility and user preference. Include a user unfamiliar with Locco terminology. Select by observed task outcomes, not aesthetics or item count.

## 3. Shared shell contract

- Global capture: text, voice, Android share and file intake use one canonical flow with source context and cancel.
- Global search/command explicitly distinguishes workspace, current page, model catalogue and capabilities; never silently switches corpus.
- Activity shows queued/running/waiting approval/paused/uncertain/failed/verified with real controls. Approval is not success.
- Model status shows active model/runtime and health; measured resource state only. No model installed is normal.
- Inspector exposes provenance/source/time/uncertainty and edits through canonical domain commands.
- Tabs, tasks, drafts, selection and scroll survive ordinary navigation. Only private browser state is ephemeral.
- Back unwinds the active environment then contextual surfaces; never silently discards drafts or approvals.
- Compact one-hand use, expanded rails/panes only when width supports them; TalkBack, keyboard/focus, large fonts, 48dp targets, contrast and reduced motion are required.

## 4. Browser: real browser first, intelligence second

- Omnibox supports URL/search and active-profile history/bookmark suggestions, with origin/security state.
- Page gets most of the display. Loading/offline/TLS-blocked/renderer-crash/error states are explicit.
- Controls: Back, Forward, Stop/Reload, tab overview and More; no decorative/inert buttons.
- Tabs show title/domain and normal/private context. Private previews and history do not leak.
- Downloads provide progress, pause/resume/cancel/retry, destination, MIME/size and explicit open/share; never auto-run executables.
- Site permissions and data are accessible from the origin panel.
- DOM Inspect is contextual and reversible. The user enters Inspect, selects semantic regions, sees role/name/text/provenance/limits and opens a structured view of ancestors/repeated siblings without credential values.
- Stitch uses local AI to propose relationships/schema/locators; show examples, coverage, virtualized/missing content, conflicts and freshness before acceptance.
- Accepted definitions are versioned and backed by original observations, not arbitrary scripts.
- Mutations/navigation invalidate stale targets; every later action re-resolves live locators and stops on ambiguity.
- Exiting Inspect returns to browsing. Capture is a separate explicit action; merely visiting a page does not archive it.

## 5. Model Hub: catalogue and lifecycle manager

- Offline-capable and useful with no installed model.
- Search/filter by name, publisher, task/modality, architecture, format/quantization, runtime, license, source trust, size and measured device-fit.
- Categories: Language, Embedding, Reranking, Vision, Speech, Audio, Image and Multimodal only where runtime support exists.
- Separate curated, official, user-added and local-import sources; show publisher, immutable revision, license and trust.
- Result rows show tasks/modalities, format/quantization, artifact footprint, runtime support, measured fit, license, provenance, benchmark evidence and unknowns.
- Detail sections: Overview, Files, Runtime compatibility, Device measurements, Quality, License/Provenance and Changelog.
- Compare models on identical axes: exact artifact, runtime/backend, task corpus/version, memory, latency, sustained thermal behavior, license and unknowns. Never rank solely by parameters or headline tokens/s.
- Transfers: resumable/pause/cancel/retry, preflight, per-file digest verification and visible staging space.
- Digest integrity and publisher authenticity are separate.
- Stage → parse/validate → runtime-load-test → benchmark → activate. Failure leaves previous active model untouched; partial downloads cannot activate.
- Never silently switch to remote inference; user chooses model or explicit policy.
- Resource view shows active model/backend, measured context/load state and unload/switch rationale. One heavyweight generative model resident initially unless measured combinations prove safe.

## 6. Visual direction and unresolved choices

Use Locco-specific tokens only after navigation tests: calm reading surfaces, clear work/page hierarchy, restrained active focus, semantic risk/wait/error/verified colors, dense evidence/model rows, meaningful motion/haptics, reduced-motion and high-contrast light/dark variants. Do not restyle remote page content except user-controlled page settings.

Not decided yet: winning navigation, colors/typography/icons, browser engine, initial runtimes, optional remote providers or later optional browser features. These require prototypes and device measurements.
