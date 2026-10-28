# Builds, then starts the game. Any options go to the game, e.g. --replay runs/stage1-victory.txt.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
& ./build.ps1
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java -cp out/main rush.Main @args
exit $LASTEXITCODE
