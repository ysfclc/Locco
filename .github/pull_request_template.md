## Delivery identity

- LOC issue(s):
- Product requirement IDs (LOC-Rxxx):
- Roadmap phase / delivery step:
- Base SHA:
- Change type: architecture / data contract / Rust / Android / browser / model / security / UX / release

## User outcome and scope

- What user-visible or foundation outcome changes?
- Acceptance scenarios satisfied:
- Explicitly not included:
- Existing behavior/regressions checked:

## Contract and continuity impact

- ADR(s) added/updated:
- FFI / schema / storage / browser-session compatibility:
- Migration and rollback/recovery:
- App identity, signing and user-data continuity:
- Permission, privacy and external network effects:

## Verification on this exact head

| Check | Result (PASS / FAIL / NOT RUN) | Command / evidence / environment |
|---|---|---|
| Static/unit/contract tests | | |
| Integration/UI tests | | |
| Security/dependency/workflow checks | | |
| Android APK / native ABI inspection | | |
| Physical device / upgrade / recovery | | |

- Tested commit SHA:
- Artifact SHA-256:
- Device/Android/WebView/runtime versions:
- Known gaps / blockers:

A workflow that failed before runner allocation or has no executed steps is **NOT RUN**. Do not write "passed" unless the named command/test actually executed and passed.

## Review

- Architecture/domain reviewer:
- Android/Rust reviewer:
- Security reviewer (when relevant):
- Independent QA reviewer:
- If a required independent reviewer is unavailable, state that explicitly.

## Security and regression checklist

- [ ] No secrets, credentials, cookies, private datasets or model weights committed
- [ ] Imported/page/model content treated as untrusted data
- [ ] Permissions, egress and provider terms reviewed
- [ ] Approval bound to exact payload/target where relevant
- [ ] Existing data, package identity, sessions and accepted flows preserved
- [ ] Failure, offline, interruption and recovery paths tested

## Rollback

<!-- How can this change be reverted or migrated without losing user data? -->
