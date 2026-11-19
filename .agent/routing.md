# Agent routing

| Request | Owner | Review |
|---|---|---|
| Vision/scope/roadmap | product | architect |
| Modules/contracts/ADRs | architect | affected owner + QA |
| Rust/FFI | rust-core | architect + QA |
| Gradle/JNI/APK/Android UX | android | rust-core + QA |
| Swift/iOS bridge | ios | architect + rust-core + QA |
| Threats/secrets/signing/dependencies | security | QA |
| Test strategy/acceptance | qa | affected owner |

Handoff: record goal/base SHA/scope → accountable agent plans → contract/security review if needed → implement within authority → independent QA → return changed paths, commit, tests, evidence, gaps and rollback. Stop on unclear source permission, secret exposure, protected path, authority widening or unprovable acceptance. No agent waives a principle alone.