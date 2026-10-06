#!/usr/bin/env sh
# Claude Code PostToolUse hook (Edit|Write).
# Advisory Kotlin style check: if the edited file is a .kt file, run ktlint and
# surface any findings to the agent. Never blocks — always exits 0 — so it stays
# out of the way during non-Kotlin work (docs, config, specs).
#
# Reads the tool-call JSON from stdin and pulls out the edited file path.

set -u

input="$(cat)"

# Extract "file_path": "..." from the hook payload without requiring jq.
file="$(printf '%s' "$input" \
  | tr ',{}' '\n\n\n' \
  | grep '"file_path"' \
  | head -1 \
  | sed -E 's/.*"file_path"[[:space:]]*:[[:space:]]*"([^"]+)".*/\1/')"

# Only act on Kotlin sources; quietly no-op for anything else.
case "$file" in
  *.kt|*.kts) ;;
  *) exit 0 ;;
esac

# Locate the repo root (dir containing gradlew) so the hook works from any cwd.
dir="$(pwd)"
while [ "$dir" != "/" ] && [ ! -f "$dir/gradlew" ]; do
  dir="$(dirname "$dir")"
done
[ -f "$dir/gradlew" ] || exit 0

cd "$dir" || exit 0

# ktlintCheck is read-only; capture output and report advisory-only.
out="$(sh ./gradlew ktlintCheck -q 2>&1)"
status=$?

if [ "$status" -ne 0 ]; then
  echo "ktlint reported style issues after editing $file:"
  printf '%s\n' "$out" | grep -E '\.kt:[0-9]+' | head -20
  echo "Run 'gradlew ktlintFormat' to auto-fix, then re-check."
fi

exit 0
