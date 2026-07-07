# RESPONSE_QUALITY_AUDIT.md — ChatHilfe

## Status

Audit zur aktuellen Antwortqualität nach Phase 7.

Auslöser: Die App funktioniert technisch auf den ersten Blick, aber die generierten Antworten fühlen sich im Test noch komisch, zu künstlich oder nicht passend genug an.

Dieses Dokument bewertet die wahrscheinlichen Ursachen, den aktuellen Codepfad, die Ziel-Persona und eine Modellstrategie für natürlichere WhatsApp-nahe Antworten.

---

## Kurzurteil

Das Hauptproblem ist sehr wahrscheinlich **nicht nur das Modell**, sondern die Kombination aus:

1. falscher oder unvollständiger Modus-Verdrahtung,
2. fehlendem echten Antwort-Kontext,
3. zu allgemeinem Prompt-Stil,
4. fehlender Zielnutzer-/Sprech-Persona,
5. einem sehr leistungsfähigen, aber tendenziell zu professionell formulierenden Modell.

Aktuell ist `anthropic/claude-sonnet-5` nicht grundsätzlich falsch, aber für kurze private WhatsApp-Antworten vermutlich **nicht der beste Default**, solange keine starke Persona- und Stilkalibrierung aktiv ist.

---

## Aktueller technischer Stand

Phase 7 ist umgesetzt:

- `AiConfig`
- `AiClient`
- `OpenRouterJson`
- `PromptBuilder`
- `AiResponseParser`
- echtes OpenRouter-Modell
- Result-Panel bekommt echte Vorschläge
- Retry löst echte neue Anfrage aus

Aktuell gepinntes Modell:

```text
anthropic/claude-sonnet-5
```

OpenRouter-Konfiguration:

- ein Provider
- ein Modell
- kein Routing
- kein Fallback
- keine Sampling-Parameter
- `max_tokens = 1024`

---

## Wichtigster Qualitätsfehler: Modus und Kontext

Die Codebasis kennt drei Modi:

- `REPLY` — Antworten auf kopierte/eingefügte Nachricht
- `COMPOSE` — neue Nachricht aus Nutzerabsicht formulieren
- `REWRITE` — vorhandenen Text umschreiben

Der tatsächliche Overlay-Flow baut die erste Anfrage aktuell aber fest als:

```kotlin
ReplyRequest(
    mode = ReplyMode.COMPOSE,
    userIntent = pendingText,
    tone = pendingTone,
)
```

Retry nutzt ebenfalls fest `COMPOSE`.

### Auswirkung

Wenn der Nutzer eine WhatsApp-Nachricht kopiert und einfügt, behandelt die App diesen Text nicht sauber als **Nachricht der anderen Person**, sondern als normalen Nutzerwunsch.

Das erklärt viele komische Antworten:

```text
Eigentlich gemeint:
"Das ist die Nachricht, auf die ich antworten will."

Aktuell interpretiert:
"Das ist der Wunsch/Text, aus dem ich eine neue Nachricht formulieren soll."
```

### Bewertung

Priorität: **sehr hoch**

Ohne echten Antwort-Modus mit getrenntem Kontext kann kein Modell zuverlässig natürlich antworten.

---

## Zweiter Qualitätsfehler: Clipboard ist kein Antwort-Kontext

Der Einfügen-Button liest die Zwischenablage bewusst nach Nutzeraktion. Das ist Datenschutz-technisch korrekt.

Aktuell landet der kopierte Text aber direkt im normalen Eingabefeld.

Es fehlt eine Trennung:

```text
copiedMessage = Nachricht der anderen Person
userIntent    = was ich ungefähr antworten will
```

### Zielzustand

Für Antworten sollte die UI so arbeiten:

```text
[Antworten] [Formulieren] [Umschreiben optional später]

Antwort auf:
"Hab gehört ihr haut morgen ab. Können wir uns nochmal treffen...?"
[ändern] [entfernen]

Was willst du grob sagen?
[...]
```

Dann muss der Request so entstehen:

```kotlin
ReplyRequest(
    mode = ReplyMode.REPLY,
    copiedMessage = copiedMessage,
    userIntent = userIntent,
    tone = tone,
)
```

---

## Dritter Qualitätsfehler: Prompts sind korrekt, aber zu allgemein

Die Prompts sagen bereits:

- natürlich schreiben
- keine Analyse
- keine Erklärung
- keine übertriebene Höflichkeit
- genau 3 Varianten

Das reicht für technische Funktion, aber noch nicht für gute WhatsApp-Antworten.

Es fehlen härtere Stilregeln:

- wie WhatsApp, nicht wie E-Mail
- maximal 1–2 kurze Sätze
- keine Formulierungen wie „Vielen Dank für deine Nachricht“
- keine Sätze wie „Ich verstehe, dass...“
- keine künstliche Therapiesprache
- lieber normal als perfekt
- nicht zu sauber formulieren
- kurze Alltagssprache
- direkte Reaktion auf den kopierten Kontext

### Negativbeispiel

