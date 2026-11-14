#!/usr/bin/env sh
# Renders every diagram in this folder to SVG, next to its .puml file.
# Needs Java, plantuml.jar, Graphviz and the Inter font. Set PLANTUML_JAR to the jar's path,
# and GRAPHVIZ_DOT to dot if it is not on your PATH.
set -e
cd "$(dirname "$0")"
java -jar "${PLANTUML_JAR:-plantuml.jar}" -tsvg -charset UTF-8 class-*.puml seq-*.puml state-*.puml activity-*.puml evolution-*.puml
