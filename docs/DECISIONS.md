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

**Konsequenz:**

- Klassen wie `FloatingBubbleView` und `ReplyPanelView` werden als klassische Views gebaut.
- Kein Compose im Overlay-MVP.
- ComposeView im Overlay erst nach stabilem MVP und nur mit expliziter Entscheidung.

---

## D-004 — Application ID und SDK-Basis

**Status:** teilweise entschieden

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

## D-006 — Gerätetest-Gate

**Status:** entschieden

**Entscheidung:**

Gerätetests werden nicht erst am Ende gesammelt, sondern früh nach Overlay und WhatsApp-Erkennung durchgeführt.

**Pflicht-Gates:**

- nach Phase 3: manuelles Overlay auf echtem Gerät testen
- nach Phase 4: WhatsApp-Erkennung auf echtem Gerät testen
- nach Phase 5: Clipboard/ReplyPanel auf echtem Gerät testen

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

## Offene Punkte

Diese Punkte bleiben bewusst offen, bis sie für Code relevant werden:

- konkretes OpenRouter-Default-Modell für Phase 7
- exakte AGP-/Gradle-/Kotlin-/Compose-Versionen für Phase 1
- konkreter Foreground-Service-Typ nach finaler Manifest-Prüfung

Offen heißt hier nicht beliebig: Ein Agent muss sie vor der jeweiligen Phase entscheiden, dokumentieren und gegen `AGENTS.md` sowie `docs/ANDROID_CONSTRAINTS.md` prüfen.
