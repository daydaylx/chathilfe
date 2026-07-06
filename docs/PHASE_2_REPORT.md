# PHASE_2_REPORT.md — ChatHilfe

## Status

Phase 2 ist code-seitig abgeschlossen. Der Build (`./gradlew assembleDebug`) konnte in dieser Umgebung nicht ausgeführt werden (siehe "Not validated").

---

## Scope

Abgedeckte Phase:

- Phase 2: Settings und Berechtigungen

Nicht Teil dieser Phase:

- Overlay
- Foreground Service
- WhatsApp-Erkennung
- Clipboard-Zugriff
- KI-Anbindung
- ReplyPanel
- API-Key-Eingabe im UI
- Accessibility Service

---

## Summary

Neues Package `settings/` mit `SettingsScreen`, `PermissionStatus` und `SettingsStore`. `MainActivity` zeigt jetzt direkt den Settings-Screen statt des Phase-1-Platzhaltertexts.

Der Screen zeigt fünf Statuskarten:

1. Overlay-Berechtigung (`Settings.canDrawOverlays`), Button zu `ACTION_MANAGE_OVERLAY_PERMISSION`
2. Nutzungszugriff (`AppOpsManager.unsafeCheckOpNoThrow(OPSTR_GET_USAGE_STATS, ...)`), Button zu `ACTION_USAGE_ACCESS_SETTINGS`
3. Hintergrund-Dienst/Benachrichtigung — reine Infokarte, kein Check, kein Button (kommt in Phase 3)
4. API-Key-Build-Konfiguration — zeigt nur "Konfiguriert"/"Noch nicht konfiguriert" anhand `BuildConfig.OPENROUTER_API_KEY != "replace_me_locally"`, ohne den Wert je auszugeben
5. Overlay aktiv/inaktiv — `Switch`, persistiert über DataStore, startet keinen Dienst

Permission-Status aktualisiert sich automatisch nach Rückkehr aus den Android-Einstellungen über einen `ON_RESUME`-Lifecycle-Observer.

---

## Files added / changed

Neu:

- `app/src/main/java/de/disaai/chathilfe/settings/PermissionStatus.kt`
- `app/src/main/java/de/disaai/chathilfe/settings/SettingsStore.kt`
- `app/src/main/java/de/disaai/chathilfe/settings/SettingsScreen.kt`

Geändert:

- `app/src/main/java/de/disaai/chathilfe/MainActivity.kt` (zeigt `SettingsScreen` statt Platzhalter)
- `app/src/main/AndroidManifest.xml` (neue Berechtigungen, `tools`-Namespace)
- `app/build.gradle.kts` (neue Dependencies)
- `app/src/main/res/values/strings.xml` (neue Settings-Strings, Platzhalter-String entfernt)

---

## Permissions added

- `android.permission.SYSTEM_ALERT_WINDOW`
- `android.permission.PACKAGE_USAGE_STATS` (mit `tools:ignore="ProtectedPermissions"`)

Bewusst nicht ergänzt: `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS` — kein Service/keine Notification existiert in Phase 2; das ist für Phase 3 vorgesehen.

---

## Dependencies added

- `androidx.datastore:datastore-preferences:1.2.1` — Basis für `SettingsStore`
- `androidx.lifecycle:lifecycle-runtime-compose:2.11.0` — liefert `androidx.lifecycle.compose.LocalLifecycleOwner` für den `ON_RESUME`-Observer

Bewusst nicht ergänzt: `lifecycle-viewmodel-compose` — ein einzelner Screen ohne Navigation kommt mit `Flow.collectAsState()` direkt aus `SettingsStore` aus.

---

## API-Key handling

Unverändert gegenüber Phase 1: Build-Time-Mechanismus über `local.properties` → `BuildConfig.OPENROUTER_API_KEY`. `SettingsStore` speichert keinen API-Key. Die UI zeigt nur einen Konfigurationsstatus, kein Eingabefeld, keine Ausgabe des Werts.

---

## Scope validation

Nicht vorgezogen wurden:

- kein Overlay, kein `WindowManager`
- kein Foreground Service, keine `FOREGROUND_SERVICE`/`POST_NOTIFICATIONS`-Berechtigung
- keine WhatsApp-spezifische Erkennung (nur generischer, OS-weiter Nutzungszugriffs-Check)
- kein Clipboard-Zugriff
- keine KI-Anbindung
- kein ReplyPanel
- keine API-Key-Eingabe im UI
- kein Accessibility Service

---

## Validation

Durch Code-Review bestätigt:

- `settings/`-Package mit den drei geplanten Dateien existiert
- Fünf Statuskarten sind im `SettingsScreen` vorhanden
- Permission-Checks nutzen Standard-Android-APIs (`Settings.canDrawOverlays`, `AppOpsManager.unsafeCheckOpNoThrow`)
- Intents zu den korrekten System-Settings-Screens sind vorhanden, mit `ActivityNotFoundException`-Fallback
- `SettingsStore` referenziert an keiner Stelle `BuildConfig` oder einen API-Key
- Manifest enthält nur die drei geplanten Berechtigungen (`INTERNET`, `SYSTEM_ALERT_WINDOW`, `PACKAGE_USAGE_STATS`)
- `git diff` betrifft ausschließlich die geplanten Dateien
- `grep` nach `OPENROUTER_API_KEY` im Diff zeigt nur den Vergleich gegen den Platzhalter, keine Ausgabe/Logging

---

## Not validated

Nicht validiert in dieser Umgebung:

- `./gradlew assembleDebug` — Gradle-Wrapper konnte die Gradle-Distribution nicht herunterladen (Egress-Policy dieser Session blockiert den Downloadhost mit HTTP 403); kein lokaler Android SDK und kein `kotlinc` in der Umgebung vorhanden, um alternativ zu kompilieren
- `./gradlew test` / `./gradlew lint` — aus demselben Grund nicht ausführbar
- kein manueller Gerätetest (Berechtigung erteilen/entziehen und Statuswechsel beobachten)
- kein Installationstest der APK

Diese Punkte müssen vor Abschluss von Phase 2 einmal lokal mit Android SDK nachgeholt werden. Bis dahin gilt Phase 2 als code-seitig fertig, aber build-seitig unbestätigt.

---

## Known risks

- `androidx.datastore:datastore-preferences:1.2.1` sollte vor dem lokalen Build gegen die aktuelle Release-Seite geprüft werden.
- `AppOpsManager.unsafeCheckOpNoThrow` prüft nur den App-Op-Grant; ein sehr enges Zeitfenster bei `UsageStatsManager`-Abfragen ist damit nicht erkennbar — für die reine Statusanzeige in Phase 2 ausreichend.
- `ACTION_USAGE_ACCESS_SETTINGS` unterstützt anders als `ACTION_MANAGE_OVERLAY_PERMISSION` kein zuverlässiges Deep-Linking auf die eigene App über alle OEMs hinweg; das ist so vorgesehen und keine Lücke.
- Manche OEM-Skins könnten diese Settings-Screens abweichend benennen oder in Einzelfällen nicht auflösen; deshalb ist `startActivity` defensiv mit `try`/`catch` abgesichert.

---

## Next sensible step

Nächster Schritt ist Phase 3 (Overlay, `OverlayService`, `FloatingBubbleView`) gemäß `docs/IMPLEMENTATION_PLAN.md`.

Davor sollte einmal lokal mit Android SDK geprüft werden:

```bash
./gradlew assembleDebug
```

Optional danach:

```bash
./gradlew test
./gradlew lint
```
