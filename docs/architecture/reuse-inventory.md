# Reuse inventory

Reuse concepts and verified code selectively; no donor project defines Locco.

| Source | Contribution | Boundary |
|---|---|---|
| Outbound Sourcer | source adapters, discovery pipeline, dedupe, progressive persistence, cancellation | not sales/trade-only; backend not mobile core |
| EBO | event/signal ledger, qualification, provider-neutral capabilities, approvals | no n8n/NocoDB/VPS/Unipile dependency |
| BOS | entity/relationship, capability/workflow/policy definitions, lineage | not business OS/cockpit |
| Troy | source-aware extraction, interpretation proposals, identity resolution | research is not proof connectors exist |
| ME | immutable snapshots, observations, hypotheses, lineage | no private database/history import |
| Memo | Android execution safety, visible browser, approval binding | separate product; no shell/identity inheritance |
| BusyBee | Android observer/notification listener research | no arbitrary app internals assumption |
| VaultOS | local import/index, backup/restore, integrity | avoid duplicate vault ownership |
| PocketForge/Nexus | build verification and gated workflow concepts | not runtime dependencies |

Before porting code inspect exact branch, tests, license, build status and implementation. A spec or POC is not a shipped capability.