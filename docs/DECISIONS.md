# DECISIONS.md — ChatHilfe MVP

## Zweck

Dieses Dokument hält verbindliche technische Entscheidungen fest.

Es übernimmt die relevanten Ergebnisse aus `docs/PLAN_AUDIT.md`, ohne die übrigen Fachdocs unnötig aufzublähen.

Wenn Entscheidungen hier einer älteren Formulierung in anderen Dateien widersprechen, gilt `DECISIONS.md`, sofern `AGENTS.md` nichts Strengeres sagt.

---

## D-001 — KI-Provider

**Status:** entschieden für Provider, Modell vor Phase 7 zu pinnen

**Entscheidung:**

- Provider im MVP: **OpenRouter**
- Es wird genau **ein** Provider implementiert.
- Kein Multi-Provider-System im MVP.
- Das konkrete Default-Modell wird nicht dauerhaft in Konzeptdokumenten geraten, sondern vor Phase 7 nach aktueller OpenRouter-Verfügbarkeit in `AiConfig` gepinnt.

**Konsequenz:**

- `AiClient` implementiert zuerst nur OpenRouter.
- `AiConfig` enthält Endpoint, Modell-ID und Auth-Header.
- Phase 7 war blockiert, bis ein konkretes Modell in `AiConfig` festgelegt ist — siehe D-012 (erledigt).
- Phasen 1–6 sind nicht blockiert.

**Nicht erlaubt:**

- OpenAI + OpenRouter parallel im MVP
- Provider-Auswahl im UI
- automatische Provider-Fallbacks
- Kosten-/Billing-System

---

## D-002 — Foreground-Service-Strategie

**Status:** entschieden

**Entscheidung:**

Die Overlay-Laufzeit wird in einem **Foreground Service** gehostet, der ausschließlich aus sichtbarer Nutzeraktion gestartet wird.

**Startfluss:**

```text
Nutzer öffnet MainActivity
↓
Nutzer gewährt Berechtigungen
↓
Nutzer aktiviert Overlay bewusst
↓
Foreground Service startet
↓
startForeground() wird zeitnah aufgerufen
↓
OverlayController + ForegroundAppDetector laufen innerhalb dieser Overlay-Laufzeit
```

**Konsequenz:**

- `FOREGROUND_SERVICE` wird für die Overlay-Laufzeit benötigt.
- `POST_NOTIFICATIONS` wird ab API 33 zur Laufzeit angefragt. Eine Ablehnung blockiert den Service nicht: `SYSTEM_ALERT_WINDOW` bleibt die für den Bubble maßgebliche Berechtigung, ohne `POST_NOTIFICATIONS` läuft der Foreground Service weiter, nur die Notification bleibt unsichtbar.
- **Foreground-Service-Typ (Phase 3, entschieden): `specialUse`.** Ein manuell gestarteter, verschiebbarer Floating-Bubble-Overlay-Dienst ist exakt das in der aktuellen Android-Dokumentation genannte Referenzbeispiel für `specialUse` (kein Fit für `mediaPlayback`, `location`, `dataSync` etc.). Dafür zusätzlich nötig: `FOREGROUND_SERVICE_SPECIAL_USE`-Permission sowie eine `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" .../>` im `<service>`-Element mit kurzer, ehrlicher Begründung (siehe `AndroidManifest.xml`, Service `.overlay.OverlayService`).

**Nicht erlaubt:**

- automatischer Background-Start
- Autostart-Hacks
- WorkManager-/JobScheduler-Ketten aus dem Service
- KI-Dauerjobs im Service
- Clipboard-Lesen im Hintergrund

---

## D-003 — Overlay-UI-Technik

**Status:** entschieden

**Entscheidung:**

- MainActivity / Setup: **Jetpack Compose**
- Floating Bubble + ReplyPanel im Overlay: **klassische Android Views** für den MVP

**Konsequenz:**

- Klassen wie `FloatingBubbleView` und `ReplyPanelView` werden als klassische Views gebaut.
- Kein Compose im Overlay-MVP.
- ComposeView im Overlay erst nach stabilem MVP und nur mit expliziter Entscheidung.

---

## D-004 — Application ID und SDK-Basis

**Status:** entschieden

