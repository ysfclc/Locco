# Reference project audit — inspiration without inheritance

**Status:** Source audit completed; product decisions remain proposed  
**Audit date:** 2026-11-20  
**Purpose:** Extract proven patterns and observed limitations from Memo-1 and Memo-2 without copying either product's identity, UI, planner or assumptions into Locco.

This is a source inspection, not a runtime/device test. “Not found in the inspected source tree” is not proof that a feature can never exist elsewhere. Statements about login failures remain user reports until reproduced.

## 1. Memo-2: useful interaction breadth, intentionally small current implementation

### Current native navigation

The current Memo-2 Compose root (app/src/main/kotlin/app/memo/ui/MemoRoot.kt) exposes six bottom-navigation destinations: Live, Talk, Work, Studio, Models and Settings.

The useful lesson is that the product presents more than a chat screen: current activity, conversation, records, creation, model management and system settings are visible concepts. Locco should preserve that breadth of capability.

The limitation is that a fixed six-item bottom bar is a particular navigation decision, not a product requirement. It does not include a browser destination in the current source, and it should not be copied without testing Locco's own task frequency, one-hand reach, compact-width legibility and adaptive navigation.

### Model Hub: implementation versus plan

The current source catalogue (core/src/main/kotlin/app/memo/core/catalog/ModelCatalog.kt) contains three fixed Qwen LiteRT-LM entries. ModelsScreen.kt presents those entries, offers device scan/import and exposes download/use states. This is a clear, understandable starting interaction.

ModelStore.kt provides useful engineering patterns: pinned artifact URLs, SHA-256 verification for catalog entries, resumable partial downloads, storage preflight, local discovery and explicit activation. These ideas should be retained and strengthened.

The inspected implementation also reveals limitations Locco must avoid:

- A discovered file's initial catalog match is inferred from file size; size is a discovery hint, never an integrity proof.
- Imports that do not match a pinned catalog entry are retained as untrusted imports, but are not eligible for runtime activation.
- Import exceptions delete the temporary file without exposing a detailed user-visible failure through this method.
- The screen is a fixed catalogue list, not a searchable and filterable multi-source model hub with comparison, artifact detail, runtime compatibility, device-fit evidence and benchmark history.
- A catalogue entry being installed is not the same as the runtime loading it successfully or the model producing usable results on this device.

Memo-2's broader build plan discusses a much richer Hugging Face discovery and model-family list. That is a plan, not the current three-entry source catalogue. Locco must not describe planned Memo-2 breadth as shipped functionality.

### Runtime and routing

Memo-2's LLM contract distinguishes LiteRT-LM and GGUF formats, has streamed output and cancellation semantics, and makes a one-request-at-a-time constraint explicit for the inspected runtime contract. Its router records health, rate limits, routing rationale and rejected alternatives.

Useful lessons:
- Define lifecycle, cancellation and error contracts instead of hiding them behind a “generate” button.
- Explain why a runtime was selected and why alternatives were rejected.
- Make resource and concurrency constraints explicit.

Do not inherit the static role-to-channel affinity table as Locco's planner architecture. Locco should resolve task requirements against declared runtime capabilities, quality evidence, permissions, privacy, health and current device resources.

### Browser and DOM: plan versus source

The inspected Memo-2 main source tree contains no first-class BrowserSession/WebView browser implementation or browser screen. Its browser/DOM executor is described in planning documents, including M2_DESIGN.md and docs/BUILD_PLAN.md. Those plans are useful research material, not a working browser to copy.

M2_DESIGN.md proposes a limited JavaScript interface on the page. Locco must not expose a privileged native bridge to arbitrary website content. The DOM observer must use a reviewed, origin-scoped and runtime-supported isolated observation mechanism, with bounded structured messages and deterministic validation. Where the platform cannot provide that isolation, Locco must disable privileged DOM automation while keeping ordinary browsing usable.

## 2. Memo-1: more concrete browser and design-system lessons, with limits

### Native design system

