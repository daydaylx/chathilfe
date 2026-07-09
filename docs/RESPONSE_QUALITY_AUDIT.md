# RESPONSE_QUALITY_AUDIT.md — ChatHilfe

## Status

Audit zur aktuellen Antwortqualität nach Phase 7.

Auslöser: Die App funktioniert technisch auf den ersten Blick, aber die generierten Antworten fühlen sich im Test noch komisch, zu künstlich oder nicht passend genug an.

Dieses Dokument bewertet die wahrscheinlichen Ursachen, den aktuellen Codepfad, die Ziel-Persona und eine Modellstrategie für natürlichere WhatsApp-nahe Antworten.

### Umsetzungsstand (Stand 2026-07-08)

Die fünf Qualitätsfehler werden wie folgt adressiert:

| # | Fehler | Status |
|---|---|---|
| 1 | Modus und Kontext (sehr hoch) | **erledigt** durch Overlay-Redesign: `buildRequest()` mappt `REPLY→copiedMessage`, `COMPOSE→userIntent`, Default-Modus `REPLY`; Kontext-Vorschau + Entfernen in der Input-Bar ergänzt |
| 2 | Clipboard ist kein Antwort-Kontext | **erledigt**: Eingabefeld = kopierte Nachricht (`copiedMessage`), Antwort-Chips = `userIntent`; UX folgt der Capsule-Ausrichtung |
| 3 | Prompts zu allgemein | **erledigt**: härtere WhatsApp-Stilregeln in `PromptBuilder`/`docs/PROMPTS.md` (1–2 Sätze, keine Floskeln, keine Therapiesprache) |
| 4 | Keine Persona | **entschieden als D-013**: feste App-Stimme im Prompt, klar abgegrenzt vom verbotenen Profilbegriff; dokumentiert in `docs/PRIVACY_SECURITY.md` |
| 5 | Claude Sonnet 5 zu professionell | **Default geändert**: `deepseek/deepseek-v4-flash` ist auf Nutzerauftrag gepinnt; A/B gegen Sonnet/Haiku/GPT Mini bleibt offen |

Pflicht 1–5 aus „Konkrete nächste Umsetzung / Phase 7.5“ sind damit erledigt bzw.
entschieden; Testset (Punkt 6) liegt in `docs/TEST_PLAN.md` (Abschnitt
„Antwortqualitäts-Testset (A/B)“). Issue #8 ist zusätzlich umgesetzt:
Schreibstil-Werte werden lokal als reine Enum-Settings gespeichert und als
`{{style_rules}}` in den Prompt eingebunden (D-014). Ein lokaler Modell-
Testschalter (Punkt 7) ist im MVP nicht umgesetzt — der A/B-Vergleich läuft
manuell über das Testset.

---

## Kurzurteil

Das Hauptproblem ist sehr wahrscheinlich **nicht nur das Modell**, sondern die Kombination aus:

1. falscher oder unvollständiger Modus-Verdrahtung,
2. fehlendem echten Antwort-Kontext,
3. zu allgemeinem Prompt-Stil,
4. fehlender Zielnutzer-/Sprech-Persona,
5. einem sehr leistungsfähigen, aber tendenziell zu professionell formulierenden Modell.

Der aktuelle App-Default ist jetzt `deepseek/deepseek-v4-flash`. `anthropic/claude-sonnet-5` bleibt als starke Referenz wichtig, wirkte aber für kurze private WhatsApp-Antworten tendenziell zu professionell/glatt.

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
deepseek/deepseek-v4-flash
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

Dieser Fehler war vor Phase 7.5 der Hauptverdacht: die erste Anfrage wurde fest als
`COMPOSE` gebaut und Retry nutzte ebenfalls `COMPOSE`. Das ist inzwischen behoben.

Aktueller Stand:

```kotlin
ReplyMode.REPLY   -> copiedMessage = text, userIntent = replyIntentChip
ReplyMode.COMPOSE -> userIntent = text
```

Retry baut erneut aus demselben transienten Modus/Kontext und verwendet damit
denselben Antwort-Kontext. Zusätzlich zeigt der Antworten-Modus eine kompakte
Kontext-Vorschau und bietet „Kontext entfernen".

### Bewertung

Priorität war: **sehr hoch**

Status: **erledigt**. Ohne diese Trennung konnte kein Modell zuverlässig
natürlich antworten; sie ist jetzt Grundlage für weitere Qualitätsarbeit.

---

## Zweiter Qualitätsfehler: Clipboard ist kein Antwort-Kontext

Der Einfügen-Button liest die Zwischenablage bewusst nach Nutzeraktion. Das ist Datenschutz-technisch korrekt.

Der Capsule-Flow trennt die Bedeutung inzwischen so:

```text
copiedMessage = Nachricht der anderen Person (Textfeld im Antworten-Modus)
userIntent    = optionaler Antwort-Hinweis aus Kurz-Chip
```

