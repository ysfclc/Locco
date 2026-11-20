# General-purpose capability compiler

**Status:** Proposed for LOC-0004 review. The execution engine must remain domain-neutral.

## 1. Purpose

Locco receives goals whose exact shape may not have been anticipated. It determines which information, tools, skills, constraints, permissions and success evidence are needed, then assembles only the capabilities relevant to that task.

Do not build application branches such as “if job application, run these steps” or “if CRM, run this fixed workflow.” Domain knowledge can exist as versioned, editable components and test fixtures; the engine remains generic.

## 2. Compilation pipeline

Objective → TaskSpec → RequirementGraph → Component resolution → Readiness → ExecutionPackage → ExecutionGraph → Observe/execute/verify → Evaluate → Diagnose/repair → Outcome/experience.

Every stage emits a typed, versioned artifact. It must not depend on hidden prompt state.

## 3. TaskSpec

TaskSpec captures objective, deliverables, acceptance criteria, constraints, exclusions, evidence/data needs, user preferences, deadline, resource budgets, risk class, language, consent/authority envelope, uncertainty, missing inputs and evaluation strategy.

A model may propose it. Deterministic validators enforce schemas, bounds and mandatory fields. The user can inspect/edit when risk or uncertainty requires.

## 4. RequirementGraph

A bounded typed graph has node kinds:
- Knowledge: facts, evidence and references.
- Data: queries and user-provided data bindings.
- Capability: registered operation.
- Procedure/Skill: reusable method and guardrails.
- Constraint: policy, budget, compatibility and preferences.
- Persona: style/perspective only; no authority.
- Evaluator: schema, tests, rubric and postcondition.
- HumanInput: clarification, credential handoff, approval or takeover.
- Dependency: prerequisite relation.

Edges express requires, produces, refines, conflicts-with and depends-on. Limits on node count, depth, fan-out, tokens and time are explicit. Hitting a limit produces a capability gap, not silent truncation.

## 5. Versioned capability library

| Kind | Declares |
|---|---|
| Capability | Operation, input/output schema, side-effect class, authority, pre/postconditions |
| ToolSpec | Typed callable interface |
| Procedure | Validated reusable graph fragment, not opaque prose alone |
| Skill | Preconditions, procedure, outputs, constraints and verification |
| KnowledgePack | Source-linked knowledge and freshness rules |
| DataBinding | Scoped query, allowed fields, authorization and output schema |
| Constraint | Deterministic restriction or budget |
| Evaluator | Required evidence, schema checks, tests and optional rubric |
| Persona | Communication style/perspective, never permissions |
| Experience | Prior outcome, component versions, conditions and evidence |

Each component includes stable identity, semver, source/license, provenance, input/output schema, provides/requires/conflicts/compatibility, trust, language, budgets and tests.

User/model-generated components begin UNTRUSTED and cannot authorize side effects. Trust promotion requires validation and recorded success under stated conditions; a model cannot self-certify.

## 6. Deterministic assembly and readiness

The linker:
1. Resolves requirements against components and currently available runtimes.
2. Checks versions, conflicts, data shapes and dependencies.
3. Builds a candidate DAG.
4. Checks user preferences, permissions, scopes and policy.
5. Estimates storage, RAM, tokens, time, network and battery.
6. Ensures all critical requirements are covered.
7. Ensures required outputs can be evaluated.
8. Returns explicit capability gaps when not ready.

It may draft a component or ask the user. It cannot invent access, fabricate evidence or widen authority.

## 7. ExecutionPackage and graph

The immutable manifest pins TaskSpec/graph revisions, component versions, data-binding queries and evidence IDs, model/runtime choices, permissions/consent, budgets, evaluators, postconditions, idempotency requirements and recovery guidance.

The generic operation vocabulary may include read, parse, transform, infer, search, compare, ask-user, approve, write, communicate, schedule, wait and verify. It is extended by reviewed capability registration, not vertical-specific planner branches.

## 8. Deterministic authority

The model never executes directly. Each proposed call is parsed and checked against registered capability/version, argument schema, target scope, current permission, autonomy policy, exact approval envelope, observation freshness, idempotency, budget and kill switch.

Side-effect approval binds exact target, action sequence, values, payload hash, authority, expiry and expected postcondition. A changed target or payload invalidates approval.

## 9. Evaluate, repair and learn

Deterministic facts (schema valid, receipt found, postcondition true) are separate from model-scored quality (clarity, relevance, coverage). Model scores never replace required evidence.

Failure is classified by the responsible layer: ambiguous task, missing data, unavailable capability, incompatible runtime, invalid procedure, source drift, refusal, poor output, evaluator gap or uncertain external outcome.

Repair is bounded and changes only the responsible component/plan fragment. It creates a versioned diff. Side effects are not replayed during repair.

Experience records task shape, component versions, executor, conditions, outcome, evidence and failure. Statistics may inform recommendations but never mutate trust or permissions automatically.

## 10. Generality acceptance

The same compiler and execution engine must handle, without planner code changes:
- Extract records from an unfamiliar public site, validate a user schema and preserve evidence.
- Compare selected files and websites, reconcile conflicting claims and produce a report.
- Find a local file, transform it and create a versioned artifact.
- Compose browser observation, inference, user clarification and an exact approved external action.
- Detect a missing capability or permission and stop with an explicit gap.

These are fixtures, not shipped workflow templates. Architecture tests must prevent planner branches on vertical labels such as CRM, job application or social publishing.

## 11. Open decisions

- Persisted DAG state machine versus event-sourced transitions or a hybrid.
- Component/package serialization and evolution format.
- Trust promotion requirements.
- User-authoring and model-drafting boundaries.
- Distinction between reusable procedure and one execution instance.

Resolve by ADR and tests before the component library becomes stable.
