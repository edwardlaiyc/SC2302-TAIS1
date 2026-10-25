#!/usr/bin/env sh
# Compiles the game into out/main. Needs JDK 25 and nothing else.
set -e
cd "$(dirname "$0")"

version=$(javac -version 2>&1 | sed -E 's/javac ([0-9]+).*/\1/')
if [ "$version" -lt 25 ] 2>/dev/null; then
  echo "This project needs JDK 25 or newer. Found: $(javac -version 2>&1)" >&2
  exit 1
fi

rm -rf out/main
mkdir -p out/main
find src/main/java -name '*.java' > out/main-sources.txt
javac --release 25 -encoding UTF-8 -d out/main @out/main-sources.txt
