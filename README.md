# ChatHilfe

Private Android-App als MVP für einen schwebenden KI-Formulierungshelfer über WhatsApp.

Die App soll kein Messenger-Ersatz und keine WhatsApp-Automation werden. Sie soll nur dabei helfen, schwierige oder unklare Chatnachrichten besser zu formulieren.

---

## Projektstatus

**Status:** Konzept- und Vorbereitungsphase  
**Ziel:** private Android-APK  
**Primäres Gerät:** Samsung Galaxy S25 / moderne Android-Version  
**Hauptplattform:** Android 15/16  
**Veröffentlichung:** zunächst nicht Play Store

Aktuell enthält das Repository vor allem Projektvorgaben für Coding-Agenten:

- [`AGENTS.md`](AGENTS.md) — verbindliche Arbeitsregeln für Coding-Agenten
- [`Konzept.md`](Konzept.md) — Produktidee, Scope, Modi, Risiken und Abschlusskriterien
- [`Arbeitsauftrag.md`](Arbeitsauftrag.md) — konkreter Arbeitsauftrag für die MVP-Umsetzung

---

## Ziel des MVP

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

## Nicht-Ziele

Diese Punkte sind bewusst nicht Teil des MVP:

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

Wenn eine Umsetzung eines dieser Themen benötigen würde, ist sie außerhalb des aktuellen Scopes.

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
| KI-Anbindung | OpenRouter oder OpenAI |
| Distribution | private APK |

---

## Grundprinzip der App

Die App soll WhatsApp nicht steuern. Sie soll nur ein Hilfsfenster über WhatsApp bereitstellen.

Der Nutzer bleibt immer selbst verantwortlich:

- Text prüfen
- Vorschlag kopieren
- in WhatsApp einfügen
- Nachricht senden

Die App soll keine automatischen Aktionen innerhalb von WhatsApp durchführen.

---

## Modi

## 1. Antworten

Für Situationen, in denen der Nutzer auf eine konkrete kopierte Nachricht reagieren möchte.

Beispiel:

```text
Kopierte Nachricht:
Warum meldest du dich erst jetzt?

Nutzerabsicht:
Ich will mich entschuldigen, aber nicht unterwürfig klingen.

Ton:
ruhig, ehrlich, kurz
```

Die KI erzeugt drei passende Antwortvorschläge.

---

## 2. Formulieren

Für Situationen, in denen der Nutzer selbst etwas sagen möchte, aber die richtige Formulierung fehlt.

Beispiel:

```text
Ich will sagen, dass ich heute Ruhe brauche, aber nicht kalt klingen.
```

Die KI erzeugt drei sendbare Chatnachrichten.

---

## 3. Umschreiben

Für Situationen, in denen ein vorhandener Text verbessert werden soll.

Beispiel:

```text
Original:
Keine Ahnung, mach halt was du willst.

Gewünschte Änderung:
weniger passiv-aggressiv
```

Die KI erzeugt drei bessere Varianten.

---

## Benötigte Android-Berechtigungen

| Berechtigung | Zweck |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Floating Button und Mini-Fenster über WhatsApp anzeigen |
| `PACKAGE_USAGE_STATS` | erkennen, ob WhatsApp im Vordergrund ist |
| `INTERNET` | Anfrage an den KI-Anbieter senden |
| `POST_NOTIFICATIONS` | optional, falls ein Foreground Service verwendet wird |
| `FOREGROUND_SERVICE` | optional, falls ein Foreground Service verwendet wird |

Nicht verwenden:

- Kontakte
- SMS
- Kamera
- Mikrofon
- Standort
- Medienzugriff
- Accessibility Service

---

## Datenschutzregeln

Die App verarbeitet nur Inhalte, die der Nutzer aktiv eingibt oder bewusst bestätigt.

Erlaubt:

- Nutzer gibt eine Absicht ein
- Nutzer bestätigt eine kopierte Nachricht aus der Zwischenablage
- KI bekommt Modus, Ton, Absicht und optional bestätigten kopierten Text
- Nutzer kopiert einen generierten Vorschlag

Nicht erlaubt:

- WhatsApp-Chats automatisch lesen
- Clipboard dauerhaft überwachen
- kopierte Nachrichten speichern
- generierte Antworten speichern
- Nutzertexte loggen
- API-Keys loggen
- Kontakte oder Gerätekennungen senden

---

## Geplante Architektur

Kleine, feature-nahe Struktur ohne unnötige Clean-Architecture-Schichten:

```text
app/
├── MainActivity.kt
├── settings/
│   ├── SettingsScreen.kt
│   ├── SettingsStore.kt
│   └── PermissionStatus.kt
├── overlay/
│   ├── OverlayService.kt
│   ├── OverlayController.kt
│   ├── FloatingBubbleView.kt
│   ├── ReplyPanelView.kt
│   └── OverlayPositionStore.kt
├── detection/
│   └── ForegroundAppDetector.kt
├── clipboard/
│   └── ClipboardHelper.kt
├── ai/
│   ├── AiClient.kt
│   ├── PromptBuilder.kt
│   ├── AiResponseParser.kt
│   └── AiConfig.kt
└── model/
    ├── ReplyMode.kt
    ├── ToneOption.kt
    ├── ReplyRequest.kt
    └── ReplySuggestion.kt
```

