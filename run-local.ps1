#!/usr/bin/env pwsh

Write-Host "=== Verificando prerequisitos ===" -ForegroundColor Cyan
$javaCheck = try { java -version 2>&1 } catch { $null }
if (-not $javaCheck) {
    Write-Host "ERROR: Java 17 requerido" -ForegroundColor Red
    exit 1
}

$nodeCheck = try { node --version } catch { $null }
if (-not $nodeCheck) {
    Write-Host "ERROR: Node.js 18+ requerido" -ForegroundColor Red
    exit 1
}

Write-Host "=== Arrancando backend ===" -ForegroundColor Cyan
Push-Location guarantees-service
Start-Process -NoNewWindow -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -PassThru | Out-Null
$backendPid = (Get-Process | Where-Object { $_.ProcessName -like "*mvnw*" -or $_.ProcessName -like "*java*" } | Select-Object -Last 1).Id
Pop-Location

Write-Host "=== Esperando backend (max 90s) ===" -ForegroundColor Cyan
$success = $false
for ($i = 1; $i -le 90; $i++) {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/guarantees" -ErrorAction Stop
        Write-Host "Backend listo!" -ForegroundColor Green
        $success = $true
        break
    } catch {
        Start-Sleep -Seconds 1
    }
}

if (-not $success) {
    Write-Host "ERROR: Backend no respondió en 90s" -ForegroundColor Red
    exit 1
}

Write-Host "=== Arrancando frontend ===" -ForegroundColor Cyan
Push-Location guarantees-ui
npm install --silent
Start-Process -NoNewWindow -FilePath "npm" -ArgumentList "start" -PassThru | Out-Null
Pop-Location

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "  DEMO LISTA" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host "  Frontend:   http://localhost:4200" -ForegroundColor Yellow
Write-Host "  API:        http://localhost:8080/api/v1/guarantees" -ForegroundColor Yellow
Write-Host "  Swagger:    http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
Write-Host "  H2 Console: http://localhost:8080/h2-console" -ForegroundColor Yellow
Write-Host ""
Write-Host "  Ctrl+C para parar todo"
Write-Host "=========================================" -ForegroundColor Green

# Keep running
while ($true) {
    Start-Sleep -Seconds 1
}
