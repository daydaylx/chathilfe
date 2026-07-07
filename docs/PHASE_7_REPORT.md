# PHASE_7_REPORT.md — ChatHilfe

## Status

Phase 7 ist code-seitig umgesetzt: echte KI-Vorschläge über OpenRouter (einziges Provider/Modell
`anthropic/claude-sonnet-5`), `AiConfig`/`AiClient` ohne neue Dependency, `DummySuggestionSource`
ersetzt. Das Overlay ist jetzt an die echte KI verdrahtet (Initial-Anfrage + Retry), mit
Lade-/Fehlerzuständen. `./gradlew assembleDebug test lintDebug` ✅ wurden in dieser Sitzung lokal
ausgeführt. Echte Modellqualität, Lade-/Fehler-UX und Netzwerk-/Rate-Limit-Verhalten bleiben dem
Gerätetest in Phase 8 vorbehalten.

## Summary

Phase 7 schließt die KI-Anbindung an: ein Provider (OpenRouter), ein gepinntes Modell, kein
Routing/Fallback, keine Sampling-Parameter. Die Provider-Logik ist in drei schlanke,
provider-spezifische Klassen aufgeteilt und an das bestehende Overlay gekoppelt.

- **Modell gepinnt (D-012):** `anthropic/claude-sonnet-5`, gegen OpenRouter-Modell-Metadaten
  verifiziert (2026-07-07): `supported_parameters` enthält `max_tokens`, **nicht** `temperature`/
  `top_p`/`top_k` — passt exakt zu `docs/PROMPT_PARAMETER_POLICY.md`.
- **Keine neue Dependency:** HTTP via `HttpURLConnection`, JSON per eigener Minimal-Implementierung
  `OpenRouterJson` (RFC-8259-Escaping + tolerante `content`-Extraktion). Kein `org.json`/OkHttp/
  Retrofit. Damit ist die Logik auf der reinen JVM unit-testbar.
- **`AiConfig`:** Endpoint, Model-ID, `max_tokens`, Timeout, API-Key aus
  `BuildConfig.OPENROUTER_API_KEY`, `isKeyConfigured` (Platzhalter-Erkennung).
- **`AiClient`:** `suspend fun request(req): ParseResult` auf `Dispatchers.IO`. Fehlender Key,
  IOException, HTTP-Status (401/403/429/5xx) und leere/ungültige Antwort → `ParseResult.Error`
  mit festem deutschen Text. Antwort über `OpenRouterJson.extractContent` + `AiResponseParser`.
  Loggt nie Key, Prompt (Nutzertext), Retry-Anweisung oder Vorschläge; nie den HTTP-Body.
- **Verdrahtung:** `OverlayService` ersetzt `DummySuggestionSource`. Initial-Anfrage aus der
  Input-Bar (Modus fest `COMPOSE`, Text = userIntent), Retry aus dem Result-Panel via neuem
  `ResultPanelView.Listener.onRetry(chips)`. Höchstens ein laufender Request (`requestJob`); bei
  Close/WhatsApp-weg wird er abgebrochen. Lade-/Fehlerhinweise kompakt in den Views.
- **Retry-Fehler** lässt bisherige Vorschläge sichtbar; Chip-Auswahl wird erst bei Erfolg
  gelöscht. Kein Verlauf, keine Speicherung.
- `DummySuggestionSource.kt` gelöscht.

## Files changed

Neu (Phase 7):

- `app/src/main/java/de/disaai/chathilfe/ai/AiConfig.kt` — OpenRouter-Config, gepinntes Modell,
  Key aus BuildConfig, `isKeyConfigured`.
- `app/src/main/java/de/disaai/chathilfe/ai/OpenRouterJson.kt` — dependency-freier JSON-Bau +
  tolerante `content`-Extraktion.
- `app/src/main/java/de/disaai/chathilfe/ai/AiClient.kt` — suspend OpenRouter-Aufruf,
  Fehler-Mapping, keine Secrets/User-Text-Logs.
- `app/src/test/java/de/disaai/chathilfe/ai/OpenRouterJsonTest.kt` — 9 Tests.
- `app/src/test/java/de/disaai/chathilfe/ai/AiConfigTest.kt` — 4 Tests.
- `docs/PHASE_7_REPORT.md` — dieser Bericht.

Geändert:

- `app/src/main/java/de/disaai/chathilfe/overlay/OverlayService.kt` — `AiClient`-Verdrahtung
  (Initial + Retry), `requestJob`-Steuerung, Lade-/Fehlerpfade, Modus `COMPOSE`.
