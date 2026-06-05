#!/bin/bash
# Setup NOVA CLI environment from this repo's toolchain
# Usage: source setup-nova.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export NOVA_HOME="$SCRIPT_DIR/toolchain/nova-le"
export JAVA_HOME="$SCRIPT_DIR/toolchain/zulu-jdk11"
export MAVEN_HOME="$NOVA_HOME/tools/maven"
export PATH="$NOVA_HOME/nova-cli/bin:$NOVA_HOME/nodejs:$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

echo "NOVA environment configured:"
echo "  NOVA_HOME=$NOVA_HOME"
echo "  JAVA_HOME=$JAVA_HOME"
echo "  MAVEN_HOME=$MAVEN_HOME"

# Verify
if [ -f "$NOVA_HOME/nova-cli/bin/nova.js" ]; then
  echo ""
  node "$NOVA_HOME/nova-cli/bin/nova.js" --version 2>/dev/null || echo "  (run 'nova --version' to verify)"
else
  echo "  WARNING: nova-cli not found. Did you clone with git lfs? Try: git lfs pull"
fi
