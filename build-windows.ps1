$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to a JDK 17 or newer.' }
New-Item -ItemType Directory -Force build/classes, build/input, dist | Out-Null
& "$env:JAVA_HOME/bin/javac.exe" --release 17 -d build/classes src/KeyBuilder.java
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
& "$env:JAVA_HOME/bin/jar.exe" --create --file build/input/key-builder.jar --main-class KeyBuilder -C build/classes .
if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed.' }
& "$env:JAVA_HOME/bin/jpackage.exe" --type app-image --name JProfilerKeyBuilder --app-version 1.0 --input build/input --main-jar key-builder.jar --main-class KeyBuilder --add-modules java.base,java.desktop --dest dist
if ($LASTEXITCODE -ne 0) { throw 'Windows packaging failed.' }
Compress-Archive -Path dist/JProfilerKeyBuilder -DestinationPath dist/JProfilerKeyBuilder-windows-x64.zip
