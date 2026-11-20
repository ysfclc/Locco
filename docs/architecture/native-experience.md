# Native product experience architecture

**Status:** Proposed for LOC-0004 review. This is Locco's UX design work, not a copied Memo theme or screen inventory.

## 1. Design objective

Locco should feel like one capable native environment: fast for capture, calm for research, precise when inspecting evidence and unmistakable when an action needs approval. It must not resemble a generic metrics dashboard, a set of donor-app screens or a chatbot with unrelated mini-apps attached.

Memo-2 is a useful interaction reference. Memo-1 adds a design-system and visible browser-session reference. Neither should be copied. We will inspect what works, identify gaps, design Locco-specific alternatives and test them against actual tasks.

## 2. Reference lessons

| Reference | Learn from | Do not inherit |
|---|---|---|
| Memo-2 | Clear work areas for Live, Talk, Work, Studio, Models and Settings; Compose surfaces; a dedicated model area | Fixed bottom tabs as unquestioned IA, three-model catalogue, placeholder planner, unimplemented browser roadmap or any unverified assumption |
| Memo-1 | Visible browser sessions, explicit prompt preview/approval, status language and shared Compose primitives | Browser-specific workflow coupling, split data stores, incomplete navigation integration or unverified behavior |
| Locco | Evidence-first workspace, local-first/native constraints and generic capability boundaries | Donor-project identity or architecture by screen copying |

Use concepts, not code/screen copy. Keep the successful elements, then improve information hierarchy, completeness, state handling, accessibility, adaptive layouts and motion.

## 3. Experience domains

Make these capabilities discoverable in one product:
- Intent/conversation: capture, voice/text, files/share, conversations, clarification and plan inspection.
- Workspace/knowledge: search, records, evidence, entities, claims, relationships and curated memory.
- Browser: tabs, address/navigation, accounts/sessions, downloads, inspect mode, DOM and page-to-workspace actions.
- Activity/control: runs, checkpoints, approvals, evidence, pause, stop, takeover, uncertainty and recovery.
- Model Hub: discovery, inspection, comparison, import, install, configuration and benchmarks.
- Capabilities: available operations, trust/permissions, plan composition and reusable components.
- Artifacts: creation, editing, versions, preview, export and provenance.
- System controls: privacy, permissions, storage, profiles, backups and diagnostics.

These are conceptual domains, not a predetermined list of bottom tabs. Browser and Model Hub are first-class capabilities. Their exact top-level navigation placement must be tested.

## 4. Interaction principles

1. One dominant goal per surface; progressive disclosure for technical detail.
2. Evidence and provenance appear where a decision depends on them.
3. Users can inspect what Locco saw, inferred, proposed and actually did.
4. No decorative progress, invented metrics or success animation before evidence exists.
5. Conversations, tabs, drafts, selections and run state survive navigation/process recreation where appropriate.
6. Async work exposes progress, pause/cancel, uncertainty and recovery.
7. Permission is requested when a capability becomes useful, with scope and denial fallback.
8. Browser accounts and profiles are visible, not buried in provider settings.
9. Model recommendations explain device/task fit.
10. TalkBack, font scale, keyboard/IME, touch targets, reduced motion and adaptive windows are foundational.
11. Every major capability has empty, loading, partial, offline, denied, blocked, stale, failed, uncertain and verified states.
12. Every visible control maps to a real operation and acceptance test.

## 5. Original design system

Create a Locco identity rather than renaming Memo-2 Paper/Mint or Memo-1 Signal/Graphite. Define semantic light/dark colors, typography, spacing/density, touch targets, shapes, elevation, iconography, motion/haptics/reduced-motion, and components for evidence, origin, model compatibility, task graph, approvals, progress and uncertainty.

Use restraint. Information hierarchy should carry the screen. Avoid wrapping every row in a card, adding dashboard metrics without a decision, or using glow/animation instead of meaningful state.

## 6. Navigation research

Before locking primary navigation:
1. Rank user tasks by frequency, urgency and context.
2. Map entry points: launch, share, voice, notification, deep link and browser.
3. Map capture → evidence → understanding → workspace → action flows.
4. Test discoverability of Browser, Model Hub, Workspace and Activity without a tutorial.
5. Test one-hand reach, compact/expanded layouts, keyboard and landscape.
6. Test destination stacks, Android Back, unsaved work and process recreation.
7. Reject layouts that bury important capabilities in technical settings.

Do not create multiple shell Activities or separate sub-apps for capabilities within Locco.

## 7. Browser and Model Hub UX

Browser should be usable without AI: navigation, tabs, login, history, bookmarks, downloads and permissions. AI assistance is contextual: inspect, select, stitch observations, extract, compare or compile a plan. Work remains visible and pausable.

Model Hub must show task/modality, architecture, quantization, license, size, runtime/device compatibility, benchmark evidence and unknowns. Comparison is first-class. Install state is durable; settings expose only runtime-supported controls.

## 8. Iteration workflow

Every consequential UX slice:
- Inspect reference source and existing Locco code.
- Define the user task and friction.
- Produce alternatives where navigation/hierarchy is consequential.
- Build interactive native UI, not static mockups presented as features.
- Test real content/device conditions.
- Verify every action, state and accessibility path.
- Record feedback and revise.
- Capture light/dark and compact/expanded screenshots.

Screenshots prove visual appearance only; they do not prove behavior, accessibility, session persistence or successful login.

## 9. Acceptance

- User can browse and sign in without invoking AI.
- User can inspect model compatibility before downloading.
- Workspace remains useful without a model installed.
- User can locate, pause and stop active work.
- Navigation preserves browser tabs, conversation, drafts and run state.
- Every control has a real action and failure state.
- Accessibility and responsive reviews pass.
- Browser account login and session continuity pass LOC-0005.
