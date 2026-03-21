#!/bin/bash
# Aero DevTools — Debug loop (Linux/Mac/Windows Git Bash)
# Builds, launches with DCEVM + JDWP, auto-restarts on F7
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$BASE"

echo ""
echo -e "\033[1;32m     ___    __________  ____ "
echo -e "    /   |  / ____/ __ \\/ __ \\"
echo -e "   / /| | / __/ / /_/ / / / /"
echo -e "  / ___ |/ /___/ _, _/ /_/ / "
echo -e " /_/  |_/_____/_/ |_|\\____/  \033[0;36mDevTools\033[0m"
echo ""
echo -e "  \033[0;33mF6\033[0m Hot Swap  \033[0;33mF7\033[0m Restart  \033[0;33mF9\033[0m Textures  \033[0;33mF10\033[0m Overlay"
echo ""

mkdir -p "$BASE/temp"

# Detect JDK
JAVA_CMD="java"
if [ -f "/c/Program Files/Java/jdk1.8.0_181/bin/java" ]; then
    export JAVA_HOME="/c/Program Files/Java/jdk1.8.0_181"
    export PATH="/c/Program Files/Java/jdk1.8.0_181/bin:$PATH"
    JAVA_CMD="/c/Program Files/Java/jdk1.8.0_181/bin/java"
elif [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

# Check DCEVM
"$JAVA_CMD" -XXaltjvm=dcevm -version 2>&1 | grep -q "Dynamic" && \
    echo "[Aero] DCEVM detected!" || \
    echo "[Aero] Warning: DCEVM not found. Hot swap limited to method bodies."

while true; do
    rm -f "$BASE/temp/.restart"

    echo ""
    echo "[Aero] === Transpiling ==="
    bash scripts/transpile.sh

    echo "[Aero] === Building ==="
    cd "$BASE/mcp"
    echo "build" | "$JAVA_CMD" -jar RetroMCP-Java-CLI.jar
    cd "$BASE"

    echo "[Aero] === Preparing jar ==="
    cp tests/data/minecraft_test.jar tests/data/minecraft_run.jar

    # Strip signatures (JDK 8u181 enforces SHA1)
    STRIP_DIR="$BASE/temp/jar_strip"
    mkdir -p "$STRIP_DIR" && cd "$STRIP_DIR" && rm -rf *
    jar xf "$BASE/tests/data/minecraft_run.jar"
    rm -f META-INF/*.SF META-INF/*.RSA META-INF/*.DSA
    jar cf "$BASE/tests/data/minecraft_run.jar" *
    cd "$BASE"

    # Inject mod classes
    TMP="$BASE/temp/build_tmp"
    rm -rf "$TMP" && mkdir -p "$TMP" && cd "$TMP"
    jar xf "$BASE/mcp/build/minecraft.zip"
    jar uf "$BASE/tests/data/minecraft_run.jar" *.class
    cd "$BASE" && rm -rf "$TMP"

    # Inject textures
    if [ -d "$BASE/temp/merged" ]; then
        cd "$BASE/temp/merged"
        jar uf "$BASE/tests/data/minecraft_run.jar" .
        cd "$BASE"
    fi

    mkdir -p "$BASE/tests/data/tmp"
    LIBS="mcp/libraries"

    echo ""
    echo "[Aero] === Launching Minecraft (DCEVM) ==="
    echo "[Aero] JDWP port: 5006"
    echo ""
    "$JAVA_CMD" -Xms1024M -Xmx1024M \
      -XXaltjvm=dcevm \
      -Daero.dev=true \
      -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 \
      -Djava.library.path="$LIBS/natives" \
      -Djava.io.tmpdir="$BASE/tests/data/tmp" \
      -cp "temp/merged;tests/data/minecraft_run.jar;$LIBS/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar;$LIBS/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar;$LIBS/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar;$LIBS/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar;$LIBS/com/paulscode/codecjorbis/20230120/codecjorbis-20230120.jar;$LIBS/com/paulscode/codecwav/20101023/codecwav-20101023.jar;$LIBS/com/paulscode/libraryjavasound/20101123/libraryjavasound-20101123.jar;$LIBS/com/paulscode/librarylwjglopenal/20100824/librarylwjglopenal-20100824.jar;$LIBS/com/paulscode/soundsystem/20120107/soundsystem-20120107.jar" \
      net.minecraft.client.Minecraft || true

    # Check restart flag
    if [ -f "$BASE/temp/.restart" ]; then
        echo ""
        echo "[Aero] Restart requested (F7). Rebuilding..."
        rm -f "$BASE/temp/.restart"
        sleep 2
        continue
    fi

    echo ""
    echo "[Aero] Minecraft closed. Press Enter to rebuild, or Ctrl+C to exit."
    read -r || break
done
