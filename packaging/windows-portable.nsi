; A single-file GUI launcher. NSIS extracts the private runtime to its unique
; temporary directory, waits for Java to exit, then removes the extracted files.
Unicode true
Name "JFucker"
OutFile "${OUTPUT_EXE}"
RequestExecutionLevel user
SilentInstall silent
AutoCloseWindow true
SetCompressor /SOLID lzma
ManifestDPIAware true
VIProductVersion "1.0.0.0"
VIAddVersionKey /LANG=1033 "ProductName" "JFucker"
VIAddVersionKey /LANG=1033 "FileDescription" "JFucker portable launcher"
VIAddVersionKey /LANG=1033 "FileVersion" "1.0.0"
VIAddVersionKey /LANG=1033 "LegalCopyright" ""

!include "x64.nsh"

Function .onInit
    ${IfNot} ${RunningX64}
        MessageBox MB_OK|MB_ICONSTOP "This application requires 64-bit Windows."
        SetErrorLevel 2
        Quit
    ${EndIf}
FunctionEnd

Section
    InitPluginsDir
    SetOutPath "$PLUGINSDIR"
    ClearErrors
    File /oname=JFucker.jar "${APP_JAR}"
    SetOutPath "$PLUGINSDIR\runtime"
    File /r "${WINDOWS_JRE}/*"
    IfErrors extraction_failed

    ; Keep the working directory outside the temporary runtime so cleanup works.
    SetOutPath "$EXEDIR"
    ClearErrors
    ExecWait '"$PLUGINSDIR\runtime\bin\javaw.exe" -jar "$PLUGINSDIR\JFucker.jar"' $0
    IfErrors launch_failed
    SetErrorLevel $0
    Goto done

extraction_failed:
    MessageBox MB_OK|MB_ICONSTOP "Could not unpack the application. Check free space and access to your temporary folder."
    SetErrorLevel 2
    Goto done

launch_failed:
    MessageBox MB_OK|MB_ICONSTOP "Could not start the bundled Java runtime."
    SetErrorLevel 2

done:
SectionEnd
