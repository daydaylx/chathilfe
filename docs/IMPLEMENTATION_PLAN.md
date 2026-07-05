# IMPLEMENTATION_PLAN.md — ChatHilfe MVP

## Zweck

Dieses Dokument definiert die Umsetzungsreihenfolge für den MVP.

Agenten sollen phasenweise arbeiten und keine späteren Features vorziehen.

---

## Vor jeder Umsetzung lesen

1. `AGENTS.md`
2. `README.md`
3. `Konzept.md`
4. `docs/ARCHITECTURE.md`
5. `docs/ANDROID_CONSTRAINTS.md`
6. dieses Dokument

Task-spezifisch zusätzlich:

- KI/Prompt: `docs/PROMPTS.md`
- UI: `docs/UI_UX_SPEC.md`
- Datenschutz: `docs/PRIVACY_SECURITY.md`
- Tests: `docs/TEST_PLAN.md`

---

## Phase 0 — Projektprüfung

Ziel: Repo verstehen.

Aufgaben:

- vorhandene Dateien lesen
- prüfen, ob Android-/Gradle-Projekt existiert
- Branch und Status prüfen
- keine Nutzeränderungen überschreiben

Nicht tun:

- kein Code generieren, bevor die Projektbasis klar ist
- keine Dependencies hinzufügen

---

## Phase 1 — Android-Projektbasis

Ziel: minimale App baut und startet.

Aufgaben:

- Kotlin-Android-Projekt anlegen
- Jetpack Compose für MainActivity einrichten
- Package-Struktur vorbereiten
- App-Name: ChatHilfe
- dunkles Basis-Theme
- Gradle-Setup minimal halten

Akzeptanz:

- `./gradlew assembleDebug` erfolgreich
- App startet
- MainActivity zeigt einfache Setup-Seite

Nicht tun:

- kein Overlay
- keine KI
- kein Service

---

## Phase 2 — Settings und Berechtigungen

Ziel: Nutzer sieht, was fehlt.

Aufgaben:

- `SettingsScreen`
- `PermissionStatus`
- Overlay-Berechtigung prüfen
- Usage Access prüfen
- Einstellungsseiten öffnen
- `SettingsStore` mit DataStore
- API-Key speichern
- Overlay aktiv/inaktiv speichern

Akzeptanz:

- Status wird korrekt angezeigt
- API-Key kann gespeichert werden
- keine API-Keys werden geloggt

---

## Phase 3 — Manuelles Overlay

Ziel: Floating Button manuell testbar.

Aufgaben:

- `OverlayController`
- `OverlayService` oder Overlay-Runtime
- `FloatingBubbleView`
- `TYPE_APPLICATION_OVERLAY`
- Button anzeigen/entfernen
- Dragging
- Tap-vs-Drag trennen
- Position speichern

Akzeptanz:

- Button erscheint über Apps
- Button ist verschiebbar
- keine doppelten Buttons
- Button kann deaktiviert werden

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

Akzeptanz:

- WhatsApp öffnen → Button erscheint
- WhatsApp verlassen → Button verschwindet
- keine doppelten Views

Nicht tun:

- kein Accessibility-Fallback
- kein Notification Listener

---

## Phase 5 — ReplyPanel ohne KI

Ziel: UI funktioniert mit Dummy-Daten.

Aufgaben:

- `ReplyPanelView`
- Modi: Antworten, Formulieren, Umschreiben
- Ton-Auswahl
- Eingabefelder
- Clipboard-Vorschau nur nach Panel-Öffnung
- Clipboard erst nach Bestätigung übernehmen
- Dummy-Vorschläge anzeigen
- Kopieren testen

Akzeptanz:

- Panel öffnet/schließt
- Modus und Ton wählbar
- Clipboard wird nicht heimlich gelesen
- Vorschläge kopierbar

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

---

## Phase 7 — KI-Anbindung

Ziel: echte Vorschläge erzeugen.

Aufgaben:

- `AiClient`
- ein Provider, z. B. OpenRouter oder OpenAI
- API-Key aus DataStore
- Ladezustand
- Fehlerbehandlung
- Antwort parsen
- 3 Vorschläge anzeigen

Akzeptanz:

- fehlender API-Key → klare Meldung
- kein Internet → klare Meldung
- gültige Anfrage → Vorschläge
- keine Nutzertexte/API-Keys in Logs

Nicht tun:

- kein Multi-Provider-System
- kein Verlauf
- keine automatische Anfrage

---

## Phase 8 — Stabilisierung auf Gerät

Ziel: private APK ist real nutzbar.

Aufgaben:

- APK bauen
- Samsung S25 testen
- Overlay Permission testen
- Usage Access testen
- WhatsApp-Appwechsel testen
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
- bekannte Einschränkungen dokumentieren
- Teststatus dokumentieren

Agenten-Abschlussformat steht in `AGENTS.md`.

---

## Priorität

Muss zuerst funktionieren:

1. App startet
2. Berechtigungen sichtbar
3. Overlay manuell testbar
4. Button nur bei WhatsApp
5. Panel öffnet
6. Clipboard bewusst übernehmen
7. KI-Vorschläge erzeugen
8. Kopieren

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
