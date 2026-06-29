#!/usr/bin/env pwsh
# setup-nova.ps1 — Download-on-demand NOVA CLI toolchain setup (Windows)
#
# Downloads the NOVA toolchain from GitHub Release if not already installed,
# then exports environment variables so nova commands are available.
#
# Usage:
#   . .\setup-nova.ps1     (dot-source to export variables to current shell)
#   .\setup-nova.ps1       (run in child shell — variables not exported to parent)
#
# The toolchain is cached at ~\.nova-toolchain\7.8.0\ (or $env:NOVA_INSTALL_DIR).
# Subsequent runs skip the download.

$ErrorActionPreference = "Stop"

$NOVA_VERSION = "7.8.0"
$NOVA_RELEASE_BASE = "https://github.com/FerCagigasQ/CasoUsoNova/releases/download/nova-toolchain-v$NOVA_VERSION"
$NOVA_ZIP_URL = "$NOVA_RELEASE_BASE/nova-le-$NOVA_VERSION-windows.zip"
$ZULU_JDK_URL = "https://cdn.azul.com/zulu/bin/zulu11.74.15-ca-jdk11.0.24-win_x64.zip"
$MAVEN_URL = "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"

$NOVA_DIR = if ($env:NOVA_INSTALL_DIR) { $env:NOVA_INSTALL_DIR } else { "$env:USERPROFILE\.nova-toolchain\$NOVA_VERSION" }

# Helper: download and extract zip
function Download-And-Extract {
    param([string]$Url, [string]$Dest, [int]$StripComponents = 0)
    $tmpZip = [System.IO.Path]::GetTempFileName() + ".zip"
    try {
        Write-Host "[NOVA]   Downloading from $Url..." -ForegroundColor DarkGray
        Invoke-WebRequest -Uri $Url -OutFile $tmpZip -UseBasicParsing
        $tmpExtract = [System.IO.Path]::Combine([System.IO.Path]::GetTempPath(), [System.IO.Path]::GetRandomFileName())
        New-Item -ItemType Directory -Force -Path $tmpExtract | Out-Null
        Expand-Archive -Path $tmpZip -DestinationPath $tmpExtract -Force
        # Strip top-level directory if requested
        if ($StripComponents -gt 0) {
            $topLevel = Get-ChildItem $tmpExtract | Select-Object -First 1
            if ($topLevel -and $topLevel.PSIsContainer) {
                New-Item -ItemType Directory -Force -Path $Dest | Out-Null
                Get-ChildItem $topLevel.FullName | Copy-Item -Destination $Dest -Recurse -Force
            }
        } else {
            New-Item -ItemType Directory -Force -Path $Dest | Out-Null
            Get-ChildItem $tmpExtract | Copy-Item -Destination $Dest -Recurse -Force
        }
    } finally {
        Remove-Item $tmpZip -ErrorAction SilentlyContinue
        Remove-Item $tmpExtract -Recurse -ErrorAction SilentlyContinue
    }
}

# If NOVA_HOME already set and valid (Docker/Devin), use it directly
if ($env:NOVA_HOME -and (Test-Path "$env:NOVA_HOME\nova-cli\bin\nova.js")) {
    Write-Host "[NOVA] Using existing NOVA_HOME=$env:NOVA_HOME" -ForegroundColor Cyan
} else {
    $SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path

    # Check local toolchain/ directory (legacy layout)
    if (Test-Path "$SCRIPT_DIR\toolchain\nova-le\nova-cli\bin\nova.js") {
        $NOVA_DIR = "$SCRIPT_DIR\toolchain\nova-le"
        Write-Host "[NOVA] Using local toolchain at $NOVA_DIR" -ForegroundColor Cyan
    } elseif (-not (Test-Path "$NOVA_DIR\nova-cli\bin\nova.js")) {
        Write-Host "[NOVA] Setting up toolchain v$NOVA_VERSION at $NOVA_DIR..." -ForegroundColor Cyan
        New-Item -ItemType Directory -Force -Path $NOVA_DIR | Out-Null
        New-Item -ItemType Directory -Force -Path "$NOVA_DIR\tools\java" | Out-Null
        New-Item -ItemType Directory -Force -Path "$NOVA_DIR\tools\maven" | Out-Null

        # Try nova-cli from GitHub Release
        try {
            $null = Invoke-WebRequest -Uri $NOVA_ZIP_URL -Method Head -UseBasicParsing
            Write-Host "[NOVA]   Downloading nova-cli toolchain (Windows)..." -ForegroundColor DarkGray
            Download-And-Extract -Url $NOVA_ZIP_URL -Dest $NOVA_DIR -StripComponents 1
        } catch {
            Write-Host "[NOVA]   nova-cli zip not yet in release — skipping nova-cli install." -ForegroundColor Yellow
            Write-Host "[NOVA]   To install nova-cli, upload nova-le-$NOVA_VERSION-windows.zip to:" -ForegroundColor Yellow
            Write-Host "[NOVA]   https://github.com/FerCagigasQ/CasoUsoNova/releases/tag/nova-toolchain-v$NOVA_VERSION" -ForegroundColor Yellow
        }

        # Always download Zulu JDK 11
        if (-not (Test-Path "$NOVA_DIR\tools\java\bin\java.exe")) {
            Write-Host "[NOVA]   Downloading Zulu JDK 11 (Windows)..." -ForegroundColor DarkGray
            Download-And-Extract -Url $ZULU_JDK_URL -Dest "$NOVA_DIR\tools\java" -StripComponents 1
        }

        # Always download Maven 3.9.9
        if (-not (Test-Path "$NOVA_DIR\tools\maven\bin\mvn.cmd")) {
            Write-Host "[NOVA]   Downloading Maven 3.9.9..." -ForegroundColor DarkGray
            Download-And-Extract -Url $MAVEN_URL -Dest "$NOVA_DIR\tools\maven" -StripComponents 1
        }

        Write-Host "[NOVA] Setup complete." -ForegroundColor Green
    }

    $env:NOVA_HOME = $NOVA_DIR
}

# Export environment variables
if (-not $env:NOVA_HOME) { $env:NOVA_HOME = $NOVA_DIR }
$env:JAVA_HOME = "$env:NOVA_HOME\tools\java"
$env:MAVEN_HOME = "$env:NOVA_HOME\tools\maven"
$env:NOVA_CLI_PATH = "$env:NOVA_HOME\nova-cli\bin\nova.js"
$env:PATH = "$env:NOVA_HOME\nova-cli\bin;$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

Write-Host "[NOVA] Environment configured:" -ForegroundColor Cyan
Write-Host "  NOVA_HOME=$env:NOVA_HOME"
Write-Host "  JAVA_HOME=$env:JAVA_HOME"
Write-Host "  MAVEN_HOME=$env:MAVEN_HOME"

# Verify
if ((Test-Path "$env:NOVA_HOME\nova-cli\bin\nova.js") -and (Get-Command node -ErrorAction SilentlyContinue)) {
    try { node "$env:NOVA_HOME\nova-cli\bin\nova.js" --version 2>&1 } catch { Write-Host "  (nova-cli present but --version failed)" }
} else {
    try { & "$env:JAVA_HOME\bin\java.exe" -version 2>&1 | Select-Object -First 1 } catch { Write-Host "  java not found" }
}
