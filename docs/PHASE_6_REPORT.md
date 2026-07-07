# PHASE_6_REPORT.md — ChatHilfe

## Status

Phase 6 ist code-seitig abgeschlossen: `PromptBuilder` und `AiResponseParser` (mit
`ParseResult`) sind als reine JVM-Logik ohne Provider umgesetzt und per Unit-Test abgedeckt.
`./gradlew test` ✅ (33 Tests, 0 failures) und `./gradlew lintDebug` ✅ (0 errors, 9 warnings —
alle bereits vor Phase 6 bekannt) wurden in dieser Sitzung lokal ausgeführt.

Kein Netzwerkcode, kein `AiClient`, kein `AiConfig`, kein konkretes Modell: Phase 7 bleibt
die KI-Anbindung. Das Overlay läuft weiter über `DummySuggestionSource`.

## Scope

- `PromptBuilder`: erzeugt aus `ReplyRequest` den finalen Prompt-String gemäß den drei
  Templates in `docs/PROMPTS.md` (Antworten / Formulieren / Umschreiben).
- `AiResponseParser`: toleranter Parser als `sealed class ParseResult`
  (`Success` / `Partial` / `Error`), crasht nie.
- Neue Models: `ReplyMode` (drei Modi), `ReplyRequest` (alle Anfrage-Eingaben, transient).
- Retry-Mapping: Change-Chips (`Kürzer` … `Weniger künstlich`) erzeugen Text im Prompt;
  `NOCHMAL` wird herausgefiltert („neu erzeugen, keine Änderung").
- Unit-Tests für Builder und Parser.

## Summary

Phase 6 führt nur die KI-Logik **ohne Provider** ein: Prompt-Erzeugung und Antwort-Parsing,
jeweils als zustandslose, leicht testbare JVM-Komponenten. Es entsteht ein neues Package
`de.disaai.chathilfe.ai`. Bestehende Models (`ReplySuggestion`, `RetryInstruction`,
`ToneOption`) werden nur referenziert, nicht geändert. Das Overlay, das Manifest,
`build.gradle.kts` und Settings bleiben unangetastet; `DummySuggestionSource` wird nicht
ersetzt (das ist Phase 7).

Zentrale Entscheidungen (siehe `.agent/plans/current-plan.md`):

- Parser-Vertrag = **Sealed `ParseResult`** (`Success` / `Partial` / `Error`), damit Phase 7
  sauber zwischen „3 OK", „weniger anzeigen" und „Fehlermeldung" unterscheiden kann.
- `NOCHMAL` trägt keinen Text bei (`{NOCHMAL}` → identischer Basis-Prompt).
- Tone-Bedeutungen und Retry-Bedeutungen liegen als private Maps im `PromptBuilder`, damit
  die bestehenden Model-Enums unverändert bleiben (Scope-Disziplin).
- Deutsch und „genau 3 Varianten" sind fest im Template; keine Sampling-/Thinking-Parameter
  (`docs/PROMPT_PARAMETER_POLICY.md` — Provider-spezifisch, Phase 7).
- Parser-Strategie: bevorzugt strukturierte Items (`1.`, `1)`, `- * • · –`); sonst
  Absatz-Fallback, dann Einzelzeilen-Fallback; Duplikate werden verworfen; alles in
  `try/catch`, nie werfen.

## Files changed

Neu (Phase 6):

- `app/src/main/java/de/disaai/chathilfe/model/ReplyMode.kt` — Enum Antworten / Formulieren / Umschreiben.
- `app/src/main/java/de/disaai/chathilfe/model/ReplyRequest.kt` — Datenklasse (transient).
- `app/src/main/java/de/disaai/chathilfe/ai/PromptBuilder.kt` — drei Templates + Tone-/Retry-Maps + `NOCHMAL`-Filter.
- `app/src/main/java/de/disaai/chathilfe/ai/AiResponseParser.kt` — `ParseResult` + Parser.
- `app/src/test/java/de/disaai/chathilfe/ai/PromptBuilderTest.kt` — 8 Tests.
- `app/src/test/java/de/disaai/chathilfe/ai/AiResponseParserTest.kt` — 12 Tests.
- `docs/PHASE_6_REPORT.md` — dieser Bericht.
- `README.md` — Status auf Phase 6 aktualisiert (Statuszeilen, Doku-Übersicht, „aktueller Schritt").

Geändert wurde **keine** bestehende App-Datei (Overlay, Manifest, Gradle, Settings). Kein
`AiClient`, kein `AiConfig`, kein HTTP, kein Modell-Pinning.

## Validation

### Build / Test / Lint (in dieser Sitzung lokal ausgeführt)

```text
./gradlew test        → BUILD SUCCESSFUL
./gradlew lintDebug   → BUILD SUCCESSFUL
```

Unit-Test-Ergebnisse (`app/build/test-results/testDebugUnitTest/*.xml`):

| Suite | Tests | Failures | Errors |
|---|---|---|---|
| `ai.AiResponseParserTest` | 12 | 0 | 0 |
| `ai.PromptBuilderTest` | 8 | 0 | 0 |
| `model.RetryInstructionTest` | 4 | 0 | 0 |
| `model.ToneOptionTest` | 4 | 0 | 0 |
| `overlay.SuggestionPagerTest` | 5 | 0 | 0 |
| **Summe** | **33** | **0** | **0** |

Lint: `0 errors, 9 warnings` (`app/build/reports/lint-results-debug.txt`). Keiner der 9
Warnings verweist auf eine Phase-6-Datei; es sind die bereits seit Phase 4 bekannten Warnings
(`InlinedApi`, `OldTargetApi`, `ObsoleteSdkInt`, `UnusedResources` ×2,
`MonochromeLauncherIcon` ×2, `UseKtx`, `ClickableViewAccessibility`) — siehe
`docs/PHASE_4_REPORT.md`.

`./gradlew assembleDebug` wurde in dieser Sitzung nicht erneut ausgeführt, ist aber nicht
blockiert (`compileDebugKotlin` war Teil des `test`-Laufs und erfolgreich; SDK mit platform-37
und build-tools 36.0.0 vorhanden, Gradle-Wrapper 9.6.1 lokal gecacht).

### Statische Privatsphäre-/Netzwerk-Prüfung

```text
grep -RnE "Log\.|DataStore|SharedPreferences|HttpURLConnection|OkHttp|OpenRouter|AiClient" \
    app/src/main/java/de/disaai/chathilfe/ai \
    app/src/main/java/de/disaai/chathilfe/model/ReplyMode.kt \
    app/src/main/java/de/disaai/chathilfe/model/ReplyRequest.kt
```

Ergebnis: keine Treffer. Die neuen `ai/`-Dateien enthalten kein Logging, keine Persistenz und
keinen Netzwerkcode. Der Prompt-String ist rein transient; Retry bleibt nur Bestandteil des
Prompt-Strings und wird nirgendwo gespeichert.

### Akzeptanzkriterien (`docs/IMPLEMENTATION_PLAN.md` Phase 6)

- jeder Modus erzeugt passenden Prompt ✅ (`PromptBuilderTest`: reply/compose/rewrite).
- Retry-Anweisung nur, wenn für diese Anfrage aktiv gesetzt ✅ (`retry text appears only …`).
- Retry-Anweisung im Prompt berücksichtigt, aber nicht als Profil gespeichert ✅ (transienter
  String, keine Persistenz, kein Log).
- Parser extrahiert robust ✅ (`AiResponseParserTest`: `1.`, `1)`, Bullets, Absatz-/Zeilen-Fallback).
- kein Crash bei schlechter Modellantwort ✅ (`malformed input never throws`, `try/catch`).

## Not validated

- `./gradlew assembleDebug` nicht in dieser Sitzung erneut ausgeführt (siehe oben; nicht
  blockiert, nur nicht abgelaufen).
- Gerätetest: nicht durchgeführt (laut `docs/DEVICE_TEST_POLICY.md` gebündelt in Phase 8).
  Phase 6 ist ohnehin reine JVM-Logik ohne Gerätetest-Anteil.
- Keine Integration in das Overlay: `PromptBuilder`/`AiResponseParser` sind aktuell nirgendwo
  verdrahtet (gewollt — die Verdrahtung mit echtem Provider kommt in Phase 7).
- Echte Modellantwortqualität: nicht testbar ohne Provider (Phase 7). Parser-Robustheit ist
  nur gegen synthetische Antworten getestet.

## Known risks

- Parser-Heuristik ist auf kurze MVP-Antworten ausgelegt. Sehr lange oder stark abweichende
  Modellantworten könnten die Absatz-/Zeilen-Fallbacks unerwartet interpretieren; die Tests
  decken repräsentative Fälle ab, aber nicht jede reale Modellantwort. Risiko bleibt bis Phase 7
  (dann gegen echte Antworten validieren) offen.
- Keine echte Retry-Qualitätsprüfung („Retry wirkt still, ohne erklärt zu werden") möglich,
  solange kein Provider angeschlossen ist (Phase 7 / Gerätetest Phase 8).
- Lint-Warnings (9) bleiben offen, bis sie gezielt angegangen werden; sie sind nicht durch
  Phase 6 entstanden.

## Next sensible step

Phase 7 — KI-Anbindung gemäß `docs/IMPLEMENTATION_PLAN.md`. Vor Phase 7 zu erledigen:

- konkretes OpenRouter-Default-Modell in `AiConfig` pinnen (`docs/DECISIONS.md` offener Punkt;
  D-001/D-009).
- API-Key aus lokaler Build-Time-Konfiguration (`BuildConfig`) lesen, Key nicht committen.
- `AiClient` (OpenRouter, genau ein Provider/Modell), Lade-/Fehlerzustände, Antwort über
  `AiResponseParser` parsen, 3 Vorschläge ans Result-Panel, Retry als neue bewusste Anfrage.
- `DummySuggestionSource` dann ersetzen.

Vor Phase 7 sollten außerdem die 9 offenen Lint-Warnings gesichtet werden, sofern das nicht
bereits früher geschieht.
