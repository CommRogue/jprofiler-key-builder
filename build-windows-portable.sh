#!/usr/bin/env bash
# Cross-build a self-contained Windows executable on Linux with NSIS.
set -euo pipefail
cd -- "$(dirname -- "$0")"
builder_jdk="${KEY_BUILDER_JDK:-${JAVA_HOME:-}}"
builder_nsis="${MAKENSIS:-makensis}"
if [ ! -x "$builder_jdk/bin/javac" ] || [ ! -x "$builder_jdk/bin/jar" ]; then
    echo 'Set KEY_BUILDER_JDK or JAVA_HOME to a JDK 17 or newer.' >&2
    exit 2
fi
if [ ! -f "${WINDOWS_JRE:-}/bin/javaw.exe" ] || [ ! -f "${WINDOWS_JRE:-}/bin/server/jvm.dll" ]; then
    echo 'Set WINDOWS_JRE to an extracted Windows x64 JRE 17 or newer.' >&2
    exit 2
fi
if ! command -v "$builder_nsis" >/dev/null 2>&1; then
    echo 'Install NSIS 3 or set MAKENSIS to its compiler path.' >&2
    exit 2
fi
builder_runtime="$(cd -- "$WINDOWS_JRE" && pwd)"
rm -rf -- build/windows/classes build/windows/input
mkdir -p build/windows/classes build/windows/input dist
"$builder_jdk/bin/javac" --release 17 -d build/windows/classes src/KeyBuilder.java
"$builder_jdk/bin/jar" --create --file build/windows/input/JFucker.jar \
    --main-class KeyBuilder -C build/windows/classes .
"$builder_nsis" -V3 \
    "-DAPP_JAR=$PWD/build/windows/input/JFucker.jar" \
    "-DWINDOWS_JRE=$builder_runtime" \
    "-DOUTPUT_EXE=$PWD/dist/JFucker-windows-x64.exe" \
    packaging/windows-portable.nsi
echo "Created $PWD/dist/JFucker-windows-x64.exe"
