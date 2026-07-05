# ChatHilfe

Private Android-App als MVP für einen schwebenden KI-Formulierungshelfer über WhatsApp.

Die App ist kein Messenger-Ersatz und keine WhatsApp-Automation. Sie hilft nur beim Formulieren, Umschreiben und Beantworten von Chatnachrichten. Der Nutzer kopiert, fügt ein und sendet immer selbst.

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

1. einen Modus wählen:
   - **Antworten**
   - **Formulieren**
   - **Umschreiben**
2. optional eine kopierte Nachricht aus der Zwischenablage verwenden,
3. grob beschreiben, was er sagen möchte,
4. einen Ton auswählen,
5. von der KI drei direkt kopierbare Vorschläge erzeugen lassen,
6. einen Vorschlag kopieren,
7. ihn selbst in WhatsApp einfügen und selbst senden.

---

## Harte Nicht-Ziele

Nicht Teil des MVP:

- kein automatisches Auslesen von WhatsApp-Chats
- kein Lesen vollständiger Chatverläufe
- kein Zugriff auf Kontakte
- kein automatisches Einfügen in WhatsApp
- kein automatisches Senden
- kein Accessibility Service
- kein Notification Scraping
- kein Screen Scraping
- keine Hintergrundüberwachung der Zwischenablage
- keine Speicherung von Nachrichten oder Chatverläufen
- kein Account-System
- keine Cloud-Synchronisierung
- keine Analytics oder Tracking-SDKs
- kein Play-Store-Release als erstes Ziel
- kein Multi-App-Support für Telegram, Instagram, SMS usw.

Wenn eine Umsetzung eines dieser Themen benötigt, ist sie außerhalb des aktuellen Scopes.

---

## Dokumentenübersicht

| Dokument | Zweck |
|---|---|
| [`AGENTS.md`](AGENTS.md) | verbindliche Kurzregeln für Coding-Agenten |
| [`Konzept.md`](Konzept.md) | Produktziel, Scope, Modi, Risiken und Abschlusskriterien |
| [`Arbeitsauftrag.md`](Arbeitsauftrag.md) | ausführlicher Startauftrag für externe Coding-Agenten |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | technische Architektur und Komponenten |
| [`docs/ANDROID_CONSTRAINTS.md`](docs/ANDROID_CONSTRAINTS.md) | Android 15/16, Overlay, Services und Berechtigungen |
| [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md) | phasenweiser Umsetzungsplan |
| [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md) | Build-, Geräte-, Overlay-, Clipboard- und KI-Tests |
| [`docs/PRIVACY_SECURITY.md`](docs/PRIVACY_SECURITY.md) | Datenschutz- und Sicherheitsgrenzen |
| [`docs/PROMPTS.md`](docs/PROMPTS.md) | KI-Prompts und Parser-Regeln |
| [`docs/UI_UX_SPEC.md`](docs/UI_UX_SPEC.md) | UI/UX-Regeln für Button, Panel und Fehlerzustände |

---

## Empfohlene Lesereihenfolge für Agenten

Für Implementierungsaufgaben:

1. [`AGENTS.md`](AGENTS.md)
2. [`Konzept.md`](Konzept.md)
3. [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
4. [`docs/ANDROID_CONSTRAINTS.md`](docs/ANDROID_CONSTRAINTS.md)
5. [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md)
6. nur die zusätzlich relevanten Fachdocs

Nicht alle Dokumente pauschal laden. Das reduziert Kontext-Bloat.

---

## Geplanter Tech-Stack

| Bereich | Entscheidung |
|---|---|
| Plattform | Android |
| Sprache | Kotlin |
| Haupt-UI | Jetpack Compose |
| Overlay | Android `WindowManager` |
| Overlay-Typ | `TYPE_APPLICATION_OVERLAY` |
| WhatsApp-Erkennung | `UsageStatsManager.queryEvents()` |
| Lokale Einstellungen | DataStore |
| KI-Anbindung | OpenRouter oder OpenAI, ein Provider im MVP |
| Distribution | private APK |

---

## Grundprinzip der App

Die App steuert WhatsApp nicht. Sie stellt nur ein Hilfsfenster über WhatsApp bereit.

Der Nutzer bleibt verantwortlich für:

- Text prüfen
- Vorschlag kopieren
- in WhatsApp einfügen
- Nachricht senden

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
- Floating Button außerhalb von WhatsApp verschwindet
- Mini-Fenster stabil funktioniert
- alle drei Modi funktionieren
- Clipboard nur bewusst übernommen wird
- KI genau drei Vorschläge erzeugt, soweit möglich
- Vorschläge kopierbar sind
- keine WhatsApp-Automation eingebaut wurde
- kein Accessibility Service verwendet wurde
- keine unnötigen Berechtigungen verwendet wurden
- keine Nutzertexte oder API-Keys geloggt werden
- Code klein und wartbar bleibt

---

## Kurzurteil

Der Scope ist realistisch und als privater Android-MVP gut machbar.

Die Hauptschwierigkeit liegt nicht in der KI, sondern in Android-Overlay-Verhalten, Berechtigungen, Vordergrund-App-Erkennung, Service-Lifecycle, Samsung-/Android-Hintergrundlimits und einer unaufdringlichen UX.
