# API_KEY_STRATEGY.md — ChatHilfe private APK

## Zweck

Dieses Dokument beschreibt die API-Key-Strategie für die private APK.

Entscheidung: Der OpenRouter-Key wird für private Builds fest in die APK eingebettet, aber niemals ins GitHub-Repo committed.

---

## Grundregel

Der echte API-Key darf nur lokal vorhanden sein.

Erlaubt:

- lokale Datei wie `local.properties` oder `secrets.properties`
- lokale Environment-Variable wie `OPENROUTER_API_KEY`
- BuildConfig-Feld oder vergleichbare Build-Time-Konfiguration
- Platzhalter in Dokumentation

Verboten:

- echter API-Key in GitHub
- echter API-Key in Markdown-Dateien
- echter API-Key in Kotlin-Dateien
- echter API-Key in Logs
- echter API-Key in Screenshots
- API-Key-Eingabe im MVP
- API-Key in DataStore

---

## Empfohlener Ansatz

Für den MVP:

```text
local.properties oder secrets.properties
↓
Gradle liest OPENROUTER_API_KEY
↓
BuildConfig.OPENROUTER_API_KEY
↓
AiConfig
↓
AiClient
```

Die Secret-Datei muss in `.gitignore` stehen.

Beispiel nur mit Platzhalter:

```properties
OPENROUTER_API_KEY=replace_me_locally
```

Niemals den echten Wert in ein Commit schreiben.

---

## Sicherheitsbewertung

Ein in eine APK eingebetteter Key ist nicht wirklich geheim. APKs können dekompiliert werden.

Für dieses Projekt ist es trotzdem akzeptabel, weil:

- die APK privat genutzt wird
- keine Play-Store-Veröffentlichung geplant ist
- keine Nutzeraccounts nötig sind
- die Bedienung einfacher bleibt

Pflichtmaßnahmen:

- Key mit niedrigem Credit-/Usage-Limit verwenden
- APK nicht öffentlich teilen
- Key bei Verdacht sofort löschen/rotieren
- keine Logs mit Key

---

## Umsetzungskriterien

Ein Agent darf die API-Key-Strategie erst als umgesetzt markieren, wenn:

- `.gitignore` lokale Secret-Dateien ausschließt
- Build ohne echten Key verständlich fehlschlägt oder Stub-Modus nutzt
- Build mit lokalem Key funktioniert
- kein echter Key im Repo steht
- `AiClient` den Key aus Build-Time-Konfiguration liest
- keine API-Key-Eingabe im UI existiert
