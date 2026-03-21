#!/bin/bash
# Hot restart: kill Minecraft + full rebuild + relaunch in debug mode
# Use when hot swap isn't enough (structural changes, new classes, etc.)
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$BASE"

echo "=== Killing Minecraft ==="
taskkill //F //IM java.exe 2>/dev/null || true
sleep 2

echo "=== Restarting in debug mode ==="
bash scripts/debug.sh
