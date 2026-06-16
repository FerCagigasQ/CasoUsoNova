# NOVA-1 Trade Finance Demo - Local Development Start

Write-Host "=== NOVA-1 Trade Finance Demo - Local Development Start ===" -ForegroundColor Cyan
Write-Host ""

# Install frontend dependencies if needed
if (-not (Test-Path "guarantees-ui/node_modules")) {
    Write-Host "Installing frontend dependencies..." -ForegroundColor Green
    Push-Location guarantees-ui
    npm install
    Pop-Location
}

# Start backend
Write-Host "Starting backend (Spring Boot)..." -ForegroundColor Green
$backendProcess = Start-Process -PassThru -NoNewWindow -FilePath "cmd.exe" `
    -ArgumentList "/c cd guarantees-service && mvnw.bat spring-boot:run"

# Wait for backend to be ready
Write-Host "Waiting for backend to be ready..."
$backendReady = $false
for ($i = 0; $i -lt 30; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $backendReady = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 1
    }
}

if ($backendReady) {
    Write-Host "✓ Backend is ready" -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host "✗ Backend failed to start" -ForegroundColor Red
    $backendProcess.Kill()
    exit 1
}

# Start frontend
Write-Host "Starting frontend (Angular)..." -ForegroundColor Green
$frontendProcess = Start-Process -PassThru -NoNewWindow -FilePath "cmd.exe" `
    -ArgumentList "/c cd guarantees-ui && npm start"

Start-Sleep -Seconds 3
Write-Host "✓ Frontend is starting" -ForegroundColor Green
Write-Host ""

Write-Host "=== Demo is running ===" -ForegroundColor Cyan
Write-Host "Frontend:  http://localhost:4200" -ForegroundColor Cyan
Write-Host "Backend:   http://localhost:8080/api/v1/guarantees" -ForegroundColor Cyan
Write-Host "Swagger:   http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "H2 Console: http://localhost:8080/h2-console" -ForegroundColor Cyan
Write-Host ""

# Keep processes running until Ctrl+C
try {
    while ($true) {
        Start-Sleep -Seconds 1
    }
} finally {
    $backendProcess.Kill()
    $frontendProcess.Kill()
}
