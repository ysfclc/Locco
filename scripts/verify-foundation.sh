#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
echo "Checking required foundation files..."
for path in README.md PRINCIPLES.md AGENTS.md SECURITY.md CODEOWNERS core/Cargo.toml android/settings.gradle.kts android/app/build.gradle.kts android/app/src/main/res/xml/backup_rules.xml android/app/src/main/res/xml/data_extraction_rules.xml docs/architecture/ffi-contract.md docs/product/scope.md; do
  test -s "$ROOT/$path" || { echo "Missing: $path" >&2; exit 1; }
done
echo "Checking required agent and skill counts..."
test "$(find "$ROOT/.agent/agents" -maxdepth 1 -name '*.md' | wc -l)" -ge 7
test "$(find "$ROOT/.agent/skills" -maxdepth 1 -name '*.md' | wc -l)" -ge 10
echo "Checking shell script syntax..."
for script in "$ROOT"/scripts/*.sh; do
  bash -n "$script"
done
echo "Checking JSON..."
python3 -m json.tool "$ROOT/docs/brainstorm/structured.json" >/dev/null
echo "Checking Android backup policy..."
python3 "$ROOT/scripts/validate_android_backup_policy.py"
echo "Checking full-SHA action pins..."
if grep -RhoE 'uses: [^ ]+@[0-9a-fA-F]+' "$ROOT/.github/workflows" 2>/dev/null | grep -Ev '@[0-9a-fA-F]{40}$' >/dev/null; then
  echo "An action is not pinned to a full SHA." >&2
  exit 1
fi
echo "Foundation structure checks passed. Native build/toolchain gates are separate."
