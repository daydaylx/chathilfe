# UI_UX_SPEC.md — ChatHilfe MVP

## 1. Zweck

Dieses Dokument definiert die UI/UX-Anforderungen für den ChatHilfe-MVP.

Die App darf nicht wie ein schweres Produkt, ein Chatbot oder eine verdächtige Overlay-App wirken. Sie soll klein, schnell, verständlich und unaufdringlich sein.

---

## 2. UX-Ziel

Der Nutzer soll WhatsApp normal weiterverwenden können und bei Bedarf schnell Hilfe beim Formulieren bekommen.

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
- keine Spielerei, die Bedienung verlangsamt

---

## 3. Hauptflow

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

## 4. MainActivity / Setup-Screen

## Zweck

Die MainActivity ist nicht die eigentliche Nutzungsoberfläche. Sie dient nur für:

- Berechtigungen
- API-Key
- Overlay aktivieren/deaktivieren
- Test-Overlay
- kurze Erklärung

## Layout

Abschnitte:

1. Titelbereich
2. Statuskarten
3. API-Key-Bereich
4. Overlay-Steuerung
5. Kurze Datenschutzhinweise
6. Link/Hinweis auf bekannte Einschränkungen

## Statuskarten

Statuskarten für:

- Overlay-Berechtigung
- Nutzungsdatenzugriff
- API-Key
- Overlay aktiv/inaktiv

Jede Statuskarte soll zeigen:

- Status: OK / fehlt / optional
- kurze Erklärung
- Button zur Aktion, falls nötig

Beispiel:

```text
Overlay-Berechtigung fehlt
Wird benötigt, damit der Button über WhatsApp angezeigt werden kann.
[In Einstellungen öffnen]
```

---

## 5. Floating Button

## Anforderungen

Der Button ist die wichtigste sichtbare Komponente.

Er muss:

- klein sein
- am Rand sitzen
- verschiebbar sein
- nicht beim Tippen stören
- nur bei WhatsApp sichtbar sein
- keine Panik erzeugen
- nicht wie Werbung aussehen

## Verhalten

| Aktion | Verhalten |
|---|---|
| Tippen | ReplyPanel öffnen |
| Ziehen | Button verschieben |
| Loslassen nah am Rand | an Rand andocken |
| WhatsApp verlassen | Button ausblenden |
| WhatsApp öffnen | Button einblenden |

## Tap-vs-Drag

Ein Drag darf keinen Tap auslösen.

Regel:

- kleine Bewegung unter Threshold = Tap möglich
- Bewegung über Threshold = Drag

## Größe

Startempfehlung:

```text
48dp bis 56dp
```

Nicht größer, sonst stört der Button.

## Position

Default:

- rechter Bildschirmrand
- ungefähr mittlere Höhe

Position muss lokal gespeichert werden.

---

## 6. ReplyPanel

## Ziel

Das ReplyPanel ist ein kleines Formular, kein Chatfenster.

Es soll WhatsApp nicht komplett verdecken.

## Grundlayout

```text
┌──────────────────────────────┐
│ Antworthelfer              X │
├──────────────────────────────┤
│ [Antworten] [Formulieren]    │
│ [Umschreiben]                │
│                              │
│ Kopierter Text erkannt       │
│ „Warum meldest du dich...“   │
│ [Verwenden] [Ignorieren]     │
│                              │
│ Was willst du sagen?         │
│ ____________________________ │
│                              │
│ Ton                          │
│ [kurz] [freundlich] [direkt] │
│ [entschuldigend] [Grenze]    │
│                              │
│ [Vorschläge erstellen]       │
│                              │
│ 1. Vorschlag ... [Kopieren]  │
│ 2. Vorschlag ... [Kopieren]  │
│ 3. Vorschlag ... [Kopieren]  │
└──────────────────────────────┘
```

## Größe

Startempfehlung:

- Breite: ca. 88–94 % der Displaybreite
- maximale Höhe: ca. 70–80 % der Displayhöhe
- Inhalt scrollbar, wenn nötig

Nicht als Vollbilddialog bauen.

---

## 7. Modusauswahl

## Modi

- Antworten
- Formulieren
- Umschreiben

## UX-Regel

Modus muss klar sein. Die KI soll nicht raten.

## Default

Wenn Clipboard-Text erkannt wird:

- Default: Antworten

Wenn kein Clipboard-Text erkannt wird:

- Default: Formulieren

## Umschreiben

Umschreiben sollte klar machen, dass ein Originaltext gebraucht wird.

Beispiel-Hinweis:

```text
Füge deinen vorhandenen Text ein oder verwende den kopierten Text.
```

---

## 8. Clipboard-UX

## Wenn Clipboard-Text vorhanden ist

Anzeigen:

```text
Kopierter Text erkannt
„<Vorschau>“
[Verwenden] [Ignorieren]
```

## Regeln

- Vorschau maximal 2–3 Zeilen
- langer Text wird gekürzt
- Text erst nach „Verwenden“ in Request übernehmen
- „Ignorieren“ muss sichtbar sein

## Wenn kein Clipboard-Text vorhanden ist

Nicht groß stören. Optional kleine Info:

```text
Keine kopierte Nachricht erkannt.
```

---

## 9. Eingabefelder

## Antworten-Modus

Label:

```text
Was willst du ausdrücken?
```

Placeholder:

```text
z. B. entschuldigen, aber nicht unterwürfig klingen
```

