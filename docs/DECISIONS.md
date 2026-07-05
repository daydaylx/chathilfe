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

**Grund:**

OpenRouter passt besser zum privaten MVP, weil Modellwechsel später möglich bleibt, ohne mehrere Anbieter-Clients im MVP zu bauen.

**Konsequenz:**

- `AiClient` implementiert zuerst nur OpenRouter.
- `AiConfig` enthält Endpoint, Modell-ID und Auth-Header.
- Phase 7 ist blockiert, bis ein konkretes Modell in `AiConfig` festgelegt ist.
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

**Grund:**

Overlay + UsageStats-Polling sind auf Samsung/modernem Android ohne Foreground Service zu instabil. Der Service darf aber nicht heimlich aus dem Hintergrund starten.

**Konsequenz:**

- `FOREGROUND_SERVICE` wird für die Overlay-Laufzeit benötigt.
- `POST_NOTIFICATIONS` wird benötigt, falls die Android-Version für die sichtbare Service-Notification danach fragt.
- Der passende Foreground-Service-Typ muss in Phase 1/3 gegen aktuelle Android-Doku geprüft und im Manifest dokumentiert werden.
- Falls `specialUse` verwendet wird, muss die Begründung im Manifest und in der Doku stehen.

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

**Grund:**

`WindowManager` arbeitet direkt mit Views. `ComposeView` im Overlay ist möglich, braucht aber korrekt gesetzte Lifecycle-/SavedState-/ViewModel-Owner und erhöht die Crash-Gefahr. Für den MVP sind klassische Views robuster und einfacher zu debuggen.

**Konsequenz:**

- Klassen wie `FloatingBubbleView` und `ReplyPanelView` werden als klassische Views gebaut.
- Kein Compose im Overlay-MVP.
- ComposeView im Overlay erst nach stabilem MVP und nur mit expliziter Entscheidung.

---

## D-004 — Application ID und SDK-Basis

**Status:** teilweise entschieden

**Entscheidung:**

| Punkt | Wert |
|---|---|
| App-Name | ChatHilfe |
| `applicationId` | `de.disaai.chathilfe` |
| `compileSdk` | 36 |
| `targetSdk` | 35 für ersten stabilen MVP |
| späteres Ziel | targetSdk 36 nach Stabilisierung |
| `minSdk` | 29 |

**Noch vor Phase 1 zu pinnen:**

- Android Gradle Plugin Version
- Gradle Wrapper Version
- Kotlin Version
- Compose BOM Version

**Regel:**

Diese Toolchain-Versionen müssen direkt beim Scaffolden anhand aktueller offizieller Release-/Kompatibilitätsinformationen festgelegt werden. Keine alten Beispielversionen verwenden.

---

## D-005 — Clipboard-Fallback

**Status:** entschieden

**Entscheidung:**

Clipboard-Nutzung bleibt optional. Wenn Android das Lesen der Zwischenablage aus Overlay-/Service-Kontext verhindert oder leer liefert, muss der Nutzer den Text manuell ins Panel einfügen können.

**Grund:**

Moderne Android-Versionen beschränken Clipboard-Zugriffe, wenn die App nicht fokussiert ist. Der MVP darf deshalb nicht davon abhängen, dass Clipboard-Lesen immer funktioniert.

**Konsequenz:**

- ReplyPanel zeigt Clipboard-Vorschau nur, wenn tatsächlich Text lesbar ist.
- Es gibt immer ein Eingabefeld für manuelles Einfügen.
- Clipboard wird erst nach Nutzeraktion gelesen.
- Clipboard wird erst nach Bestätigung verwendet.
- Kein Hintergrund-Monitoring.

---

## D-006 — Gerätetest-Gate

**Status:** entschieden

**Entscheidung:**

Gerätetests werden nicht erst am Ende gesammelt, sondern früh nach Overlay und WhatsApp-Erkennung durchgeführt.

**Pflicht-Gates:**

- nach Phase 3: manuelles Overlay auf echtem Gerät testen
- nach Phase 4: WhatsApp-Erkennung auf echtem Gerät testen
- nach Phase 5: Clipboard/ReplyPanel auf echtem Gerät testen

**Grund:**

Overlay, UsageStats, Clipboard und Samsung-Hintergrundverhalten sind nicht zuverlässig durch reine Unit-Tests prüfbar.

---

## Offene Punkte

Diese Punkte bleiben bewusst offen, bis sie für Code relevant werden:

- konkretes OpenRouter-Default-Modell für Phase 7
- exakte AGP-/Gradle-/Kotlin-/Compose-Versionen für Phase 1
- konkreter Foreground-Service-Typ nach finaler Manifest-Prüfung

Offen heißt hier nicht beliebig: Ein Agent muss sie vor der jeweiligen Phase entscheiden, dokumentieren und gegen `AGENTS.md` sowie `docs/ANDROID_CONSTRAINTS.md` prüfen.
