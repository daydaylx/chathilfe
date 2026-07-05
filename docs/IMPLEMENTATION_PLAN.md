# IMPLEMENTATION_PLAN.md — ChatHilfe MVP

## Zweck

Dieses Dokument definiert die Umsetzungsreihenfolge für den MVP.

Agenten sollen phasenweise arbeiten und keine späteren Features vorziehen.

---

## Vor jeder Umsetzung lesen

1. `AGENTS.md`
2. `README.md`
3. `Konzept.md`
4. `docs/DECISIONS.md`
5. `docs/ARCHITECTURE.md`
6. `docs/ANDROID_CONSTRAINTS.md`
7. dieses Dokument

Task-spezifisch zusätzlich:

- KI/Prompt: `docs/PROMPTS.md`
- UI: `docs/UI_UX_SPEC.md`
- Datenschutz: `docs/PRIVACY_SECURITY.md`
- Tests: `docs/TEST_PLAN.md`

---

## Phase 0 — Projektprüfung und Entscheidungs-Gate

Ziel: Repo verstehen und offene technische Grundsatzentscheidungen vor Code klären.

Bereits entschieden:

- Provider: OpenRouter im MVP
- API-Key: lokal beim Build einbetten, niemals committen
- Overlay-Laufzeit: Foreground Service aus sichtbarer Nutzeraktion
- Overlay-UI: klassische Android Views
- `applicationId`: `de.disaai.chathilfe`
- SDK-Basis: `compileSdk 36`, `targetSdk 35`, `minSdk 29`
- Clipboard-Fallback: manuelles Einfügen ins Panel

Vor Code-Scaffold noch zu pinnen:

- Android Gradle Plugin Version
- Gradle Wrapper Version
- Kotlin Version
- Compose BOM Version
- Mechanismus für lokalen API-Key: `local.properties`, `secrets.properties` oder Environment-Variable
- `.gitignore` für lokale Secret-Dateien

Vor Phase 7 zu pinnen:

- konkretes OpenRouter-Default-Modell in `AiConfig`

Nicht tun:

- kein Code generieren, bevor die Projektbasis klar ist
- keine Dependencies hinzufügen, bevor Toolchain festgelegt ist
- keine Architekturentscheidungen ohne Update von `docs/DECISIONS.md`
- keinen echten API-Key ins Repo schreiben

Akzeptanz:

- Entscheidungen sind in `docs/DECISIONS.md` aktuell
- kein Widerspruch zu `AGENTS.md` oder `docs/ANDROID_CONSTRAINTS.md`

---

## Phase 1 — Android-Projektbasis

Ziel: minimale App baut und startet.

Aufgaben:

- Kotlin-Android-Projekt anlegen
- Jetpack Compose für MainActivity einrichten
- Package-Struktur vorbereiten
- App-Name: ChatHilfe
- `applicationId`: `de.disaai.chathilfe`
- `compileSdk 36`, `targetSdk 35`, `minSdk 29`
- aktuelle kompatible AGP-/Gradle-/Kotlin-/Compose-Versionen pinnen
- lokalen API-Key-Mechanismus vorbereiten, aber nur mit Platzhalter dokumentieren
- `.gitignore` für lokale Secret-Dateien ergänzen
- dunkles Basis-Theme
- Gradle-Setup minimal halten

Akzeptanz:

- `./gradlew assembleDebug` erfolgreich
- App startet
- MainActivity zeigt einfache Setup-Seite
- kein echter API-Key im Repo

Nicht tun:

- kein Overlay
- keine KI
- kein Service
- keine API-Key-Eingabe im UI

---

## Phase 2 — Settings und Berechtigungen

Ziel: Nutzer sieht, was fehlt.

Aufgaben:

- `SettingsScreen`
- `PermissionStatus`
- Overlay-Berechtigung prüfen
- Usage Access prüfen
- Foreground-Service-/Notification-Anforderungen als Status vorbereiten
- Einstellungsseiten öffnen
- `SettingsStore` mit DataStore für UI-/Overlay-Einstellungen
- Overlay aktiv/inaktiv speichern
- bevorzugten Ton und Position speichern

Akzeptanz:

- Status wird korrekt angezeigt
- kein API-Key-Feld im UI
- keine API-Keys werden geloggt
- Status aktualisiert sich nach Rückkehr aus Android-Einstellungen

---

## Phase 3 — Manuelles Overlay

Ziel: Floating Button manuell testbar.

Aufgaben:

- `OverlayController`
- `OverlayService` als Foreground Service
- `FloatingBubbleView` als klassische Android View
- `TYPE_APPLICATION_OVERLAY`
- Service-Start nur aus sichtbarer Nutzeraktion
- `startForeground()` zeitnah aufrufen
- Button anzeigen/entfernen
- Dragging
- Tap-vs-Drag trennen
- Position speichern
- sauberes Entfernen bei Stop

Akzeptanz:

- Button erscheint über Apps
- Button ist verschiebbar
- keine doppelten Buttons
- Button kann deaktiviert werden
- Service-Notification ist verständlich

Validierung:

- Gerätetest Pflicht

Nicht tun:

