#!/bin/bash
# Hot restart: spawns a detached rebuilder that waits for MC to die, then relaunches
# This script is called FROM Minecraft — it must outlive the Java process
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"

# Write a restart script that runs independently
RESTARTER="$BASE/temp/restarter.sh"
cat > "$RESTARTER" << 'SCRIPT'
#!/bin/bash
BASE="$1"
cd "$BASE"

# Wait for Minecraft to fully exit
sleep 2
while tasklist 2>/dev/null | grep -q "java.exe"; do
    sleep 1
done
sleep 1

echo "=== Minecraft closed. Rebuilding... ==="
export JAVA_HOME="/c/Program Files/Java/jdk1.8.0_181"
export PATH="/c/Program Files/Java/jdk1.8.0_181/bin:$PATH"
bash scripts/debug.sh
SCRIPT

chmod +x "$RESTARTER"

# Launch restarter as detached process, then kill Minecraft
nohup bash "$RESTARTER" "$BASE" > "$BASE/temp/restart.log" 2>&1 &
disown

# Kill Minecraft (this process's parent)
sleep 0.5
taskkill //F //IM java.exe 2>/dev/null || true
