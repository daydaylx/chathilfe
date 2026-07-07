# Build-Validierungsbericht

Erster lokaler Build-Validierungslauf für ChatHilfe.

- **Datum:** 2026-07-07
- **Zweck:** Repo lokal einrichten, Toolchain prüfen, `assembleDebug` lauffähig machen. Keine Feature-Implementierung, kein Refactoring.
- **Branch:** `main`

---

## Umgebung

| Komponente | Wert |
|---|---|
| OS | Linux Mint 22.2 (Zara), Kernel 6.8.0-117-generic x86_64 |
| Git | 2.43.0 |
| Java (Default) | OpenJDK 21.0.10 (JDK 17 ebenfalls installiert) |
| `JAVA_HOME` | leer (nicht blockierend) |
| `ANDROID_HOME` | `/home/d/Android/Sdk` |
| `ANDROID_SDK_ROOT` | leer (nicht blockierend) |
| Android cmdline-tools | 13.0 |
| platform-tools | vorhanden (adb) |
| AGP | 9.2.0 |
| Gradle Wrapper | 9.6.1 |
| Kotlin (Compose-Plugin) | 2.4.0 |
| Compose BOM | 2026.06.01 (material3 aufgelöst zu 1.4.0) |

---

## Setup-Aktionen

- Repo `daydaylx/chathilfe` nach `~/Projekte/chathilfe` geklont.
- `gradlew` war bereits ausführbar.
- `local.properties` angelegt: `sdk.dir=/home/d/Android/Sdk` + `OPENROUTER_API_KEY=replace_me_locally` (Platzhalter, kein echter Key).
- `git check-ignore` bestätigt: `local.properties` wird von `.gitignore` (Zeile 6) ignoriert.
- Beim ersten Build hat AGP `platforms;android-36` und `build-tools;36.0.0` automatisch installiert — keine manuelle `sdkmanager`-Installation nötig.
- `compileSdk` von 36 auf 37 angehoben (siehe unten), danach lief der Build.

---

## Gefundene Probleme und Behebung

### 1. SDK anfangs unvollständig (nicht blockierend)
`platforms;android-36` und `build-tools;36.0.0` fehlten zu Beginn. AGP hat beide beim ersten Build automatisch nachgeladen. `JAVA_HOME` und `ANDROID_SDK_ROOT` sind leer, waren aber nicht blockierend.

### 2. Projekt-interne Inkonsistenz `compileSdk` (Build-Blocker, behoben)
Die in Phase 1/2 fixierten androidx-Versionen
- `androidx.core:core-ktx:1.19.0` und
- `androidx.lifecycle:lifecycle-runtime-compose:2.11.0`

erfordern zum Kompilieren API-Level 37. Der Task `:app:checkDebugAarMetadata` schlug daher mit compileSdk 36 fehl. AGP empfiehlt als Fix das Anheben von `compileSdk` auf 37.

**Behebung:** `compileSdk` 36 → 37 in `app/build.gradle.kts` (genau eine Zeile). `targetSdk` bleibt 35 (Laufzeitverhalten unverändert), `minSdk` bleibt 29. Entscheidungsdokument [`docs/DECISIONS.md`](DECISIONS.md) D-004 wurde entsprechend aktualisiert.

### 3. Lint: 1 Error + 9 Warnings (nicht APK-blockierend, offen)
`./gradlew lintDebug` scheitert mit 1 Error in vorhandenem Code:
- `LocalContextGetResourceValueCall` in `app/src/main/java/de/disaai/chathilfe/settings/SettingsScreen.kt:136` (Compose-Regel aus `androidx.compose.ui`).

Zusätzlich 9 Warnings:
- `InlinedApi` — `POST_NOTIFICATIONS` benötigt API 33 bei minSdk 29 (`SettingsScreen.kt:105`)
- `OldTargetApi` — `targetSdk` 35 < `compileSdk` 37 (erwartet, `build.gradle.kts:26`)
- `ObsoleteSdkInt` — `mipmap-anydpi-v26` unnötig, da minSdk 29
- `UnusedResources` × 2 — `R.color.chathilfe_surface`, `R.color.chathilfe_on_background`
- `MonochromeLauncherIcon` × 2 — adaptives Icon ohne `monochrome`-Tag
- `UseKtx` — `PermissionStatus.kt:54`
- `ClickableViewAccessibility` — `FloatingBubbleView.kt:50` (`onTouchEvent` ohne `performClick`)

Diese Lint-Funde sind separater Auftrag und hier nicht behoben.

### 4. Compile-Warnung (harmlos)
`PermissionStatus.kt:26`: `unsafeCheckOpNoThrow` ist deprecated. Keine Auswirkung auf den Build.

---

## Build-Ergebnis

| Befehl | Ergebnis |
|---|---|
| `./gradlew assembleDebug` | ✅ BUILD SUCCESSFUL |
| `./gradlew test` | ✅ BUILD SUCCESSFUL, aber NO-SOURCE (keine Unit-Tests vorhanden) |
| `./gradlew lintDebug` | ❌ BUILD FAILED (1 Error + 9 Warnings, siehe oben) |

### APK
- Pfad: `app/build/outputs/apk/debug/app-debug.apk`
- Größe: ca. 31 MB
- Verifiziert via `aapt dump badging`:
  - `package=de.disaai.chathilfe`, `versionCode=1`, `versionName=0.1.0`
  - `minSdk=29`, `targetSdk=35`, `compileSdk=37`
  - Label: `ChatHilfe`

---

## Nicht validiert

- Kein Gerät angeschlossen (`adb devices` leer): `installDebug`, App-Start, Settings-Screen, Overlay-Berechtigung, Nutzungszugriff, Overlay-Toggle und Crash-Freiheit konnten nicht getestet werden.
- Keine Unit-Tests vorhanden, daher ist `test` inhaltsleer.

---

## Geänderte Dateien in diesem Lauf

- `app/build.gradle.kts` — `compileSdk` 36 → 37
- `docs/DECISIONS.md` — D-004 aktualisiert + Begründung
- `README.md` — Toolchain-Tabelle aktualisiert
- `docs/ANDROID_CONSTRAINTS.md` — SDK-Strategie-Tabelle aktualisiert
- `docs/BUILD_VALIDATION_REPORT.md` — neu (diese Datei)

`local.properties` ist lokal und bleibt ignoriert; kein echter API-Key wurde committed.

---

## Empfohlene nächste Schritte

1. Lint-Error in `SettingsScreen.kt:136` beheben (Compose-konforme Ressourcen-Abfrage), damit `lintDebug` grün wird; danach die Warnings aufräumen.
2. Gerät anschließen und `./gradlew installDebug` sowie einen manuellen Smoke-Test durchführen.
3. Für echte KI-Aufrufe später den echten `OPENROUTER_API_KEY` nur lokal in `local.properties` eintragen.
