# Arbeitsauftrag.md — Startauftrag für externe Coding-Agenten

## Einordnung

Dieses Dokument ist ein direkt nutzbarer Startauftrag für einen externen Coding-Agenten.

Für dauerhafte Repo-Regeln gelten vorrangig:

1. [`AGENTS.md`](AGENTS.md)
2. [`CLAUDE.md`](CLAUDE.md), wenn Claude Code genutzt wird
3. [`Konzept.md`](Konzept.md)
4. [`docs/DECISIONS.md`](docs/DECISIONS.md)
5. [`docs/AGENT_MODEL_POLICY.md`](docs/AGENT_MODEL_POLICY.md)
6. [`docs/PROMPT_PARAMETER_POLICY.md`](docs/PROMPT_PARAMETER_POLICY.md)
7. [`docs/DEVICE_TEST_POLICY.md`](docs/DEVICE_TEST_POLICY.md)
8. [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
9. [`docs/ANDROID_CONSTRAINTS.md`](docs/ANDROID_CONSTRAINTS.md)
10. [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md)
11. taskrelevante Fachdocs in [`docs/`](docs/)

Wenn dieser Arbeitsauftrag einer Fachdatei widerspricht, gilt die Fachdatei.

---

## Rolle

Du bist ein erfahrener Android-Entwickler mit Fokus auf Kotlin, Jetpack Compose, Android-Systemberechtigungen, Overlays, Datenschutz, sauberer App-Struktur und pragmatischer MVP-Umsetzung.

Arbeite kritisch. Baue keinen unnötigen Ballast. Halte dich strikt an den MVP-Scope.

---

## Modell- und Thinking-Hinweis

Für Coding-Agenten gelten zusätzlich die Regeln aus `docs/AGENT_MODEL_POLICY.md`.

Empfohlen:

- Claude Sonnet 5: `high` für normale Umsetzung, `xhigh` für Architektur, Android-Lifecycle, Datenschutz, Berechtigungen, Security, Multi-Datei-Refactors und harte Fehlersuche.
- GLM-5.2: Max-Effort für lange oder riskante Coding-Aufgaben; High nur bewusst für kleinere oder latenzsensiblere Teilaufgaben.
- Keine non-default `temperature`, `top_p` oder `top_k` mit Claude Sonnet 5 verwenden.
- Modellregeln gelten nur für Coding-Agenten, nicht als App-Feature.

Für Prompt- und Providerparameter gilt zusätzlich `docs/PROMPT_PARAMETER_POLICY.md`.

---

## Ziel

Erstelle oder erweitere eine private Android-App als MVP.

Die App soll über WhatsApp als schwebender Formulierungshelfer funktionieren:

- Wenn WhatsApp geöffnet ist, erscheint ein kleiner Floating Button.
- Beim Antippen öffnet sich zuerst eine schmale Input-Bar.
- Die Input-Bar enthält Ton/Stil, Texteingabe, Einfügen und Start.
- Der Start-Button darf nicht `Senden` heißen.
- Optional kann eine kopierte Nachricht bewusst eingefügt werden.
- Falls Clipboard nicht lesbar ist, kann der Nutzer Text manuell eingeben oder einfügen.
- Der Nutzer beschreibt grob, was er sagen möchte.
- Die KI erzeugt 3 Antwortvorschläge.
- Nach der KI-Antwort erscheint ein kompaktes Result-Panel.
- Das Result-Panel zeigt immer nur einen Vorschlag.
- Der Nutzer wechselt zwischen 3 Vorschlägen per Swipe, Pfeil oder Pager.
- Der Nutzer kann den sichtbaren Vorschlag kopieren.
- Der Nutzer fügt den Text selbst in WhatsApp ein und sendet selbst.
- Retry ist erlaubt über `Nochmal` und kompakte temporäre Änderungs-Chips.

---

## Harte Nicht-Ziele

Baue ausdrücklich nicht:

- kein automatisches Auslesen von WhatsApp-Chats
- kein Lesen vollständiger Chatverläufe
- kein Zugriff auf Kontakte
- kein automatisches Einfügen in WhatsApp
- kein automatisches Senden
- keine Accessibility-basierte WhatsApp-Steuerung
- kein Notification Scraping
- kein Screen Scraping
- kein Account-System
- keine Cloud-Speicherung
- kein Play-Store-Release-Setup
- kein Multi-App-Support
- keine überdimensionierte Architektur
- keine API-Key-Eingabe im UI
- kein Modellrouting im MVP
- keine Modell- oder Provider-Auswahl im Overlay
- kein Verlauf
- kein Gedächtnis
- kein Stiltraining
- keine Profile
- kein großes Formular als Startzustand
- keine drei Vorschläge untereinander als Standardansicht

Wenn eine Funktion eines dieser Themen benötigt, stoppe und erkläre die Konsequenz.

---

## Technische Entscheidungen

Übernehme `docs/DECISIONS.md` verbindlich.

Kurzfassung:

- Provider: OpenRouter im MVP
- App-Modellstrategie: genau ein OpenRouter-Default-Modell im MVP, vor Phase 7 pinnen
- Overlay-Laufzeit: Foreground Service aus sichtbarer Nutzeraktion
- MainActivity: Jetpack Compose
- Overlay Bubble, Input-Bar und Result-Panel: klassische Android Views
- `applicationId`: `de.disaai.chathilfe`
- SDK-Basis: `compileSdk 37`, `targetSdk 35`, `minSdk 29`
- Clipboard-Fallback: manuelles Eingeben oder Einfügen im Overlay
- API-Key: lokaler Build-Time-Key, kein UI-Feld, nicht in DataStore
- Gerätetest: gesammelt in Phase 8, nicht als Zwischen-Gate

---

## Vorgehen

Arbeite strikt nach [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md).

Aktuelle Kurzfassung:

1. Repo prüfen
2. offene Toolchain-Details und aktuelle Entscheidungen prüfen
3. Android-Projektbasis prüfen oder anlegen
4. Settings und Berechtigungen bauen
5. Foreground Service + manuelles Overlay bauen
6. WhatsApp-Erkennung bauen
7. Input-Bar und Result-Panel ohne KI bauen
8. PromptBuilder und Parser bauen
9. KI-Anbindung bauen
10. Stabilisierung und gebündelter Gerätetest in Phase 8
11. README und Teststatus aktualisieren

Keine Phase überspringen, wenn dadurch ungetestete oder unklare Grundfunktionalität verdeckt wird.

---

## Änderungsregeln

- Vor Änderungen relevante Source-of-Truth-Dateien lesen.
- Änderungen klein und fokussiert halten.
- Keine neuen Produktionsdependencies ohne Begründung und Freigabe.
- Keine echten Secrets schreiben.
- Keine Nutzertexte, Clipboard-Texte, generierten Vorschläge oder Retry-Anweisungen speichern oder loggen.
- Keine verbotenen Android-Berechtigungen einführen.
- Keine Entscheidungen still ändern; bei Bedarf `docs/DECISIONS.md` aktualisieren.
- Visual Scope gegen `docs/VISUAL_SCOPE.md` prüfen.
- Device-Test-Flow gegen `docs/DEVICE_TEST_POLICY.md` prüfen.
- Provider- und Promptparameter gegen `docs/PROMPT_PARAMETER_POLICY.md` prüfen.

---

## Verifikation

Vor Abschluss prüfen:

- Build läuft, wenn Gradle-Projekt und Umgebung verfügbar sind
- relevante Tests laufen, wenn vorhanden
- Lint geprüft oder begründet nicht geprüft
- keine verbotenen Permissions im Manifest
- kein Accessibility Service
- keine WhatsApp-Automation
- kein Hintergrund-Clipboard-Monitoring
- keine Nutzertexte/API-Keys/Retry-Anweisungen in Logs
- Overlay erzeugt keine doppelten Views
- README und relevante Fachdocs sind aktuell, wenn die Änderung Doku betrifft

Geräteverhalten ehrlich als `nicht validiert` markieren, wenn kein echtes Gerät genutzt wurde. Bis Phase 8 bleibt echter Gerätetest ein offenes Risiko.

---

## Ausgabeformat

Am Ende liefern:

```text
Summary:
- ...

Files changed:
- ...

Validation:
- ...

Not validated:
- ...

Risks:
- ...

Next sensible step:
- ...
```

---

Schwierigkeiten: 6/10 | Thinking: high