- `app/src/main/java/de/disaai/chathilfe/overlay/OverlayController.kt` — typsichere Delegaten
  (`setInputBarLoading/Error`, `setResultPanelLoading/Error`, `replaceResultSuggestions`).
- `app/src/main/java/de/disaai/chathilfe/overlay/InputBarView.kt` — kompakter Status-/Ladehinweis,
  Start/Einfügen während Anfrage deaktiviert. Keine Strukturänderung.
- `app/src/main/java/de/disaai/chathilfe/overlay/ResultPanelView.kt` — Retry herausgelöst
  (`onRetry(chips)` statt internem Dummy), Status-/Lade-/Fehlerhinweis, `replaceSuggestions`.
- `app/src/main/res/values/strings.xml` — zwei Lade-Strings.
- `docs/DECISIONS.md` — D-012 (Modell gepinnt), offener Punkt geschlossen.
- `README.md` — Status auf Phase 7 aktualisiert.

Gelöscht:

- `app/src/main/java/de/disaai/chathilfe/overlay/DummySuggestionSource.kt`.

Unangetastet (bewusst): `AndroidManifest.xml` (INTERNET bereits vorhanden, keine neue Berechtigung),
`build.gradle.kts` (`BuildConfig.OPENROUTER_API_KEY` + `.gitignore` aus Phase 1), Settings/DataStore,
ForegroundAppDetector, ClipboardHelper, FloatingBubbleView, SuggestionPager, PromptBuilder,
AiResponseParser, alle Models, Theme, Toolchain.

## Validation

### Build / Test / Lint (in dieser Sitzung lokal ausgeführt)

```text
./gradlew assembleDebug   → BUILD SUCCESSFUL
./gradlew test            → BUILD SUCCESSFUL
./gradlew lintDebug       → BUILD SUCCESSFUL (0 errors, 9 warnings)
```

Unit-Test-Ergebnisse (`app/build/test-results/testDebugUnitTest/*.xml`):

| Suite | Tests | Failures | Errors |
|---|---|---|---|
| `ai.AiConfigTest` (neu) | 4 | 0 | 0 |
| `ai.OpenRouterJsonTest` (neu) | 9 | 0 | 0 |
| `ai.AiResponseParserTest` | 12 | 0 | 0 |
| `ai.PromptBuilderTest` | 8 | 0 | 0 |
| `model.RetryInstructionTest` | 4 | 0 | 0 |
| `model.ToneOptionTest` | 4 | 0 | 0 |
| `overlay.SuggestionPagerTest` | 5 | 0 | 0 |
| **Summe** | **46** | **0** | **0** |

Lint: `0 errors, 9 warnings` — dieselben 9 wie vor Phase 7 (keine neue Phase-7-Warnung).

### Statische Privatsphäre-/Sicherheitsprüfung

```text
grep -rnE "Log\." app/src/main/java/de/disaai/chathilfe/ai   → keine Treffer
grep -niE "Log\..*(apiKey|key|intent|prompt|retry|suggest|content|text)" (ai + OverlayService) → keine
grep -rniE "sk-or-v1|sk-ant|Bearer …" app/src docs README     → nur Test-Platzhalter "sk-or-v1-abc123"
grep -rniE "Accessibility|BIND_ACCESSIBILITY|NotificationListener|READ_CONTACTS|READ_SMS" → nur Doku-Kommentare, die dies verbieten
grep -rniE "DataStore|SharedPreferences" ai/                  → nur Doku-Kommentar (never from DataStore)
```

- Kein Logging von Key, Nutzertext, Prompt, Retry-Anweisung oder Vorschlägen im neuen Code.
- `AiClient` gibt bei HTTP-Fehlern feste Texte zurück, nie den Response-Body.
- Keine neue Berechtigung; Manifest unverändert.
- Kein Accessibility/NotificationListener/Clipboard-Monitoring/Verlauf/Speicher hinzugefügt.
- Echter API-Key steht nicht im Repo; `local.properties` ist git-ignored; Build-Platzhalter
  `"replace_me_locally"` → `isKeyConfigured=false` → klare Laufzeitmeldung.

### Akzeptanzkriterien (`docs/IMPLEMENTATION_PLAN.md` Phase 7)