## Formulieren-Modus

Label:

```text
Was willst du sagen?
```

Placeholder:

```text
z. B. Ich brauche heute Ruhe, will aber nicht kalt wirken
```

## Umschreiben-Modus

Felder:

```text
Originaltext
```

```text
Wie soll er klingen?
```

Placeholder:

```text
z. B. weniger passiv-aggressiv, kürzer, freundlicher
```

---

## 10. Ton-Auswahl

## Ton-Chips

Anzeigen als Chips/Buttons:

- kurz
- freundlich
- direkt
- entschuldigend
- deeskalierend
- klare Grenze
- flirtend

## Verhalten

- genau ein Ton muss auswählbar sein
- später mehrere Tonwerte optional, aber nicht MVP-Pflicht
- zuletzt gewählter Ton darf lokal gespeichert werden

## Default

Empfohlen:

```text
freundlich
```

Oder im Antworten-Modus:

```text
ruhig/freundlich
```

---

## 11. Ladezustand

Während KI-Anfrage:

- Button deaktivieren
- Ladehinweis anzeigen
- keine zweite Anfrage parallel starten

Text:

```text
Vorschläge werden erstellt ...
```

Nicht tun:

- Panel schließen
- Nutzer im Unklaren lassen
- mehrfaches Tippen zulassen

---

## 12. Ergebnisdarstellung

## Vorschläge

Jeder Vorschlag als eigene kleine Karte:

```text
1.
Sorry, dass ich mich erst jetzt melde...
[Kopieren]
```

## Kopierfeedback

Nach Kopieren:

```text
Kopiert
```

Kurz anzeigen, z. B. Toast/Snackbar oder Textstatus.

## Regeln

- genau 3 Plätze vorsehen
- wenn weniger Vorschläge: vorhandene anzeigen und Fehlerhinweis klein halten
- keine Analyse anzeigen
- keine Modell-Rohantwort anzeigen, wenn sie unbrauchbar ist

---

## 13. Fehlerzustände

## Fehlertexte

| Fall | Text |
|---|---|
| API-Key fehlt | „API-Key fehlt. Bitte in der App eintragen.“ |
| Internet fehlt | „Keine Verbindung. Bitte Internet prüfen.“ |
| KI-Fehler | „Vorschläge konnten nicht erstellt werden.“ |
| Clipboard leer | „Keine kopierte Nachricht erkannt.“ |
| Overlay fehlt | „Overlay-Berechtigung fehlt.“ |
| Usage Access fehlt | „Nutzungsdatenzugriff fehlt.“ |

Fehlertexte müssen kurz und verständlich sein.

Keine Stacktraces.

---

## 14. Visuelle Richtung

## Stil

- dunkel bevorzugt
- ruhige Flächen
- hohe Lesbarkeit
- klare Buttons
- keine grellen Farben
- keine schweren Glassmorphism-Effekte
- keine unnötigen Animationen

## Button-Stil

- klar erkennbar
- nicht wie Werbung
- nicht zu bunt
- Symbol sollte nach Hilfe/Schreiben wirken

Mögliche Symbolideen:

- Stift
- kleine Sprechblase
- Zauberstab sehr dezent

---

## 15. Barrierearme Mindestanforderungen

- Text gut lesbar
- Touch-Ziele ausreichend groß
- keine winzigen Schließen-Buttons
- Kontraste ausreichend
- Panel scrollbar
- keine kritischen Infos nur über Farbe
- Button nicht direkt über WhatsApp-Sendebutton defaulten

---

## 16. Anti-Nerv-Regeln

Die App wird scheitern, wenn der Button nervt.

Regeln:

- Button nur bei WhatsApp anzeigen
- Button klein halten
- Button verschiebbar machen
- Overlay deaktivierbar machen
- Position speichern
- Panel nicht automatisch öffnen
- keine Popups ohne Nutzeraktion
- keine KI-Anfrage ohne Nutzeraktion

---

## 17. Erste UX-Version: Pflichtumfang

Pflicht für MVP:

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
- komplexe Icons
- Sound/Vibration

---

## 18. UX-Akzeptanzkriterien

Die UI/UX ist akzeptabel, wenn:

- Nutzer versteht, wofür die App ist
- Berechtigungen verständlich erklärt werden
- Button stört nicht beim Tippen
- Panel ist in wenigen Sekunden bedienbar
- Modi sind klar
- Clipboard-Nutzung ist freiwillig
- Vorschläge sind leicht kopierbar
- Fehler sind verständlich
- keine App-Funktion wirkt heimlich oder invasiv

---

## 19. Schlechte UX-Entscheidungen

Nicht bauen:

- Vollbild-Overlay über WhatsApp
- dauerhaft blinkender Button
- automatisch aufpoppendes Panel
- KI-Antwort ohne Nutzeraktion
- versteckte Clipboard-Nutzung
- zu viele Tonoptionen
- Chatverlauf im Panel
- Modellpicker im MVP
- Werbe-ähnliches Design
- unnötig verspielte Animationen

---

## 20. Kurzfazit

Die beste UX ist hier nicht spektakulär, sondern unauffällig.

Der Nutzer soll WhatsApp normal nutzen und nur bei Bedarf den kleinen Helfer öffnen.

Der MVP ist gelungen, wenn er sich wie ein kleines Werkzeug anfühlt, nicht wie eine zweite App über WhatsApp.
