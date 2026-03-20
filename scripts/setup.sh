#!/bin/bash
# First-time setup: initialize RetroMCP, inject ModLoader+Forge, decompile MC Beta 1.7.3
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"
WORKSPACE="$BASE/../../.."
DEPS="$WORKSPACE/deps"
MCP="$BASE/mcp"

echo "=== Step 1: Initialize MCP directory ==="
rm -rf "$MCP"
mkdir -p "$MCP"

# Copy RetroMCP CLI
cp "$DEPS/RetroMCP-Java-CLI.jar" "$MCP/"
echo "Copied RetroMCP CLI"

echo "=== Step 2: Setup RetroMCP (download MC b1.7.3) ==="
cd "$MCP"
echo -e "setup\nb1.7.3" | java -jar RetroMCP-Java-CLI.jar

echo "=== Step 3: Inject ModLoader + ModLoaderMp into minecraft.jar ==="
TMP=$(mktemp -d)
cd "$TMP"

# Extract ModLoader + ModLoaderMp
unzip -o "$DEPS/ModLoader B1.7.3.zip" -d modloader > /dev/null 2>&1
unzip -o "$DEPS/ModLoaderMp 1.7.3 Unofficial v2.zip" -d modloadermp > /dev/null 2>&1
echo "Extracted ModLoader + ModLoaderMp"

# Inject into minecraft.jar
cd "$TMP/modloader" && jar uf "$MCP/jars/minecraft.jar" *.class 2>/dev/null || true
cd "$TMP/modloadermp" && jar uf "$MCP/jars/minecraft.jar" *.class 2>/dev/null || true
echo "Injected ModLoader + ModLoaderMp into minecraft.jar"

# Remove META-INF signatures
zip -d "$MCP/jars/minecraft.jar" "META-INF/MOJANGCS.RSA" "META-INF/MOJANGCS.SF" > /dev/null 2>&1 || true
echo "Removed META-INF signatures"

echo "=== Step 4: Decompile ==="
cd "$MCP"
echo "decompile" | java -jar RetroMCP-Java-CLI.jar

rm -rf "$TMP"

echo "=== Step 6: Update MD5 baseline ==="
cd "$MCP"
echo "updatemd5" | java -jar RetroMCP-Java-CLI.jar

echo "=== Step 7: Create test jar baseline ==="
mkdir -p "$BASE/tests/data" "$BASE/tests/libs"
cp "$MCP/jars/minecraft.jar" "$BASE/tests/data/minecraft_test.jar"
echo "Test jar created"

echo ""
echo "=== Setup complete ==="
echo "Run 'bash scripts/test.sh' to transpile, build, and launch"
