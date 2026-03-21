#!/bin/bash
# Hot swap: transpile + recompile only (no restart needed)
# The Aero_DevOverlay runs HotSwapAgent after this script completes
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$BASE"

echo "=== Transpiling ==="
bash scripts/transpile.sh

echo "=== Recompiling ==="
cd "$BASE/mcp"
JAVA_CMD="java"
if [ -f "/c/Program Files/Java/jdk1.8.0_181/bin/java" ]; then
    JAVA_CMD="/c/Program Files/Java/jdk1.8.0_181/bin/java"
fi
echo "build" | "$JAVA_CMD" -jar RetroMCP-Java-CLI.jar
cd "$BASE"

echo "=== Recompiled OK ==="
