# AGENT_MODEL_POLICY.md — Coding-Agent Model Policy

## Zweck

Dieses Dokument definiert, wie Coding-Agenten mit Claude Sonnet 5 und GLM-5.2 arbeiten sollen.

Es betrifft Agenten, die dieses Repo bearbeiten. Es ist keine Modellrouting-Entscheidung für die ChatHilfe-App selbst.

---

## Grundregel

- Repo-Scope, Datenschutz, Berechtigungen und MVP-Grenzen gelten unabhängig vom Modell.
- Das Modell darf keine neuen Features erfinden, nur weil es sie technisch umsetzen kann.
- Bei Konflikten gelten `AGENTS.md`, `docs/PRIVACY_SECURITY.md`, `docs/ANDROID_CONSTRAINTS.md`, `docs/DECISIONS.md` und dann dieses Dokument.
- Build-, Test-, Lint- und Gerätetest-Ergebnisse dürfen nur behauptet werden, wenn sie tatsächlich ausgeführt wurden.

---

## Claude Sonnet 5

Claude Sonnet 5 ist bevorzugt für:

- Architekturentscheidungen
- Android-Lifecycle- und Permission-Fragen
- Sicherheits- und Datenschutzprüfungen
- Multi-Datei-Refactors
- Review von Agentenplänen
- schwer erklärbare Build-, Lint- oder Runtime-Fehler

Empfohlener Effort:

| Aufgabe | Effort |
|---|---|
| kleine Doku-Korrektur | `medium` |
| normale Implementierung | `high` |
| Architektur / Sicherheit / Android-Lifecycle | `xhigh` |
| sehr harte Fehlersuche | `xhigh` oder `max` |
| triviale Formatierung | `low` |

Regeln:

- `high` ist der Standard für normale Repo-Arbeit.
- `xhigh` nutzen, wenn mehrere Dateien, Architektur, Security oder Android-Systemverhalten betroffen sind.
- `low` nicht für Implementierungsphasen verwenden.
- Kein manuelles extended-thinking-budget verwenden.
- Keine non-default `temperature`, `top_p` oder `top_k` setzen.
- Stil- und Qualitätsvariation über Prompt, Scope und Akzeptanzkriterien steuern.
- Bei UI-/Design-Arbeit erst Varianten oder klare Constraints beschreiben, nicht durch Temperatur randomisieren.

---

## GLM-5.2

GLM-5.2 ist bevorzugt für:

- lange Umsetzungsphasen
- umfangreiche Repo-Analyse
- agentische Coding-Aufgaben mit vielen Dateien
- zweite Meinung zu Architektur und Vereinfachung
- große Doku-Synchronisierung

Empfohlener Effort:

| Aufgabe | Einstellung |
|---|---|
| lange Coding-/Agentenaufgabe | `reasoning_effort=max` oder unset, wenn Max Default ist |
| normale Implementierung | `reasoning_effort=max` |
| kleinere Teilaufgabe mit Kosten-/Latenz-Fokus | `reasoning_effort=high` |
| reine Formatierung/Doku ohne Analyse | `enable_thinking=false` nur falls bewusst gewünscht |

Regeln:

- Für lange oder riskante Aufgaben Max bevorzugen.
- `high` nur bewusst wählen, wenn Kosten oder Latenz wichtiger sind als maximale Sorgfalt.
- Thinking nicht ausschalten, wenn Android-Lifecycle, Berechtigungen, Datenschutz, Secrets oder Architektur betroffen sind.
- Wegen großem Kontext nicht automatisch alle Dokumente laden. Erst Source-of-Truth und taskrelevante Dateien lesen.
- Lange Kontextfähigkeit nicht als Ausrede für Kontext-Bloat nutzen.

---

## Modellunabhängige Agentenregeln

Vor jeder Änderung:

1. Aktuelle Source-of-Truth-Dateien lesen.
2. Scope gegen `AGENTS.md` und `docs/DECISIONS.md` prüfen.
3. Keine verbotenen Features hinzufügen.
4. Bestehende Entscheidungen nicht stillschweigend überschreiben.

Während der Änderung:

- klein und fokussiert arbeiten
- keine unnötigen Abstraktionen bauen
- keine echten Secrets schreiben
- keine Nutzertexte, Vorschläge oder Retry-Anweisungen speichern oder loggen
- keine Accessibility-, Notification- oder Screen-Scraping-Umgehung einführen

Nach der Änderung:

- geänderte Dateien nennen
- Validierung nennen
- nicht validierte Punkte nennen
- ungetestetes Geräteverhalten bis Phase 8 als Risiko markieren
- nächstes sinnvolles Vorgehen nennen

---

## Thinking-Empfehlung für Projektphasen

| Phase | Claude Sonnet 5 | GLM-5.2 |
|---|---|---|
| Phase 4 WhatsApp-Erkennung | `high` | `max` |
| Phase 5 Input-Bar / Result-Panel | `high` | `max` |
| Phase 6 PromptBuilder / Parser | `high` | `max` |
| Phase 7 KI-Anbindung | `xhigh` | `max` |
| Phase 8 Stabilisierung / Gerätetest | `xhigh` | `max` |
| reine Doku-Korrektur | `medium` | `high` |

---

## Nicht tun

- kein Modellrouting für die App daraus ableiten
- keine Modell-Auswahl in der App-UI bauen
- keine Provider-Fallbacks in den MVP einbauen
- kein Agentenmodell als Produktfeature behandeln
- keine nicht validierten Builds oder Gerätetests behaupten
