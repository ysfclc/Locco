# Skill: Native UI and interaction acceptance

**Trigger:** Any Compose screen, navigation, Android interaction, permission prompt, accessibility, dynamic layout or user-visible action change.

## Inputs
User journey and acceptance criteria; affected route/state owner; supported Android/API/device/window sizes; permission/action contract; screenshots or baseline behavior when available.

## Procedure
1. Trace entry point, navigation destinations, state owner, event source and persistence. Identify adjacent screens and back-stack behavior.
2. Define states explicitly: initial/loading, populated, empty, partial, offline, permission-required/denied, recoverable error, terminal error, submitting, success and cancelled where relevant.
3. Specify action semantics. Every button/gesture must have a real effect, disabled/busy behavior, error recovery and observable postcondition. Remove dead controls; do not simulate success.
4. Preserve state across recomposition, configuration change and process recreation as required. Verify back navigation, deep links, external intents and repeated taps.
5. Check loading/empty/error and long-content layout on narrow and expanded windows, font scaling, system bars, keyboard/IME and dark/light theme if supported.
6. Check accessibility labels/semantics, traversal order, touch-target size, contrast, TalkBack, keyboard/switch access and non-color status cues.
7. Respect reduced-motion/system animation settings. Motion must explain state change and not obscure content or controls.
8. For model-driven suggestions, expose uncertainty/source evidence and keep the user in control. For actions, bind approval to the exact payload and invalidate approval if the payload changes.
9. Test offline, network loss, permission revocation, cancellation, lifecycle interruption, duplicate events and empty/error responses.
10. Use automated UI tests for deterministic behavior and a manual device checklist for platform semantics that automation cannot prove.

## Required evidence
Screens/routes changed; state table; automated test names/results; accessibility checks; device/size/API matrix; manual steps and screenshots where useful; known platform limitations.

## Fail conditions
Dead control, false success, inaccessible action, lost user state, permission prompt without explanation, unhandled lifecycle exit or unverified approval payload blocks acceptance.
