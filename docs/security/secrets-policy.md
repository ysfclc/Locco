# Secrets policy

One home: local ignored env/secrets for development; GitHub Actions secrets for CI; release signing only in protected `android-release` Environment.

Names: `ANDROID_RELEASE_KEYSTORE_BASE64`, `ANDROID_RELEASE_STORE_PASSWORD`, `ANDROID_RELEASE_KEY_ALIAS`, `ANDROID_RELEASE_KEY_PASSWORD`.

Inject through environment, never command-line literals. Disable shell trace. Never echo environment/signing config. Decode keystore to unique temp file with restrictive permissions and guaranteed cleanup. Never upload keystore, local.properties, .env or secret-bearing logs. Rotate immediately on suspected exposure/access change; review annually. Record rotation metadata, never values.