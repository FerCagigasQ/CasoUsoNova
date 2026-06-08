#!/bin/bash
# Setup NOVA toolchain from GitHub Release (fallback when git lfs pull fails)
# Usage: source setup-nova-from-release.sh [release-tag]
# Requires: gh CLI (GitHub CLI) authenticated, or GITHUB_TOKEN env var

RELEASE="${1:-toolchain-v1}"
REPO="FerCagigasQ/CasoUsoNova"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLCHAIN_DIR="$SCRIPT_DIR/toolchain"
TOOLCHAIN_ZIP="$SCRIPT_DIR/toolchain-nova-le.zip"

echo "=== NOVA Toolchain Setup from GitHub Release ==="
echo "Repo:    $REPO"
echo "Release: $RELEASE"
echo ""

# Check if toolchain binaries already present (node.exe as proxy)
NODE_EXE="$TOOLCHAIN_DIR/nova-le/nodejs/node.exe"
if [ -f "$NODE_EXE" ] && [ "$(stat -c%s "$NODE_EXE" 2>/dev/null || stat -f%z "$NODE_EXE" 2>/dev/null)" -gt 1048576 ]; then
    echo "[OK] Toolchain binaries already present. Skipping download."
    source "$SCRIPT_DIR/setup-nova.sh"
    exit 0
fi

# Download toolchain zip from GitHub Release
echo "[1/3] Downloading toolchain from GitHub Release '$RELEASE'..."
if command -v gh &>/dev/null; then
    gh release download "$RELEASE" --repo "$REPO" --pattern "toolchain-nova-le.zip" --output "$TOOLCHAIN_ZIP" --clobber
elif [ -n "$GITHUB_TOKEN" ]; then
    ASSET_URL=$(curl -s -H "Authorization: Bearer $GITHUB_TOKEN" \
        "https://api.github.com/repos/$REPO/releases/tags/$RELEASE" \
        | grep -o '"browser_download_url": "[^"]*toolchain-nova-le\.zip[^"]*"' \
        | grep -o 'https://[^"]*')
    if [ -z "$ASSET_URL" ]; then
        echo "ERROR: Asset 'toolchain-nova-le.zip' not found in release '$RELEASE'."
        exit 1
    fi
    curl -L -H "Authorization: Bearer $GITHUB_TOKEN" -o "$TOOLCHAIN_ZIP" "$ASSET_URL"
else
    echo "ERROR: Neither 'gh' CLI nor GITHUB_TOKEN found."
    echo "  Install GitHub CLI: https://cli.github.com"
    echo "  Then: gh auth login"
    exit 1
fi

echo "[2/3] Extracting toolchain..."
if [ -d "$TOOLCHAIN_DIR" ]; then
    rm -rf "$TOOLCHAIN_DIR"
fi
unzip -q "$TOOLCHAIN_ZIP" -d "$SCRIPT_DIR"
rm -f "$TOOLCHAIN_ZIP"

echo "[3/3] Configuring environment..."
source "$SCRIPT_DIR/setup-nova.sh"

echo ""
echo "[OK] NOVA toolchain ready."
