/**
 * seed-nova-company.mjs
 *
 * Lee los ficheros del Company Package de NOVA desde disco y los importa
 * a Paperclip via POST /api/companies/import con source.type: "inline".
 *
 * Uso:
 *   node scripts/seed-nova-company.mjs
 *
 * Variables de entorno:
 *   PAPERCLIP_URL  - URL base del servidor Paperclip (default: http://localhost:3100)
 *   NOVA_PKG_DIR   - Directorio del company package (default: ./company)
 */

import { readFileSync, readdirSync, statSync } from "node:fs";
import { join, relative } from "node:path";
import { fileURLToPath } from "node:url";
import { dirname } from "node:path";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const BASE_URL = process.env.PAPERCLIP_URL ?? "http://localhost:3100";
const PKG_DIR = process.env.NOVA_PKG_DIR ?? join(__dirname, "..", "company");

/**
 * Recursively collect all files in a directory.
 * Returns Record<relativePath, fileContent>.
 */
function collectFiles(dir, root = dir, acc = {}) {
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    if (statSync(full).isDirectory()) {
      collectFiles(full, root, acc);
    } else {
      const relPath = relative(root, full).split("\\").join("/");
      acc[relPath] = readFileSync(full, "utf8");
    }
  }
  return acc;
}

/**
 * Wait for the Paperclip server to be ready.
 * Polls /api/health every 2 seconds, up to maxRetries times.
 */
async function waitForServer(maxRetries = 60) {
  console.log(`Esperando a que Paperclip esté listo en ${BASE_URL}...`);
  for (let i = 0; i < maxRetries; i++) {
    try {
      const res = await fetch(`${BASE_URL}/api/health`);
      if (res.ok) {
        console.log("Paperclip listo.");
        return;
      }
    } catch {
      // Server not ready yet
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(
    `Paperclip no respondió después de ${maxRetries * 2}s en ${BASE_URL}`
  );
}

/**
 * Import the company package into Paperclip.
 */
async function importCompany(files) {
  const body = {
    source: {
      type: "inline",
      files,
    },
    target: {
      mode: "new_company",
    },
    include: {
      company: true,
      agents: true,
      skills: true,
      projects: true,
      issues: true,
    },
    collisionStrategy: "skip", // Idempotente: re-ejecutar no duplica
  };

  console.log(
    `Importando ${Object.keys(files).length} ficheros a ${BASE_URL}/api/companies/import...`
  );

  const res = await fetch(`${BASE_URL}/api/companies/import`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    const errorText = await res.text();
    console.error(`Import falló: HTTP ${res.status}`);
    console.error(errorText);
    process.exit(1);
  }

  return res.json();
}

async function main() {
  try {
    // 1. Wait for server
    await waitForServer();

    // 2. Collect package files
    console.log(`Leyendo Company Package desde: ${PKG_DIR}`);
    const files = collectFiles(PKG_DIR);
    console.log(`Ficheros encontrados: ${Object.keys(files).length}`);
    console.log(
      Object.keys(files)
        .map((f) => `  - ${f}`)
        .join("\n")
    );

    // 3. Import
    const result = await importCompany(files);
    console.log("\nOrganización NOVA importada exitosamente:");
    console.log(JSON.stringify(result, null, 2));
  } catch (err) {
    console.error("Error:", err.message);
    process.exit(1);
  }
}

main();
