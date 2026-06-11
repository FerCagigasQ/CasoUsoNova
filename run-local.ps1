#!/usr/bin/env pwsh
$ErrorActionPreference = "Stop"

Write-Host "=== Verificando prerequisitos ===" -ForegroundColor Cyan

try {
  $javaVersion = java -version 2>&1 | Select-String "version"
  if (-not $javaVersion) { throw "Java no detectado" }
  Write-Host "✓ Java detectado: $($javaVersion[0])" -ForegroundColor Green
} catch {
  Write-Host "❌ Java 17 requerido. Instalar: https://adoptium.net" -ForegroundColor Red
  exit 1
}

try {
  $nodeVersion = node --version
  Write-Host "✓ Node.js $nodeVersion" -ForegroundColor Green
} catch {
  Write-Host "❌ Node.js 18+ requerido. Instalar: https://nodejs.org" -ForegroundColor Red
  exit 1
}

Write-Host ""

$backendProcess = $null
$frontendProcess = $null

function Cleanup {
  Write-Host ""
  Write-Host "=== Deteniendo servicios ===" -ForegroundColor Yellow
  if ($backendProcess) {
    try { Stop-Process -Id $backendProcess.Id -Force -ErrorAction SilentlyContinue }
    catch { }
  }
  if ($frontendProcess) {
    try { Stop-Process -Id $frontendProcess.Id -Force -ErrorAction SilentlyContinue }
    catch { }
  }
}

$null = Register-EngineEvent -SourceIdentifier PowerShell.Exiting -Action { Cleanup }

Write-Host "=== Arrancando backend (Spring Boot) ===" -ForegroundColor Cyan
Push-Location guarantees-service
$backendProcess = Start-Process -NoNewWindow -PassThru -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" `
  -RedirectStandardOutput "$env:TEMP\guarantees-backend.log" `
  -RedirectStandardError "$env:TEMP\guarantees-backend.err"
Pop-Location

Write-Host "Esperando backend (máx 60s)..." -ForegroundColor Gray
$backendReady = $false
for ($i = 1; $i -le 60; $i++) {
  try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -ErrorAction SilentlyContinue
    if ($response.StatusCode -eq 200) {
      Write-Host "✓ Backend listo" -ForegroundColor Green
      $backendReady = $true
      break
    }
  } catch { }
  Start-Sleep -Seconds 1
}

if (-not $backendReady) {
  Write-Host "❌ Backend no respondió en 60s" -ForegroundColor Red
  Get-Content "$env:TEMP\guarantees-backend.log" | Select-Object -Last 20
  Cleanup
  exit 1
}

Write-Host ""
Write-Host "=== Arrancando frontend (Angular) ===" -ForegroundColor Cyan
Push-Location guarantees-ui
Write-Host "npm install..." -ForegroundColor Gray
$null = npm install 2>&1
Write-Host "npm start..." -ForegroundColor Gray
$frontendProcess = Start-Process -NoNewWindow -PassThru -FilePath "npm" -ArgumentList "start" `
  -RedirectStandardOutput "$env:TEMP\guarantees-frontend.log" `
  -RedirectStandardError "$env:TEMP\guarantees-frontend.err"
Pop-Location

Write-Host "Esperando frontend (máx 30s)..." -ForegroundColor Gray
$frontendReady = $false
for ($i = 1; $i -le 30; $i++) {
  try {
    $response = Invoke-WebRequest -Uri "http://localhost:4200" -ErrorAction SilentlyContinue
    if ($response.StatusCode -eq 200) {
      Write-Host "✓ Frontend listo" -ForegroundColor Green
      $frontendReady = $true
      break
    }
  } catch { }
  Start-Sleep -Seconds 1
}

if (-not $frontendReady) {
  Write-Host "⚠ Frontend puede estar compilando, continuando..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                   🏦 DEMO LISTA 🏦                             ║" -ForegroundColor Cyan
Write-Host "╠════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
Write-Host "║  Frontend:      http://localhost:4200                          ║" -ForegroundColor Cyan
Write-Host "║  API REST:      http://localhost:8080/api/v1/guarantees        ║" -ForegroundColor Cyan
Write-Host "║  Swagger UI:    http://localhost:8080/swagger-ui.html          ║" -ForegroundColor Cyan
Write-Host "║  H2 Console:    http://localhost:8080/h2-console              ║" -ForegroundColor Cyan
Write-Host "║                                                                ║" -ForegroundColor Cyan
Write-Host "║  Logs:                                                         ║" -ForegroundColor Cyan
Write-Host "║    Backend:  Get-Content `$env:TEMP\guarantees-backend.log      ║" -ForegroundColor Cyan
Write-Host "║    Frontend: Get-Content `$env:TEMP\guarantees-frontend.log     ║" -ForegroundColor Cyan
Write-Host "║                                                                ║" -ForegroundColor Cyan
Write-Host "║  Ctrl+C para detener todo                                      ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

try {
  while ($true) { Start-Sleep -Seconds 1 }
} finally {
  Cleanup
}
