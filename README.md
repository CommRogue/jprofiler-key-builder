# JProfiler Evaluation Key Builder

A desktop application and CLI implementing the evaluation-key format recovered from JProfiler 16.2.1. It produces the native CRC32 body and Java checksum, with a single expiration slider in the desktop app and optional advanced settings in the CLI.

## Run on Linux

The Linux x86_64 archive includes its own Java runtime. Extract the complete folder and keep its contents together:

```bash
tar -xzf JProfilerKeyBuilder-linux-x64.tar.gz
./JProfilerKeyBuilder/bin/JProfilerKeyBuilder
```

The native executable is `JProfilerKeyBuilder/bin/JProfilerKeyBuilder`. It needs a graphical desktop for the GUI; the command-line mode also works without a display. Tested on this Linux x86_64 host. Other distributions require compatible glibc and standard desktop libraries.

## Run on Windows or Linux with Java

Use the small, portable `key-builder.jar` with Java 17 or newer:

```text
java -jar key-builder.jar
```

The delivered native bundle is for Linux. A Windows native executable can be built on Windows using `build-windows.ps1`; that build was not performed here.

## Desktop controls

Move the **Expires in** slider to choose one month to five years, in monthly steps. It starts at **1 year** and shows the resulting expiration date. Select **Generate key**, then **Copy key** or **Save to file**. Changing the slider clears the previous result.

The desktop app handles the remaining details automatically: version 16, issuer `ERWEW`, record ID `2053215`, and a days field matching the selected duration. The one-year default uses the same calendar date next year, accounting for leap years. Expiration takes effect near the beginning of the displayed date.

The issuer and ID come from the supplied example; their vendor-side allocation semantics remain unknown. Advanced values remain available through the CLI, whose existing defaults are unchanged.

## Command line

```bash
./JProfilerKeyBuilder/bin/JProfilerKeyBuilder --generate
./JProfilerKeyBuilder/bin/JProfilerKeyBuilder --generate --expires 2026-11-01
./JProfilerKeyBuilder/bin/JProfilerKeyBuilder --help
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

Windows PowerShell:

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk'
.\build-windows.ps1
```

The Windows script produces an app-image folder with `JProfilerKeyBuilder.exe` and a ZIP containing its runtime. Windows packaging is documented according to the [official jpackage options](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html); it has not been tested on a Windows host.

## Validation and limits

Validation performed on 2026-10-16:

- Reproduced the user-supplied vendor key byte for byte.
- Tested malformed dates, leap years, unsupported versions, invalid issuer/ID fields, and invalid days values.
- Exercised the one-year GUI default, both slider endpoints, mixed year/month labels, generated expiry and days fields, clearing stale output, and clipboard copying.
- Opened and closed the packaged Linux desktop executable successfully, with exit 0 and no stderr.
- Tested the packaged CLI, including error messages and exit codes.
- Generated a code expiring 2026-11-01 and passed it to the unmodified installed JProfiler Java validator in an isolated JVM. It returned `15` whole days remaining.

The native token calculation matches the recovered native algorithm and the original vendor sample. A live profiling session with a generated code has not been tested. The investigation did not establish vendor server issuance policies or the contents of the native prefix rejection list. The generator does not implement those unknown checks.

`tests/KeyBuilderTest.java` contains the known fixture and UI tests. `tests/JProfilerIntegration.java` can optionally invoke the shipped Java validator; JProfiler is not required to build or run this app. Test results are included alongside this README in the workspace. `preview.png` is rendered from the application itself and does not capture the desktop.
