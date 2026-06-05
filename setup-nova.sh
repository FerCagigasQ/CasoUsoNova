#!/usr/bin/env bash
# setup-nova.sh — Download-on-demand NOVA CLI toolchain setup
#
# Downloads the NOVA toolchain from GitHub Release if not already installed,
# then exports environment variables so nova commands are available.
#
# Usage:
#   source setup-nova.sh
#
# The toolchain is cached at ~/.nova-toolchain/7.8.0/ (or NOVA_INSTALL_DIR).
# Subsequent runs skip the download.

# Save caller's shell options and restore on exit (safe for sourcing)
_nova_old_opts="$(set +o)"
set -euo pipefail
_nova_restore_opts() { eval "$_nova_old_opts"; unset _nova_old_opts; unset -f _nova_restore_opts; }
trap _nova_restore_opts RETURN 2>/dev/null || true

NOVA_VERSION="7.8.0"
NOVA_RELEASE_URL="https://github.com/FerCagigasQ/CosmosPaperClip/releases/download/nova-toolchain-v${NOVA_VERSION}/nova-toolchain-v${NOVA_VERSION}.tar.gz"
ZULU_JDK_URL="https://cdn.azul.com/zulu/bin/zulu11.74.15-ca-jdk11.0.24-linux_x64.tar.gz"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz"

NOVA_DIR="${NOVA_INSTALL_DIR:-${HOME}/.nova-toolchain/${NOVA_VERSION}}"

# If NOVA_HOME is already set and valid (Docker/Devin), use it directly
if [ -n "${NOVA_HOME:-}" ] && [ -f "${NOVA_HOME}/nova-cli/bin/nova.js" ]; then
  echo "[NOVA] Using existing NOVA_HOME=${NOVA_HOME}"
else
  # Also check local toolchain/ directory (legacy layout)
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  if [ -f "${SCRIPT_DIR}/toolchain/nova-le/nova-cli/bin/nova.js" ]; then
    NOVA_DIR="${SCRIPT_DIR}/toolchain/nova-le"
    echo "[NOVA] Using local toolchain at ${NOVA_DIR}"
  elif [ ! -f "${NOVA_DIR}/nova-cli/bin/nova.js" ]; then
    echo "[NOVA] Downloading toolchain v${NOVA_VERSION} to ${NOVA_DIR}..."
    mkdir -p "${NOVA_DIR}" "${NOVA_DIR}/tools/java" "${NOVA_DIR}/tools/maven"

    echo "[NOVA]   Toolchain..."
    curl -fsSL "${NOVA_RELEASE_URL}" | tar -xz -C "${NOVA_DIR}" --strip-components=4

    echo "[NOVA]   Zulu JDK 11..."
    curl -fsSL "${ZULU_JDK_URL}" | tar -xz -C "${NOVA_DIR}/tools/java" --strip-components=1

    echo "[NOVA]   Maven 3.9.9..."
    curl -fsSL "${MAVEN_URL}" | tar -xz -C "${NOVA_DIR}/tools/maven" --strip-components=1

    chmod +x "${NOVA_DIR}/nova-cli/bin/"*.js 2>/dev/null || true
    chmod +x "${NOVA_DIR}/tools/java/bin/"* 2>/dev/null || true
    chmod +x "${NOVA_DIR}/tools/maven/bin/"* 2>/dev/null || true

    echo "[NOVA] Download complete."
  fi

  export NOVA_HOME="${NOVA_DIR}"
fi

# Export environment variables
export NOVA_HOME="${NOVA_HOME:-${NOVA_DIR}}"
export JAVA_HOME="${NOVA_HOME}/tools/java"
export MAVEN_HOME="${NOVA_HOME}/tools/maven"
export NOVA_CLI_PATH="${NOVA_HOME}/nova-cli/bin/nova.js"
export PATH="${NOVA_HOME}/nova-cli/bin:${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"

echo "[NOVA] Environment configured:"
echo "  NOVA_HOME=${NOVA_HOME}"
echo "  JAVA_HOME=${JAVA_HOME}"
echo "  MAVEN_HOME=${MAVEN_HOME}"

# Verify
if command -v node >/dev/null 2>&1 && [ -f "${NOVA_HOME}/nova-cli/bin/nova.js" ]; then
  node "${NOVA_HOME}/nova-cli/bin/nova.js" --version 2>/dev/null || echo "  (nova.js present but --version failed — may need yarn link)"
else
  echo "  WARNING: node not found or nova-cli missing. Install Node.js first."
fi