| Punkt | Wert |
|---|---|
| App-Name | ChatHilfe |
| `applicationId` | `de.disaai.chathilfe` |
| `compileSdk` | 37 |
| `targetSdk` | 35 für ersten stabilen MVP |
| späteres Ziel | targetSdk 36 nach Stabilisierung |
| `minSdk` | 29 |

> **Hinweis `compileSdk` 37 (Abweichung vom ursprünglichen Planwert 36):** Die in Phase 1/2 gewählten androidx-Versionen `androidx.core:core-ktx:1.19.0` und `androidx.lifecycle:lifecycle-runtime-compose:2.11.0` erfordern zum Kompilieren API-Level 37 (`checkDebugAarMetadata`). `compileSdk` wurde beim ersten lokalen Build-Validierungslauf (Juli 2026) daher von 36 auf 37 angehoben. Dies ändert nur die Kompilierungs-API, nicht das Laufzeitverhalten: `targetSdk` bleibt 35, `minSdk` bleibt 29. Siehe auch `docs/BUILD_VALIDATION_REPORT.md`.

**Toolchain (gepinnt in Phase 1, Stand Juli 2026 gegen offizielle Quellen geprüft):**

| Komponente | Version |
|---|---|
| Android Gradle Plugin (AGP) | 9.2.0 |
| Gradle Wrapper | 9.6.1 |
| Kotlin (Compose-Compiler-Plugin-Version = Kotlin-Version) | 2.4.0 |
| Compose BOM | 2026.06.01 |

Hinweis: AGP 9.x bringt Kotlin-Unterstützung "built-in" mit — das separate Plugin `org.jetbrains.kotlin.android` wird nicht mehr angewendet, nur noch `org.jetbrains.kotlin.plugin.compose` für den Compose-Compiler.

---

## D-005 — Clipboard-Fallback

**Status:** entschieden

**Entscheidung:**

Clipboard-Nutzung bleibt optional. Wenn Android das Lesen der Zwischenablage aus Overlay-/Service-Kontext verhindert oder leer liefert, muss der Nutzer den Text manuell ins Panel einfügen können.

**Konsequenz:**

- ReplyPanel zeigt Clipboard-Vorschau nur, wenn tatsächlich Text lesbar ist.
- Es gibt immer ein Eingabefeld für manuelles Einfügen.
- Clipboard wird erst nach Nutzeraktion gelesen.
- Clipboard wird erst nach Bestätigung verwendet.
- Kein Hintergrund-Monitoring.

---

## D-006 — Gerätetest-Strategie

**Status:** entschieden

**Entscheidung:**

Gerätetests werden für diesen MVP gebündelt in Phase 8 durchgeführt. Zwischen den Phasen 3 bis 7 sind Gerätetests empfohlen, aber nicht blockierend.

**Konsequenz:**

- Agenten dürfen nach Phase 3, 4 und 5 weiterarbeiten, wenn Code- und Build-Prüfungen soweit möglich sauber sind.
- Nicht getestete Geräteaspekte bleiben bis Phase 8 als Risiko offen.
- Erfolgreiche Gerätetests dürfen erst behauptet werden, wenn sie wirklich durchgeführt wurden.
- Details stehen in `docs/DEVICE_TEST_POLICY.md`.

---

## D-007 — API-Key-Strategie für private APK

**Status:** entschieden

**Entscheidung:**

Für die private APK wird der OpenRouter-API-Key **fest in den lokalen Build eingebettet**, aber **niemals ins GitHub-Repo committed**.

Der Key kommt aus einer lokalen, ignorierten Datei oder aus einer lokalen Environment-Variable, z. B.:

```text
local.properties
secrets.properties
OPENROUTER_API_KEY
```

Der Build schreibt den Wert in `BuildConfig` oder eine vergleichbare Build-Time-Konfiguration. Die App zeigt im MVP keine API-Key-Eingabe und speichert keinen API-Key per DataStore.

**Grund:**

Für eine rein private APK ist eine feste Build-Time-Konfiguration einfacher als eine API-Key-Eingabe in der App. Ein Key in einer APK ist trotzdem nicht geheim, weil APKs dekompiliert werden können. Das Risiko ist für private Verteilung akzeptabel, wenn der Key Kostenlimit hat und nicht öffentlich geteilt wird.

**Konsequenz:**