```text
Vielen Dank für deine Nachricht. Morgen ist es bei uns leider etwas schwierig, aber eventuell könnten wir eine kurze Begegnung ermöglichen.
```

### Besser

```text
Morgen wird eher knapp, aber kurz könnte vielleicht noch gehen.
```

---

## Vierter Qualitätsfehler: Keine Zielnutzer-Persona

Für private Chatantworten ist wichtig, **wer ungefähr schreibt**.

Der Nutzer sollte nicht jedes Mal als komplett neutrale KI-Stimme klingen. Für diese App ist eine einfache Kommunikations-Persona sinnvoll.

### Vorgeschlagene Arbeitsannahme

Nicht als echte personenbezogene Tatsache speichern, sondern als **kommunikative Stilannahme** im Prompt:

```text
Die Nachricht soll so klingen, als käme sie von einer normalen Frau um die 30 mit normalem Bildungsstand, natürlicher Alltagssprache und ohne übertrieben perfekte Formulierungen.
```

### Warum das hilft

Der Prompt bekommt dadurch ein klareres Ziel:

- nicht zu akademisch
- nicht zu geschäftlich
- nicht zu jugendlich
- nicht zu künstlich
- nicht zu formell
- normale Alltagssprache

### Datenschutzregel

Diese Persona darf nicht als Profil, Gedächtnis oder echte Identität gespeichert werden.

Empfohlen:

- als statische App-Prompt-Regel im MVP
- später optional als lokale Einstellung
- nicht an Analytics
- nicht in Logs
- nicht als Nutzerprofil interpretieren

---

## Fünfter Qualitätsfehler: Claude Sonnet 5 ist evtl. zu professionell

`anthropic/claude-sonnet-5` ist ein sehr starkes Modell. Für Coding, Agenten, lange Aufgaben und professionelles Schreiben ist es naheliegend.

Für sehr kurze private WhatsApp-Antworten kann es aber zu glatt wirken:

- zu korrekt
- zu erklärend
- zu höflich
- zu wenig Alltag
- zu wenig „normaler Chat“

Das bedeutet nicht, dass Claude Sonnet 5 ungeeignet ist. Es bedeutet: Es braucht harte Stilführung oder ein anderes Default-Modell.

---

## Modell-Einschätzung

Quelle der Modell-Lage: OpenRouter Modellliste und aktuelle Repo-Konfiguration. OpenRouter stellt Modell-Slugs über `/api/v1/models` bereit und dokumentiert Chat-Completions über `/api/v1/chat/completions`.

### Aktuelles Modell

| Modell | Einschätzung |
|---|---|
| `anthropic/claude-sonnet-5` | Sehr stark, aber für private Kurzantworten vermutlich zu professionell. Gut als Qualitätsmodell, nicht zwingend bester Default für WhatsApp-Ton. |

### Bessere Kandidaten für ChatHilfe

| Priorität | Modell | Einschätzung |
|---:|---|---|
| 1 | `~anthropic/claude-haiku-latest` oder konkretes Haiku-Modell nach Test | Wahrscheinlich besser für schnelle, kurze, natürlichere Alltagsantworten. Günstiger und weniger „großes Profi-Schreibmodell“ als Sonnet. Muss mit echten Beispielen getestet werden. |
| 2 | `~openai/gpt-mini-latest` oder konkretes GPT-Mini-Modell nach Test | Gute Chance auf natürlicheren Chatstil. Potenziell besser für kurze Messenger-Antworten als Sonnet. Muss hinsichtlich Kosten, Verfügbarkeit und Parameter geprüft werden. |
| 3 | `~openai/gpt-latest` oder konkretes GPT-Modell nach Test | Wahrscheinlich sehr gute Antwortqualität, aber teurer/überdimensionierter. Eher Premium-Testkandidat. |
| 4 | `anthropic/claude-sonnet-5` | Behalten als Referenzmodell, aber erst nach Prompt-/Persona-Tuning erneut bewerten. |
| 5 | `~google/gemini-flash-latest` | Schnell und stark, aber wegen Reasoning-/Parameterverhalten für simple Chatantworten nicht automatisch ideal. Nur als Testkandidat. |

### Empfehlung

Nicht sofort blind das Modell wechseln.

Besser:

1. Antwort-Modus korrekt verdrahten.
2. Persona und WhatsApp-Stilregeln ergänzen.
3. Testset mit 20 realistischen Fällen bauen.
4. Modelle gegeneinander vergleichen.
5. Danach ein Modell pinnen.

Wenn sofort ein alternatives Testmodell gewählt werden soll:

```text
Test 1: ~anthropic/claude-haiku-latest
Test 2: ~openai/gpt-mini-latest
Referenz: anthropic/claude-sonnet-5
```

Für den endgültigen MVP sollte kein `latest`-Alias dauerhaft gepinnt bleiben. `latest` ist gut zum Testen, aber nicht stabil genug für reproduzierbare APK-Builds. Nach dem Test sollte ein konkreter Modell-Slug in `AiConfig.MODEL` dokumentiert werden.

