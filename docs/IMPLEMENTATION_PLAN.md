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
- Visueller Scope: `docs/VISUAL_SCOPE.md`
- Datenschutz: `docs/PRIVACY_SECURITY.md`
- Tests: `docs/TEST_PLAN.md`

---

## Phase 0 — Projektprüfung und Entscheidungs-Gate

Ziel: Repo verstehen und offene technische Grundsatzentscheidungen vor Code klären.

Bereits entschieden:

- Provider: OpenRouter im MVP
- Modellstrategie: genau ein OpenRouter-Default-Modell im MVP; Modellrouting ist Post-MVP
- API-Key: lokal beim Build einbetten, niemals committen, keine API-Key-Eingabe im UI
- Overlay-Laufzeit: Foreground Service aus sichtbarer Nutzeraktion
- Overlay-UI: klassische Android Views
- Visueller Scope: schmaler Eingabebalken zuerst, Ergebnis-Panel erst nach KI-Antwort
- `applicationId`: `de.disaai.chathilfe`
- SDK-Basis: `compileSdk 36`, `targetSdk 35`, `minSdk 29`
- Clipboard-Fallback: manuelles Eingeben oder Einfügen im Overlay
- Retry: kompakter Retry-Bereich nach Ergebnissen, temporäre `RetryInstruction`, keine Speicherung

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
- keine API-Key-Eingabe in der App bauen
- kein Modellrouting, Multi-Provider-System oder Provider-Fallback bauen
- kein Verlauf, Gedächtnis, Profil oder Stiltraining bauen
- kein großes Formular als Startzustand bauen
- keine drei Vorschlagskarten untereinander als Standardansicht bauen

Akzeptanz:

- Entscheidungen sind in `docs/DECISIONS.md` aktuell
- kein Widerspruch zu `AGENTS.md`, `docs/VISUAL_SCOPE.md` oder `docs/ANDROID_CONSTRAINTS.md`

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
- BuildConfig-/Build-Time-Konfiguration für `OPENROUTER_API_KEY` vorbereiten
- dunkles Basis-Theme
- Gradle-Setup minimal halten

Akzeptanz:

- `./gradlew assembleDebug` erfolgreich
- App startet
- MainActivity zeigt einfache Setup-Seite
- kein echter API-Key im Repo
- keine API-Key-Eingabe im UI

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
- API-Key-Konfigurationsstatus aus Build-Time-Konfiguration anzeigen, falls sinnvoll
- Einstellungsseiten öffnen
- `SettingsStore` mit DataStore für UI-/Overlay-Einstellungen
- Overlay aktiv/inaktiv speichern
- bevorzugten Ton und Position speichern

Akzeptanz:

- Status wird korrekt angezeigt
- kein API-Key-Feld im UI
- kein API-Key in DataStore
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

- WhatsApp öffnen -> Button erscheint
- WhatsApp verlassen -> Button verschwindet
- keine doppelten Views

Validierung:

- Gerätetest Pflicht
- mehrfacher App-Wechsel
- Sperren/Entsperren kurz prüfen

Nicht tun:

- kein Accessibility-Fallback
- kein Notification Listener

---

## Phase 5 — Input-Bar und Result-Panel ohne KI

Ziel: Die Overlay-UI funktioniert mit Dummy-Daten und folgt `docs/VISUAL_SCOPE.md`.

Aufgaben:

- `InputBarView` als klassische Android View bauen
- Ton-/Stil-Button links im Eingabebalken bauen
- kompaktes Texteingabefeld bauen
- Einfügen-Button bauen
- Start-Button bauen, aber nicht `Senden` nennen
- `ResultPanelView` als klassische Android View bauen
- Dummy-Vorschläge anzeigen
- genau einen sichtbaren Vorschlag anzeigen
- Vorschlagswechsel über einfache Pager-/Pfeilnavigation bauen
- Swipe optional vorbereiten oder später ergänzen
- Kopieren des sichtbaren Vorschlags testen
- Clipboard nur nach Panel-Öffnung oder explizitem Tap lesen
- manuellen Fallback anbieten
- kompakten Retry-Bereich nach Dummy-Ergebnissen anzeigen
- Retry-Chips global umsetzen: `Nochmal`, `Kürzer`, `Lockerer`, `Direkter`, `Sanfter`, `Klarer`, `Weniger künstlich`
- maximal 1-2 Retry-Chips gleichzeitig aktiv halten
- Retry-Auswahl beim Schließen oder nach neuer Anfrage verwerfen

