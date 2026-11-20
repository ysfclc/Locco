# Native UI/UX architecture

**Status:** Target UX architecture; no production UI is implemented beyond the placeholder shell.

Composition: semantic tokens → primitives → behavior patterns → feature compositions → adaptive destinations. Android uses Compose, Navigation Compose and feature ViewModels with immutable/restorable presentation state. Navigation owns back stacks; ViewModels own transient UI state; Rust owns durable domain state.

## Experience principles

- Locco is one coherent workspace, not a collection of disconnected mini-apps or a chatbot with technical screens.
- Memo-2 is a reference for useful screen breadth and interaction patterns; Memo-1 is a reference for mature design-system and visible browser workflow thinking. Neither is copied.
- Design Locco's own IA, visual language, motion, navigation and component system by testing actual user tasks.
- Browser is a complete browser to the human independently of AI: address/navigation, tabs, history, bookmarks, downloads, account profiles and site controls.
- Model Hub is a rich catalogue and lifecycle manager, not a few static download cards.
- DOM inspection/Stitching is contextual within the browser and workspace. It must not make the browser feel like an automation tool rather than a browser.
- Capability/compiler internals are progressively disclosed: show the goal, requirements, chosen components, gaps and execution plan when useful, not as mandatory jargon on every screen.
- Every feature exposes loading/content/empty/offline/partial/permission-denied/blocked/stale/error/uncertain/verified states.
- Long tasks expose real progress, pause/cancel, checkpoints and takeover. Success requires outcome evidence.
- AI progressively discloses summary, uncertainty, supporting evidence and source provenance.

## Navigation and persistence

Design primary navigation only after task-flow mapping for capture, workspace, browser, Model Hub, activity and artifacts. Do not add a primary tab for every technical service. Browser and Model Hub are first-class capabilities, whether persistent destinations or contextual workspace surfaces must be decided through discoverability and task testing.

Switching destinations preserves conversations, drafts, browser tabs/profile state, selected records, scroll state and active execution context. Deep links open the exact record/run/page and preserve a path back. Android Back unwinds overlays and current destination before switching primary areas.

## Design system

Create Locco-specific semantic color roles, typography, spacing/density, controls, surfaces, iconography, motion/haptics and reduced-motion behavior. Do not copy Memo-2 Paper/Mint or Memo-1 Signal/Graphite tokens. Use restraint: clear hierarchy and information density, not decorative metrics, perpetual glow or a card around every row.

Dedicated design-system module and feature modules are options only after there are stable dependency/test boundaries. Current Android build has a single app module.

## Accessibility and acceptance

Shared components expose semantics and support keyboard/focus, 48dp targets, font scaling, contrast and TalkBack. Test compact and expanded layouts, landscape, IME, offline and permission denial. Every visible control must map to a real capability and named acceptance test. Screenshots are visual evidence only, not behavior/accessibility proof.

AI unavailable → deterministic search/filter/evidence remains usable. Permission denied → explain and offer permitted manual/import alternatives. Browser sign-in and profile lifecycle must pass LOC-0005 before browser subsystem acceptance.


## Supporting UX decision records

The high-level UX invariants in this file are elaborated by:
- native-experience.md — Locco interaction domains, task journeys and competing navigation hypotheses.
- reference-inspiration-audit.md — verified Memo-1/Memo-2 source findings and the explicit do-not-copy rules.
- requirement-traceability.md — planned acceptance evidence for UX and every other product requirement.

Use ui-ux.md for normative UX invariants; native-experience.md for design research and iteration. Neither selects final primary navigation until task-based prototype evaluation is complete.


## Comparative prototype brief

See [locco-ux-prototype-brief.md](locco-ux-prototype-brief.md) for Workspace-led, Intent-led and Environment-led alternatives plus the Browser and Model Hub experience contracts.
