MyApplication — Android port of SPT Data Analysis and Reporting Tool

What I changed
- Ported core flows from desktop JavaFX to Android activities.
- Added `DBHelper` and `AppSession` for local persistence and session state.
- Implemented login, client/location management, borehole listing, raw SPT entry UI.
- Added CSV export/import helper: `ExportImportHelper` (exports `clients_export.csv`).
- Added a simple `AnalysisActivity` showing client/SPT counts.

Build notes
- This project requires at least `minSdk 23` (set in `app/build.gradle.kts`).
- Gradle wrapper default: Gradle 8.13. If you get a Java compatibility error, set `org.gradle.java.home` in `gradle.properties` to a JDK 17+ installation.

How to export clients to CSV (from code)
```
File out = ExportImportHelper.exportClientsToCsv(context);
```

How to import clients CSV
```
int count = ExportImportHelper.importClientsFromCsv(context, new File("path/to/clients.csv"));
```

Next steps
- Fix remaining resource linking errors (I fixed a malformed layout already). Run `./gradlew assembleDebug --no-daemon --stacktrace` and attach the resource linking output if errors persist.
- Port analysis / visualisation features fully.
- Add CSV import UI and export buttons.
SPT Data — Android module (MyApplication)

Quick build & run

From project root or this module:

PowerShell:

cd "MyApplication"
.\gradlew.bat assembleDebug

or to install on a connected device/emulator:

cd "MyApplication"
.\gradlew.bat installDebug

Notes

- `minSdk` was raised to 23 to match required library versions.
- Activities were added as skeletons to mirror the JavaFX controllers (`Hello`, `Login`, `Dashboard`, `ClientDetails`, `ClientLocations`, `RawData`, `SeniorLogin`, `SubLogin`, `ModifiersDashboard`, `EnterNewClient`, `EditLocation`, `EditClient`, `BoreholeDashboard`, `Analysis`, `VisualClassification`).
- UI layouts are simple placeholders; follow-up work is required to port FXML views and controller logic.

If the build fails, rerun with more logging to diagnose:

cd "MyApplication"
.\gradlew.bat assembleDebug --stacktrace --info

If you want, I can:
- Start porting a specific controller's logic and UI next (tell me which), or
- Run the build here with `--stacktrace` and debug the failure.
