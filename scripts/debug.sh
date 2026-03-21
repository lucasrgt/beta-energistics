#!/bin/bash
# Dev mode: transpile + recompile + inject + launch with JDWP debug port
# Similar to test.sh but with debug flags for hot swap
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$BASE"

echo "=== Transpiling ==="
bash scripts/transpile.sh

echo "=== Recompiling ==="
cd "$BASE/mcp"
echo "recompile" | java -jar RetroMCP-Java-CLI.jar
cd "$BASE"

# Ensure assets are in temp/merged for F9 hot reload
ASSETS="$BASE/src/betaenergistics/assets"
if [ -d "$ASSETS" ]; then
    mkdir -p "$BASE/temp/merged"
    cp -r "$ASSETS"/* "$BASE/temp/merged/"
fi

echo "=== Preparing test jar ==="
cp tests/data/minecraft_test.jar tests/data/minecraft_run.jar
cd mcp/minecraft/reobf
jar uf "$BASE/tests/data/minecraft_run.jar" *.class 2>/dev/null || true
cd "$BASE"

# Inject assets from temp/merged into jar
if [ -d "temp/merged" ]; then
    cd temp/merged
    jar uf "$BASE/tests/data/minecraft_run.jar" . 2>/dev/null || true
    cd "$BASE"
fi

mkdir -p "$BASE/tests/data/tmp"
echo "=== Launching Minecraft (dev mode) ==="
echo "  JDWP debug port: 5006"
echo "  F9 = hot-reload textures | F10 = debug overlay"
echo "  Use hotswap.sh to push code changes without restarting"
LIBS="mcp/libraries"
java -Xms1024M -Xmx1024M \
  -Daero.dev=true \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 \
  -Djava.library.path="$LIBS/natives" \
  -Djava.io.tmpdir="$BASE/tests/data/tmp" \
  -cp "tests/data/minecraft_run.jar;$LIBS/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar;$LIBS/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar;$LIBS/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar;$LIBS/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar;$LIBS/com/paulscode/codecjorbis/20230120/codecjorbis-20230120.jar;$LIBS/com/paulscode/codecwav/20101023/codecwav-20101023.jar;$LIBS/com/paulscode/libraryjavasound/20101123/libraryjavasound-20101123.jar;$LIBS/com/paulscode/librarylwjglopenal/20100824/librarylwjglopenal-20100824.jar;$LIBS/com/paulscode/soundsystem/20120107/soundsystem-20120107.jar" \
  net.minecraft.client.Minecraft