Wichtig: `addView`, `removeView` und `updateViewLayout` sollen zentral in einem Overlay-Controller verwaltet werden, damit keine doppelten oder hängenden Overlays entstehen.

---

## Vorgesehene Entwicklungsphasen

### Phase 1: Projektbasis

- Android-Projekt erstellen
- Kotlin und Jetpack Compose einrichten
- Startscreen bauen
- DataStore vorbereiten
- API-Key lokal speichern

### Phase 2: Berechtigungen

- Overlay-Berechtigung prüfen
- Nutzungsdatenzugriff prüfen
- passende Android-Einstellungsseiten öffnen
- verständliche Statusanzeige bauen

### Phase 3: Manuelles Overlay

- Floating Button über `WindowManager` anzeigen
- Button verschiebbar machen
- Position speichern
- Test-Overlay aus der App starten

### Phase 4: WhatsApp-Erkennung

- aktive Vordergrund-App über `UsageStatsManager.queryEvents()` erkennen
- Button nur bei `com.whatsapp` anzeigen
- Button außerhalb von WhatsApp ausblenden

### Phase 5: Mini-Fenster

- Modusauswahl bauen
- Eingabefeld bauen
- Ton-Auswahl bauen
- Clipboard-Vorschau nur nach Nutzeraktion
- Ergebnisbereich und Kopierbuttons bauen

### Phase 6: KI-Anbindung

- `AiClient` bauen
- `PromptBuilder` für drei Modi bauen
- Fehlerbehandlung
- Ladezustand
- 3 Vorschläge parsen und anzeigen

### Phase 7: Stabilisierung

- echtes Android-Gerät testen
- Sperren/Entsperren prüfen
- App-Wechsel prüfen
- Internetfehler prüfen
- fehlende Berechtigungen prüfen
- Samsung-Akkuoptimierung dokumentieren

---

## Build-Hinweise

Das eigentliche Android-Projekt ist noch nicht angelegt. Sobald Gradle-Dateien vorhanden sind, sollten typische Befehle ergänzt und geprüft werden:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

Agenten sollen keine erfolgreichen Builds oder Tests behaupten, wenn sie nicht ausgeführt wurden.

---

## Manuelle Testcheckliste

Vor einem brauchbaren MVP muss mindestens Folgendes auf einem echten Android-Gerät geprüft werden:

- APK installieren
- App öffnen
- Overlay-Berechtigung setzen
- Nutzungsdatenzugriff setzen
- API-Key speichern
- Overlay aktivieren
- WhatsApp öffnen → Floating Button erscheint
- WhatsApp verlassen → Floating Button verschwindet
- Button verschieben
- Button-Position bleibt erhalten
- Button antippen → Mini-Fenster öffnet
- kopierte Nachricht verwenden
- Modus **Antworten** testen
- Modus **Formulieren** testen
- Modus **Umschreiben** testen
- KI-Vorschläge generieren
- Vorschlag kopieren
- Vorschlag manuell in WhatsApp einfügen
- Bildschirm sperren/entsperren
- prüfen, dass kein doppeltes Overlay entsteht
- Internet deaktivieren und Fehlermeldung prüfen
- API-Key entfernen und Fehlermeldung prüfen

---

## Abschlusskriterien für den MVP

Der MVP gilt erst als fertig, wenn:

- die App als APK installierbar ist
- die Berechtigungsseite verständlich ist
- Overlay-Berechtigung und Nutzungsdatenzugriff korrekt geprüft werden
- Floating Button nur bei WhatsApp erscheint
- Floating Button außerhalb von WhatsApp verschwindet
- Mini-Fenster stabil funktioniert
- alle drei Modi funktionieren
- Zwischenablage nur bewusst übernommen wird
- KI genau drei Vorschläge erzeugt
- Vorschläge kopierbar sind
- keine WhatsApp-Automation eingebaut wurde
- kein Accessibility Service verwendet wurde
- keine unnötigen Berechtigungen verwendet wurden
- keine Nutzertexte oder API-Keys geloggt werden
- Code klein und wartbar bleibt

---

## Agenten-Hinweis

Vor jeder größeren Änderung muss [`AGENTS.md`](AGENTS.md) gelesen werden.

Wenn Anweisungen kollidieren, gilt die strengere Regel bezüglich Datenschutz, Automatisierung und Scope-Begrenzung.

---

## Projekturteil

Der Scope ist realistisch und als privater Android-MVP gut machbar.

Die Hauptschwierigkeit liegt nicht in der KI, sondern in:

- Android-Overlay-Verhalten
- Berechtigungen
- Vordergrund-App-Erkennung
- Service-Lifecycle
- Samsung-/Android-Hintergrundlimits
- sauberer, unaufdringlicher UX

Der MVP soll klein bleiben: Floating Button, Mini-Fenster, Moduswahl, KI-Vorschläge, Kopieren. Alles darüber hinaus erhöht Risiko und Entwicklungsaufwand deutlich.
