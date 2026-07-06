# UI_UX_SPEC.md — ChatHilfe MVP

## Zweck

Dieses Dokument definiert die UI/UX-Regeln für den MVP.

Die App soll klein, schnell, verständlich und unaufdringlich sein.

---

## Entscheidungen aus dem Audit

Für den MVP gilt:

- MainActivity wird mit Jetpack Compose gebaut.
- Floating Button und Overlay-UI werden als klassische Android Views gebaut.
- Das Overlay muss immer eine Möglichkeit bieten, Text manuell einzugeben oder einzufügen.
- Clipboard ist Komfortfunktion, aber keine Pflichtabhängigkeit.

---

## UX-Ziel

Der Nutzer soll WhatsApp normal weiterverwenden können und nur bei Bedarf den Formulierungshelfer öffnen.

Zielgefühl:

- klein
- ruhig
- klar
- schnell
- nicht störend

Nicht-Ziel:

- keine Vollbild-App über WhatsApp
- keine komplexe Navigation
- kein Chatbot-Verlauf
- kein Dashboard
- keine unnötigen Animationen
- kein großes Formular als Startzustand

---

## Hauptflow

```text
WhatsApp öffnen
↓
Floating Button erscheint am Rand
↓
Button antippen
↓
schmaler Eingabebalken öffnet
↓
Ton prüfen oder ändern
↓
Text eingeben oder einfügen
↓
Vorschläge erstellen
↓
Overlay erweitert sich zum Ergebnis-Panel
↓
einen Vorschlag ansehen
↓
zwischen 3 Vorschlägen wechseln
↓
Vorschlag kopieren
oder bei unpassenden Vorschlägen gezielt neu versuchen
↓
manuell in WhatsApp einfügen
```

---

## MainActivity / Setup

Die MainActivity ist nur für Einrichtung gedacht:

- Berechtigungen
- API-Key-Konfigurationsstatus aus Build-Time-Konfiguration
- Overlay aktivieren/deaktivieren
- Test-Overlay
- kurze Erklärung

Nicht in die MainActivity:

- API-Key-Eingabefeld
- Modell-Auswahl
- Provider-Auswahl
- Verlauf
- Gedächtnis
- Dashboard

---

## Floating Button

Anforderungen:

- 48dp bis 56dp Startgröße
- kleiner Randbutton
- verschiebbar
- Rand-Andocken nach Drag
- nicht über WhatsApp-Sendebutton defaulten
- sichtbar nur bei WhatsApp
- Tap öffnet den Eingabebalken
- Drag löst keinen Tap aus
- Position wird gespeichert

---

## Overlay-Zustände

Das Overlay hat im MVP zwei visuelle Zustände:

1. **Input-Bar / Eingabebalken**
2. **Result-Panel / Ergebnis-Panel**

Der Startzustand ist immer der schmale Eingabebalken. Das größere Ergebnis-Panel erscheint erst nach einer KI-Antwort.

---

## Zustand 1: Eingabebalken

Der Eingabebalken ist der primäre Startzustand nach dem Öffnen über den Floating Button.

Beispielstruktur:

```text
[Ton]  Was willst du sagen?  [Einfügen] [Los]
```

Alternativ ist ein Icon-Button für Start erlaubt:

```text
[Ton]  Was willst du sagen?  [Einfügen] [>]
```

Pflichtelemente:

- Ton-/Stil-Button links
- einzeiliges oder kompakt wachsendes Texteingabefeld
- Einfügen-Button
- Start-Button für KI-Vorschläge

Regeln:

- Der Eingabebalken bleibt schmal und verdeckt WhatsApp möglichst wenig.
- Kein Ergebnisbereich vor der ersten KI-Antwort.
- Kein großes Formular als Startzustand.
- Der Start-Button darf nicht „Senden“ heißen, weil die App nichts in WhatsApp sendet.
- Erlaubte Start-Labels: `Los`, `Erstellen` oder ein schlichtes Pfeil-Icon.
- Wenn das Textfeld mehr Platz braucht, darf der Balken moderat wachsen, aber nicht zum Vollbild werden.

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
- Button zeigt den aktuell aktiven Ton kompakt an
- Tap öffnet eine kleine Chip-Zeile oder ein kleines Popover
- keine große Einstellungsansicht

