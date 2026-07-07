# VISUAL_SCOPE.md — ChatHilfe MVP

## Zweck

Dieses Dokument definiert den visuellen Scope des MVP.

Ziel: Die App soll wie ein kleiner Messenger-Helfer wirken, nicht wie ein Chatbot, Dashboard, KI-Labor oder Formularsystem.

---

## Grundentscheidung

Das Overlay hat zwei visuelle Zustände:

1. **Input-Bar** — schmaler Eingabebalken direkt nach dem Öffnen.
2. **Result-Panel** — etwas größeres Ergebnis-Panel erst nach der KI-Antwort.

Der MVP startet niemals mit einem großen Formularfenster.

---

## Zustand 1: Input-Bar

Beim Tippen auf den Floating Button öffnet sich zuerst nur ein schmaler Textbalken.

Beispiel:

```text
[Ton]  Was willst du sagen?  [Einfügen] [Los]
```

Alternativ:

```text
[Ton]  Was willst du sagen?  [Einfügen] [>]
```

Pflichtelemente:

- links ein kleiner Ton-/Stil-Button
- kompaktes Texteingabefeld
- Einfügen-Option
- Start-Button für KI-Vorschläge
- rechts eine kleine Schließen-/Zurück-Aktion (z. B. `×`), die zur Bubble zurückführt

Regeln:

- sehr schmal halten
- WhatsApp möglichst wenig verdecken
- keine Vorschlagsfläche vor der KI-Antwort
- kein Dashboard
- kein großes Formular beim Öffnen
- kein Modell-/Provider-/Prompt-Menü
- Start-Button nicht „Senden“ nennen

Erlaubte Start-Labels:

- `Los`
- `Erstellen`
- Pfeil-Icon

---

## Ton-/Stil-Button

Der Ton-/Stil-Button sitzt links im Eingabebalken.

Empfohlene sichtbare Labels:

| Intern | Sichtbar |
|---|---|
| kurz | Kurz |
| freundlich | Freundlich |
| direkt | Direkt |
| entschuldigend | Sorry |
| deeskalierend | Sanft |
| klare Grenze | Grenze |
| flirtend | Flirtend |

Regeln:

- genau ein Ton aktiv
- Default: Freundlich
- letzter Ton darf gespeichert werden
- Tap öffnet nur kleine Chip-Zeile oder kleines Popover
- keine Einstellungsseite öffnen

---

## Einfügen-Option

`Einfügen` ist nur Komfort.

Regeln:

- Clipboard nur nach Nutzeraktion lesen
- Text erst nach Nutzeraktion übernehmen
- wenn Clipboard leer/blockiert ist, bleibt manuelle Eingabe möglich
- keine versteckte Clipboard-Überwachung
- kein Speichern von Clipboard-Inhalten

---

## Zustand 2: Result-Panel

Nach der KI-Antwort erweitert sich das Overlay zu einem kompakten Ergebnis-Panel.

Beispiel:

```text
1 / 3                         [x]

Aktueller Vorschlag

[Kopieren]
[Nicht passend?] [Nochmal]
[Kürzer] [Direkter] [Sanfter]
```

Regeln:

- nur ein Vorschlag sichtbar
- 3 Vorschläge nicht untereinander anzeigen
- WhatsApp bleibt sichtbar
- Panel wird nicht Vollbild
- Kopieren bezieht sich immer auf den sichtbaren Vorschlag

---

## Vorschlagswechsel

Die drei Vorschläge werden als Carousel/Pager behandelt.

MVP-erlaubt:

```text
[<] 1/3 [>]
```

Optional:

```text
● ○ ○
```

Swipe ist erwünscht, aber nicht zwingend für den ersten MVP. Pfeile oder eine einfache Pager-Anzeige reichen, wenn Swipe zu aufwendig wird.

---

## Retry-Bereich

Retry bleibt klein und global.

```text
Nicht passend?
[Nochmal]
[Kürzer] [Lockerer] [Direkter]
[Sanfter] [Klarer] [Weniger künstlich]
```

Regeln:

- erscheint erst nach Ergebnissen
- nicht pro Vorschlagskarte
- maximal 1–2 Chips aktiv
- nur für die nächste Anfrage
- nichts speichern
- kein Feedback-Verlauf
- kein Stiltraining
- kein Gedächtnis

---

## Visuell verboten im MVP

Nicht bauen:

- großes Formular als Startzustand
- Vollbild-Dialog
- drei Vorschlagskarten untereinander als Standardansicht
- Chatbot-Verlauf
- Dashboard
- Sidebar
- Bottom Navigation
- Modellliste
- Provider-Auswahl
- Prompt-Einstellungen
- Token-/Kostenanzeige
- Bewertungsfunktion
- Profil-/Gedächtnisbereich

---

## Visuelles Zielbild

ChatHilfe soll sich so anfühlen:

```text
kleiner WhatsApp-naher Formulierungshelfer
schnell geöffnet
schnell genutzt
leicht wegzulegen
keine Technik sichtbar
kein KI-Labor
```

## Akzeptanzkriterien

Visueller Scope ist erfüllt, wenn:

- Floating Button klein und verschiebbar ist
- erster Zustand nur ein schmaler Eingabebalken ist
- der Nutzer direkt Text eingeben oder einfügen kann
- Ton/Stil ohne große UI änderbar ist
- Ergebnis-Panel erst nach KI-Antwort erscheint
- nur ein Vorschlag sichtbar ist
- 3 Varianten per Swipe, Pfeil oder Pager wechselbar sind
- Retry klein bleibt
- keine sichtbare Modell-/Provider-/Prompt-Technik existiert
- kein Verlauf, Profil, Gedächtnis oder Dashboard existiert
