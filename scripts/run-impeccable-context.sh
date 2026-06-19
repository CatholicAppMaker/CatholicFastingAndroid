#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)

exec "$SCRIPT_DIR/node" "$REPO_ROOT/.agents/skills/impeccable/scripts/load-context.mjs"
