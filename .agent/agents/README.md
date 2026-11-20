# Agent role prompt library

These files are the repository-owned role specifications for Locco. They define responsibility, boundaries, required reading, execution procedures, handoff evidence and stop conditions.

## Important: files do not activate themselves
The presence of a Markdown file does not spawn an agent, assign a role or guarantee that a coding tool has loaded it. The orchestrator/contributor performing a task MUST deliberately load:
1. Root AGENTS.md and the normative governance documents.
2. Exactly one accountable owner role prompt from this directory.
3. The specialist reviewer prompts required by routing.
4. Every skill whose trigger matches the task.

If the runtime cannot load a separate role prompt or create independent agents, say so. Follow the role procedure in the current session and do not claim an independent review that did not occur.

## Role selection
- product: user feedback, user outcomes, scope, acceptance intent and priority.
- architect: module boundaries, contracts, ADRs, migration design and cross-platform coherence.
- rust-core: Rust domain behavior, persistence, invariants and FFI.
- android: Kotlin/Compose, Android lifecycle/adapters, JNI, Gradle and APKs.
- ios: only an explicitly authorized iOS phase.
- security: independent threat, permission, privacy, secret, dependency and supply-chain review.
- qa: independent behavior/regression/upgrade/release verification.

Routing and mandatory reviewer combinations are defined in [routing.md](../routing.md). A task has one accountable implementation owner. Reviewers are not co-owners by implication and must inspect evidence independently.

## Prompt maintenance
A prompt change must preserve the role's authority limits and all required links. Do not copy the whole constitution into each prompt. Keep role procedures specific to their technical responsibilities. When a role or repository structure changes, update routing and references in the same PR.
