[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$trainingServiceRoot = Join-Path $repositoryRoot "fit-bridge-be/training-service"
$deployRoot = Join-Path $repositoryRoot "deploy"
$composeFile = Join-Path $deployRoot "docker-compose.yml"
$isWindowsHost = [System.Environment]::OSVersion.Platform -eq [System.PlatformID]::Win32NT
$gradleWrapper = Join-Path $repositoryRoot $(if ($isWindowsHost) { "gradlew.bat" } else { "gradlew" })

function Invoke-CheckedCommand {
    param(
        [Parameter(Mandatory)]
        [string]$Description,

        [Parameter(Mandatory)]
        [string]$FilePath,

        [Parameter(Mandatory)]
        [string[]]$Arguments
    )

    Write-Host "`n==> $Description"
    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

function Get-FatJar {
    param(
        [Parameter(Mandatory)]
        [string]$ServiceName,

        [Parameter(Mandatory)]
        [string]$LibrariesDirectory
    )

    $candidates = @(
        Get-ChildItem -LiteralPath $LibrariesDirectory -Filter "*-all.jar" -File |
            Sort-Object LastWriteTimeUtc -Descending
    )

    if ($candidates.Count -eq 0) {
        throw "The shadowJar artifact for $ServiceName was not found in $LibrariesDirectory."
    }

    $artifact = $candidates[0]
    if ($artifact.Length -lt 1MB) {
        throw "The artifact selected for $ServiceName is too small to be a fat JAR: $($artifact.FullName)."
    }

    if ($candidates.Count -gt 1) {
        Write-Warning "Several fat JARs exist for $ServiceName; the newest one is selected: $($artifact.Name)."
    }

    return $artifact
}

function Copy-ServiceArtifact {
    param(
        [Parameter(Mandatory)]
        [System.IO.FileInfo]$Artifact,

        [Parameter(Mandatory)]
        [string]$Destination
    )

    Copy-Item -LiteralPath $Artifact.FullName -Destination $Destination -Force
    $stagedArtifact = Get-Item -LiteralPath $Destination
    $hash = (Get-FileHash -LiteralPath $Destination -Algorithm SHA256).Hash
    Write-Host "Staged $($stagedArtifact.Name): $($stagedArtifact.Length) bytes, SHA256=$hash"
}

if (-not (Test-Path -LiteralPath $gradleWrapper -PathType Leaf)) {
    throw "Gradle wrapper was not found: $gradleWrapper"
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker CLI was not found in PATH."
}

try {
    Invoke-CheckedCommand `
        -Description "Validate Docker Compose configuration" `
        -FilePath "docker" `
        -Arguments @("compose", "--file", $composeFile, "config", "--quiet")

    Invoke-CheckedCommand `
        -Description "Build training-service fat JAR" `
        -FilePath $gradleWrapper `
        -Arguments @("-p", $trainingServiceRoot, ":app-ktor:shadowJar", "--console=plain")

    $trainingArtifact = Get-FatJar `
        -ServiceName "training-service" `
        -LibrariesDirectory (Join-Path $trainingServiceRoot "app-ktor/build/libs")

    Copy-ServiceArtifact `
        -Artifact $trainingArtifact `
        -Destination (Join-Path $deployRoot "training-service.jar")

    Invoke-CheckedCommand `
        -Description "Start storage services" `
        -FilePath "docker" `
        -Arguments @(
            "compose", "--file", $composeFile,
            "up", "--detach", "--wait", "--wait-timeout", "180",
            "postgresql", "greptimedb"
        )

    Invoke-CheckedCommand `
        -Description "Start the logging service" `
        -FilePath "docker" `
        -Arguments @(
            "compose", "--file", $composeFile,
            "up", "--detach", "--wait", "--wait-timeout", "180",
            "fluent-bit"
        )

    Invoke-CheckedCommand `
        -Description "Recreate Keycloak with the current mounted realm configuration" `
        -FilePath "docker" `
        -Arguments @(
            "compose", "--file", $composeFile,
            "up", "--detach", "--force-recreate", "--no-deps",
            "--wait", "--wait-timeout", "180", "keycloak"
        )

    Invoke-CheckedCommand `
        -Description "Recreate Envoy with the current mounted routing configuration" `
        -FilePath "docker" `
        -Arguments @(
            "compose", "--file", $composeFile,
            "up", "--detach", "--force-recreate", "--no-deps",
            "--wait", "--wait-timeout", "180", "envoy"
        )

    Invoke-CheckedCommand `
        -Description "Build and start application services" `
        -FilePath "docker" `
        -Arguments @(
            "compose", "--file", $composeFile,
            "up", "--detach", "--build", "--no-deps", "--wait", "--wait-timeout", "180",
            "training-service"
        )

    Invoke-CheckedCommand `
        -Description "Show the local E2E stack status" `
        -FilePath "docker" `
        -Arguments @("compose", "--file", $composeFile, "ps")

    Write-Host "`n==> Verify public health endpoints"
    $healthUrls = @(
        "http://localhost:8080/health",
        "http://localhost:8080/health/training/ready"
    )
    foreach ($healthUrl in $healthUrls) {
        $response = Invoke-WebRequest -UseBasicParsing -Uri $healthUrl -TimeoutSec 15
        if ($response.StatusCode -ne 200) {
            throw "Health check returned HTTP $($response.StatusCode): $healthUrl"
        }
        Write-Host "$healthUrl -> HTTP $($response.StatusCode)"
    }

    Invoke-CheckedCommand `
        -Description "Run FitBridge E2E tests" `
        -FilePath $gradleWrapper `
        -Arguments @("-p", $repositoryRoot, "e2eTests", "--rerun-tasks", "--console=plain")

    Write-Host "`nE2E verification completed successfully. The local stack remains running."
} catch {
    Write-Host "E2E workflow failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`nCurrent Docker Compose status:"
    & docker compose --file $composeFile ps
    Write-Host "The local stack was not removed. Inspect it before running 'docker compose down'."
    exit 1
}