---

## Einfügen-Option

Der Einfügen-Button ist eine kleine Komfortfunktion im Eingabebalken.

Regeln:

- Clipboard wird nur nach Nutzeraktion gelesen.
- Falls Clipboard-Text verfügbar ist, kann er in das Textfeld übernommen werden.
- Falls Clipboard leer oder blockiert ist, bleibt manuelle Texteingabe möglich.
- Clipboard-Probleme sind kein harter Fehler.
- Es gibt kein dauerhaftes Clipboard-Monitoring.

---

## Zustand 2: Ergebnis-Panel

Nach einer KI-Antwort erweitert sich das Overlay zu einem kompakten Ergebnis-Panel.

Ziel:

- etwas größer als der Eingabebalken
- weiterhin kein Vollbild
- WhatsApp bleibt sichtbar
- eine Antwort steht im Fokus

Beispielstruktur:

```text
1 / 3                         [x]

Aktueller Vorschlag

[Kopieren]
[Nicht passend?] [Nochmal]
[Kürzer] [Direkter] [Sanfter]
```

Regeln:

- Es ist immer nur ein Vorschlag sichtbar.
- Die 3 Vorschläge werden nicht untereinander angezeigt.
- Wechsel per Swipe ist erwünscht.
- Als MVP-Fallback sind einfache Navigationselemente erlaubt, zum Beispiel `‹ 1/3 ›`.
- Kleine Punkte wie `● ○ ○` sind optional.
- Kopieren bezieht sich immer auf den aktuell sichtbaren Vorschlag.
- Schließen führt zurück zum WhatsApp-Kontext, ohne Verlauf zu speichern.

---

## Ergebnis-Navigation

Die Vorschläge werden als Carousel / Pager dargestellt.

MVP-Regel:

```text
[‹] 1/3 [›]
```

oder:

```text
● ○ ○
```

Regeln:

- Swipe darf zusätzlich unterstützt werden.
- Pfeile oder Indikator müssen sichtbar genug sein, damit der Nutzer erkennt, dass es 3 Varianten gibt.
- Keine drei großen Karten untereinander als Standardansicht.
- Kein Verlauf der Vorschläge.
- Keine Bewertung pro Vorschlag.

---

## Retry und Änderungsoptionen

Wenn die 3 Vorschläge nicht passen, soll der Nutzer direkt und ohne neues Menü neu versuchen können.

Anzeige im Ergebnis-Panel:

```text
Nicht passend?
[Nochmal]
[Kürzer] [Lockerer] [Direkter]
[Sanfter] [Klarer] [Weniger künstlich]
```

Regeln:

- Der Retry-Bereich erscheint erst nach der ersten erfolgreichen oder teilweise erfolgreichen KI-Antwort.
- `Nochmal` erzeugt 3 neue Vorschläge mit gleichem Modus, gleichem Text, gleicher Absicht und gleichem Ton.
- Änderungs-Chips sind global für alle Vorschläge, nicht pro Vorschlagskarte.
- Maximal 1–2 Änderungs-Chips gleichzeitig aktiv.
- Die Änderungs-Chips ändern nur die nächste KI-Anfrage.
- Retry-Anweisungen werden nicht gespeichert.
- Keine Bewertung einzelner Vorschläge.
- Kein Feedback-Verlauf.
- Kein Stiltraining.
- Kein Gedächtnis.
- Keine freie Feedback-Texteingabe im MVP.

Zulässige Änderungs-Chips:

| Chip | Bedeutung |
|---|---|
| Kürzer | kompakter, weniger Wörter |
| Lockerer | weniger steif, natürlicher Alltagston |
| Direkter | klarer, weniger weichgespült |
| Sanfter | vorsichtiger, weniger hart |
| Klarer | weniger schwammig, konkreter formuliert |
| Weniger künstlich | keine typischen KI-Formulierungen, natürlicher Chatstil |