- keine WhatsApp-Erkennung
- keine KI
- kein Clipboard

---

## Phase 4 — WhatsApp-Erkennung

Ziel: Bubble nur bei WhatsApp.

Aufgaben:

- `ForegroundAppDetector`
- `UsageStatsManager.queryEvents()`
- `com.whatsapp` erkennen
- Polling 1000 ms
- Button zeigen/verstecken
- fehlenden Usage Access sauber melden
- Foreground-Service-Typ und Manifest final prüfen

Akzeptanz:

- WhatsApp öffnen → Button erscheint
- WhatsApp verlassen → Button verschwindet
- keine doppelten Views

Validierung:

- Gerätetest Pflicht
- mehrfacher App-Wechsel
- Sperren/Entsperren kurz prüfen

Nicht tun:

- kein Accessibility-Fallback
- kein Notification Listener

---

## Phase 5 — ReplyPanel ohne KI

Ziel: UI funktioniert mit Dummy-Daten.

Aufgaben:

- `ReplyPanelView` als klassische Android View
- Modi: Antworten, Formulieren, Umschreiben
- Ton-Auswahl
- Eingabefelder
- Clipboard-Vorschau nur nach Panel-Öffnung oder explizitem Tap
- Clipboard erst nach Bestätigung übernehmen
- manuellen Einfügen-Fallback anbieten
- Dummy-Vorschläge anzeigen
- Kopieren testen

Akzeptanz:

- Panel öffnet/schließt
- Modus und Ton wählbar
- Clipboard wird nicht heimlich gelesen
- manuelles Einfügen funktioniert auch ohne Clipboard-Zugriff
- Vorschläge kopierbar

Validierung:

- Gerätetest Pflicht, besonders Clipboard-Fokusverhalten

---

## Phase 6 — PromptBuilder und Parser

Ziel: KI-Logik ohne Provider testen.

Aufgaben:

- `PromptBuilder`
- Prompts aus `docs/PROMPTS.md`
- `AiResponseParser`
- Parser tolerant bauen
- Unit-Tests für Builder/Parser

Akzeptanz:

- jeder Modus erzeugt passenden Prompt
- Parser extrahiert Vorschläge robust
- kein Crash bei schlechter Modellantwort

Validierung:

- `./gradlew test` für relevante Tests

---

## Phase 7 — KI-Anbindung

Ziel: echte Vorschläge erzeugen.

Aufgaben:

- konkretes OpenRouter-Default-Modell nach aktueller Verfügbarkeit in `AiConfig` pinnen
- API-Key aus lokaler Build-Time-Konfiguration lesen
- `AiClient`
- OpenRouter als einziger Provider
- Ladezustand
- Fehlerbehandlung
- Antwort parsen
- 3 Vorschläge anzeigen

Akzeptanz:

- fehlender Build-Time-Key → klarer Build- oder Laufzeitfehler ohne Secret-Ausgabe
- kein Internet → klare Meldung
- gültige Anfrage → Vorschläge
- keine Nutzertexte/API-Keys in Logs
- Anfrage nur nach Button-Klick
- echter API-Key steht nicht im Repo

Nicht tun:

- kein Multi-Provider-System
- kein Verlauf
- keine automatische Anfrage
- keine API-Key-Eingabe im UI

---

## Phase 8 — Stabilisierung auf Gerät

Ziel: private APK ist real nutzbar.

Aufgaben:

- APK bauen
- prüfen, dass API-Key nur lokal eingebettet wurde
- Samsung S25 testen
- Overlay Permission testen
- Usage Access testen
- Foreground Service testen
- WhatsApp-Appwechsel testen
- ReplyPanel testen
- Clipboard und manuellen Fallback testen
- Sperren/Entsperren testen
- Internetfehler testen
- fehlende Berechtigungen testen
- Akkuoptimierung dokumentieren

Akzeptanz:

- `docs/TEST_PLAN.md` weitgehend erfüllt
- keine verbotenen Permissions
- kein Accessibility Service
- README aktualisiert

---

## Phase 9 — Übergabe

Ziel: Repo ist für weitere Arbeit nutzbar.

Aufgaben:

- README aktualisieren
- Build-Befehle ergänzen
- lokale Secret-Konfiguration mit Platzhalter dokumentieren
- bekannte Einschränkungen dokumentieren
- Teststatus dokumentieren
- `docs/DECISIONS.md` aktualisieren, falls technische Entscheidungen während der Umsetzung geändert wurden

Agenten-Abschlussformat steht in `AGENTS.md`.

---

## Priorität

Muss zuerst funktionieren:

1. App startet
2. Berechtigungen sichtbar
3. Foreground Service startet aus Nutzeraktion
4. Overlay manuell testbar
5. Button nur bei WhatsApp
6. Panel öffnet
7. Clipboard bewusst übernehmen oder manuell einfügen
8. KI-Vorschläge über lokalen Build-Time-Key erzeugen
9. Kopieren

Darf warten:

- Icon
- Animationen
- WhatsApp Business
- Modellauswahl
- Verlauf
- Play Store

Nicht bauen:

- Auto-Senden
- Auto-Einfügen
- Chat-Auslesen
- Accessibility
