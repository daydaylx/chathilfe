# ChatHilfe

Private Android-App als MVP für einen schwebenden KI-Formulierungshelfer über WhatsApp.

Die App ist kein Messenger-Ersatz. Sie hilft nur beim Formulieren, Umschreiben und Beantworten von Chatnachrichten. Der Nutzer kopiert, fügt ein und sendet immer selbst.

---

## Status

| Punkt | Stand |
|---|---|
| Projektphase | Phase 3 code-seitig abgeschlossen; Build lokal verifiziert; nächster Schritt ist Phase 4 |
| Ziel | private Android-APK |
| Primäres Gerät | Samsung Galaxy S25 |
| Zielplattform | Android 15/16 |
| Release-Ziel | zunächst nicht Play Store |
| Android-Projektbasis | angelegt |
| Gerätetest-Strategie | gebündelte Gerätevalidierung in Phase 8 |
| Buildstatus | `assembleDebug` lokal verifiziert; `test` NO-SOURCE; `lint` mit 1 Error + 9 Warnings, nicht APK-blockierend. Siehe `docs/BUILD_VALIDATION_REPORT.md` und `docs/PHASE_3_REPORT.md` |

---

## MVP-Ziel

Wenn WhatsApp geöffnet ist, erscheint ein kleiner Floating Button am Bildschirmrand.

Beim Antippen öffnet sich zuerst ein schmaler Eingabebalken. Dort kann der Nutzer:

1. einen Ton/Stil wählen,
2. Text eingeben oder bewusst einfügen,
3. KI-Vorschläge starten,
4. nach der KI-Antwort ein kompaktes Ergebnis-Panel sehen,
5. zwischen drei Vorschlägen per Swipe, Pfeil oder Pager wechseln,
6. bei unpassenden Vorschlägen mit **Nochmal** oder kompakten Änderungs-Chips neue Varianten erzeugen,
7. den sichtbaren Vorschlag kopieren,
8. ihn selbst in WhatsApp einfügen und selbst senden.

Der Retry ist nur ein temporärer neuer Versuch. Es gibt keinen Verlauf, kein Gedächtnis, kein Stiltraining und keine Bewertung einzelner Vorschläge.

---

## Dokumentenübersicht

| Dokument | Zweck |
|---|---|
| [`AGENTS.md`](AGENTS.md) | verbindliche Kurzregeln für Coding-Agenten |
| [`Konzept.md`](Konzept.md) | Produktziel, Scope, Modi, Risiken und Abschlusskriterien |
| [`Arbeitsauftrag.md`](Arbeitsauftrag.md) | ausführlicher Startauftrag für externe Coding-Agenten |
| [`docs/PHASE_0_1_REPORT.md`](docs/PHASE_0_1_REPORT.md) | Abschlussbericht zu Phase 0/1 |
| [`docs/PHASE_2_REPORT.md`](docs/PHASE_2_REPORT.md) | Abschlussbericht zu Phase 2 |
| [`docs/PHASE_3_REPORT.md`](docs/PHASE_3_REPORT.md) | Abschlussbericht zu Phase 3 |
| [`docs/DECISIONS.md`](docs/DECISIONS.md) | angenommene technische Entscheidungen aus dem Audit |
| [`docs/DEVICE_TEST_POLICY.md`](docs/DEVICE_TEST_POLICY.md) | Strategie: Gerätetests gesammelt in Phase 8 |
| [`docs/API_KEY_STRATEGY.md`](docs/API_KEY_STRATEGY.md) | lokale API-Key-Strategie für private Builds |
| [`docs/PLAN_AUDIT.md`](docs/PLAN_AUDIT.md) | Plan-Audit, Risiken und erste Umsetzungstickets |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | technische Architektur und Komponenten |
| [`docs/ANDROID_CONSTRAINTS.md`](docs/ANDROID_CONSTRAINTS.md) | Android 15/16, Overlay, Services und Berechtigungen |
| [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md) | phasenweiser Umsetzungsplan |
| [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md) | Build-, Geräte-, Overlay-, Clipboard-, Retry- und KI-Tests |
| [`docs/PRIVACY_SECURITY.md`](docs/PRIVACY_SECURITY.md) | Datenschutz- und Sicherheitsgrenzen |
| [`docs/PROMPTS.md`](docs/PROMPTS.md) | KI-Prompts und Parser-Regeln |
| [`docs/UI_UX_SPEC.md`](docs/UI_UX_SPEC.md) | UI/UX-Regeln für Button, Eingabebalken, Ergebnis-Panel und Fehlerzustände |
| [`docs/VISUAL_SCOPE.md`](docs/VISUAL_SCOPE.md) | verbindlicher visueller Scope für Input-Bar, Result-Panel und Vorschlagswechsel |

---

## Empfohlene Lesereihenfolge für Agenten

