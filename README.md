# ChatHilfe

Private Android-App als MVP für einen schwebenden KI-Formulierungshelfer über WhatsApp.

Die App ist kein Messenger-Ersatz. Sie hilft nur beim Formulieren, Umschreiben und Beantworten von Chatnachrichten. Der Nutzer kopiert, fügt ein und sendet immer selbst.

---

## Status

| Punkt | Stand |
|---|---|
| Projektphase | Konzept- und Vorbereitungsphase |
| Ziel | private Android-APK |
| Primäres Gerät | Samsung Galaxy S25 |
| Zielplattform | Android 15/16 |
| Release-Ziel | zunächst nicht Play Store |

---

## MVP-Ziel

Wenn WhatsApp geöffnet ist, erscheint ein kleiner Floating Button am Bildschirmrand.

Beim Antippen öffnet sich ein kompaktes Mini-Fenster. Dort kann der Nutzer:

1. einen Modus wählen: **Antworten**, **Formulieren** oder **Umschreiben**
2. optional eine kopierte Nachricht aus der Zwischenablage verwenden,
3. grob beschreiben, was er sagen möchte,
4. einen Ton auswählen,
5. von der KI drei direkt kopierbare Vorschläge erzeugen lassen,
6. einen Vorschlag kopieren,
7. ihn selbst in WhatsApp einfügen und selbst senden.

---

## Dokumentenübersicht

| Dokument | Zweck |
|---|---|
| [`AGENTS.md`](AGENTS.md) | verbindliche Kurzregeln für Coding-Agenten |
| [`Konzept.md`](Konzept.md) | Produktziel, Scope, Modi, Risiken und Abschlusskriterien |
| [`Arbeitsauftrag.md`](Arbeitsauftrag.md) | ausführlicher Startauftrag für externe Coding-Agenten |
| [`docs/DECISIONS.md`](docs/DECISIONS.md) | angenommene technische Entscheidungen aus dem Audit |
| [`docs/API_KEY_STRATEGY.md`](docs/API_KEY_STRATEGY.md) | lokale API-Key-Strategie für private Builds |
| [`docs/PLAN_AUDIT.md`](docs/PLAN_AUDIT.md) | Plan-Audit, Risiken und erste Umsetzungstickets |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | technische Architektur und Komponenten |
| [`docs/ANDROID_CONSTRAINTS.md`](docs/ANDROID_CONSTRAINTS.md) | Android 15/16, Overlay, Services und Berechtigungen |
| [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md) | phasenweiser Umsetzungsplan |
| [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md) | Build-, Geräte-, Overlay-, Clipboard- und KI-Tests |
| [`docs/PRIVACY_SECURITY.md`](docs/PRIVACY_SECURITY.md) | Datenschutz- und Sicherheitsgrenzen |
| [`docs/PROMPTS.md`](docs/PROMPTS.md) | KI-Prompts und Parser-Regeln |
| [`docs/UI_UX_SPEC.md`](docs/UI_UX_SPEC.md) | UI/UX-Regeln für Button, Panel und Fehlerzustände |

---

## Empfohlene Lesereihenfolge für Agenten

1. [`AGENTS.md`](AGENTS.md)
2. [`Konzept.md`](Konzept.md)
3. [`docs/DECISIONS.md`](docs/DECISIONS.md)
4. [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
5. [`docs/ANDROID_CONSTRAINTS.md`](docs/ANDROID_CONSTRAINTS.md)
6. [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md)
7. nur die zusätzlich relevanten Fachdocs

Nicht alle Dokumente pauschal laden. Das reduziert Kontext-Bloat.

---

## Technische Grundentscheidungen

| Bereich | Entscheidung |
|---|---|
| Plattform | Android |
| Sprache | Kotlin |
| Haupt-UI | Jetpack Compose |
| Overlay-UI | klassische Android Views für MVP |
| Overlay | Android `WindowManager` |
| Overlay-Typ | `TYPE_APPLICATION_OVERLAY` |
| WhatsApp-Erkennung | `UsageStatsManager.queryEvents()` |
| Laufzeit | Foreground Service, aus sichtbarer Nutzeraktion gestartet |
| Lokale Einstellungen | DataStore für UI-/Overlay-Settings |
| KI-Anbieter | OpenRouter, ein Provider im MVP |
| API-Key | lokaler Build-Time-Key, nicht im Repo |
| Distribution | private APK |

Details stehen in [`docs/DECISIONS.md`](docs/DECISIONS.md) und [`docs/API_KEY_STRATEGY.md`](docs/API_KEY_STRATEGY.md).

---

## API-Key-Regel

Der echte OpenRouter-Key darf nie ins Repository.

Für private Builds wird er lokal bereitgestellt, z. B. über:

```text
local.properties
secrets.properties
OPENROUTER_API_KEY
```

Dokumentation und Beispielcode dürfen nur Platzhalter enthalten.

---

## Build-Hinweise

Das eigentliche Android-Projekt ist noch nicht angelegt.

Sobald Gradle-Dateien vorhanden sind, sollen diese Befehle geprüft und bei Bedarf angepasst werden:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

Agenten dürfen keine erfolgreichen Builds oder Tests behaupten, wenn sie nicht ausgeführt wurden.

---

## MVP-Abschlusskriterien

Der MVP gilt erst als fertig, wenn:

- die App als APK installierbar ist
- Berechtigungen verständlich erklärt und geprüft werden
- Floating Button nur bei WhatsApp erscheint
- Mini-Fenster stabil funktioniert
- alle drei Modi funktionieren
- Clipboard bewusst übernommen wird oder der manuelle Fallback funktioniert
- KI-Vorschläge erzeugt und kopiert werden können
- kein Accessibility Service verwendet wurde
- keine unnötigen Berechtigungen verwendet wurden
- keine Nutzertexte oder API-Keys geloggt werden
- kein echter API-Key im Repo steht
- Code klein und wartbar bleibt

---

## Kurzurteil

Der Scope ist realistisch und als privater Android-MVP gut machbar.

Die Hauptschwierigkeit liegt nicht in der KI, sondern in Android-Overlay-Verhalten, Berechtigungen, Vordergrund-App-Erkennung, Foreground-Service-Lifecycle, Clipboard-Fokusregeln, Samsung-/Android-Hintergrundlimits und einer unaufdringlichen UX.
