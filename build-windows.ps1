$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to a JDK 17 or newer.' }
$nsis = if ($env:MAKENSIS) { $env:MAKENSIS } else { (Get-Command makensis.exe -ErrorAction SilentlyContinue).Source }
if (-not $nsis) {
    $nsis = Join-Path ${env:ProgramFiles(x86)} 'NSIS\makensis.exe'
}
if (-not (Test-Path $nsis)) { throw 'Install NSIS 3 and add makensis.exe to PATH, or set MAKENSIS to its full path.' }
foreach ($path in @('build/windows/classes', 'build/windows/input')) {
    if (Test-Path $path) { Remove-Item -Recurse -Force $path }
}
New-Item -ItemType Directory -Force build/windows/classes, build/windows/input, dist | Out-Null
& "$env:JAVA_HOME/bin/javac.exe" --release 17 -d build/windows/classes src/KeyBuilder.java
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
& "$env:JAVA_HOME/bin/jar.exe" --create --file build/windows/input/key-builder.jar --main-class KeyBuilder -C build/windows/classes .
if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed.' }
if (Test-Path build/windows/runtime) { Remove-Item -Recurse -Force build/windows/runtime }
& "$env:JAVA_HOME/bin/jlink.exe" --module-path "$env:JAVA_HOME/jmods" --add-modules java.base,java.desktop --strip-debug --no-man-pages --no-header-files --output build/windows/runtime
if ($LASTEXITCODE -ne 0) { throw 'Runtime creation failed.' }
if (Test-Path dist/JProfilerKeyBuilder) { Remove-Item -Recurse -Force dist/JProfilerKeyBuilder }
& "$env:JAVA_HOME/bin/jpackage.exe" --type app-image --name JProfilerKeyBuilder --app-version 1.0 --input build/windows/input --main-jar key-builder.jar --main-class KeyBuilder --runtime-image build/windows/runtime --dest dist
if ($LASTEXITCODE -ne 0) { throw 'Windows packaging failed.' }
Compress-Archive -Path dist/JProfilerKeyBuilder -DestinationPath dist/JProfilerKeyBuilder-windows-x64.zip -Force
& $nsis /V3 "/DAPP_JAR=$PSScriptRoot/build/windows/input/key-builder.jar" "/DWINDOWS_JRE=$PSScriptRoot/build/windows/runtime" "/DOUTPUT_EXE=$PSScriptRoot/dist/JProfilerKeyBuilder-windows-x64.exe" packaging/windows-portable.nsi
if ($LASTEXITCODE -ne 0) { throw 'Single-file Windows packaging failed.' }
Write-Host "Created $PSScriptRoot/dist/JProfilerKeyBuilder-windows-x64.exe"
