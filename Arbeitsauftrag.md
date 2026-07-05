# Arbeitsauftrag.md — Android MVP: WhatsApp Reply Overlay

## Rolle

Du bist ein erfahrener Android-Entwickler mit Fokus auf Kotlin, Jetpack Compose, Android-Systemberechtigungen, Overlays, sauberer App-Architektur und pragmatischer MVP-Umsetzung.

Arbeite kritisch, reduziere unnötige Komplexität und baue keine Funktionen außerhalb des definierten Scopes.

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
- Der Nutzer beschreibt grob, was er sagen möchte.
- Der Nutzer wählt einen Ton.
- Die KI erzeugt 3 Antwortvorschläge.
- Der Nutzer kann einen Vorschlag kopieren.
- Der Nutzer fügt den Text selbst in WhatsApp ein und sendet selbst.

---

## Nicht-Ziele

Baue ausdrücklich nicht:

- kein automatisches Auslesen von WhatsApp-Chats
- kein Lesen vollständiger Chatverläufe
- kein Zugriff auf Kontakte
- kein automatisches Einfügen in WhatsApp
- kein automatisches Senden
- keine Accessibility-basierte WhatsApp-Steuerung
- kein Account-System
- keine Cloud-Speicherung
- kein Play-Store-Release-Setup
- kein Multi-App-Support
- kein unnötig großes Rollen-/Prompt-System
- keine überdimensionierte Architektur

Wenn du eine dieser Funktionen für nötig hältst, stoppe und erkläre erst die Konsequenz.

---

## Annahmen

- Zielgerät ist ein modernes Android-Gerät, primär Samsung Galaxy S25.
- Die App wird als private APK installiert.
- Kotlin und Jetpack Compose sollen verwendet werden.
- Die App darf die Berechtigungen „Über anderen Apps anzeigen“, „Nutzungsdatenzugriff“ und „Internet“ nutzen.
- Der Nutzer stellt einen API-Key für OpenRouter oder OpenAI bereit.
- Die App liest die Zwischenablage nur nach aktiver Nutzeraktion.
- WhatsApp-Paketname ist zunächst `com.whatsapp`.
- WhatsApp Business ist optional später über `com.whatsapp.w4b` möglich, aber nicht Teil des Pflichtumfangs.

---

## Technische Vorgaben

### Stack

- Kotlin
- Jetpack Compose
- Android `WindowManager`
- `TYPE_APPLICATION_OVERLAY`
- `UsageStatsManager`
- DataStore für lokale Einstellungen
- HTTP-Client für KI-Anfragen

### Architektur

Erstelle eine einfache, wartbare Struktur:

```text
ReplyOverlayApp
├── MainActivity
├── overlay
│   ├── OverlayService
│   ├── FloatingButtonOverlay
│   └── MiniReplyWindow
├── detection
│   └── ForegroundAppDetector
├── ai
│   ├── AiClient
│   ├── PromptBuilder
│   └── AiModels
├── clipboard
│   └── ClipboardHelper
├── settings
│   └── SettingsStore
└── domain
    ├── ReplyMode
    ├── ToneOption
    ├── AiRequest
    └── AiSuggestion
```

Halte die Architektur klein. Keine unnötigen Repositories, UseCases oder abstrakten Interfaces, solange sie keinen echten Nutzen bringen.

---

## Funktionsumfang

## 1. MainActivity

Baue einen einfachen Startscreen mit:

- App-Titel
- Status: Overlay-Berechtigung vorhanden / fehlt
- Status: Nutzungsdatenzugriff vorhanden / fehlt
- Status: API-Key vorhanden / fehlt
- Button: Overlay-Berechtigung öffnen
- Button: Nutzungsdatenzugriff öffnen
- Eingabefeld: API-Key
- Button: API-Key speichern
- Schalter: Overlay aktivieren/deaktivieren
- Button: Test-Overlay anzeigen

Die MainActivity ist nur für Einrichtung und Status gedacht, nicht für die eigentliche Nutzung.

---

## 2. Berechtigungen

Implementiere Prüfungen für:

- `SYSTEM_ALERT_WINDOW`
- Usage Access / `PACKAGE_USAGE_STATS`
- Internet-Berechtigung im Manifest

Wenn Berechtigungen fehlen, zeige klare Hinweise und öffne die passende Android-Einstellungsseite.

---

## 3. WhatsApp-Erkennung

Implementiere eine Foreground-App-Erkennung über `UsageStatsManager`.

Pflichtverhalten:

- Wenn `com.whatsapp` im Vordergrund ist, darf der Floating Button sichtbar sein.
- Wenn eine andere App im Vordergrund ist, muss der Floating Button verschwinden.
- Die Prüfung soll ressourcenschonend laufen.
- Keine Accessibility API verwenden.

Wenn die Erkennung unzuverlässig ist, dokumentiere die Einschränkung und baue optional einen manuellen Testmodus.

---

## 4. OverlayService

