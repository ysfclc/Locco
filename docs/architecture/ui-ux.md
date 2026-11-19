# Native UI/UX architecture

Composition: tokens → primitives → behavior patterns → feature compositions → adaptive destinations. Android uses Compose, Navigation Compose and feature ViewModels with immutable state. Navigation host owns back stack; ViewModels own restorable presentation state; Rust owns durable state.

Each feature defines loading/content/empty/offline/partial/permission-denied/error/success. Long tasks expose progress, pause/cancel and checkpoint. Success requires outcome evidence. AI progressively discloses summary, uncertainty and supporting evidence. Approval binds exact target/payload hash/authority/expiry; any change invalidates it.

Dedicated design-system module; feature modules consume public APIs. Enforce no direct Material imports via Detekt/architecture test. Central tokens; no color-only status; semantic labels, touch targets, font scaling, reduced motion. Adaptive window size classes. Accessibility target WCAG 2.1 AA principles plus native TalkBack checks.

AI unavailable → deterministic search/filters/evidence remain usable. Permission denied → explain and offer manual/import alternatives.