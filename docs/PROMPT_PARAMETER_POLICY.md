# PROMPT_PARAMETER_POLICY.md — Prompt Parameter Policy

## Zweck

Dieses Dokument ergänzt `docs/PROMPTS.md` um modellabhängige Parameterregeln.

Es verhindert, dass allgemeine Prompt-Parameter ungeprüft auf Modelle angewendet werden, die diese Parameter nicht unterstützen.

---

## Grundregel

Prompt-Inhalte, Ton-Chips und Retry-Chips steuern den Schreibstil.

Sampling- oder Thinking-Parameter dürfen nur gesetzt werden, wenn das konkrete Modell und der konkrete Provider sie unterstützen.

---

## Allgemein für App-Antworten

Für kurze Chatvorschläge gilt:

- genug Output-Budget für genau 3 kurze Varianten
- keine unnötig langen Antworten
- keine Analyseausgabe
- keine Erklärungsausgabe
- keine Meta-Sätze über Retry

---

## OpenRouter allgemein

Bei Modellen, die Sampling-Parameter unterstützen, kann ein moderater Wert genutzt werden.

Empfohlen:

- niedrige bis mittlere Varianz
- nicht zu kreativ
- natürlich, kurz und kopierbar

Wenn ein Modell Parameter nicht unterstützt, müssen sie weggelassen werden.

---

## Claude Sonnet 5

Für Claude Sonnet 5 gilt:

- keine non-default `temperature`, `top_p` oder `top_k` setzen
- kein manuelles extended-thinking-budget setzen
- Stilvariation über Prompt, Ton-Chips und Retry-Chips steuern
- ausreichend Output-Budget für finale Antwort lassen

Für Coding-Agenten siehe zusätzlich `docs/AGENT_MODEL_POLICY.md`.

---

## GLM-5.2

Für GLM-5.2 gilt:

- Thinking-/Effort-Parameter nur bewusst setzen
- für kurze App-Antworten kein unnötig hohes Thinking erzwingen
- für Coding-Agenten Max-Effort bevorzugen, wenn lange oder riskante Aufgaben bearbeitet werden
- für kleinere Agentenaufgaben kann High-Effort reichen

Für Coding-Agenten siehe zusätzlich `docs/AGENT_MODEL_POLICY.md`.

---

## Nicht tun

- keine pauschalen Sampling-Parameter für alle Modelle setzen
- keine nicht unterstützten Parameter an Claude Sonnet 5 senden
- keine Modellparameter in der App-UI anzeigen
- keine Provider- oder Modellauswahl im MVP bauen
- keine Modellrouting-Logik aus dieser Policy ableiten
