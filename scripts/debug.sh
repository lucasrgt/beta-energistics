#!/bin/bash
# Debug mode: full build + launch with JDWP + dev overlay
# Same build pipeline as test.sh, but with debug flags
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$BASE"

echo "=== Transpiling ==="
bash scripts/transpile.sh

echo "=== Building ==="
cd "$BASE/mcp"
JAVA_CMD="java"
if [ -f "/c/Program Files/Java/jdk1.8.0_181/bin/java" ]; then
    JAVA_CMD="/c/Program Files/Java/jdk1.8.0_181/bin/java"
    export JAVA_HOME="/c/Program Files/Java/jdk1.8.0_181"
    export PATH="/c/Program Files/Java/jdk1.8.0_181/bin:$PATH"
fi
echo "build" | "$JAVA_CMD" -jar RetroMCP-Java-CLI.jar
cd "$BASE"

echo "=== Preparing test jar ==="
cp tests/data/minecraft_test.jar tests/data/minecraft_run.jar

# Strip jar signatures (JDK 8u181 enforces SHA1 verification)
STRIP_DIR="$BASE/temp/jar_strip"
mkdir -p "$STRIP_DIR" && cd "$STRIP_DIR" && rm -rf *
jar xf "$BASE/tests/data/minecraft_run.jar"
rm -f META-INF/*.SF META-INF/*.RSA META-INF/*.DSA
jar cf "$BASE/tests/data/minecraft_run.jar" *
cd "$BASE"
echo "Stripped jar signatures"

# Inject mod classes from build zip into run jar
TMP="$BASE/temp_build"
rm -rf "$TMP" && mkdir -p "$TMP"
cd "$TMP"
jar xf "$BASE/mcp/build/minecraft.zip"
jar uf "$BASE/tests/data/minecraft_run.jar" *.class
echo "Injected mod classes"

# Inject custom textures into run jar
if [ -d "$BASE/temp/merged" ]; then
    cd "$BASE/temp/merged"
    jar uf "$BASE/tests/data/minecraft_run.jar" .
    echo "Injected custom textures"
fi

cd "$BASE"
rm -rf "$TMP"

mkdir -p "$BASE/tests/data/tmp"
echo "=== Launching Minecraft (DCEVM debug mode) ==="
echo "  DCEVM: full class hot-reload (add/remove classes, methods, fields)"
echo "  F6 = Hot Swap | F7 = Hot Restart | F9 = Reload Textures | F10 = Toggle Overlay"
echo "  JDWP debug port: 5006"
echo "  Attach your IDE debugger to localhost:5006 for live swap"
LIBS="mcp/libraries"
JAVA_CMD="java"
# Use JDK 8u181 with DCEVM if available
if [ -f "/c/Program Files/Java/jdk1.8.0_181/bin/java" ]; then
    JAVA_CMD="/c/Program Files/Java/jdk1.8.0_181/bin/java"
fi
"$JAVA_CMD" -Xms1024M -Xmx1024M \
  -XXaltjvm=dcevm \
  -Daero.dev=true \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 \
  -Djava.library.path="$LIBS/natives" \
  -Djava.io.tmpdir="$BASE/tests/data/tmp" \
  -cp "temp/merged;tests/data/minecraft_run.jar;$LIBS/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar;$LIBS/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar;$LIBS/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar;$LIBS/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar;$LIBS/com/paulscode/codecjorbis/20230120/codecjorbis-20230120.jar;$LIBS/com/paulscode/codecwav/20101023/codecwav-20101023.jar;$LIBS/com/paulscode/libraryjavasound/20101123/libraryjavasound-20101123.jar;$LIBS/com/paulscode/librarylwjglopenal/20100824/librarylwjglopenal-20100824.jar;$LIBS/com/paulscode/soundsystem/20120107/soundsystem-20120107.jar" \
  net.minecraft.client.Minecraft