1. [`AGENTS.md`](AGENTS.md)
2. [`Konzept.md`](Konzept.md)
3. [`docs/DECISIONS.md`](docs/DECISIONS.md)
4. [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
5. [`docs/ANDROID_CONSTRAINTS.md`](docs/ANDROID_CONSTRAINTS.md)
6. [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md)
7. bei UI-Arbeit zusätzlich [`docs/UI_UX_SPEC.md`](docs/UI_UX_SPEC.md) und [`docs/VISUAL_SCOPE.md`](docs/VISUAL_SCOPE.md)
8. nur die zusätzlich relevanten Fachdocs

Nicht alle Dokumente pauschal laden. Das reduziert Kontext-Bloat.

---

## Technische Grundentscheidungen

| Bereich | Entscheidung |
|---|---|
| Plattform | Android |
| Sprache | Kotlin |
| Haupt-UI | Jetpack Compose |
| Overlay-UI | klassische Android Views für MVP |
| Visueller Scope | Floating Button, Input-Bar, Result-Panel, ein sichtbarer Vorschlag |
| Overlay | Android `WindowManager` |
| Overlay-Typ | `TYPE_APPLICATION_OVERLAY` |
| WhatsApp-Erkennung | `UsageStatsManager.queryEvents()` |
| Laufzeit | Foreground Service, aus sichtbarer Nutzeraktion gestartet |
| Lokale Einstellungen | DataStore für UI-/Overlay-Settings, keine Texte/API-Keys |
| KI-Anbieter | OpenRouter, ein Provider im MVP |
| KI-Modell | ein OpenRouter-Default-Modell, vor Phase 7 pinnen |
| API-Key | lokaler Build-Time-Key, nicht im Repo, kein UI-Feld |
| Retry | temporäre Änderungs-Chips, keine Speicherung |
| Gerätetest | gesammelt in Phase 8 |
| Distribution | private APK |

Details stehen in [`docs/DECISIONS.md`](docs/DECISIONS.md), [`docs/API_KEY_STRATEGY.md`](docs/API_KEY_STRATEGY.md), [`docs/UI_UX_SPEC.md`](docs/UI_UX_SPEC.md), [`docs/VISUAL_SCOPE.md`](docs/VISUAL_SCOPE.md) und [`docs/DEVICE_TEST_POLICY.md`](docs/DEVICE_TEST_POLICY.md).

---

## Toolchain Stand Phase 1

| Komponente | Version |
|---|---|
| Android Gradle Plugin | 9.2.0 |
| Gradle Wrapper | 9.6.1 |
| Kotlin / Compose Compiler Plugin | 2.4.0 |
| Compose BOM | 2026.06.01 |
| `compileSdk` | 37 |
| `targetSdk` | 35 |
| `minSdk` | 29 |
| `applicationId` | `de.disaai.chathilfe` |

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

Die App hat im MVP kein API-Key-Eingabefeld. Der Key wird nicht in DataStore gespeichert und nicht im UI angezeigt.

---

## Build-Hinweise

Das Android-Projekt ist angelegt.

Standardbefehle:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

Ein Agent darf erfolgreiche Builds oder Tests nur behaupten, wenn sie tatsächlich ausgeführt wurden.

---

## Aktueller nächster Schritt

Phase 3 ist code-seitig umgesetzt. `./gradlew assembleDebug` ist lokal verifiziert und liefert eine installierbare Debug-APK. `./gradlew test` läuft ohne vorhandene Unit-Tests. `./gradlew lint` meldet 1 Error und 9 Warnings; das ist ein separater Auftrag.

Nächster sinnvoller Schritt: **Phase 4 — WhatsApp-Erkennung** (`ForegroundAppDetector`) gemäß `docs/IMPLEMENTATION_PLAN.md`.

Der echte Gerätetest wird bewusst nicht als Zwischen-Gate genutzt, sondern gebündelt in **Phase 8 — Stabilisierung und Gerätetest** durchgeführt.

---

## MVP-Abschlusskriterien

Der MVP gilt erst als fertig, wenn:

- die App als APK installierbar ist
- Berechtigungen verständlich erklärt und geprüft werden
- Floating Button nur bei WhatsApp erscheint
- Eingabebalken kompakt öffnet
- Ergebnis-Panel erst nach KI-Antwort erscheint
- immer nur ein Vorschlag sichtbar ist
- Wechsel zwischen drei Vorschlägen funktioniert
- sichtbarer Vorschlag kopiert werden kann
- Clipboard bewusst übernommen wird oder der manuelle Fallback funktioniert
- KI-Vorschläge erzeugt und kopiert werden können
- Retry mit `Nochmal` und kompakten Änderungs-Chips funktioniert
- echter Gerätetest in Phase 8 durchgeführt und dokumentiert wurde
- kein Accessibility Service verwendet wurde
- keine unnötigen Berechtigungen verwendet wurden
- keine Nutzertexte, Retry-Anweisungen oder API-Keys geloggt werden
- keine Nutzertexte, Vorschläge, Retry-Anweisungen oder Verläufe gespeichert werden
- kein Gedächtnis, Stiltraining oder Profil-System existiert
- kein echter API-Key im Repo steht
- kein API-Key-Eingabefeld im UI existiert
- genau ein OpenRouter-Default-Modell genutzt wird
- Code klein und wartbar bleibt

---

## Kurzurteil

Der Scope ist realistisch und als privater Android-MVP gut machbar.

Die Hauptschwierigkeit liegt nicht in der KI, sondern in Android-Overlay-Verhalten, Berechtigungen, Vordergrund-App-Erkennung, Foreground-Service-Lifecycle, Clipboard-Fokusregeln, Samsung-/Android-Hintergrundlimits und einer unaufdringlichen UX.