Der Nutzer sieht bei vorhandenem Antworttext eine kompakte Kontext-Vorschau und
kann den Kontext entfernen. „Ändern" bleibt über das editierbare Feld bzw. erneutes
Einfügen möglich.

Der Request entsteht so:

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
| `deepseek/deepseek-v4-flash` | Aktueller Default auf Nutzerauftrag. Schnelles DeepSeek-V4-Flash-Modell mit guter Chance auf kürzere, natürlichere WhatsApp-Antworten. A/B noch offen. |

### Vergleichskandidaten für ChatHilfe

| Priorität | Modell | Einschätzung |
|---:|---|---|
| 1 | `anthropic/claude-sonnet-5` | Sehr starke Referenz, aber für private Kurzantworten vermutlich zu professionell/glatt. |
| 2 | `anthropic/claude-haiku-4.5` | Wahrscheinlich besser für schnelle, kurze, natürlichere Alltagsantworten. Muss mit echten Beispielen getestet werden. |
| 3 | `openai/gpt-5-mini` | Gute Chance auf natürlichen Chatstil. Muss hinsichtlich Kosten, Verfügbarkeit und Parameter geprüft werden. |
| 4 | `openai/gpt-4.1-mini` | Optionaler Mini-Vergleichskandidat. |
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
Default: deepseek/deepseek-v4-flash
Referenz: anthropic/claude-sonnet-5
Test 1: anthropic/claude-haiku-4.5
Test 2: openai/gpt-5-mini
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
| Kosten/Latenz | Ist es für private Nutzung bezahlbar und schnell genug? |

DeepSeek ist jetzt als Default gepinnt; ein erneuter Wechsel erfolgt nur, wenn ein Kandidat im Schnitt deutlich besser abschneidet.

---

## Umsetzungsstand Phase 7.5

### Antwortmodus und Qualitätskalibrierung

Erledigt:

1. Modusauswahl in der Input-Bar:
   - Antworten
   - Schreiben/Formulieren
   - Umschreiben bleibt vorerst ausgeblendet
2. Clipboard-/Antwort-Kontext separat verwalten:
   - `copiedMessage`
   - Kontext-Vorschau anzeigen
   - Kontext entfernen möglich
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
6. Schreibstil-Einstellungen ergänzen (D-014): Länge, Emojis, Satzzeichen,
   Groß-/Kleinschreibung, Natürlichkeit.
7. Testset ergänzen.
8. Modellvergleich vorbereiten: konkrete Slugs geprüft; A/B läuft manuell über
   `docs/TEST_PLAN.md`, kein lokaler Modell-Testschalter als Nutzerfeature.

---

## Umsetzungsstand Phase 7.6 (Dialog-Kontext, Issue #19)

Erledigt (Commit `145524b`, 2026-07-09):

- `WhatsAppChatParser` (pure Kotlin) erkennt eingefügte Dialogblöcke im Export-Layout
  `[Datum, Uhrzeit] Sprecher: Nachricht`, tolerant bei Datum-/Uhrzeitvarianten und
  mehrzeiligen Nachrichten; ab zwei Treffern, sonst Einzeltext-Fallback.
- `ParsedChatMessage`/`ParsedChatContext` mit heuristischen `likelySelf/likelyOtherSender`
  (keine harte Identität, keine Speicherung).
- `ReplyRequest.conversationContext` (rein transient).
- `PromptBuilder`: Antworten-Template mit optionaler Verlaufssektion, an `docs/PROMPTS.md`
  angepasst; `OverlayService.buildRequest()` nutzt die letzte Nachricht des Gegenübers als
  Antwortanlass und den Rest als Kontext.
- Unit-Tests für Parser, Fallback und PromptBuilder.

Damit ist einer der im Abschlussfazit genannten Hebel („Gesprächskontext richtig
modellieren") strukturtechnisch umgesetzt. Offen bis Phase 8: Wirkung auf echte Antworten
mit echtem Key sowie Parser-Verhalten gegen reale WhatsApp-Exporte auf Gerät.

---

## Risiken

| Risiko | Bewertung |
|---|---|
| Modellwechsel ohne Modus-Fix | hoch — löst Hauptproblem wahrscheinlich nicht |
| Persona zu hart codiert | mittel — kann unpassend sein, daher als Stilannahme formulieren |
| `latest`-Alias dauerhaft nutzen | mittel — Ergebnis kann sich ohne Codeänderung ändern |
| Modellwechsel ohne A/B | mittel — DeepSeek ist gepinnt, Qualität muss aber noch manuell geprüft werden |
| Zu viele Regeln im Prompt | mittel — kann Antworten steif machen |

---

## Empfehlung als Entscheidung

Kurzfristig:

```text
Nicht Modell sofort ersetzen, sondern erst Antwortmodus + Persona + Promptstil fixen.
```

Danach:

```text
A/B-Test mit DeepSeek Chat, Sonnet 5, Claude Haiku 4.5 und GPT Mini.
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
