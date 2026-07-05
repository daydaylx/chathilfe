# PHASE_0_1_REPORT.md — ChatHilfe

## Status

Phase 0 und Phase 1 sind code-seitig abgeschlossen.

Dieses Dokument hält fest, was umgesetzt wurde, was geprüft wurde und was noch nicht validiert ist.

---

## Scope

Abgedeckte Phasen:

- Phase 0: Projektprüfung und Entscheidungs-Gate
- Phase 1: Android-Projektbasis

Nicht Teil dieser Phasen:

- Overlay
- Foreground Service
- WhatsApp-Erkennung
- Clipboard-Zugriff
- KI-Anbindung
- ReplyPanel
- API-Key-Eingabe im UI

---

## Summary

Umgesetzt wurde eine minimale native Android-Projektbasis für ChatHilfe.

Vorhanden sind:

- Gradle-Projekt mit `:app`-Modul
- Kotlin-/Android-App-Setup
- Jetpack Compose MainActivity
- dunkles Basis-Theme
- App-Name `ChatHilfe`
- `applicationId` `de.disaai.chathilfe`
- Build-Time-Platzhalter für OpenRouter-Key
- `.gitignore` für lokale Secret-Dateien
- Gradle Wrapper

---

## Toolchain chosen

| Komponente | Version |
|---|---|
| Android Gradle Plugin | 9.2.0 |
| Gradle Wrapper | 9.6.1 |
| Kotlin / Compose Compiler Plugin | 2.4.0 |
| Compose BOM | 2026.06.01 |
| Java Target | 17 |
| `compileSdk` | 36 |
| `targetSdk` | 35 |
| `minSdk` | 29 |
| `applicationId` | `de.disaai.chathilfe` |
| App-Name | `ChatHilfe` |

Die Toolchain ist in `docs/DECISIONS.md` dokumentiert.

---

## Files added / relevant files

Projektbasis:

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `gradlew`
- `gradle/wrapper/gradle-wrapper.properties`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`

App-Code:

- `app/src/main/java/de/disaai/chathilfe/MainActivity.kt`
- `app/src/main/java/de/disaai/chathilfe/ui/theme/Theme.kt`
- `app/src/main/java/de/disaai/chathilfe/ui/theme/Color.kt`
- `app/src/main/java/de/disaai/chathilfe/ui/theme/Type.kt`

Ressourcen:

- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/styles.xml`
- Launcher-Icon-Ressourcen

Security / lokale Dateien:

- `.gitignore`

---

## API-Key handling

Der OpenRouter-Key wird nicht im Repo gespeichert.

Aktueller Mechanismus:

```text
local.properties
↓
OPENROUTER_API_KEY
↓
BuildConfig.OPENROUTER_API_KEY
```

Aktueller Platzhalter:

```text
replace_me_locally
```

Wichtig:

- kein echter Key im Repo
- kein API-Key-Feld im UI
- kein API-Key in DataStore
- lokale Secret-Dateien sind in `.gitignore`

---

## Scope validation

Nicht vorgezogen wurden:

- kein Overlay
- kein Foreground Service
- keine WhatsApp-Erkennung
- kein Clipboard-Zugriff
- keine KI-Anbindung
- kein ReplyPanel
- keine API-Key-Eingabe im UI
- kein Accessibility Service

Das entspricht dem Phase-1-Scope.

---

## Validation

Über GitHub-Dateiprüfung bestätigt:

- Android-Projektstruktur existiert
- `settings.gradle.kts` bindet `:app` ein
- App-Modul setzt `applicationId`, `compileSdk`, `targetSdk` und `minSdk` korrekt
- Compose ist aktiviert
- BuildConfig ist aktiviert
- MainActivity ist vorhanden
- App-Name ist gesetzt
- dunkles Theme ist vorhanden
- `.gitignore` schützt lokale Secret-Dateien
- Gradle Wrapper ist vorhanden

---

## Not validated

Nicht validiert in dieser Prüfung:

- `./gradlew assembleDebug` wurde nicht durch diese Prüfung ausgeführt
- `./gradlew test` wurde nicht durch diese Prüfung ausgeführt
- `./gradlew lint` wurde nicht durch diese Prüfung ausgeführt
- keine CI-Checks sichtbar
- keine Installation auf echtem Gerät geprüft
- kein Android-Studio-Sync geprüft

Ein Agent darf diese Punkte erst als bestanden markieren, wenn sie tatsächlich ausgeführt wurden.

---

## Known risks

- Toolchain-Versionen sind sehr aktuell; lokale Android-Studio-/JDK-/Gradle-Kompatibilität muss beim Build bestätigt werden.
- Es gibt aktuell keinen CI-Workflow, der Builds automatisch prüft.
- Build-Time-Key ist für private APK bequem, aber in einer APK nicht wirklich geheim.
- `replace_me_locally` darf nie als produktiver Key missverstanden werden.

---

## Next sensible step

Nächster Schritt ist Phase 2:

- `SettingsScreen`
- `PermissionStatus`
- Overlay-Berechtigung prüfen
- Usage Access prüfen
- Foreground-Service-/Notification-Anforderungen vorbereiten
- DataStore für UI-/Overlay-Einstellungen
- keine API-Key-Eingabe im UI

Vor Phase 2 sollte einmal lokal geprüft werden:

```bash
./gradlew assembleDebug
```

Optional danach:

```bash
./gradlew test
./gradlew lint
```
