#!/usr/bin/env sh
# Builds, then starts the game. Any options go to the game, e.g. --replay runs/stage1-victory.txt.
set -e
cd "$(dirname "$0")"
./build.sh
java -cp out/main rush.Main "$@"