---

## Interne Modell- und Qualitätslogik

Die normale Overlay-UI bleibt bewusst einfach. Der Nutzer soll im Overlay nur diese Dinge bedienen:

- Ton/Stil
- Texteingabe oder Einfügen
- Vorschläge erstellen
- Vorschlag wechseln
- Vorschlag kopieren
- Vorschläge bei Bedarf gezielt neu erzeugen

Nicht im Overlay anzeigen:

- Modell-Auswahl
- Provider-Auswahl
- Temperature
- `max_tokens`
- Reasoning-/Thinking-Einstellungen
- Prompt-Profile
- Rollen-System
- Qualitäts-Dashboard
- technische Fallback-Regeln

Im MVP nutzt die App genau ein OpenRouter-Default-Modell. Modelle, Provider, Tokenlimits und Temperatur bleiben technische Details in `AiConfig` und erscheinen nicht im Overlay.

Ton-Chips und Retry-Chips ändern im MVP nur den Prompt, nicht die Modellwahl.

Nicht im MVP:

- Modellrouting nach Tonfall
- automatische Modell-Fallbacks
- mehrere Modelle pro Stil
- sichtbare Qualitätsauswahl wie `Schnell`, `Sehr gut` oder `Beste Qualität`

Diese Punkte sind höchstens Post-MVP und brauchen eine eigene Entscheidung.

---

## Lade- und Fehlerzustände

Während Anfrage:

- Start-Button deaktivieren
- kompakter Ladehinweis im Eingabebalken oder Ergebnis-Panel
- keine parallele Anfrage

Fehlertexte müssen kurz und verständlich sein. Keine Stacktraces in der UI.

Bei Retry-Fehler:

- bisherige Vorschläge sichtbar lassen
- kurze Fehlermeldung anzeigen
- keinen Verlauf erzeugen

---

## Anti-Nerv-Regeln

- Button nur bei WhatsApp anzeigen
- Button klein halten
- Button verschiebbar machen
- Overlay deaktivierbar machen
- Position speichern
- Overlay nie automatisch öffnen
- Eingabebalken statt großem Formular als Startzustand
- Ergebnis-Panel erst nach KI-Antwort öffnen
- keine Popups ohne Nutzeraktion
- keine KI-Anfrage ohne Nutzeraktion
- keine Modell-, Provider- oder Prompt-Einstellungen im Overlay anzeigen
- Retry-Bereich nur nach Ergebnissen anzeigen
- keine Feedback-, Bewertungs- oder Memory-Abfragen anzeigen

---

## Akzeptanzkriterien

UI/UX ist akzeptabel, wenn:

- Nutzer versteht, wofür die App ist
- Berechtigungen verständlich erklärt werden
- MainActivity keinen API-Key entgegennimmt, sondern höchstens Build-Time-Key-Status zeigt
- Floating Button stört nicht beim Tippen
- Startzustand ist ein schmaler Eingabebalken
- Eingabebalken enthält Ton, Text, Einfügen und Start
- Start-Button heißt nicht „Senden“
- Ergebnis-Panel erscheint erst nach KI-Antwort
- immer nur ein Vorschlag sichtbar ist
- Nutzer erkennt, dass es 3 Vorschläge gibt
- Wechsel zwischen Vorschlägen per Swipe oder einfacher Navigation möglich ist
- Kopieren bezieht sich eindeutig auf den sichtbaren Vorschlag
- Clipboard-Nutzung ist freiwillig
- manuelles Eingeben funktioniert, wenn Clipboard leer/blockiert ist
- Nutzer kann bei unpassenden Vorschlägen gezielt neu versuchen
- Retry-Optionen bleiben kompakt und erscheinen erst nach Ergebnissen
- Fehler sind verständlich
- Overlay bleibt frei von Modell-, Provider-, Token-, Reasoning- und Prompt-Einstellungen
- Overlay bleibt frei von Verlauf, Bewertung, Stiltraining und Gedächtnis
