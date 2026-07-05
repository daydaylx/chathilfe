# UI_UX_SPEC.md — ChatHilfe MVP

## Zweck

Dieses Dokument definiert die UI/UX-Regeln für den MVP.

Die App soll klein, schnell, verständlich und unaufdringlich sein. Sie darf nicht wie Spyware, Werbung oder ein schwerer Chatbot wirken.

---

## UX-Ziel

Der Nutzer soll WhatsApp normal weiterverwenden können und nur bei Bedarf den Formulierungshelfer öffnen.

Zielgefühl:

- klein
- ruhig
- klar
- schnell
- nicht störend
- nicht wie Spyware
- nicht wie ein Bot

Nicht-Ziel:

- keine Vollbild-App über WhatsApp
- keine komplexe Navigation
- kein Chatbot-Verlauf
- kein Dashboard
- keine unnötigen Animationen

---

## Hauptflow

```text
WhatsApp öffnen
↓
Floating Button erscheint am Rand
↓
Button antippen
↓
Mini-Fenster öffnet
↓
Modus wählen
↓
optional Clipboard verwenden
↓
Absicht eingeben
↓
Ton wählen
↓
Vorschläge erstellen
↓
Vorschlag kopieren
↓
manuell in WhatsApp einfügen
```

Der Flow soll in wenigen Sekunden nutzbar sein.

---

## MainActivity / Setup

Die MainActivity ist nur für Einrichtung gedacht:

- Berechtigungen
- API-Key
- Overlay aktivieren/deaktivieren
- Test-Overlay
- kurze Erklärung

Abschnitte:

1. Titelbereich
2. Statuskarten
3. API-Key-Bereich
4. Overlay-Steuerung
5. Datenschutzhinweise
6. bekannte Einschränkungen

Statuskarten zeigen:

- Status: OK / fehlt / optional
- kurze Erklärung
- Button zur Aktion

---

## Floating Button

Anforderungen:

- 48dp bis 56dp Startgröße
- kleiner Randbutton
- verschiebbar
- Rand-Andocken nach Drag
- nicht über WhatsApp-Sendebutton defaulten
- sichtbar nur bei WhatsApp
- Tap öffnet Panel
- Drag löst keinen Tap aus
- Position wird gespeichert

Default-Position:

- rechter Bildschirmrand
- mittlere Höhe

---

## ReplyPanel

Das ReplyPanel ist ein kleines Formular, kein Chatfenster.

Empfehlung:

- Breite: 88–94 % Displaybreite
- maximale Höhe: 70–80 % Displayhöhe
- Inhalt scrollbar
- nicht Vollbild
- einfach schließbar

Grundlayout:

```text
Antworthelfer        X
[Antworten] [Formulieren] [Umschreiben]

Kopierter Text erkannt
„Vorschau...“
[Verwenden] [Ignorieren]

Was willst du sagen?
[ Eingabe ]

Ton
[kurz] [freundlich] [direkt]
[entschuldigend] [deeskalierend] [klare Grenze] [flirtend]

[Vorschläge erstellen]

1. Vorschlag ... [Kopieren]
2. Vorschlag ... [Kopieren]
3. Vorschlag ... [Kopieren]
```

---

## Modusauswahl

Modi:

- Antworten
- Formulieren
- Umschreiben

Default:

- Clipboard erkannt → Antworten
- kein Clipboard → Formulieren

Umschreiben muss klar anzeigen, dass ein Originaltext gebraucht wird.

---

## Clipboard-UX

Wenn Clipboard-Text vorhanden ist:

```text
Kopierter Text erkannt
„<Vorschau>“
[Verwenden] [Ignorieren]
```

Regeln:

- Vorschau maximal 2–3 Zeilen
- langen Text kürzen
- Text erst nach „Verwenden“ übernehmen
- „Ignorieren“ sichtbar anbieten

---

## Eingaben

Antworten-Modus:

```text
Was willst du ausdrücken?
```

Placeholder:

```text
z. B. entschuldigen, aber nicht unterwürfig klingen
```

Formulieren-Modus:

```text
Was willst du sagen?
```

Umschreiben-Modus:

```text
Originaltext
Wie soll er klingen?
```

---

## Ton-Auswahl

Chips:

- kurz
- freundlich
- direkt
- entschuldigend
- deeskalierend
- klare Grenze
- flirtend

Regeln:

- genau ein Ton aktiv
- Default: freundlich
- letzter Ton darf gespeichert werden
- keine zu vielen Tonoptionen im MVP

---

## Lade- und Fehlerzustände

Während Anfrage:

- Generate-Button deaktivieren
- Ladehinweis anzeigen
- keine parallele Anfrage

Text:

```text
Vorschläge werden erstellt ...
```

Fehlertexte:

| Fall | Text |
|---|---|
| API-Key fehlt | API-Key fehlt. Bitte in der App eintragen. |
| Internet fehlt | Keine Verbindung. Bitte Internet prüfen. |
| KI-Fehler | Vorschläge konnten nicht erstellt werden. |
| Clipboard leer | Keine kopierte Nachricht erkannt. |
| Overlay fehlt | Overlay-Berechtigung fehlt. |
| Usage Access fehlt | Nutzungsdatenzugriff fehlt. |

Keine Stacktraces in der UI.

---

## Ergebnisse

Jeder Vorschlag als eigene kleine Karte:

```text
1.
Text...
[Kopieren]
```

Nach Kopieren kurz anzeigen:

```text
Kopiert
```

Regeln:

- 3 Plätze vorsehen
- vorhandene Vorschläge anzeigen, wenn weniger als 3 extrahierbar sind
- keine Analyse anzeigen
- keine unbrauchbare Rohantwort anzeigen

---

## Visuelle Richtung

- dunkles Design bevorzugt
- ruhige Flächen
- hohe Lesbarkeit
- klare Buttons
- keine grellen Farben
- keine schweren Glassmorphism-Effekte
- keine unnötigen Animationen

Symbolideen:

- Stift
- kleine Sprechblase
- dezenter Zauberstab

---

## Anti-Nerv-Regeln

- Button nur bei WhatsApp anzeigen
- Button klein halten
- Button verschiebbar machen
- Overlay deaktivierbar machen
- Position speichern
- Panel nie automatisch öffnen
- keine Popups ohne Nutzeraktion
- keine KI-Anfrage ohne Nutzeraktion

---

## MVP-Pflichtumfang

Pflicht:

- Setup-Screen
- Statuskarten
- API-Key-Eingabe
- Overlay aktiv/inaktiv
- Floating Button
- Drag-Verhalten
- ReplyPanel
- Modusauswahl
- Ton-Auswahl
- Clipboard-Vorschau
- Generate-Button
- Ladezustand
- Fehlerzustand
- 3 Vorschlagskarten
- Kopieren pro Vorschlag

Nicht Pflicht:

- Animationen
- Onboarding-Slides
- Verlauf
- Account
- Modellauswahl
- Themes
- Sound/Vibration

---

## Akzeptanzkriterien

UI/UX ist akzeptabel, wenn:

- Nutzer versteht, wofür die App ist
- Berechtigungen verständlich erklärt werden
- Button stört nicht beim Tippen
- Panel ist schnell bedienbar
- Modi sind klar
- Clipboard-Nutzung ist freiwillig
- Vorschläge sind leicht kopierbar
- Fehler sind verständlich
- nichts wirkt heimlich oder invasiv
