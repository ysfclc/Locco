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


## 10. Comparative native UX brief

See [locco-ux-prototype-brief.md](locco-ux-prototype-brief.md) for three concrete navigation alternatives and the detailed Browser/Model Hub surface contracts. No winner is selected until user-task testing.

## 10. Source-audited inspiration and original Locco interaction design

See [reference-inspiration-audit.md](reference-inspiration-audit.md) for the full source-level comparison and explicit limitations. The summary is binding:

- Memo-2's current six areas are a useful breadth reference, not a navigation specification. Its current app source has no first-class browser implementation and its current model catalogue is three pinned Qwen entries.
- Memo-1 has stronger concrete browser-session and semantic-token examples, but its browser is not a full profile/authentication solution. Its external-browser sign-in does not transfer cookies into its embedded session.
- Reuse design disciplines—semantic tokens, consistent components, honest state, visible takeover—not donor colors, labels, screen hierarchy, provider lists or application structure.

### Locco interaction concepts

Keep the following product capabilities discoverable without committing prematurely to tab placement:

- **Intent surface:** fast text/voice/share/file capture, conversation continuity, clarification and plan inspection.
- **Workspace:** searchable entities, evidence, claims, relations, tasks and artifacts.
- **Browser environment:** a proper browser with its own tab/session controls and a contextual Locco inspection layer.
- **Activity and control:** durable work timeline, approvals, pause, cancel, takeover, uncertainty and verified outcomes.
- **Model Hub:** discover, compare, acquire, inspect and manage model artifacts and runtime/device compatibility.
- **Capability library:** inspect available operations, procedures, trust, permissions and versions.
- **System controls:** privacy, account/profile lifecycle, permissions, storage, backup and diagnostics.

These are domains, not fixed screens or tabs. A capability may appear contextually in more than one place, but it must have one authoritative underlying state and one clear owner.

### Required design iterations

1. Map the high-frequency and high-risk task journeys from LOC-R001–LOC-R019.
2. Create at least three low-fidelity native navigation prototypes using the hypotheses in the reference audit.
3. Test the same task set on each; record wrong turns, time-to-destination, state loss, one-hand reach and discoverability.
4. Choose the smallest navigation that makes Workspace, Browser, Activity and Model Hub discoverable without overcrowding compact phones.
5. Define Locco-specific semantic tokens and shared Compose components only after the information architecture is tested.
6. Prototype Browser and Model Hub as real interactive native surfaces. Every control must have a state, operation, error path and test.
7. Review light/dark, compact/expanded windows, large font scale, TalkBack, keyboard/IME, reduced motion and process recreation.
8. Iterate from observed user friction. Do not mark UX accepted from a screenshot or from visual resemblance to Memo.

Do not start final UI implementation until the architecture and task map are accepted. Do not defer the first usable workspace until a late “UI phase”; each product slice needs an honest, working native surface.