- Keine API-Key-Eingabe im MVP.
- `SettingsStore` speichert keinen API-Key.
- `AiClient` liest den Key aus BuildConfig/Build-Time-Konfiguration.
- `.gitignore` muss lokale Secret-Dateien ausschließen.
- README darf nur Platzhalter zeigen, niemals einen echten Key.
- Der OpenRouter-Key sollte ein niedriges Credit-/Usage-Limit haben.

**Nicht erlaubt:**

- echten API-Key in GitHub committen
- echten API-Key in Dokumentation schreiben
- echten API-Key in Logs ausgeben
- Key im UI anzeigen
- Key in Crashreports senden

---

## D-008 — Retry und Änderungs-Chips

**Status:** entschieden

**Entscheidung:**

Der MVP erhält einen kompakten Retry-Bereich nach den Ergebnissen. Nutzer können unpassende Vorschläge neu erzeugen lassen, ohne ein Feedback-, Bewertungs-, Verlauf- oder Gedächtnissystem zu öffnen.

**UI-Regel:**

```text
Nicht passend?
[Nochmal]
[Kürzer] [Lockerer] [Direkter]
[Sanfter] [Klarer] [Weniger künstlich]
```

**Konsequenz:**

- Retry erscheint erst nach erzeugten Vorschlägen.
- `Nochmal` nutzt denselben Modus, dieselbe Eingabe, dieselbe Absicht und denselben Ton.
- Änderungs-Chips sind global, nicht pro Vorschlagskarte.
- Maximal 1–2 Änderungs-Chips sind gleichzeitig aktiv.
- `RetryInstruction` ist nur eine temporäre Eingabe für die nächste KI-Anfrage.
- Retry-Anweisungen werden nicht gespeichert, nicht geloggt und nicht als Profil interpretiert.

**Nicht erlaubt:**

- Bewertung einzelner Vorschläge
- freies Feedbackfeld im MVP
- Stiltraining
- Verlauf der Versuche
- Gedächtnis aus Retry-Nutzung

---

## D-009 — Modellstrategie im MVP

**Status:** entschieden für MVP-Grenze, Modell-ID vor Phase 7 zu pinnen

**Entscheidung:**

Der MVP nutzt **ein OpenRouter-Default-Modell**. Modellrouting nach Tonfall, mehrere Modelle pro Stil, Provider-Fallbacks oder eine sichtbare Modellauswahl werden nicht im MVP umgesetzt.

**Grund:**

Der Hauptnutzen ist schnelles Formulieren im Overlay. Mehrere Modelle würden Fehlerfälle, Kostenkontrolle, UI-Erklärung, Tests und Fallbacklogik erhöhen. Die Tonqualität wird im MVP über Prompts, Ton-Chips und Retry-Chips verbessert.

**Konsequenz:**

- `AiConfig` enthält genau eine Modell-ID.
- `AiClient` kennt genau einen Provider und ein Default-Modell.
- Ton-Chips und Retry-Chips verändern den Prompt, nicht die Modellwahl.
- Ein späteres Modellrouting ist Post-MVP und braucht eine eigene Entscheidung.

**Nicht erlaubt im MVP:**

- Modell-Auswahl im Overlay
- Provider-Auswahl im Overlay
- Tonfall-zu-Modell-Routing
- automatische Modell-Fallbacks
- Qualitätsstufen wie `Schnell`, `Sehr gut`, `Beste Qualität`

---

## D-010 — Visuelles Overlay-Modell

**Status:** entschieden

**Entscheidung:**

Das Overlay öffnet im MVP zuerst als schmaler Eingabebalken. Erst nach einer KI-Antwort erweitert sich das Overlay zu einem kompakten Ergebnis-Panel.

**Konsequenz:**

- Startzustand ist immer eine Input-Bar, kein großes Formular.
- Die Input-Bar enthält Ton/Stil, Texteingabe, Einfügen und Start.
- Der Start-Button darf nicht `Senden` heißen.
- Das Ergebnis-Panel zeigt immer nur einen Vorschlag.
- Die 3 Vorschläge werden per Swipe, Pfeilnavigation oder Pager gewechselt.
- Drei Vorschläge untereinander sind nicht die Standardansicht.
- Der visuelle Scope ist zusätzlich in `docs/VISUAL_SCOPE.md` definiert.

**Nicht erlaubt im MVP:**

- Vollbild-Dialog
- großes Formular als Startzustand
- Dashboard-Anmutung
- Chatbot-Verlauf
- Modell-/Provider-/Prompt-Bedienung im Overlay

---

