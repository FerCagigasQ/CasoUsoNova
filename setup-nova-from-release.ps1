# Setup NOVA toolchain from GitHub Release (fallback when git lfs pull fails)
# Usage: .\setup-nova-from-release.ps1
# Requires: gh CLI (GitHub CLI) authenticated, or GITHUB_TOKEN env var

param(
    [string]$Release = "toolchain-v1",
    [string]$Repo = "FerCagigasQ/CasoUsoNova"
)

$ErrorActionPreference = "Stop"
$REPO_ROOT = $PSScriptRoot
$TOOLCHAIN_DIR = Join-Path $REPO_ROOT "toolchain"
$TOOLCHAIN_ZIP = Join-Path $REPO_ROOT "toolchain-nova-le.zip"

Write-Host "=== NOVA Toolchain Setup from GitHub Release ==="
Write-Host "Repo:    $Repo"
Write-Host "Release: $Release"
Write-Host ""

# Check if toolchain binaries already present (node.exe as proxy)
$NODE_EXE = Join-Path $TOOLCHAIN_DIR "nova-le\nodejs\node.exe"
if (Test-Path $NODE_EXE) {
    $size = (Get-Item $NODE_EXE).Length
    if ($size -gt 1MB) {
        Write-Host "[OK] Toolchain binaries already present (node.exe = $([math]::Round($size/1MB,1)) MB)"
        Write-Host "Skipping download. Run setup-nova.sh / configure env vars manually."
        exit 0
    }
}

# Download toolchain zip from GitHub Release
Write-Host "[1/3] Downloading toolchain from GitHub Release '$Release'..."
if (Get-Command gh -ErrorAction SilentlyContinue) {
    gh release download $Release --repo $Repo --pattern "toolchain-nova-le.zip" --output $TOOLCHAIN_ZIP --clobber
} elseif ($env:GITHUB_TOKEN) {
    $headers = @{ Authorization = "Bearer $env:GITHUB_TOKEN" }
    $apiUrl = "https://api.github.com/repos/$Repo/releases/tags/$Release"
    $release_data = Invoke-RestMethod -Uri $apiUrl -Headers $headers
    $asset = $release_data.assets | Where-Object { $_.name -eq "toolchain-nova-le.zip" }
    if (-not $asset) {
        Write-Error "Asset 'toolchain-nova-le.zip' not found in release '$Release'. Available: $($release_data.assets.name -join ', ')"
        exit 1
    }
    Invoke-WebRequest -Uri $asset.browser_download_url -Headers $headers -OutFile $TOOLCHAIN_ZIP
} else {
    Write-Error "Neither 'gh' CLI nor GITHUB_TOKEN found. Install GitHub CLI (https://cli.github.com) and authenticate with 'gh auth login'."
    exit 1
}

Write-Host "[2/3] Extracting toolchain to $TOOLCHAIN_DIR..."
if (Test-Path $TOOLCHAIN_DIR) {
    Remove-Item $TOOLCHAIN_DIR -Recurse -Force
}
Expand-Archive -Path $TOOLCHAIN_ZIP -DestinationPath $REPO_ROOT -Force
Remove-Item $TOOLCHAIN_ZIP

Write-Host "[3/3] Configuring environment variables..."
$NOVA_HOME = Join-Path $TOOLCHAIN_DIR "nova-le"
$JAVA_HOME = Join-Path $TOOLCHAIN_DIR "zulu-jdk11"
$MAVEN_HOME = Join-Path $NOVA_HOME "tools\maven"
$env:NOVA_HOME = $NOVA_HOME
$env:JAVA_HOME = $JAVA_HOME
$env:MAVEN_HOME = $MAVEN_HOME
$env:PATH = "$NOVA_HOME\nova-cli\bin;$NOVA_HOME\nodejs;$JAVA_HOME\bin;$MAVEN_HOME\bin;$env:PATH"

Write-Host ""
Write-Host "Environment set for current session:"
Write-Host "  NOVA_HOME = $NOVA_HOME"
Write-Host "  JAVA_HOME = $JAVA_HOME"
Write-Host "  MAVEN_HOME = $MAVEN_HOME"
Write-Host ""
Write-Host "Verifying..."
try {
    $v = & node "$NOVA_HOME\nova-cli\bin\nova.js" --version 2>&1
    Write-Host "  $v"
    Write-Host "[OK] NOVA toolchain ready."
} catch {
    Write-Warning "nova CLI not responding — check that the release zip contains toolchain/nova-le/nova-cli/bin/nova.js"
}
