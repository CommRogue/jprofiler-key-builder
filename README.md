# JFucker

A desktop application and CLI implementing the evaluation-key format recovered from JProfiler 16.2.1. It produces the native CRC32 body and Java checksum, with a single expiration slider in the desktop app and optional advanced settings in the CLI.

## Run on Windows

Download [JFucker-windows-x64.exe](dist/JFucker-windows-x64.exe) from `dist` and double-click it. This single-file portable desktop app includes its own Windows x64 Java runtime. No Java installation or administrator access is required. It unpacks into a temporary folder on each launch and removes that folder after the app closes; allow a few seconds for startup.

Use the JAR below for command-line output. The portable EXE opens the desktop app.

## Run on Linux

The Linux x86_64 archive includes its own Java runtime. Extract the complete folder and keep its contents together:

```bash
tar -xzf JFucker-linux-x64.tar.gz
./JFucker/bin/JFucker
```

The native executable is `JFucker/bin/JFucker`. It needs a graphical desktop for the GUI; the command-line mode also works without a display. Tested on this Linux x86_64 host. Other distributions require compatible glibc and standard desktop libraries.

## Run on Windows or Linux with Java

Use the small, portable `JFucker.jar` with Java 17 or newer:

```text
java -jar JFucker.jar
```

The portable JAR is useful for the CLI or systems with Java already installed. Windows users can use the single-file EXE above for the desktop app.

## Desktop controls

Move the **Expires in** slider to choose one month to ten years, in monthly steps. It starts at **5 years** and shows the resulting expiration date. Select **Generate key**, then **Copy key** or **Save to file**. Changing the slider clears the previous result.

The desktop app handles the remaining details automatically: version 16, issuer `ERWEW`, record ID `2053215`, and a days field matching the selected duration. The five-year default uses the same calendar date five years from today, accounting for leap years. Expiration takes effect near the beginning of the displayed date.

The issuer and ID come from the supplied example; their vendor-side allocation semantics remain unknown. Advanced values remain available through the CLI, whose existing defaults are unchanged.

## Command line

```bash
./JFucker/bin/JFucker --generate
./JFucker/bin/JFucker --generate --expires 2026-11-01
./JFucker/bin/JFucker --help
```

Available options:

| Option | Default |
| --- | --- |
| `--version` | `16` (also accepts `17`) |
| `--expires` | Today + 11 days, ISO `YYYY-MM-DD` |
| `--issuer` | `ERWEW` |
| `--id` | `2053215` |
| `--days` | `10` |

A successful CLI call prints only the key and exits 0. Invalid input writes a message to stderr and exits 2. The CLI can use historical dates for reproducing fixtures.

## Build

The source has no third-party dependencies. A full JDK 17 or newer supplies `javac`, `jar`, and `jpackage`.

Linux:

```bash
export JAVA_HOME=/path/to/jdk
bash build-linux.sh
```

Windows PowerShell (Windows x64 JDK 17+ and [NSIS 3](https://nsis.sourceforge.io/Download) required):

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk'
.\build-windows.ps1
```

The Windows script produces the single-file `dist/JFucker-windows-x64.exe`, plus an app-image folder with `JFucker.exe` and a ZIP containing its runtime. If NSIS is installed outside its default directory, set `MAKENSIS` to the full path to `makensis.exe`. Keep the entire app-image folder together when using the ZIP distribution.

To cross-build the single-file Windows EXE on Linux, install NSIS 3, supply a Windows x64 JRE 17+ (for example, [Eclipse Temurin](https://adoptium.net/temurin/releases/)), and run:

```bash
export JAVA_HOME=/path/to/linux/jdk
export WINDOWS_JRE=/path/to/extracted/windows/jre
bash build-windows-portable.sh
```

Set `MAKENSIS` if the NSIS compiler is outside `PATH`. The shared [NSIS launcher](packaging/windows-portable.nsi) packages the JAR and runtime into a self-extracting GUI executable, using NSIS's temporary directory and process-waiting behavior. See the [NSIS scripting reference](https://nsis.sourceforge.io/Docs/Chapter4.html) and [jpackage options](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html).

## Validation and limits

Validation performed on 2026-10-16:

- Reproduced the user-supplied vendor key byte for byte.
- Tested malformed dates, leap years, unsupported versions, invalid issuer/ID fields, and invalid days values.
- Exercised the five-year GUI default, both slider endpoints, mixed year/month labels, generated expiry and days fields, clearing stale output, and clipboard copying.
- Opened and closed the packaged Linux desktop executable successfully, with exit 0 and no stderr.
- Tested the packaged CLI, including error messages and exit codes.
- Generated a code expiring 2026-11-01 and passed it to the unmodified installed JProfiler Java validator in an isolated JVM. It returned `15` whole days remaining.
- Cross-built the single-file Windows EXE with Temurin 17.0.20.1+1 x64. Verified the runtime download's published SHA-256, the EXE archive's integrity, its JAR manifest, and the embedded Windows x64 runtime against the original files.

The Windows EXE has not been run on native Windows. A Wine smoke test could not start because this host rejected executable DLL mappings. The Windows PowerShell build script has not been run here; the EXE was built with `build-windows-portable.sh`.

The native token calculation matches the recovered native algorithm and the original vendor sample. A live profiling session with a generated code has not been tested. The investigation did not establish vendor server issuance policies or the contents of the native prefix rejection list. The generator does not implement those unknown checks.

`tests/KeyBuilderTest.java` contains the known fixture and UI tests. `tests/JProfilerIntegration.java` can optionally invoke the shipped Java validator; JProfiler is not required to build or run this app. Test results are included alongside this README in the workspace. `preview.png` is rendered from the application itself and does not capture the desktop.