## D-011 — Agentenmodell-Policy

**Status:** entschieden

**Entscheidung:**

Für Coding-Agenten gelten separate Modellregeln in `docs/AGENT_MODEL_POLICY.md`. Diese Regeln betreffen nur Agentenarbeit am Repo und sind keine Produktfunktion der ChatHilfe-App.

**Konsequenz:**

- Claude Code nutzt `CLAUDE.md`, das `AGENTS.md` importiert.
- Claude Sonnet 5 wird für normale Implementierung mit `high` und für Architektur, Android-Lifecycle, Security, Datenschutz, Berechtigungen, Multi-Datei-Refactors und harte Fehlersuche mit `xhigh` geführt.
- Claude Sonnet 5 erhält keine non-default `temperature`, `top_p` oder `top_k`.
- GLM-5.2 wird für lange oder riskante Coding-Aufgaben bevorzugt mit Max-Effort geführt.
- GLM-5.2 High-Effort ist nur für kleinere oder bewusst latenzsensiblere Aufgaben gedacht.
- Prompt- und Providerparameter für die App werden zusätzlich durch `docs/PROMPT_PARAMETER_POLICY.md` begrenzt.

**Nicht erlaubt:**

- daraus Modellrouting für die App ableiten
- Modell- oder Provider-Auswahl im Overlay bauen
- Agentenmodell-Regeln als Nutzerfeature behandeln

---

## D-012 — Gepinntes OpenRouter-Default-Modell

**Status:** entschieden / aktualisiert auf Nutzerauftrag (2026-07-08)

**Entscheidung:**

Das konkrete MVP-Default-Modell ist **`deepseek/deepseek-v4-flash`** über den einzigen Provider OpenRouter.

Grund: Für kurze private WhatsApp-Antworten soll testweise ein schnelles DeepSeek-
Flash-Modell genutzt werden, das ggf. weniger professionell/glatt formuliert als Sonnet.

**Verifikation (gegen OpenRouter-Modell-Metadaten, 2026-07-08):**

- Modell verfügbar via `https://openrouter.ai/api/v1/models`.
- Name laut OpenRouter: `DeepSeek: DeepSeek V4 Flash`.
- `context_length`: 1048576.
- `supported_parameters` enthält u. a. `max_tokens`, `temperature`, `top_p`, `top_k`
  sowie optionale `reasoning`-/`include_reasoning`-Parameter.
- Der MVP sendet weiterhin nur `model`, `max_tokens` und eine User-Message; **keine**
  non-default Sampling-Parameter und kein Reasoning-/Thinking-Parameter. Stil wird über Prompt,
  Ton-Chips, Schreibstil-Settings und Retry-Chips gesteuert.

**Konsequenz:**

- `AiConfig` hält genau diese Modell-ID; kein Routing, kein Fallback.
- `AiClient` sendet `model`, `max_tokens` (Output-Budget) und genau eine User-Message.
- Keine Modell-/Provider-Auswahl im Overlay.
- Sonnet 5 bleibt Referenzkandidat für spätere Qualitätsvergleiche, aber nicht mehr App-Default.

**Nicht erlaubt:**

- weitere Modell-IDs, Mehrfachmodelle, Modell-Fallback, Tonfall-Routing.
- Reasoning-/Thinking-Parameter ohne eigene Entscheidung.

---

## D-013 — Feste App-Stimme (Persona) im Prompt

**Status:** entschieden (folgt aus `docs/RESPONSE_QUALITY_AUDIT.md`)

**Auslöser:** Antworten wirkten trotz laufendem Phase-7-Pfad zu geschäftlich,
akademisch oder künstlich. Das Audit (`docs/RESPONSE_QUALITY_AUDIT.md`) nennt
als Hebel neben Modus-Verdrahtung und WhatsApp-Stilregeln auch eine Ziel-Stimme.

**Entscheidung:**

Die Prompts enthalten eine feste kommunikative Stimme als **statische
App-Vorgabe**: die Antworten sollen klingen, als hätte sie eine alltägliche
Person geschrieben – eine Frau Anfang 30 mit normaler Bildung, natürlicher
Alltagssprache, nicht zu akademisch, geschäftlich, jugendlich oder künstlich
perfekt. Die Stimme ist hart codiert in `PromptBuilder` und in `docs/PROMPTS.md`
dokumentiert.

