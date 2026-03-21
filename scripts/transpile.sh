#!/bin/bash
# Transpiles organized src/betaenergistics/ + libraries/ -> flat mcp/minecraft/src/net/minecraft/src/
# Rewrites packages and removes internal imports so RetroMCP can compile
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$BASE/src/betaenergistics"
LIBS="$BASE/../../../libraries"
DEST="$BASE/mcp/minecraft/src/net/minecraft/src"

# Remove old transpiled mod files (only BE_ prefixed + Aero_ prefixed + mod_BetaEnergistics)
find "$DEST" -maxdepth 1 -name "BE_*.java" -delete 2>/dev/null || true
find "$DEST" -maxdepth 1 -name "Aero_*.java" -delete 2>/dev/null || true
find "$DEST" -maxdepth 1 -name "mod_BetaEnergistics.java" -delete 2>/dev/null || true

# Transpile function: flatten packages and strip internal imports
transpile_file() {
    local file="$1"
    local filename
    filename=$(basename "$file")
    sed \
        -e 's/^package betaenergistics\(\.[a-z]*\)\?;/package net.minecraft.src;/' \
        -e 's/^package aero\.\([a-z_]*\);/package net.minecraft.src;/' \
        -e '/^import betaenergistics\./d' \
        -e '/^import static betaenergistics\./d' \
        -e '/^import aero\./d' \
        -e '/^import static aero\./d' \
        -e '/^import net\.minecraft\.src\.\*;/d' \
        "$file" > "$DEST/$filename"
}

# Transpile libraries (aero modellib, machineapi, devtools)
LIB_COUNT=0
if [ -d "$LIBS" ]; then
    if [ "$AERO_RELEASE" = "1" ]; then
        find "$LIBS" -name "*.java" -not -path "*/devtools/*" | while read -r file; do
            transpile_file "$file"
        done
        LIB_COUNT=$(find "$LIBS" -name '*.java' -not -path "*/devtools/*" | wc -l)
        # Defense in depth: remove any devtools that leaked
        find "$DEST" -maxdepth 1 -name "Aero_Dev*.java" -delete 2>/dev/null || true
        echo "[RELEASE] Excluded devtools from transpile"
    else
        find "$LIBS" -name "*.java" -not -path "*/tools/*" -not -path "*/scripts/*" | while read -r file; do
            transpile_file "$file"
        done
        LIB_COUNT=$(find "$LIBS" -name '*.java' -not -path "*/tools/*" -not -path "*/scripts/*" | wc -l)
    fi
fi

# Transpile mod source
find "$SRC" -name "*.java" | while read -r file; do
    transpile_file "$file"
done
SRC_COUNT=$(find "$SRC" -name '*.java' | wc -l)

echo "Transpiled $LIB_COUNT library + $SRC_COUNT mod files to $DEST"

# Copy assets (textures, models) to temp/merged for jar injection
ASSETS="$SRC/assets"
if [ -d "$ASSETS" ]; then
    mkdir -p "$BASE/temp/merged"
    cp -r "$ASSETS"/* "$BASE/temp/merged/"
    echo "Copied assets to temp/merged/"
fi
