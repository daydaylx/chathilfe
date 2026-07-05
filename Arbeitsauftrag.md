# Arbeitsauftrag.md — Startauftrag für externe Coding-Agenten

## Einordnung

Dieses Dokument ist ein direkt nutzbarer Startauftrag für einen externen Coding-Agenten.

Für dauerhafte Repo-Regeln gelten vorrangig:

1. [`AGENTS.md`](AGENTS.md)
2. [`Konzept.md`](Konzept.md)
3. [`docs/DECISIONS.md`](docs/DECISIONS.md)
4. [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
5. [`docs/ANDROID_CONSTRAINTS.md`](docs/ANDROID_CONSTRAINTS.md)
6. [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md)
7. taskrelevante Fachdocs in [`docs/`](docs/)

Wenn dieser Arbeitsauftrag einer Fachdatei widerspricht, gilt die Fachdatei.

---

## Rolle

Du bist ein erfahrener Android-Entwickler mit Fokus auf Kotlin, Jetpack Compose, Android-Systemberechtigungen, Overlays, Datenschutz, sauberer App-Struktur und pragmatischer MVP-Umsetzung.

Arbeite kritisch. Baue keinen unnötigen Ballast. Halte dich strikt an den MVP-Scope.

---

## Ziel

Erstelle eine private Android-App als MVP.

Die App soll über WhatsApp als schwebender Formulierungshelfer funktionieren:

- Wenn WhatsApp geöffnet ist, erscheint ein kleiner Floating Button.
- Beim Antippen öffnet sich ein kompaktes Mini-Fenster.
- Der Nutzer wählt einen Modus:
  - Antworten
  - Formulieren
  - Umschreiben
- Optional kann eine kopierte Nachricht aus der Zwischenablage verwendet werden.
- Falls Clipboard nicht lesbar ist, kann der Nutzer Text manuell ins Panel einfügen.
- Der Nutzer beschreibt grob, was er sagen möchte.
- Der Nutzer wählt einen Ton.
- Die KI erzeugt 3 Antwortvorschläge.
- Der Nutzer kann einen Vorschlag kopieren.
- Der Nutzer fügt den Text selbst in WhatsApp ein und sendet selbst.

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

Wenn eine Funktion eines dieser Themen benötigt, stoppe und erkläre die Konsequenz.

---

## Technische Entscheidungen

Übernehme `docs/DECISIONS.md` verbindlich.

Kurzfassung:

- Provider: OpenRouter im MVP
- Overlay-Laufzeit: Foreground Service aus sichtbarer Nutzeraktion
- MainActivity: Jetpack Compose
- Overlay Bubble + ReplyPanel: klassische Android Views
- `applicationId`: `de.disaai.chathilfe`
- SDK-Basis: `compileSdk 36`, `targetSdk 35`, `minSdk 29`
- Clipboard-Fallback: manuelles Einfügen ins Panel

---

## Vorgehen

Arbeite strikt nach [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md).

Kurzfassung:

1. Repo prüfen
2. offene Toolchain-Details pinnen
3. Android-Projektbasis anlegen
4. Settings und Berechtigungen bauen
5. Foreground Service + manuelles Overlay bauen
6. WhatsApp-Erkennung bauen
7. ReplyPanel ohne KI bauen
8. PromptBuilder und Parser bauen
9. KI-Anbindung bauen
10. Gerätetests und Stabilisierung
11. README und Teststatus aktualisieren

Keine Phase überspringen, wenn dadurch ungetestete Grundfunktionalität verdeckt wird.

---

## Verifikation

Vor Abschluss prüfen:

- Build läuft, wenn Gradle-Projekt existiert
- relevante Tests laufen, wenn vorhanden
- keine verbotenen Permissions im Manifest
- kein Accessibility Service
- keine WhatsApp-Automation
- kein Hintergrund-Clipboard-Monitoring
- keine Nutzertexte/API-Keys in Logs
- Overlay erzeugt keine doppelten Views
- README und relevante Fachdocs sind aktuell

Geräteverhalten ehrlich als „nicht validiert“ markieren, wenn kein echtes Gerät genutzt wurde.

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
