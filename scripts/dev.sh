#!/bin/bash
# Aero DevTools — Development loop
# Builds, launches Minecraft with DCEVM + JDWP, and auto-restarts on F7
# Works on Linux, Mac, and Windows (Git Bash)
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$BASE"

echo ""
echo "  ============================================"
echo "    Aero DevTools - Debug Mode"
echo "    F6 = Hot Swap | F7 = Restart | F9 = Textures | F10 = Overlay"
echo "  ============================================"
echo ""

# Create restart flag dir
mkdir -p "$BASE/temp"

while true; do
    # Clean restart flag
    rm -f "$BASE/temp/.restart"

    echo "[Aero] Building and launching..."
    bash scripts/debug.sh || true

    # Check if F7 requested restart (flag file)
    if [ -f "$BASE/temp/.restart" ]; then
        echo ""
        echo "[Aero] Restart requested (F7). Rebuilding..."
        rm -f "$BASE/temp/.restart"
        sleep 2
        continue
    fi

    # Normal exit — ask user
    echo ""
    echo "[Aero] Minecraft closed. Press Enter to rebuild, or Ctrl+C to exit."
    read -r || break
done
