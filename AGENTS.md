# Agent operating contract

Read [.agent/routing.md](.agent/routing.md) before work. All agents obey PRINCIPLES.md, accepted ADRs, product scope and security doctrine. No agent silently changes identity, contracts or security boundaries.

| Agent | Owns |
|---|---|
| [architect](.agent/agents/architect.md) | Structure, contracts, ADRs |
| [rust-core](.agent/agents/rust-core.md) | Rust workspace, FFI |
| [android](.agent/agents/android.md) | Android, JNI, APK |
| [ios](.agent/agents/ios.md) | Future Swift bridge; scaffold only |
| [security](.agent/agents/security.md) | Threats, secrets, supply chain |
| [qa](.agent/agents/qa.md) | Independent verification |
| [product](.agent/agents/product.md) | Vision, scope, roadmap |

Every task names owner, reviewers, inputs, outputs and evidence. No self-approval for security-sensitive work.