Akzeptanz:

- Floating Button öffnet zuerst nur den schmalen Eingabebalken
- Eingabebalken verdeckt WhatsApp möglichst wenig
- kein großer Formularzustand beim Öffnen
- Ton, Text, Einfügen und Start sind direkt erreichbar
- Start-Button heißt nicht `Senden`
- ResultPanel erscheint erst nach Dummy-Vorschlägen
- immer nur ein Vorschlag sichtbar
- Nutzer erkennt, dass es 3 Vorschläge gibt
- Wechsel zwischen Vorschlägen funktioniert
- Kopieren kopiert den sichtbaren Vorschlag
- Clipboard wird nicht heimlich gelesen
- manuelles Eingeben funktioniert auch ohne Clipboard-Zugriff
- Retry-Bereich erscheint erst nach Vorschlägen
- Retry-Auswahl wird nicht gespeichert

Validierung:

- Gerätetest Pflicht, besonders Overlay-Größe, Clipboard-Fokusverhalten und Bedienbarkeit über WhatsApp

---

## Phase 6 — PromptBuilder und Parser

Ziel: KI-Logik ohne Provider testen.

Aufgaben:

- `PromptBuilder`
- Prompts aus `docs/PROMPTS.md`
- `RetryInstruction`-Mapping
- optionale Retry-Anweisung in Prompt einbauen
- `AiResponseParser`
- Parser tolerant bauen
- Unit-Tests für Builder/Parser/RetryInstruction

Akzeptanz:

- jeder Modus erzeugt passenden Prompt
- Prompt enthält Retry-Anweisung nur, wenn sie aktiv für diese Anfrage gesetzt wurde
- Retry-Anweisung wird im Prompt berücksichtigt, aber nicht als Profil gespeichert
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
- genau ein Default-Modell nutzen
- Ladezustand
- Fehlerbehandlung
- Antwort parsen
- 3 Vorschläge an das ResultPanel übergeben
- ResultPanel zeigt einen Vorschlag und erlaubt Wechsel zwischen 3 Vorschlägen
- Retry mit optionaler `RetryInstruction` als neue bewusste Anfrage unterstützen

Akzeptanz:

- fehlender Build-Time-Key -> klarer Build- oder Laufzeitfehler ohne Secret-Ausgabe
- kein Internet -> klare Meldung
- gültige Anfrage -> 3 Vorschläge
- Ergebnis-Panel zeigt nicht alle 3 Vorschläge untereinander
- Retry erzeugt neue Vorschläge und lässt bisherige Vorschläge bei Fehler sichtbar
- keine Nutzertexte/API-Keys/Retry-Anweisungen in Logs
- Anfrage nur nach Button-Klick oder bewusstem Retry
- echter API-Key steht nicht im Repo

Nicht tun:

- kein Multi-Provider-System
- kein Modellrouting
- kein automatisches Modell-Fallback
- kein Verlauf
- keine automatische Anfrage
- keine API-Key-Eingabe im UI
- kein Speichern von Vorschlägen oder Retry-Anweisungen

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
- InputBar testen
- ResultPanel testen
- Vorschlagswechsel testen
- Retry-Bereich testen
- Clipboard und manuellen Fallback testen
- Sperren/Entsperren testen
- Internetfehler testen
- fehlende Berechtigungen testen
- Akkuoptimierung dokumentieren

Akzeptanz:

- `docs/TEST_PLAN.md` weitgehend erfüllt
- `docs/VISUAL_SCOPE.md` erfüllt
- keine verbotenen Permissions
- kein Accessibility Service
- kein Verlauf, Gedächtnis, Profil, Stiltraining oder Analytics
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
6. InputBar öffnet kompakt
7. Text kann eingegeben oder eingefügt werden
8. ResultPanel zeigt Dummy-Vorschläge einzeln wechselbar
9. Retry-Chips funktionieren mit Dummy-Daten
10. KI-Vorschläge über lokalen Build-Time-Key erzeugen
11. Kopieren des sichtbaren Vorschlags

Darf warten:

- Icon
- Animationen
- echtes Swipe, falls Pfeilnavigation zuerst gebaut wird
- WhatsApp Business
- Modellauswahl
- weitere Retry-Chips
- Play Store

Nicht bauen:

- Auto-Senden
- Auto-Einfügen
- API-Key-Eingabe im UI
- Verlauf
- Gedächtnis
- Stiltraining
- Modellrouting
- großes Formular als Startzustand
- drei Vorschläge untereinander als Standardansicht
