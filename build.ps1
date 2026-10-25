# Compiles the game into out/main. Needs JDK 25 and nothing else.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$versionLine = (& cmd /c "javac -version 2>&1") | Out-String
if ($versionLine -notmatch 'javac (\d+)' -or [int]$Matches[1] -lt 25) {
    Write-Host "This project needs JDK 25 or newer. Found: $($versionLine.Trim())"
    exit 1
}

if (Test-Path out/main) { Remove-Item -Recurse -Force out/main }
New-Item -ItemType Directory -Force out/main | Out-Null
Get-ChildItem src/main/java -Recurse -Filter *.java |
    ForEach-Object { '"' + ($_.FullName -replace '\\', '/') + '"' } |
    Set-Content -Encoding ascii out/main-sources.txt
javac --release 25 -encoding UTF-8 -d out/main "@out/main-sources.txt"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
