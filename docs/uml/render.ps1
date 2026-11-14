# Renders every diagram in this folder to SVG, next to its .puml file.
# Needs Java, plantuml.jar, Graphviz and the Inter font. Set $env:PLANTUML_JAR to the jar's path,
# and $env:GRAPHVIZ_DOT to dot.exe if it is not on your PATH.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
$jar = if ($env:PLANTUML_JAR) { $env:PLANTUML_JAR } else { "plantuml.jar" }
$files = Get-ChildItem -Name class-*.puml, seq-*.puml, state-*.puml, activity-*.puml, evolution-*.puml
java -jar $jar -tsvg -charset UTF-8 @files
exit $LASTEXITCODE