**Scope-Eingrenzung (warum das kein verbotenes Profil ist):**

- die Stimme ist eine **Prompt-Regel**, kein gespeicherter Zustand
- sie gilt für alle Nutzenden gleich, ist nicht individuell und nicht erlernbar
- sie wird **nicht** in DataStore, BuildConfig, Logs oder Analytics abgelegt
- sie bildet keine Identität, kein Gedächtnis und keinen Verlauf ab
- die demografische Formulierung ist eine Stilschablone, keine Aussage über die
  reale nutzende Person

Das ist mit `docs/PRIVACY_SECURITY.md` („Personen-/Kontakt-/Beziehungsprofile
verboten“) vereinbar, weil keine nutzerbezogenen Daten verarbeitet oder
gespeichert werden.

**Nicht erlaubt:**

- ein speicherbares, nutzerbezogenes Stil-/Personenprofil
- Ableitung der Stimme aus Nutzertexten (Lernen/Training)
- eine Profill-/Identitäts- oder Gedächtnisdatenbank

---

## D-014 — Schreibstil-Einstellungen als reine lokale Stilwerte

**Status:** entschieden / umgesetzt (Issue #8)

**Entscheidung:**

Die Settings enthalten einen kompakten Bereich **Schreibstil** mit fünf lokal
gespeicherten Enum-Werten:

- Antwortlänge
- Emojis
- Satzzeichen
- Groß-/Kleinschreibung
- Natürlichkeit

Diese Werte werden in `PromptBuilder` als `{{style_rules}}`-Block ergänzt. Sie
sind reine Komfort-/Stilpräferenzen und werden in DataStore gespeichert.

**Wichtige Abgrenzung zu D-013:**

Die feste App-Stimme / Persona bleibt **keine** Nutzereinstellung. Es gibt kein
freies Persona-Feld und kein speicherbares Personen-, Stil- oder Kontaktprofil.

**Konsequenz:**

- `WritingStyleSettings` enthält nur neutrale Stilwerte.
- `SettingsStore` speichert nur Enum-`internalValue`-Strings.
- `PromptBuilder` übersetzt die Werte in klare Prompt-Regeln.
- Keine Nutzertexte, kopierten Nachrichten, Vorschläge oder Retry-Anweisungen
  werden gespeichert.
- Keine Stilwerte werden aus Nutzertexten gelernt.

**Nicht erlaubt:**

- freie Persona-Eingabe
- gespeichertes Stilprofil aus echten Nachrichten
- automatisches Lernen/Training aus Nutzertexten
- Overlay-Bedienung für Modell-/Provider-/Prompttechnik

---

## Offene Punkte

**Modell-A/B nach Testset (Audit-Empfehlung):** Der Default wurde auf ausdrücklichen
Nutzerauftrag bereits auf `deepseek/deepseek-v4-flash` geändert. Ein manueller A/B-Test
bleibt offen, um DeepSeek nach Prompt-/Persona-/Schreibstil-Fix gegen die Referenz
`anthropic/claude-sonnet-5` und weitere Kandidaten zu bewerten.

Geprüfte OpenRouter-Slugs (2026-07-08 via `/api/v1/models`, noch **nicht** A/B-getestet):

| Rolle | Slug | Status |
|---|---|---|
| Default | `deepseek/deepseek-v4-flash` | gepinnt in `AiConfig` |
| Referenz | `anthropic/claude-sonnet-5` | verfügbar, A/B offen |
| Haiku | `anthropic/claude-haiku-4.5` | verfügbar, A/B offen |
| GPT Mini | `openai/gpt-5-mini` | verfügbar, A/B offen |
| weiterer Mini-Kandidat | `openai/gpt-4.1-mini` | verfügbar, optionaler A/B-Kandidat |

Aliases wie `~anthropic/claude-haiku-latest` oder `~openai/gpt-mini-latest`
sind für Exploration verfügbar, dürfen aber nicht dauerhaft als Release-Default
gepinnt werden. Ein Provider-/Routing-/Fallback-System bleibt weiterhin
ausgeschlossen (D-009).

Weitere offene Punkte für den MVP-Scope: keine.

"Offen" heißt hier nicht beliebig: Ein Agent muss diese Punkte vor der
jeweiligen Phase entscheiden, dokumentieren und gegen `AGENTS.md` sowie
`docs/ANDROID_CONSTRAINTS.md` prüfen.