Memo-1's MEMO_DESIGN_SYSTEM.md and MemoTheme.kt show useful foundations:
- Semantic light/dark roles rather than screen-local colors.
- Shared typography, spacing, touch-target, shape, elevation and motion tokens.
- A restrained information hierarchy, purposeful motion and reduced-motion behavior.
- Reusable primitives with accessibility and loading/error states.

Its design-system document also says implementation migration and visual acceptance remain incomplete. The token file is evidence of a foundation, not proof that all screens consistently use it.

Locco should adopt the discipline of semantic tokens and reusable tested components, but define its own visual identity, type scale, spacing rhythm, components, motion and information density. Do not rename or copy Signal & Graphite.

### Browser session behavior

Memo-1's BrowserSession.kt is a concrete visible WebView session. The inspected code:
- Enables cookies, JavaScript and DOM storage.
- Uses a visible WebView with browser progress/title callbacks.
- Blocks insecure HTTP, non-HTTPS schemes and SSL certificate errors.
- Disables automatic JavaScript windows and multiple-window support.
- Detects Google account sign-in pages and directs the user to open the page in the external browser.

Its external-browser handoff explicitly tells the user that the external browser's cookies are not automatically shared with Memo's embedded session. This is an honest limitation, not seamless account continuity.

The inspected file does not show an explicit browser profile/session manager or profile-isolation policy. It also does not explicitly set a per-WebView third-party-cookie policy. Its BrowserWorkflow provider list and new-conversation URL resolver contain provider-specific host and route knowledge. Those may support a bounded Memo feature, but they are not a general Locco task engine.

These are test hypotheses, not established causes of the user's login problem:
- Popup-based login may be affected by disabled multiple-window handling.
- Provider redirects or app-link callbacks may be affected by the strict non-HTTPS scheme block.
- Cross-site sign-in may depend on cookie policy and SameSite behavior.
- Embedded identity-provider policy may reject a WebView regardless of local settings.
- External sign-in may complete in Chrome without authenticating the embedded browser profile.

No one of these is assigned as the root cause without a reproduction.

## 3. What Locco should take forward

| Reference pattern | Keep the idea | Locco improvement |
|---|---|---|
| Memo-2's distinct work areas | Make the product's breadth discoverable | Derive navigation from Locco task journeys; test alternatives rather than clone six tabs |
| Memo-2's dedicated model screen | Treat model management as a first-class activity | Build a data-driven hub with discovery, filtering, comparison, transfers, installed library, runtimes and device-fit evidence |
| Memo-2's verified/resumable acquisition | Never trust a model file merely because it exists | Verify complete immutable manifests, shards and digests; stage atomically; load-test before activation; surface all failures |
| Memo-2's runtime contract | Explicit streaming, cancellation and lifecycle | General runtime capability manifests, concurrency/resource arbitration and typed failure |
| Memo-1's visible browser session | The user remains in control and can take over | A complete browser with tabs, profiles, accounts, downloads, history, permissions and durable restoration |
| Memo-1's semantic design tokens | Consistency, accessibility and meaningful states | A distinct Locco design language validated on real tasks and device sizes |
| Memo-1's visible approval/status concepts | Make consequential work inspectable | One generic execution model across browser, files, models and future adapters |

## 4. Locco UI iteration protocol

Do not select navigation by copying a donor application's tab count. First derive the journeys from the Locco vision and test competing hypotheses.

### Required task set

At minimum, test these journeys:
1. Open Locco and capture an idea or item in one hand.
2. Share a URL/file into Locco and find the preserved evidence later.
3. Resume an interrupted task and understand what is known, uncertain or waiting.
4. Open the browser, navigate, switch tabs and sign in without invoking AI.
5. Return to a previously authenticated site after process restart.
6. Inspect a page, select a region and understand what DOM evidence Locco can and cannot access.
7. Find a model by task/modality, compare its artifact and runtime requirements, and assess device fit before downloading.
8. Pause/resume a model transfer and recover from checksum, storage or load failure.
9. Ask Locco to perform an unfamiliar task, inspect the proposed plan and discover a missing capability.
10. Approve, reject, take over or stop an external action and later verify what actually happened.
11. Search entities/evidence and inspect conflicting claims with provenance.
12. Recover drafts, tabs, conversations and execution state after process recreation.

