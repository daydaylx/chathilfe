# UI_UX_SPEC.md — ChatHilfe MVP

## Zweck

Dieses Dokument definiert die UI/UX-Regeln für den MVP.

Die App soll klein, schnell, verständlich und unaufdringlich sein.

---

## Entscheidungen aus dem Audit

Für den MVP gilt:

- MainActivity wird mit Jetpack Compose gebaut.
- Floating Button und ReplyPanel im Overlay werden als klassische Android Views gebaut.
- Das ReplyPanel muss immer eine Möglichkeit bieten, Text manuell einzufügen.
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
optional Clipboard verwenden oder Text manuell einfügen
↓
Absicht eingeben
↓
Ton wählen
↓
Vorschläge erstellen
↓
Vorschlag kopieren
oder bei unpassenden Vorschlägen einmal gezielt neu versuchen
↓
manuell in WhatsApp einfügen
```

---

## MainActivity / Setup

Die MainActivity ist nur für Einrichtung gedacht:

- Berechtigungen
- API-Key
- Overlay aktivieren/deaktivieren
- Test-Overlay
- kurze Erklärung

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

---

## ReplyPanel

Das ReplyPanel ist ein kleines Formular, kein Chatfenster.

Empfehlung:

- Breite: 88–94 % Displaybreite
- maximale Höhe: 70–80 % Displayhöhe
- Inhalt scrollbar
- nicht Vollbild
- einfach schließbar

Pflichtfelder:

- Modusauswahl
- optional Clipboard-Vorschau
- Textfeld für manuelles Einfügen
- Eingabefeld für Nutzerabsicht
- Ton-Auswahl
- Generate-Button
- Ladezustand
- Fehlerzustand
- 3 Vorschlagskarten
- Kopieren-Button pro Vorschlag
- kompakter Retry-Bereich nach erzeugten Vorschlägen

---

## Modusauswahl

Modi:

- Antworten
- Formulieren
- Umschreiben

Default:

- Clipboard erkannt → Antworten
- kein Clipboard → Formulieren

---

## Clipboard-UX und manueller Fallback

Wenn Clipboard-Text vorhanden ist:

```text
Kopierter Text erkannt
„<Vorschau>“
[Verwenden] [Ignorieren]
```

Wenn Clipboard nicht lesbar oder leer ist:

```text
Keine kopierte Nachricht erkannt.
Du kannst den Text auch manuell einfügen.
[Textfeld]
```

Regeln:

- Vorschau maximal 2–3 Zeilen
- langen Text kürzen
- Text erst nach „Verwenden“ übernehmen
- „Ignorieren“ sichtbar anbieten
- manuelles Einfügen immer ermöglichen
- Clipboard-Probleme nicht als harten Fehler behandeln

---

## Eingaben

Antworten-Modus:

```text
Nachricht, auf die du antworten willst
```

```text
Was willst du ausdrücken?
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

---

## Interne Modell- und Qualitätslogik

Die normale Overlay-UI bleibt bewusst einfach. Der Nutzer soll im ReplyPanel nur diese Dinge bedienen:

- Modus
- optional Clipboard oder manuelle Texteingabe
- Nutzerabsicht
- Ton-Auswahl
- Vorschläge erstellen
- Vorschläge kopieren
- Vorschläge bei Bedarf gezielt neu erzeugen

Nicht im ReplyPanel anzeigen:

- Modell-Auswahl
- Provider-Auswahl
- Temperature
- `max_tokens`
- Reasoning-/Thinking-Einstellungen
- Prompt-Profile
- Rollen-System
- Qualitäts-Dashboard
- technische Fallback-Regeln

Modelle, Provider, Tokenlimits, Temperatur, Fallbacks und mögliche Tonfall-zu-Modell-Zuordnungen sind interne technische Details.

Wenn mehrere Modelle genutzt werden, entscheidet die App intern anhand von Modus, Tonfall und Verfügbarkeit. Der Nutzer sieht weiterhin nur verständliche Ton-Chips wie `freundlich`, `direkt` oder `deeskalierend`.

Eine sichtbare Qualitätsauswahl wie `Schnell`, `Sehr gut` oder `Beste Qualität` ist kein MVP-Bestandteil. Falls sie später ergänzt wird, muss sie als optionales Post-MVP-Feature behandelt werden und darf das ReplyPanel nicht überladen.

---

## Lade- und Fehlerzustände

Während Anfrage:

- Generate-Button deaktivieren
- Ladehinweis anzeigen
- keine parallele Anfrage

Fehlertexte müssen kurz und verständlich sein. Keine Stacktraces in der UI.

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

---

## Retry und Änderungsoptionen

Wenn die 3 Vorschläge nicht passen, soll der Nutzer direkt und ohne neues Menü neu versuchen können.

Anzeige erst nach erzeugten Vorschlägen:

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

## Anti-Nerv-Regeln

- Button nur bei WhatsApp anzeigen
- Button klein halten
- Button verschiebbar machen
- Overlay deaktivierbar machen
- Position speichern
- Panel nie automatisch öffnen
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
- Button stört nicht beim Tippen
- Panel ist schnell bedienbar
- Modi sind klar
- Clipboard-Nutzung ist freiwillig
- manuelles Einfügen funktioniert, wenn Clipboard leer/blockiert ist
- Vorschläge sind leicht kopierbar
- Nutzer kann bei unpassenden Vorschlägen gezielt neu versuchen
- Retry-Optionen bleiben kompakt und erscheinen erst nach Ergebnissen
- Fehler sind verständlich
- ReplyPanel bleibt frei von Modell-, Provider-, Token-, Reasoning- und Prompt-Einstellungen
- ReplyPanel bleibt frei von Verlauf, Bewertung, Stiltraining und Gedächtnis
