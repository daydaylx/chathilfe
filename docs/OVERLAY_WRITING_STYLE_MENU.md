# OVERLAY_WRITING_STYLE_MENU.md — ChatHilfe

## Ziel

Der Nutzer kann Ton, Länge, Emojis, Satzzeichen, Groß-/Kleinschreibung und
Natürlichkeit **direkt im Overlay** anpassen, ohne die große Settings-Seite zu
öffnen.

## Overlay-Flow

```text
Floating Button antippen
↓
Input-Bar (Capsule) erscheint
↓
[Ton/Stil] antippen → kompaktes Stil-Menü klappt auf
↓
Chips antippen → Stil wird live aktualisiert
↓
Menü schließen (erneuter Klick auf [Ton/Stil] oder × im Menü)
↓
Text eingeben/einfügen, optional Intent-Chip wählen
↓
[Los] → Vorschläge erstellen
```

## Verfügbare Einstellungen

### Ton

| Chip | Prompt-Bedeutung |
|---|---|
| Kurz | knapp, direkt, nicht hart |
| Freundlich | warm, angenehm, nicht unterwürfig |
| Direkt | klar, ehrlich, ohne Ausschmückung |
| Sorry | Verantwortung übernehmen, nicht kriechen |
| Sanft | beruhigend, konfliktmindernd |
| Grenze | bestimmt, respektvoll, nicht aggressiv |
| Flirtend | leicht verspielt, nicht peinlich |

### Antwortlänge

| Chip | Prompt-Regel |
|---|---|
| Kurz | sehr kurz, möglichst ein Satz |
| Normal | knapp halten, 1–2 kurze Sätze |
| Etwas länger | etwas ausführlicher darf sein, aber nicht lang |

### Emojis

| Chip | Prompt-Regel |
|---|---|
| Keine | keine Emojis |
| Sparsam | sparsam Emojis, nur wenn es natürlich passt |
| Normal | Emojis dürfen, aber nicht überladen |

### Satzzeichen

| Chip | Prompt-Regel |
|---|---|
| Sauber | saubere Satzzeichen |
| Locker | lockere Satzzeichen, wie im echten Chat |
| Sehr locker | sehr lockere Satzzeichen, oft ohne Punkte |

### Groß-/Kleinschreibung

| Chip | Prompt-Regel |
|---|---|
| Korrekt | Groß-/Kleinschreibung korrekt |
| Locker | Groß-/Kleinschreibung darf locker sein |

### Natürlichkeit

| Chip | Prompt-Regel |
|---|---|
| Normal | natürliche Sprache |
| Weniger KI | natürlicher Chatstil, keine typischen KI-Formulierungen |
| Sehr locker | sehr lockerer, umgangssprachlicher Stil |

## Defaults (Issue #8)

| Dimension | Default |
|---|---|
| Ton | Freundlich |
| Länge | Normal |
| Emojis | Sparsam |
| Satzzeichen | Locker |
| Groß-/Kleinschreibung | Korrekt |
| Natürlichkeit | Weniger KI |

## Antwort-Intent-Chips (Antworten-Modus)

Im Antworten-Modus erscheinen optionale Kurz-Chips unter dem Eingabefeld:

`Zustimmen` `Absagen` `Entschuldigen` `Nachfragen` `Beruhigen` `Grenze`

- Einfachauswahl (ein Chip aktiv oder keiner)
- Der gewählte Chip wird als `userIntent`-Hinweis in den Prompt eingebaut
- Kein Chip aktiv = kein Intent-Hinweis

## Datenschutzregeln

- Stilwerte werden als reine Enum-Strings in DataStore persistiert
- Keine Nutzertexte werden gespeichert
- Keine kopierten Nachrichten werden gespeichert
- Keine generierten Antworten werden gespeichert
- Keine Retry-Instruktionen werden über die Request-Dauer hinaus aufbewahrt
- Keine Intent-Chips werden gespeichert
- Kein API-Key wird in DataStore abgelegt
- Fixe Persona-Stimme ist eine statische Prompt-Regel (D-013), kein Nutzerprofil

## PromptBuilder-Anbindung

`PromptBuilder.build(request, style)` erzeugt aus den Stilwerten einen
`{{style_rules}}`-Block im Prompt:

```text
Schreibstil (Nutzervorgabe):
- Länge: knapp halten, 1–2 kurze Sätze
- Emojis: sparsam Emojis, nur wenn es natürlich passt
- Satzzeichen: lockere Satzzeichen, wie im echten Chat
- Groß-/Kleinschreibung: Groß-/Kleinschreibung korrekt
- Natürlichkeit: natürlicher Chatstil, keine typischen KI-Formulierungen
```

Der Persona-Block (D-013) ist davon getrennt und statisch im Prompt-Template
enthalten.

## Validierung

- `./gradlew test` ✅
- `./gradlew lintDebug` ✅
- `./gradlew assembleDebug` ✅
- Statische Checks: keine Logs von Nutzertexten/Prompts, DataStore nur in SettingsStore, kein Accessibility Service

## Bekannte Risiken

- Overlay-Styling auf kleinen Displays: horizontales Scrollen für Chip-Reihen
  ist vorgesehen, aber sehr viele Chips auf sehr schmalen Geräten könnten
  unübersichtlich werden (Gerätetest Phase 8)
- Style-Panel überdeckt WhatsApp-Inhalte — akzeptiert, da es schließbar ist
- Performance bei häufigem Stil-Wechsel: jeder Chip-Tap löst `onStyleChanged`
  aus, was einen DataStore-Write triggert — für MVP akzeptabel
