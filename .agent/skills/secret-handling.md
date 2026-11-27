# Skill: Secret and credential handling

**Trigger:** Any API token, password, cookie, keystore, private key, signing credential, private endpoint, user data export or authentication flow.

## Rules
1. Classify the material before use: public configuration, credential, signing material, personal data or security-sensitive evidence.
2. Use the least-privileged credential and only the approved owner/store. Prefer GitHub Actions secrets/environments or a local ignored secret store; never hard-code secrets or place them in prompts, source, issues or test fixtures.
3. Read secrets into environment variables or a protected file descriptor. Do not pass them as command-line arguments where process lists/logs may expose them.
4. Disable shell tracing and command echo around secret use. Avoid printing environment, config dumps, crash payloads or HTTP debug bodies that may contain credentials.
5. Create temporary secret files with unique names, restrictive permissions and guaranteed cleanup. Minimize lifetime and never upload them as artifacts.
6. Do not expose release secrets to fork/untrusted pull-request code. Use protected environments and explicit reviewer approval for release signing.
7. Use synthetic credentials in tests. Redact tokens, cookies, personal content and signing details from bug reports while preserving sanitized diagnostic meaning.
8. Scan diffs, logs, build outputs and uploaded artifacts before handoff. Inspect likely accidental encodings/base64 blobs; a secret scanner alone is not proof of absence.
9. If exposure is suspected, stop use, preserve sanitized incident details, revoke/rotate via the credential owner, inspect access logs and update affected LOC/security records. Do not merely delete visible text and continue.

## Required evidence
Secret class/store/permission rationale; exposure boundary; redaction/scan result; cleanup behavior; rotation/incident action if applicable.

## Fail conditions
Credential in source/chat/issue/log/artifact, excessive scope, untrusted workflow exposure, retained temporary key file, shell trace leakage or no response to suspected exposure blocks work/release.