- konkretes OpenRouter-Default-Modell in `AiConfig` gepinnt ✅ (`anthropic/claude-sonnet-5`, D-012).
- API-Key aus lokaler Build-Time-Konfiguration gelesen ✅ (`BuildConfig.OPENROUTER_API_KEY`).
- `AiClient`, OpenRouter einziger Provider, genau ein Modell ✅.
- Ladezustand ✅ (Input-Bar + Result-Panel, Start deaktiviert).
- Fehlerbehandlung ✅ (fehlender Key, kein Internet, 401/403/429/5xx, leer/ungültig).
- Antwort geparsed, 3 Vorschläge ans Result-Panel ✅ (`AiResponseParser` + `showResultPanel`).
- ein sichtbarer Vorschlag, Wechsel zwischen 3 ✅ (unverändert aus Phase 5).
- Retry als neue bewusste Anfrage ✅ (`onRetry(chips)` → `runRetry`).
- fehlender Build-Time-Key → klare Meldung ohne Secret ✅.
- kein Internet → klare Meldung ✅.
- Ergebnis-Panel zeigt nicht alle 3 untereinander ✅.
- Retry-Fehler lässt bisherige Vorschläge sichtbar ✅.
- keine Logs von Nutzertext/API-Key/Retry ✅.
- Anfrage nur nach Button-Klick/bewusstem Retry ✅ (kein Auto-Request, `requestJob` serialisiert).
- kein echter API-Key im Repo ✅.

## Not validated

- Kein Live-Test gegen OpenRouter: in `local.properties` steht der Build-Platzhalter
  `OPENROUTER_API_KEY=replace_me_locally` (kein echter Key hinterlegt). `isKeyConfigured=false`
  → zur Laufzeit „Kein API-Key konfiguriert." Der HTTP-Pfad selbst ist nur über die
  JSON-Unit-Tests (`OpenRouterJson`) und den Klassen-Compile abgedeckt, nicht mit echtem Request.
- `AiClient`-Netzwerk-Glue ist **nicht** unit-getestet (kein Mock-Dependency, kein echtes Netz).
  Nur die reine JSON-/Config-Logik ist getestet.
- Gerätetest: nicht durchgeführt (laut `docs/DEVICE_TEST_POLICY.md` gebündelt in Phase 8).
- Echte Modellqualität, Retry-Wirkung („still, ohne erklärt zu werden") und Ton-Anpassung können
  erst mit echtem Key / Gerät bewertet werden.
- Keine Überprüfung des Verhaltens bei Sperrbildschirm während laufender Anfrage oder
  Samsung-Hintergrundlimits während eines Requests.

## Known risks

- **Modell-ID-Verfügbarkeit:** `anthropic/claude-sonnet-5` war am 2026-07-07 über
  `https://openrouter.ai/api/v1/models` verfügbar. Sollte OpenRouter die ID ändern/entfernen,
  liefert `AiClient` einen sauberen `ParseResult.Error`; die ID ist nur in `AiConfig.MODEL`
  (D-012) zu aktualisieren.
- **`OpenRouterJson` ist kein genereller Parser:** tolerante Extraktion des ersten `"content"`.
  Standard-OpenRouter-Antworten haben genau ein solches Feld; ungewöhnliche/zukünftige Formate
  könnten `null` liefern → „Leere Antwort der KI." Risiko bis Phase 8 gegen echte Antworten offen.
- **Retry-Tippen während laufendem Retry:** bricht den vorherigen Job ab und startet neu (max.
  eine Anfrage). Könnte kurz flackern; für MVP akzeptiert.
- **Edge-Case View-Lifecycle:** schließt der Nutzer die Input-Bar exakt während einer Anfrage und
  öffnet sie neu, könnte ein verspätetes `setLoading` der alten Coroutine die neue View treffen.
  Die typsicheren Controller-Delegaten fangen Typ-/Detach-Fälle ab; der seltene Race bleibt als
  minimales Risiko notiert.
- **9 Lint-Warnings** bleiben offen (vor Phase 7 bekannt); nicht durch Phase 7 entstanden.

## Next sensible step

**Phase 8 — Stabilisierung und Gerätetest** gemäß `docs/IMPLEMENTATION_PLAN.md`. Dafür nötig:

- Echten OpenRouter-Key lokal in `local.properties` hinterlegen (`isKeyConfigured` → true).
- APK bauen und auf Samsung S25 testen: echte Modellqualität, Lade-/Fehler-UX, Vorschlagswechsel,
  Retry-Wirkung, Netzwerk-/Rate-Limit-Verhalten, Sperrbildschirm während Anfrage, Samsung-Akku.
- `docs/TEST_PLAN.md` (KI-Fehlertests) und `docs/VISUAL_SCOPE.md` auf Gerät abhaken.
- Ggf. die 9 offenen Lint-Warnings gezielt angehen.