---

## Empfohlenes A/B-Testset

Für eine fundierte Entscheidung sollten mindestens diese Fälle getestet werden:

### Antworten

```text
Kopierte Nachricht:
Hab gehört ihr haut morgen ab. Können wir uns nochmal treffen oder passt das eher schlecht?

Nutzerabsicht:
sagen dass es knapp ist aber vielleicht kurz geht

Erwartung:
kurz, normal, nicht zu förmlich
```

```text
Kopierte Nachricht:
Warum meldest du dich nie richtig?

Nutzerabsicht:
sagen dass es nicht böse gemeint war und ich gerade viel um die Ohren habe

Erwartung:
ruhig, nicht defensiv, nicht therapeutisch
```

```text
Kopierte Nachricht:
Kannst du mir das heute noch schicken?

Nutzerabsicht:
sagen ja später wenn ich zuhause bin

Erwartung:
kurz und alltagstauglich
```

### Formulieren

```text
Nutzerabsicht:
fragen ob sie gut angekommen ist

Erwartung:
normale kurze WhatsApp-Frage
```

```text
Nutzerabsicht:
absagen weil ich zu müde bin aber freundlich bleiben

Erwartung:
nicht zu entschuldigend, nicht zu förmlich
```

### Umschreiben

```text
Originaltext:
Keine Ahnung, mach halt was du willst.

Gewünschte Änderung:
sanfter aber trotzdem genervt

Erwartung:
Kern bleibt, aber weniger hart
```

---

## Bewertungsraster für Modelltests

Jede Antwort mit 1–5 bewerten:

| Kriterium | Frage |
|---|---|
| Natürlichkeit | Klingt es wie WhatsApp? |
| Kürze | Ist es kurz genug? |
| Passung | Reagiert es wirklich auf den Kontext? |
| Ton | Passt der gewählte Ton? |
| Nicht-KI-Gefühl | Klingt es nicht nach ChatGPT/Brief/E-Mail? |
| Kopierbarkeit | Kann man es direkt senden? |

Modell nur wechseln, wenn es im Schnitt deutlich besser ist als Sonnet nach Prompt-Fix.

---

## Konkrete nächste Umsetzung

### Phase 7.5 — Antwortmodus und Qualitätskalibrierung

Pflichtpunkte:

1. Modusauswahl in der Input-Bar oder als kompakter Chip:
   - Antworten
   - Formulieren
   - Umschreiben optional später
2. Clipboard-Kontext separat verwalten:
   - `copiedMessage`
   - Vorschau anzeigen
   - entfernen möglich
3. `ReplyRequest(REPLY, copiedMessage, userIntent, tone)` korrekt bauen.
4. `PromptBuilder` stilistisch schärfen:
   - WhatsApp
   - 1–2 Sätze
   - keine E-Mail-Sprache
   - keine KI-Floskeln
5. Persona-Regel ergänzen:
   - normale Frau um die 30
   - normaler Bildungsstand
   - Alltagssprache
   - nicht künstlich perfekt
6. Testset ergänzen.
7. Optional Modell-Testschalter nur für lokale Entwicklung, nicht als Nutzer-UI.

---

## Risiken

| Risiko | Bewertung |
|---|---|
| Modellwechsel ohne Modus-Fix | hoch — löst Hauptproblem wahrscheinlich nicht |
| Persona zu hart codiert | mittel — kann unpassend sein, daher als Stilannahme formulieren |
| `latest`-Alias dauerhaft nutzen | mittel — Ergebnis kann sich ohne Codeänderung ändern |
| Sonnet weiter nutzen ohne Prompt-Fix | hoch — Antworten bleiben vermutlich zu glatt |
| Zu viele Regeln im Prompt | mittel — kann Antworten steif machen |

---

## Empfehlung als Entscheidung

Kurzfristig:

```text
Nicht Modell sofort ersetzen, sondern erst Antwortmodus + Persona + Promptstil fixen.
```

Danach:

```text
A/B-Test mit Sonnet 5, Claude Haiku Latest und GPT Mini Latest.
```

Wahrscheinlich bester Default nach aktuellem Stand:

```text
Claude Haiku Latest oder GPT Mini Latest
```

Wahrscheinlich beste Referenz für Qualität:

```text
Claude Sonnet 5
```

Wahrscheinlich nicht nötig:

```text
Großes Reasoning-/Agentenmodell für einfache WhatsApp-Antworten.
```

---

## Abschlussfazit

Die komischen Antworten entstehen wahrscheinlich vor allem, weil die App aktuell echte KI zwar nutzt, aber den Gesprächskontext noch nicht richtig modelliert. Die KI bekommt zu wenig Information darüber, ob sie antworten, formulieren oder umschreiben soll. Zusätzlich fehlt eine klare Nutzer-/Sprech-Persona.

Ein besseres Modell kann helfen, aber der wichtigste Hebel ist:

```text
REPLY-Modus + getrennter kopierter Kontext + kurze WhatsApp-Persona + gezieltes Testset.
```
