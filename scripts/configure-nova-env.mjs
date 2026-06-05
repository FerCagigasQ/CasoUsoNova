/**
 * configure-nova-env.mjs
 *
 * Detects the NOVA toolchain location and patches all agents in the
 * NOVA company so their adapterConfig.env includes NOVA_HOME, JAVA_HOME,
 * MAVEN_HOME, and an extended PATH. This ensures that when the claude_local
 * adapter spawns a Claude process, the NOVA CLI is available in the PATH.
 *
 * The adapter merges config.env into process.env when spawning the Claude
 * process (see execute.ts line 264). Env vars set here override process.env.
 *
 * Usage:
 *   node scripts/configure-nova-env.mjs
 *
 * Environment variables:
 *   PAPERCLIP_URL  - URL base del servidor Paperclip (default: http://localhost:3100)
 *   NOVA_HOME      - Override for NOVA toolchain path (auto-detected if not set)
 */

import { existsSync } from "node:fs";
import { join, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";
import { dirname } from "node:path";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const BASE_URL = process.env.PAPERCLIP_URL ?? "http://localhost:3100";
const isWindows = process.platform === "win32";
const pathSep = isWindows ? ";" : ":";

/**
 * Try to auto-detect NOVA_HOME from known locations.
 */
function detectNovaHome() {
  // 1. Explicit env var
  if (process.env.NOVA_HOME) return process.env.NOVA_HOME;

  // 2. Relative to repo: containers/nova-org/toolchain/nova-le
  const repoToolchain = resolve(__dirname, "..", "toolchain", "nova-le");
  if (existsSync(join(repoToolchain, "nova-cli"))) return repoToolchain;

  // 3. Docker default
  if (existsSync("/opt/nova/nova-cli")) return "/opt/nova";

  // 4. Windows common locations
  if (isWindows) {
    const home = process.env.USERPROFILE || process.env.HOME || "";
    const candidates = [
      join(home, "nova-le-7.8.0"),
      "C:\\nova-le",
      "C:\\nova-le-7.8.0",
      join(home, "Documents", "nova-le"),
    ];
    for (const c of candidates) {
      if (existsSync(join(c, "nova-cli"))) return c;
    }
  }

  return null;
}

/**
 * Build the env vars object for a given NOVA_HOME.
 */
function buildNovaEnv(novaHome) {
  const p = (...parts) => join(novaHome, ...parts);
  const novaBins = [
    join(p("nova-cli", "bin")),
    p("nodejs"),
    join(p("tools", "java", "bin")),
    join(p("tools", "maven", "bin")),
  ];
  // Prepend NOVA paths to current PATH
  const currentPath = process.env.PATH || process.env.Path || "";
  const extendedPath = [...novaBins, currentPath].join(pathSep);

  return {
    NOVA_HOME: novaHome,
    JAVA_HOME: p("tools", "java"),
    MAVEN_HOME: p("tools", "maven"),
    NODE_HOME: p("nodejs"),
    NOVA_CLI_PATH: join(p("nova-cli", "bin"), "nova.js"),
    PATH: extendedPath,
  };
}

/**
 * Fetch NOVA company agents.
 */
async function fetchNovaCompany() {
  const res = await fetch(`${BASE_URL}/api/companies`);
  if (!res.ok) throw new Error(`GET /companies failed: ${res.status}`);
  const companies = await res.json();
  const nova = companies.find(
    (c) => c.name && c.name.toLowerCase().includes("nova")
  );
  if (!nova) throw new Error("NOVA company not found. Run import first.");
  return nova;
}

async function fetchAgents(companyId) {
  const res = await fetch(`${BASE_URL}/api/companies/${companyId}/agents`);
  if (!res.ok) throw new Error(`GET /agents failed: ${res.status}`);
  return res.json();
}

/**
 * Patch an agent's adapterConfig.env, merging with existing env vars.
 */
async function patchAgent(agent, novaEnv) {
  const existingEnv = agent.adapterConfig?.env ?? {};
  const mergedEnv = { ...existingEnv, ...novaEnv };

  const res = await fetch(`${BASE_URL}/api/agents/${agent.id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      adapterConfig: { env: mergedEnv },
    }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`PATCH agent ${agent.id} failed: ${res.status} — ${text}`);
  }
  return res.json();
}

async function main() {
  const novaHome = detectNovaHome();
  if (!novaHome) {
    console.error(
      "ERROR: No se encontró el toolchain NOVA.\n" +
      "Opciones:\n" +
      "  1. Definir NOVA_HOME=/ruta/a/nova-le antes de ejecutar\n" +
      "  2. Colocar el toolchain en containers/nova-org/toolchain/nova-le/\n" +
      "  3. En Docker, el compose ya monta el toolchain en /opt/nova"
    );
    process.exit(1);
  }

  console.log(`NOVA_HOME detectado: ${novaHome}`);
  const novaEnv = buildNovaEnv(novaHome);
  console.log("Env vars para agentes:");
  for (const [k, v] of Object.entries(novaEnv)) {
    if (k === "PATH") {
      console.log(`  ${k}: <${v.split(pathSep).length} entries>`);
    } else {
      console.log(`  ${k}: ${v}`);
    }
  }

  // Fetch NOVA company & agents
  const company = await fetchNovaCompany();
  console.log(`\nCompany: ${company.name} (${company.id})`);

  const agents = await fetchAgents(company.id);
  const claudeAgents = agents.filter((a) => a.adapterType === "claude_local");
  console.log(`Agentes claude_local: ${claudeAgents.length}`);

  // Patch each agent (merges NOVA env with any existing env vars)
  for (const agent of claudeAgents) {
    try {
      await patchAgent(agent, novaEnv);
      console.log(`  ✓ ${agent.name}`);
    } catch (err) {
      console.error(`  ✗ ${agent.name}: ${err.message}`);
    }
  }

  console.log("\nConfiguración completada.");
  console.log("Los agentes ahora tienen NOVA_HOME y PATH configurados.");
  console.log("Recuerda autenticar Claude CLI si no lo has hecho:");
  console.log("  Local:  claude /login");
  console.log("  Docker: docker exec -it -u node <container> claude /login");
}

main().catch((err) => {
  console.error("Error:", err.message);
  process.exit(1);
});
