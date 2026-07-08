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
- Eingefügte WhatsApp-Dialogblöcke: `docs/WHATSAPP_DIALOG_CONTEXT.md`

---

## Gerätetest-Strategie

Gerätetests werden für diesen Projektablauf gebündelt in Phase 8 durchgeführt.

Das ist nicht die risikoärmste technische Variante, aber die gewünschte Arbeitsweise für dieses Projekt.

Regeln:

- Nach Phase 3, 4 und 5 sind Gerätetests empfohlen, aber nicht blockierend.
- Agenten dürfen mit der nächsten Phase fortfahren, wenn Code- und Build-Prüfungen soweit möglich sauber sind.
- Alle nicht auf Gerät geprüften Punkte bleiben bis Phase 8 als Risiko offen.
- Erfolgreiche Gerätetests dürfen erst behauptet werden, wenn sie wirklich durchgeführt wurden.

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
- Gerätetest-Strategie: gebündelte Gerätevalidierung in Phase 8

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

Ziel: Floating Button manuell testbar im Code, echter Gerätetest folgt gebündelt in Phase 8.

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

- Button-Logik ist implementiert
- Drag/Tap-Logik ist implementiert
- doppelte Views werden defensiv verhindert
- Button kann deaktiviert werden
- Service-Notification ist verständlich vorbereitet

Validierung:

- Build-/Codeprüfung soweit lokal möglich
- Gerätetest empfohlen, aber nicht blockierend
- finaler Gerätetest in Phase 8

Nicht tun:

- keine WhatsApp-Erkennung
- keine KI
- kein Clipboard

---

## Phase 4 — WhatsApp-Erkennung

Ziel: Bubble nur bei WhatsApp; echter Gerätetest folgt gebündelt in Phase 8.

Aufgaben:

- `ForegroundAppDetector`
- `UsageStatsManager.queryEvents()`
- `com.whatsapp` erkennen
- Polling 1000 ms
- Button zeigen/verstecken
- fehlenden Usage Access sauber melden
- Foreground-Service-Typ und Manifest final prüfen

Akzeptanz:

- WhatsApp-Erkennung ist implementiert
- Button-Sichtbarkeit wird an erkannte Vordergrund-App gekoppelt
- fehlender Usage Access wird sauber behandelt
- keine doppelten Views in der Logik

Validierung:

- Build-/Codeprüfung soweit lokal möglich
- Gerätetest empfohlen, aber nicht blockierend
- finaler Gerätetest in Phase 8

Nicht tun:

- kein Accessibility-Fallback
- kein Notification Listener

---

## Phase 5 — Input-Bar und Result-Panel ohne KI

Ziel: Die Overlay-UI funktioniert mit Dummy-Daten und folgt `docs/VISUAL_SCOPE.md`; echter Gerätetest folgt gebündelt in Phase 8.

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

- Build-/Codeprüfung soweit lokal möglich
- Gerätetest empfohlen, aber nicht blockierend
- finaler Gerätetest in Phase 8

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

## Phase 7.5 — Antwortqualitäts-Kalibrierung (Audit)

Quelle: `docs/RESPONSE_QUALITY_AUDIT.md`. Ziel: Antworten wirken weniger
geschäftlich/künstlich, sondern wie echte WhatsApp-Nachrichten.

Aufgaben (Stand 2026-07-08):

- korrekte Modus-Verdrahtung (`REPLY→copiedMessage`, `COMPOSE→userIntent`) — bereits durch Overlay-Redesign erledigt
- Trennung kopierte Nachricht vs. Nutzerabsicht — bereits erledigt (Feld = `copiedMessage`, Antwort-Chips = `userIntent`)
- härtere WhatsApp-Stilregeln in `PromptBuilder`/`docs/PROMPTS.md` (1–2 Sätze, keine Floskeln, keine Therapiesprache)
- feste App-Stimme (Persona) als statische Prompt-Vorgabe — entschieden in `docs/DECISIONS.md` D-013, dokumentiert in `docs/PRIVACY_SECURITY.md`
- Antwortqualitäts-Testset in `docs/TEST_PLAN.md` (manuell, ~18 Fälle + Bewertungsraster)

Akzeptanz:

- PromptBuilder ↔ `docs/PROMPTS.md` konsistent
- Persona ist hart codiert und nirgendwo gespeichert/gelernt
- bestehende `PromptBuilder`-Unit-Tests grün, Persona-Assertion vorhanden
- Modell bleibt `anthropic/claude-sonnet-5` (D-012); A/B-Modelltest ist offener Punkt

Nicht tun:

- kein Modellwechsel vor Testset/A/B
- kein speicherbares Stil-/Personenprofil
- kein automatisiertes A/B-Framework

---

## Phase 7.6 — Eingefügte WhatsApp-Dialogblöcke als Kontext

Quelle: `docs/WHATSAPP_DIALOG_CONTEXT.md` und Issue #19.

Ziel: Wenn der Nutzer mehrere WhatsApp-Nachrichten bewusst einfügt, soll die App den Text als temporären Dialogauszug erkennen und den aktuellen Antwortanlass besser bestimmen.

Aufgaben:

- `WhatsAppChatParser` als pure Kotlin-Komponente bauen
- WhatsApp-Zeilen im Muster `[Datum, Uhrzeit] Sprecher: Nachricht` erkennen
- mindestens zwei passende Zeilen verlangen, sonst Einzeltext-Fallback
- mehrzeilige Nachrichten tolerant behandeln
- `ParsedChatMessage` und `ParsedChatContext` ergänzen
- Sprecher nur heuristisch als `likelySelfSender` / `likelyOtherSender` markieren, keine harte Identität behaupten
- `ReplyRequest.conversationContext` optional ergänzen
- `PromptBuilder` um getrennte Kontextsektion erweitern
- `OverlayService.buildRequest()` so anbinden, dass die letzte relevante Nachricht des Gegenübers als Antwortanlass genutzt wird
- Unit-Tests für Parser, Fallback und PromptBuilder ergänzen

Akzeptanz:

- Dialogblock wird erkannt und strukturiert
- normale Einzeltexte funktionieren unverändert
- alter Verlauf wird nur als Kontext genutzt
- keine Chattexte, Namen oder Vorschläge werden gespeichert oder geloggt
- kein neues großes UI
- kein automatisches WhatsApp-Lesen
- kein Accessibility Service

Nicht tun:

- kein Chatverlauf-Import
- kein Verlauf/Gedächtnis
- kein Kontakt-/Beziehungsprofil
- kein Speichern von Chattexten

---

## Phase 8 — Stabilisierung und Gerätetest

Ziel: private APK ist real nutzbar.

Phase 8 ist der Sammelpunkt für echte Gerätetests.

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
- bewusst eingefügten WhatsApp-Dialogblock testen
- Sperren/Entsperren testen
- Internetfehler testen
- fehlende Berechtigungen testen
- Akkuoptimierung dokumentieren

Akzeptanz:

- `docs/TEST_PLAN.md` weitgehend erfüllt
- `docs/VISUAL_SCOPE.md` erfüllt
- echte Gerätetestergebnisse dokumentiert
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
4. Overlay manuell testbar im Code
5. Button-Logik an WhatsApp-Erkennung gekoppelt
6. InputBar öffnet kompakt
7. Text kann eingegeben oder eingefügt werden
8. ResultPanel zeigt Dummy-Vorschläge einzeln wechselbar
9. Retry-Chips funktionieren mit Dummy-Daten
10. KI-Vorschläge über lokalen Build-Time-Key erzeugen
11. Kopieren des sichtbaren Vorschlags
12. gebündelter Gerätetest in Phase 8

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
