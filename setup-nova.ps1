#!/usr/bin/env pwsh
# setup-nova.ps1 - Download-on-demand NOVA CLI toolchain setup (Windows)
#
# Usage:
#   . .\setup-nova.ps1
#   .\setup-nova.ps1

$ErrorActionPreference = "Stop"

$NOVA_VERSION = "7.8.0"
$NOVA_RELEASE_API = "https://api.github.com/repos/FerCagigasQ/CasoUsoNova/releases/tags/nova-toolchain-v$NOVA_VERSION"
$NOVA_RELEASE_PAGE = "https://github.com/FerCagigasQ/CasoUsoNova/releases/tag/nova-toolchain-v$NOVA_VERSION"
$NOVA_ZIP_NAME = "nova-le-$NOVA_VERSION-windows.zip"
$ZULU_JDK_URL = "https://cdn.azul.com/zulu/bin/zulu11.74.15-ca-jdk11.0.24-win_x64.zip"
$MAVEN_URL = "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"

$NOVA_DIR = if ($env:NOVA_INSTALL_DIR) {
    $env:NOVA_INSTALL_DIR
} else {
    Join-Path $env:USERPROFILE ".nova-toolchain\$NOVA_VERSION"
}

function Download-And-Extract {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][string]$Dest,
        [int]$StripComponents = 0
    )

    $tmpZip = [System.IO.Path]::GetTempFileName() + ".zip"
    $tmpExtract = Join-Path ([System.IO.Path]::GetTempPath()) ([System.IO.Path]::GetRandomFileName())

    try {
        Write-Host "[NOVA]   Downloading from $Url..." -ForegroundColor DarkGray
        Invoke-WebRequest -Uri $Url -OutFile $tmpZip -UseBasicParsing
        New-Item -ItemType Directory -Force -Path $tmpExtract | Out-Null
        Expand-Archive -Path $tmpZip -DestinationPath $tmpExtract -Force
        New-Item -ItemType Directory -Force -Path $Dest | Out-Null

        if ($StripComponents -gt 0) {
            $topLevel = Get-ChildItem $tmpExtract | Select-Object -First 1
            if ($topLevel -and $topLevel.PSIsContainer) {
                Get-ChildItem $topLevel.FullName | Copy-Item -Destination $Dest -Recurse -Force
            }
        } else {
            Get-ChildItem $tmpExtract | Copy-Item -Destination $Dest -Recurse -Force
        }
    } finally {
        Remove-Item $tmpZip -ErrorAction SilentlyContinue
        Remove-Item $tmpExtract -Recurse -ErrorAction SilentlyContinue
    }
}

$existingNovaCli = if ($env:NOVA_HOME) { Join-Path $env:NOVA_HOME "nova-cli\bin\nova.js" } else { $null }

if ($existingNovaCli -and (Test-Path $existingNovaCli)) {
    Write-Host ("[NOVA] Using existing NOVA_HOME={0}" -f $env:NOVA_HOME) -ForegroundColor Cyan
} else {
    $SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
    $localNovaCli = Join-Path $SCRIPT_DIR "toolchain\nova-le\nova-cli\bin\nova.js"

    if (Test-Path $localNovaCli) {
        $NOVA_DIR = Join-Path $SCRIPT_DIR "toolchain\nova-le"
        Write-Host "[NOVA] Using local toolchain at $NOVA_DIR" -ForegroundColor Cyan
    } elseif (-not (Test-Path (Join-Path $NOVA_DIR "nova-cli\bin\nova.js"))) {
        Write-Host "[NOVA] Setting up toolchain v$NOVA_VERSION at $NOVA_DIR..." -ForegroundColor Cyan
        New-Item -ItemType Directory -Force -Path $NOVA_DIR | Out-Null
        New-Item -ItemType Directory -Force -Path (Join-Path $NOVA_DIR "tools\java") | Out-Null
        New-Item -ItemType Directory -Force -Path (Join-Path $NOVA_DIR "tools\maven") | Out-Null

        try {
            $release = Invoke-RestMethod -Uri $NOVA_RELEASE_API -UseBasicParsing
            $asset = $release.assets | Where-Object { $_.name -eq $NOVA_ZIP_NAME } | Select-Object -First 1
            if (-not $asset) {
                throw "Asset $NOVA_ZIP_NAME is not available in $NOVA_RELEASE_PAGE"
            }
            Write-Host "[NOVA]   Downloading nova-cli toolchain (Windows)..." -ForegroundColor DarkGray
            Download-And-Extract -Url $asset.browser_download_url -Dest $NOVA_DIR -StripComponents 1
        } catch {
            Write-Host "[NOVA]   nova-cli zip not yet in release - skipping nova-cli install." -ForegroundColor Yellow
            Write-Host "[NOVA]   Upload $NOVA_ZIP_NAME to:" -ForegroundColor Yellow
            Write-Host "[NOVA]   $NOVA_RELEASE_PAGE" -ForegroundColor Yellow
        }

        if (-not (Test-Path (Join-Path $NOVA_DIR "tools\java\bin\java.exe"))) {
            Write-Host "[NOVA]   Downloading Zulu JDK 11 (Windows)..." -ForegroundColor DarkGray
            Download-And-Extract -Url $ZULU_JDK_URL -Dest (Join-Path $NOVA_DIR "tools\java") -StripComponents 1
        }

        if (-not (Test-Path (Join-Path $NOVA_DIR "tools\maven\bin\mvn.cmd"))) {
            Write-Host "[NOVA]   Downloading Maven 3.9.9..." -ForegroundColor DarkGray
            Download-And-Extract -Url $MAVEN_URL -Dest (Join-Path $NOVA_DIR "tools\maven") -StripComponents 1
        }

        Write-Host "[NOVA] Setup complete." -ForegroundColor Green
    }

    $env:NOVA_HOME = $NOVA_DIR
}

if (-not $env:NOVA_HOME) {
    $env:NOVA_HOME = $NOVA_DIR
}

$env:JAVA_HOME = Join-Path $env:NOVA_HOME "tools\java"
$env:MAVEN_HOME = Join-Path $env:NOVA_HOME "tools\maven"
$env:NOVA_CLI_PATH = Join-Path $env:NOVA_HOME "nova-cli\bin\nova.js"
$env:PATH = ("{0}\nova-cli\bin;{1}\bin;{2}\bin;{3}" -f $env:NOVA_HOME, $env:JAVA_HOME, $env:MAVEN_HOME, $env:PATH)

Write-Host "[NOVA] Environment configured:" -ForegroundColor Cyan
Write-Host ("  NOVA_HOME={0}" -f $env:NOVA_HOME)
Write-Host ("  JAVA_HOME={0}" -f $env:JAVA_HOME)
Write-Host ("  MAVEN_HOME={0}" -f $env:MAVEN_HOME)

if ((Test-Path $env:NOVA_CLI_PATH) -and (Get-Command node -ErrorAction SilentlyContinue)) {
    try {
        node $env:NOVA_CLI_PATH --version
    } catch {
        Write-Host "  nova-cli present but --version failed"
    }
} else {
    try {
        & (Join-Path $env:JAVA_HOME "bin\java.exe") -version
    } catch {
        Write-Host "  java not found"
    }
}