Der OverlayService verwaltet:

- Start/Stop des Overlays
- Anzeigen des Floating Buttons
- Ausblenden des Floating Buttons
- Öffnen des Mini-Fensters
- Schließen des Mini-Fensters
- Speichern und Wiederherstellen der Button-Position

Der Service darf nicht unnötig dauerhaft CPU verbrauchen.

---

## 5. Floating Button

Anforderungen:

- klein
- verschiebbar
- am Rand positioniert
- beim Ziehen nicht versehentlich öffnen
- beim Tippen Mini-Fenster öffnen
- halbtransparent oder visuell unaufdringlich
- Position lokal speichern
- außerhalb von WhatsApp ausblenden

---

## 6. Mini-Fenster

Das Mini-Fenster soll kompakt sein und WhatsApp nicht komplett verdecken.

Pflichtbestandteile:

- Titel: „Antworthelfer“
- Modusauswahl:
  - Antworten
  - Formulieren
  - Umschreiben
- Zwischenablage-Vorschau, wenn Text vorhanden ist
- Button: „Zwischenablage verwenden“
- Button: „Ignorieren“
- Eingabefeld: „Was willst du sagen?“
- Ton-Auswahl:
  - kurz
  - freundlich
  - direkt
  - entschuldigend
  - deeskalierend
  - klare Grenze
  - flirtend
- Button: „Vorschläge erstellen“
- Ladezustand
- Fehleranzeige
- Ergebnisbereich mit 3 Vorschlägen
- Button je Vorschlag: „Kopieren“
- Button: „Schließen“

---

## 7. Modi-Logik

Implementiere drei Modi.

### Antworten

Verwendet:

- kopierte Nachricht
- Nutzerabsicht
- Ton

Ziel:
3 passende Antwortvorschläge auf die kopierte Nachricht.

### Formulieren

Verwendet:

- Nutzerabsicht
- Ton

Ziel:
3 sendbare Chatnachrichten aus einer groben Idee.

### Umschreiben

Verwendet:

- Originaltext
- gewünschte Änderung
- Ton

Ziel:
3 verbesserte Varianten des Originaltexts.

---

## 8. Clipboard-Logik

Wichtig:

- Die App darf die Zwischenablage nicht dauerhaft heimlich überwachen.
- Die Zwischenablage wird nur gelesen, wenn der Nutzer das Mini-Fenster aktiv öffnet oder auf „Zwischenablage verwenden“ tippt.
- Zeige eine Vorschau des kopierten Textes.
- Verwende den Text erst nach Bestätigung.
- Generierte Vorschläge können in die Zwischenablage kopiert werden.

---

## 9. KI-Anbindung

Implementiere einen einfachen AiClient.

Pflicht:

- API-Key aus SettingsStore lesen
- Anfrage senden
- Fehler behandeln
- Ladezustand anzeigen
- genau 3 Vorschläge zurückgeben
- keine API-Keys loggen
- keine Nutzertexte unnötig lokal speichern

Der Anbieter kann OpenRouter oder OpenAI sein. Baue die Schnittstelle pragmatisch, aber nicht überabstrahiert.

---

## 10. PromptBuilder

Implementiere feste Prompts pro Modus.

### Prompt: Antworten

```text
Du bist ein Formulierungsassistent für private Chatnachrichten.

Aufgabe:
Formuliere passende Antwortvorschläge auf die kopierte Nachricht.

Regeln:
- Antworte in natürlichem Deutsch.
- Schreibe wie eine normale Chatnachricht.
- Keine langen Erklärungen.
- Keine Analyse ausgeben.
- Nicht künstlich oder übertrieben höflich klingen.
- Keine Nachricht automatisch senden.
- Erzeuge genau 3 Varianten.
- Jede Variante soll direkt kopierbar sein.

Kopierte Nachricht:
{{copied_message}}

Was der Nutzer ausdrücken will:
{{user_intent}}

Gewünschter Ton:
{{tone}}

Ausgabeformat:
1. ...
2. ...
3. ...
```

### Prompt: Formulieren

```text
Du bist ein Formulierungsassistent für private Chatnachrichten.

Aufgabe:
Formuliere aus dem Wunsch des Nutzers 3 sendbare Chatnachrichten.

Regeln:
- Natürlich und menschlich schreiben.
- Keine Erklärung ausgeben.
- Keine Analyse ausgeben.
- Keine übertriebene Höflichkeit.
- Keine künstliche Therapiesprache.
- Jede Variante soll direkt kopierbar sein.

Nutzerwunsch:
{{user_intent}}

Gewünschter Ton:
{{tone}}

Ausgabeformat:
1. ...
2. ...
3. ...
```

### Prompt: Umschreiben

