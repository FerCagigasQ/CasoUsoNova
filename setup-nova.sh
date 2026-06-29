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
NOVA_RELEASE_BASE="https://github.com/FerCagigasQ/CasoUsoNova/releases/download/nova-toolchain-v${NOVA_VERSION}"
NOVA_TARBALL_URL="${NOVA_RELEASE_BASE}/nova-toolchain-v${NOVA_VERSION}.tar.gz"
ZULU_JDK_URL="https://cdn.azul.com/zulu/bin/zulu11.74.15-ca-jdk11.0.24-linux_x64.tar.gz"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz"

NOVA_DIR="${NOVA_INSTALL_DIR:-${HOME}/.nova-toolchain/${NOVA_VERSION}}"

# If NOVA_HOME is already set and valid (Docker/Devin), use it directly
if [ -n "${NOVA_HOME:-}" ] && [ -f "${NOVA_HOME}/nova-cli/bin/nova.js" ]; then
  echo "[NOVA] Using existing NOVA_HOME=${NOVA_HOME}"
else
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

  # Check local toolchain/ directory (legacy layout)
  if [ -f "${SCRIPT_DIR}/toolchain/nova-le/nova-cli/bin/nova.js" ]; then
    NOVA_DIR="${SCRIPT_DIR}/toolchain/nova-le"
    echo "[NOVA] Using local toolchain at ${NOVA_DIR}"
  elif [ ! -f "${NOVA_DIR}/nova-cli/bin/nova.js" ]; then
    echo "[NOVA] Setting up toolchain v${NOVA_VERSION} at ${NOVA_DIR}..."
    mkdir -p "${NOVA_DIR}" "${NOVA_DIR}/tools/java" "${NOVA_DIR}/tools/maven"

    # Try to download nova-cli from GitHub Release
    if curl -fsSL --head "${NOVA_TARBALL_URL}" >/dev/null 2>&1; then
      echo "[NOVA]   Downloading nova-cli toolchain..."
      curl -fsSL "${NOVA_TARBALL_URL}" | tar -xz -C "${NOVA_DIR}" --strip-components=4
      chmod +x "${NOVA_DIR}/nova-cli/bin/"*.js 2>/dev/null || true
    else
      echo "[NOVA]   nova-cli tarball not yet in release — skipping nova-cli install."
      echo "[NOVA]   To install nova-cli, upload nova-toolchain-v${NOVA_VERSION}.tar.gz to:"
      echo "[NOVA]   https://github.com/FerCagigasQ/CasoUsoNova/releases/tag/nova-toolchain-v${NOVA_VERSION}"
    fi

    # Always download Zulu JDK 11 (public CDN)
    if [ ! -f "${NOVA_DIR}/tools/java/bin/java" ]; then
      echo "[NOVA]   Downloading Zulu JDK 11..."
      curl -fsSL "${ZULU_JDK_URL}" | tar -xz -C "${NOVA_DIR}/tools/java" --strip-components=1
      chmod +x "${NOVA_DIR}/tools/java/bin/"* 2>/dev/null || true
    fi

    # Always download Maven 3.9.9 (public Apache mirror)
    if [ ! -f "${NOVA_DIR}/tools/maven/bin/mvn" ]; then
      echo "[NOVA]   Downloading Maven 3.9.9..."
      curl -fsSL "${MAVEN_URL}" | tar -xz -C "${NOVA_DIR}/tools/maven" --strip-components=1
      chmod +x "${NOVA_DIR}/tools/maven/bin/"* 2>/dev/null || true
    fi

    echo "[NOVA] Setup complete."
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
if [ -f "${NOVA_HOME}/nova-cli/bin/nova.js" ] && command -v node >/dev/null 2>&1; then
  node "${NOVA_HOME}/nova-cli/bin/nova.js" --version 2>/dev/null || echo "  (nova-cli present but --version failed)"
else
  "${JAVA_HOME}/bin/java" -version 2>&1 | head -1 || echo "  java not found"
fi