### Navigation hypotheses to prototype, not assume

- **A — Workspace-led:** a central workspace is the home; capture is globally available; browser and activity are prominent destinations; Model Hub is reachable from a global model selector and a dedicated library entry.
- **B — Intent-led:** one persistent intent surface starts/resumes work; workspace, browser and activity are contextual full surfaces; Model Hub remains a dedicated system capability, not a buried advanced setting.
- **C — Environment-led:** adaptive navigation exposes a small set of equally important environments on compact devices and a rail/navigation suite on wider windows; the exact set is chosen only after the task test.

These are hypotheses for comparative prototypes, not final IA decisions. Do not expose dead destinations or use placeholders to make a layout look complete.

### Evaluation and selection

For each prototype, record task completion, time-to-destination, wrong turns, lost state, one-hand reach, keyboard/IME conflicts, accessibility issues and user preference. Test compact phone and expanded layouts, light/dark themes, large font scale, TalkBack, reduced motion, process recreation and offline/no-model operation.

Only select an IA after the same tasks have been run against each candidate. Then create Locco-specific design tokens and interactive Compose components. A screenshot is visual evidence only; it does not prove navigation, persistence, accessibility or behavior.

## 5. Guardrails

- Never copy Memo package names, app identity, themes, navigation labels or source structure.
- Never copy provider-specific browser routes into the core planner.
- Never infer authentication success from opening a login page or completing an external OAuth callback.
- Never infer model compatibility from file extension, filename, file size or catalogue metadata alone.
- Never present planned reference features as implemented.
- Preserve every user correction and regression as a traceable Locco requirement.


## Audited source revisions

The comparison above was made against these default-branch source revisions on 2026-11-20:
- Memo-1: commit c307f94f5d296622e9e3b2fa92b02fe9c58361f2.
- Memo-2: commit 00a8a9e1244c5dba7af107b1ee0e808738d5d022.

Key files for reproduction:
- Memo-1 BrowserSession.kt: https://github.com/ysfclc/memo/blob/c307f94f5d296622e9e3b2fa92b02fe9c58361f2/app/src/main/java/app/memo/android/browser/BrowserSession.kt
- Memo-1 design tokens: https://github.com/ysfclc/memo/blob/c307f94f5d296622e9e3b2fa92b02fe9c58361f2/app/src/main/java/app/memo/android/ui/MemoTheme.kt
- Memo-1 design-system notes: https://github.com/ysfclc/memo/blob/c307f94f5d296622e9e3b2fa92b02fe9c58361f2/docs/product/MEMO_DESIGN_SYSTEM.md
- Memo-2 Compose root: https://github.com/ysfclc/memo-2/blob/00a8a9e1244c5dba7af107b1ee0e808738d5d022/app/src/main/kotlin/app/memo/ui/MemoRoot.kt
- Memo-2 model screen: https://github.com/ysfclc/memo-2/blob/00a8a9e1244c5dba7af107b1ee0e808738d5d022/app/src/main/kotlin/app/memo/ui/ModelsScreen.kt
- Memo-2 model catalogue: https://github.com/ysfclc/memo-2/blob/00a8a9e1244c5dba7af107b1ee0e808738d5d022/core/src/main/kotlin/app/memo/core/catalog/ModelCatalog.kt
- Memo-2 model store: https://github.com/ysfclc/memo-2/blob/00a8a9e1244c5dba7af107b1ee0e808738d5d022/app/src/main/kotlin/app/memo/models/ModelStore.kt
- Memo-2 design plan: https://github.com/ysfclc/memo-2/blob/00a8a9e1244c5dba7af107b1ee0e808738d5d022/docs/M2_DESIGN.md

A future audit must record a new revision and explain whether any finding changed.