```text
Du bist ein Formulierungsassistent für private Chatnachrichten.

Aufgabe:
Schreibe den vorhandenen Text passend um.

Regeln:
- Bedeutung erhalten.
- Ton gemäß Vorgabe anpassen.
- 3 Varianten erzeugen.
- Keine Erklärung ausgeben.
- Jede Variante soll direkt kopierbar sein.

Originaltext:
{{original_text}}

Gewünschte Änderung:
{{user_intent}}

Gewünschter Ton:
{{tone}}

Ausgabeformat:
1. ...
2. ...
3. ...
```

---

## 11. Speicherung

Speichere lokal:

- API-Key
- Overlay aktiv/inaktiv
- bevorzugter Ton
- letzte Button-Position
- optional: zuletzt gewählter Modus

Speichere nicht:

- kopierte WhatsApp-Nachrichten
- generierte Antworten
- Chatverläufe
- Kontakte
- personenbezogene Daten

---

## 12. Fehlerbehandlung

Behandle mindestens:

- Overlay-Berechtigung fehlt
- Nutzungsdatenzugriff fehlt
- API-Key fehlt
- Internet fehlt
- KI-Anfrage fehlgeschlagen
- KI-Antwort leer
- Zwischenablage leer
- WhatsApp nicht aktiv
- Service wurde beendet
- Bildschirm wurde gesperrt/entsperrt

Fehler müssen für den Nutzer verständlich sein. Keine technischen Stacktraces in der UI.

---

## 13. UI-Anforderungen

Die App soll funktional, klein und nicht nervig wirken.

### Keine übertriebene Gestaltung

- kein schweres Glasdesign
- keine Animationen ohne Nutzen
- keine riesigen Dialoge
- keine überladene Navigation
- keine unnötigen Seiten

### Gewünscht

- kompakt
- klar
- gut lesbar
- schnell bedienbar
- Daumen-freundlich
- dunkles Design bevorzugt
- Overlay darf WhatsApp nicht komplett verdecken

---

## 14. Sicherheit und Datenschutz

- Keine WhatsApp-Inhalte automatisch lesen.
- Keine Nachrichten automatisch senden.
- Keine Kontakte lesen.
- Keine unnötigen Berechtigungen.
- Keine API-Keys loggen.
- Keine Nutzertexte in Logs schreiben.
- Keine versteckte Hintergrundüberwachung.
- Clipboard nur bei aktiver Nutzeraktion verwenden.
- In der App klar erklären, wofür jede Berechtigung gebraucht wird.

---

## 15. Tests

Teste auf echtem Android-Gerät.

Mindestens prüfen:

- Installation als APK
- Overlay-Berechtigung setzen
- Nutzungsdatenzugriff setzen
- WhatsApp öffnen → Button erscheint
- WhatsApp verlassen → Button verschwindet
- Button verschieben
- Mini-Fenster öffnen
- kopierte Nachricht verwenden
- Modus Antworten
- Modus Formulieren
- Modus Umschreiben
- Ton-Auswahl
- KI-Anfrage
- Vorschläge kopieren
- Bildschirm sperren/entsperren
- App aus Recent Apps entfernen
- Internet aus/an
- fehlender API-Key
- fehlende Berechtigungen

---

## 16. Abschlusskriterien

Die Aufgabe ist abgeschlossen, wenn:

- Der MVP als installierbare APK gebaut werden kann.
- Die App auf einem modernen Android-Gerät startet.
- Die Berechtigungsseite verständlich ist.
- Overlay-Berechtigung und Nutzungsdatenzugriff prüfbar sind.
- Floating Button erscheint nur bei WhatsApp.
- Floating Button verschwindet außerhalb von WhatsApp.
- Mini-Fenster funktioniert.
- Alle drei Modi funktionieren.
- Zwischenablage kann bewusst übernommen werden.
- KI erzeugt 3 Vorschläge.
- Vorschläge können kopiert werden.
- Keine WhatsApp-Automation eingebaut wurde.
- Kein Accessibility Service verwendet wurde.
- Keine unnötigen Berechtigungen verwendet wurden.
- Der Code ist klein, verständlich und wartbar.
- README enthält Setup, Berechtigungen, API-Key-Konfiguration und bekannte Einschränkungen.

---

## Ausgabeformat

Liefere am Ende:

1. Kurze Zusammenfassung der Umsetzung
2. Liste der erstellten/geänderten Dateien
3. Hinweise zur Installation der APK
4. Hinweise zu benötigten Berechtigungen
5. bekannte Einschränkungen
6. offene Risiken
7. Testcheckliste
8. klare Aussage, ob alle Abschlusskriterien erfüllt sind

---

## Harte Regeln

- Baue keinen automatischen WhatsApp-Sender.
- Baue keinen WhatsApp-Chat-Reader.
- Nutze keinen Accessibility Service.
- Frage nicht nach zusätzlichen Features, bevor der MVP stabil läuft.
- Vermeide unnötige Architektur.
- Implementiere zuerst lauffähig, dann sauber polieren.
- Keine Schönfärberei: Wenn etwas auf Android/Samsung instabil ist, dokumentiere es klar.

Schwierigkeiten: 6/10 | Thinking: high
