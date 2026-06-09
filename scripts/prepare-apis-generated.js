#!/usr/bin/env node

/**
 * Script para generar cliente TypeScript/Angular desde especificación OpenAPI
 * Uso: node scripts/prepare-apis-generated.js
 */

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const swaggerFile = '../../gdpd-backend/gdpd-pedidos-api/src/main/resources/swagger/gdpd-pedidos-api.yaml';
const outputDir = './src/app/core/api/generated';

// Validar que el archivo Swagger exista
if (!fs.existsSync(swaggerFile)) {
  console.error(`✗ Error: Archivo Swagger no encontrado: ${swaggerFile}`);
  process.exit(1);
}

// Crear directorio de salida
if (!fs.existsSync(outputDir)) {
  fs.mkdirSync(outputDir, { recursive: true });
  console.log(`✓ Directorio creado: ${outputDir}`);
}

// Ejecutar generador OpenAPI
const generatorCommand = [
  'npx openapi-generator-cli generate',
  `-i ${swaggerFile}`,
  '-g typescript-angular',
  `-o ${outputDir}`,
  '-c openapi-config.json',
  '--skip-validate-spec'
].join(' ');

console.log('Generando cliente Angular desde OpenAPI...');
try {
  execSync(generatorCommand, { stdio: 'inherit', cwd: process.cwd() });
  console.log('✓ Código generado exitosamente');
} catch (error) {
  console.error('✗ Error generando código:', error.message);
  process.exit(1);
}

// Crear/actualizar archivos de índice
updateIndexFiles();

console.log('✓ Cliente Angular generado');
console.log(`✓ Ubicación: ${outputDir}`);
console.log('\nPasos siguientes:');
console.log('1. Actualizar los servicios generados si es necesario');
console.log('2. Importar GdpdApiModule en tu módulo principal');
console.log('3. Inyectar los servicios en tus componentes');

function updateIndexFiles() {
  // Crear index.ts principal
  const indexContent = `/**
 * GDPD Pedidos API - Cliente generado automáticamente
 * Generado desde: gdpd-pedidos-api.yaml
 */

export * from './models/';
export * from './services/';
export { Configuration, ConfigurationParameters } from './configuration';
export { BASE_PATH, COLLECTION_FORMATS } from './variables';
`;

  const indexPath = path.join(outputDir, 'index.ts');
  fs.writeFileSync(indexPath, indexContent);
  console.log('✓ Archivo index.ts actualizado');

  // Crear barril de modelos
  const modelsDir = path.join(outputDir, 'models');
  if (fs.existsSync(modelsDir)) {
    const modelFiles = fs.readdirSync(modelsDir)
      .filter(f => f.endsWith('.ts') && f !== 'index.ts')
      .map(f => f.replace('.ts', ''));

    const modelsIndexContent = modelFiles
      .map(f => `export * from './${f}';`)
      .join('\n');

    fs.writeFileSync(path.join(modelsDir, 'index.ts'), modelsIndexContent);
    console.log(`✓ ${modelFiles.length} modelos registrados`);
  }

  // Crear barril de servicios
  const servicesDir = path.join(outputDir, 'services');
  if (fs.existsSync(servicesDir)) {
    const serviceFiles = fs.readdirSync(servicesDir)
      .filter(f => f.endsWith('.ts') && f !== 'index.ts')
      .map(f => f.replace('.ts', ''));

    const servicesIndexContent = serviceFiles
      .map(f => `export * from './${f}';`)
      .join('\n');

    fs.writeFileSync(path.join(servicesDir, 'index.ts'), servicesIndexContent);
    console.log(`✓ ${serviceFiles.length} servicios registrados`);
  }
}
