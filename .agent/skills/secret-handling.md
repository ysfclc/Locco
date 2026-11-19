# Skill: Secret handling

When: any credential, token, key, cookie or private endpoint. Preconditions: approved owner/storage.

Procedure: least privilege/rotation; store in ignored local secret store or GitHub secret/environment; inject via env; disable shell trace; minimize lifetime; cleanup temp; revoke on exposure.

Verify secret scan, logs/artifacts/commits clean and permissions minimal.

Failure: echo, crash dump, temp persistence or fork PR exposure. Stop and rotate.