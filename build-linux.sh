#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
builder_jdk="${KEY_BUILDER_JDK:-${JAVA_HOME:-}}"
if [ -z "$builder_jdk" ]; then
    builder_javac="$(command -v javac || true)"
    if [ -n "$builder_javac" ]; then
        builder_jdk="$(dirname -- "$(dirname -- "$(readlink -f -- "$builder_javac")")")"
    fi
fi
if [ ! -x "$builder_jdk/bin/jpackage" ]; then
    echo 'Set KEY_BUILDER_JDK or JAVA_HOME to a JDK 17 or newer.' >&2
    exit 2
fi
mkdir -p build/classes build/input dist
"$builder_jdk/bin/javac" --release 17 -d build/classes src/KeyBuilder.java
"$builder_jdk/bin/jar" --create --file build/input/JFucker.jar --main-class KeyBuilder -C build/classes .
cp build/input/JFucker.jar dist/JFucker.jar
# This minimal build host has no objcopy, so retain runtime debug symbols.
if [ -d dist/JFucker ]; then rm -rf -- dist/JFucker; fi
"$builder_jdk/bin/jpackage" --type app-image --name JFucker --app-version 1.0 \
    --description 'Evaluation key builder' --input build/input --main-jar JFucker.jar \
    --main-class KeyBuilder --add-modules java.base,java.desktop \
    --jlink-options '--strip-native-commands --no-man-pages --no-header-files' --dest dist
tar -czf dist/JFucker-linux-x64.tar.gz -C dist JFucker
