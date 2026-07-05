# ARCHITECTURE.md — ChatHilfe Android MVP

## 1. Zweck dieses Dokuments

Dieses Dokument legt die technische Architektur für den ChatHilfe-MVP fest.

Ziel ist eine kleine, robuste Android-App, die über WhatsApp einen schwebenden KI-Formulierungshelfer bereitstellt.

Die Architektur soll bewusst pragmatisch bleiben. Das Projekt ist kein großes Plattformprodukt, sondern ein privater Android-MVP. Übertriebene Clean-Architecture-Strukturen, unnötige Abstraktionen und generische Frameworks sind ausdrücklich zu vermeiden.

---

## 2. Architekturziele

## 2.1 Ziele

- kleiner, wartbarer Kotlin-Code
- native Android-Umsetzung
- klare Trennung zwischen Einrichtung, Overlay, Erkennung, KI und Einstellungen
- stabile Overlay-Verwaltung über `WindowManager`
- keine doppelten oder hängenden Overlay-Views
- klare Berechtigungslogik
- privacy-sicherer Clipboard-Umgang
- KI-Anfragen nur mit aktiv bestätigten Nutzerdaten
- lauffähige private APK als erstes Ergebnis

## 2.2 Nicht-Ziele

Nicht bauen:

- keine WhatsApp-Automation
- kein automatisches Senden
- kein automatisches Einfügen
- kein WhatsApp-Chat-Reader
- kein Accessibility Service
- kein Notification Scraping
- kein Screen Scraping
- kein Multi-Messenger-Framework
- kein Account-System
- kein Backend
- keine Analytics
- keine große Plugin-Architektur
- keine überdimensionierte Clean Architecture

---

## 3. Grundentscheidung

## 3.1 Empfohlene Architektur

Feature-nahe, kleine Struktur:

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

## 3.2 Warum keine große Clean Architecture?

Für den MVP gibt es nur wenige fachliche Abläufe:

- Berechtigungen prüfen
- Overlay anzeigen/verstecken
- WhatsApp-Vordergrund erkennen
- Nutzereingabe einsammeln
- Prompt bauen
- KI-Anfrage senden
- Vorschläge kopieren

Eine zusätzliche Repository-/UseCase-/Interactor-Schicht würde hier mehr Reibung als Nutzen erzeugen. Sie darf erst eingeführt werden, wenn ein konkretes Problem entsteht, etwa mehrere KI-Provider, komplexe Persistenz oder echte Testnotwendigkeit für Fachlogik.

---

## 4. UI-Architektur

## 4.1 MainActivity

Aufgabe:

- Einrichtung und Status anzeigen
- API-Key speichern
- Overlay-Berechtigung prüfen
- Nutzungsdatenzugriff prüfen
- Overlay aktivieren/deaktivieren
- Test-Overlay starten

Technik:

- Jetpack Compose
- keine komplexe Navigation
- eine einfache Settings-/Status-Seite reicht für den MVP

MainActivity ist nicht der Hauptnutzungsort. Die eigentliche Nutzung passiert über das Overlay in WhatsApp.

---

## 4.2 Overlay-UI

Für den MVP wird das Overlay primär mit klassischen Android Views umgesetzt.

Grund:

- `WindowManager` arbeitet direkt mit Views
- klassische Views sind im Overlay-Lifecycle einfacher
- weniger Probleme mit Compose-LifecycleOwner, SavedStateRegistryOwner und ViewModelStoreOwner
- besser kontrollierbar für Floating Button und kleines Panel

Spätere ComposeView-Nutzung ist möglich, aber erst nach einem stabilen View-basierten MVP.

---

## 5. Laufzeitmodell

## 5.1 Normaler Ablauf

```text
Nutzer öffnet MainActivity
↓
Nutzer gewährt Berechtigungen
↓
Nutzer aktiviert Overlay bewusst
↓
OverlayRuntime startet aus sichtbarer Nutzeraktion
↓
ForegroundAppDetector prüft Vordergrund-App
↓
Wenn com.whatsapp aktiv:
    Floating Button anzeigen
Sonst:
    Floating Button entfernen/verstecken
↓
Nutzer tippt Floating Button
↓
ReplyPanel öffnet sich
↓
Nutzer wählt Modus + Ton + Eingabe
↓
AiClient erzeugt 3 Vorschläge
↓
Nutzer kopiert Vorschlag
↓
Nutzer fügt manuell in WhatsApp ein
```

## 5.2 Startregel

Der Overlay-Runtime-Start muss aus einer sichtbaren Nutzeraktion kommen.

Nicht gewünscht:

```text
App wird heimlich im Hintergrund gestartet
↓
Service startet ungefragt
↓
Overlay erscheint ohne klare Aktivierung
```

Gewünscht:

```text
Nutzer öffnet App
↓
Nutzer aktiviert Overlay
↓
Dienst/Runtime startet bewusst
```

---

## 6. Zentrale Komponenten

## 6.1 `SettingsScreen`

Aufgaben:

- Statuskarten für Berechtigungen anzeigen
- API-Key-Eingabe bereitstellen
- Overlay aktivieren/deaktivieren
- Testfunktion für Overlay anbieten
- verständliche Fehlhinweise anzeigen

Darf nicht:

- Chatfunktion enthalten
- KI-Vorschläge dauerhaft anzeigen
- komplexe Navigation einführen

---

## 6.2 `SettingsStore`

Speichert lokal:

- API-Key
- Overlay aktiv/inaktiv
- bevorzugter Ton
- zuletzt gewählter Modus optional
- Floating-Button-Position

Speichert nicht:

- kopierte WhatsApp-Nachrichten
- Nutzerabsichten
- generierte Vorschläge
- Chatverläufe
- Kontakte
- Clipboard-Historie

Empfohlene Technik:

- Jetpack DataStore

---

## 6.3 `PermissionStatus`

Aufgaben:

- `Settings.canDrawOverlays()` prüfen
- Usage-Access-Zustand prüfen
- Internet-Berechtigung statisch über Manifest abdecken
- passende Android-Einstellungsseiten öffnen

Darf nicht:

- Berechtigungen erzwingen
- Nutzer in Schleifen schicken
- unnötige Berechtigungen anfordern

---

## 6.4 `OverlayService`

Aufgaben:

- Overlay-Runtime starten und stoppen
- Foreground-Erkennung periodisch auslösen
- OverlayController informieren
- sauber auf App-/Service-Ende reagieren

Regeln:

- Start möglichst aus sichtbarer Nutzeraktion
- keine dauerhafte Netzwerkaktivität im Service
- keine KI-Anfrage ohne Nutzeraktion
- keine Clipboard-Abfrage im Hintergrund
- kein unkontrolliertes Polling

Falls Foreground Service nötig ist:

- Notification klar und knapp
- `startForeground()` zeitnah aufrufen
- passenden Service-Typ und Berechtigung beachten
- keine FGS-Nutzung für schwere Hintergrundjobs

---

## 6.5 `OverlayController`

Zentrale Klasse für alle `WindowManager`-Operationen.

Aufgaben:

- Floating Button hinzufügen
- Floating Button entfernen
- ReplyPanel hinzufügen
- ReplyPanel entfernen
- View-Position aktualisieren
- doppelte Views verhindern
- `addView`, `removeView`, `updateViewLayout` zentral verwalten

Wichtige interne Zustände:

```text
bubbleAttached: Boolean
panelAttached: Boolean
lastBubblePosition: Point
isDragging: Boolean
```

Regeln:

- Nie direkt aus anderen Klassen `WindowManager.addView()` aufrufen.
- Vor `addView()` prüfen, ob View bereits attached ist.
- Vor `removeView()` prüfen, ob View wirklich attached ist.
- Exceptions beim Entfernen defensiv behandeln.
- Beim Service-Ende alle Views entfernen.

---

## 6.6 `FloatingBubbleView`

Aufgaben:

- kleiner schwebender Button
- Drag-Verhalten
- Tap-Erkennung
- Randpositionierung
- visuell unaufdringlich bleiben

Verhalten:

- Tap öffnet ReplyPanel
- Drag verschiebt Button
- Drag darf keinen Tap auslösen
- Position wird gespeichert
- Button wird nur bei WhatsApp angezeigt

---

## 6.7 `ReplyPanelView`

Aufgaben:

- Modusauswahl
- Clipboard-Vorschau
- Nutzereingabe
- Ton-Auswahl
- KI-Anfrage starten
- Ladezustand anzeigen
- Fehler anzeigen
- 3 Vorschläge anzeigen
- Kopieren pro Vorschlag

MVP-Felder:

- Modus: Antworten / Formulieren / Umschreiben
- optionaler Clipboard-Text
- Eingabe: „Was willst du sagen?“
- Ton: kurz, freundlich, direkt, entschuldigend, deeskalierend, klare Grenze, flirtend
- Button: Vorschläge erstellen
- Ergebnis: genau 3 Vorschläge

Regeln:

- kein Vollbilddialog
- kein Chatverlauf
- keine Speicherung generierter Antworten
- Panel muss einfach schließbar sein

---

## 6.8 `ForegroundAppDetector`

Aufgaben:

- aktuelle Vordergrund-App über `UsageStatsManager.queryEvents()` bestimmen
- prüfen, ob Paketname `com.whatsapp` ist
- später optional `com.whatsapp.w4b` vorbereiten

Empfehlung:

- Polling-Intervall initial 1000 ms
- bei zu träger UX maximal auf 500 ms senken
- nicht aggressiver pollen
- bei fehlendem Usage Access klaren Status liefern

Rückgabe-Beispiel:

```kotlin
data class ForegroundAppState(
    val packageName: String?,
    val isWhatsApp: Boolean,
    val hasUsageAccess: Boolean
)
```

---

## 6.9 `ClipboardHelper`

Aufgaben:

- Clipboard nur bei Nutzeraktion lesen
- Clipboard-Vorschau erzeugen
- bestätigten Clipboard-Text an ReplyPanel liefern
- generierte Vorschläge kopieren

Regeln:

- keine Hintergrundüberwachung
- keine Clipboard-Historie
- kein Logging von Clipboard-Inhalt
- Clipboard-Text erst nach Nutzerbestätigung in KI-Anfrage verwenden

---

## 6.10 `AiClient`

Aufgaben:

- API-Key lesen
- Anfrage an KI-Anbieter senden
- Fehler behandeln
- Antwort an Parser weitergeben

MVP-Regel:

- nur ein Provider aktiv umsetzen
- keine Provider-Abstraktion bauen, solange kein zweiter Provider konkret implementiert wird
- API-Key nie loggen
- Nutzertexte nie loggen

Fehlerfälle:

- API-Key fehlt
- Netzwerkfehler
- Rate Limit
- ungültige Antwort
- leerer Vorschlag

---

## 6.11 `PromptBuilder`

Aufgaben:

- Prompt anhand des Modus bauen
- Sprache standardmäßig Deutsch
- genau 3 Varianten verlangen
- keine Analyseausgabe verlangen

Modi:

- `Reply`: auf kopierte Nachricht antworten
- `Compose`: eigene Nachricht aus Nutzerabsicht schreiben
- `Rewrite`: vorhandenen Text umschreiben

Die KI entscheidet nicht selbst den Modus. Der Modus kommt aus der UI.

---

## 6.12 `AiResponseParser`

Aufgaben:

- Modellantwort robust in 3 Vorschläge aufteilen
- Nummerierung tolerieren
- leere Zeilen ignorieren
- Fallback: gesamten Text als Vorschlagsblock anzeigen, statt zu crashen

Nicht tun:

- App abstürzen lassen, wenn Nummerierung nicht exakt passt
- Halluzinierte Metadaten speichern
- Markdown-Tabellen erwarten

---

## 7. Datenfluss

## 7.1 Antworten-Modus

```text
Clipboard erkannt
↓
Nutzer bestätigt Clipboard-Nutzung
↓
Nutzer gibt Absicht ein
↓
Nutzer wählt Ton
↓
PromptBuilder baut Reply-Prompt
↓
AiClient sendet Anfrage
↓
AiResponseParser extrahiert 3 Vorschläge
↓
ReplyPanel zeigt Vorschläge
↓
Nutzer kopiert einen Vorschlag
```

## 7.2 Formulieren-Modus

```text
Nutzer gibt Absicht ein
↓
Nutzer wählt Ton
↓
PromptBuilder baut Compose-Prompt
↓
KI erzeugt 3 sendbare Nachrichten
```

## 7.3 Umschreiben-Modus

```text
Nutzer gibt Originaltext ein oder bestätigt Clipboard-Text
↓
Nutzer beschreibt gewünschte Änderung
↓
PromptBuilder baut Rewrite-Prompt
↓
KI erzeugt 3 verbesserte Varianten
```

---

## 8. State-Management

## 8.1 Persistenter State

Persistieren:

```text
apiKey
isOverlayEnabled
preferredTone
lastMode optional
bubbleX
bubbleY
```

## 8.2 Flüchtiger UI-State

Nicht persistieren:

```text
clipboardPreview
confirmedClipboardText
currentUserIntent
currentOriginalText
loadingState
suggestions
errorMessage
```

Grund: Nachrichteninhalte und generierte Antworten sollen nicht gespeichert werden.

---

## 9. Threading und Nebenläufigkeit

Empfehlung:

- KI-Anfragen in Coroutine / IO-Kontext
- UI-Updates auf Main Thread
- Overlay-Operationen auf Main Thread
- Foreground-App-Polling kontrolliert und abbrechbar

Regeln:

- keine blockierenden Netzwerkaufrufe im UI-Thread
- keine Endlosschleifen ohne Delay und Stop-Bedingung
- Service-Coroutines bei Stop sauber abbrechen
- keine parallelen KI-Anfragen aus demselben Panel ohne UI-Sperre

---

## 10. Fehlerstrategie

Fehler sollen im UI knapp und verständlich erscheinen.

Beispiele:

| Fehler | Nutzertext |
|---|---|
| Overlay fehlt | „Overlay-Berechtigung fehlt. Bitte erlauben, damit der Button über WhatsApp angezeigt werden kann.“ |
| Usage Access fehlt | „Nutzungsdatenzugriff fehlt. Die App kann sonst nicht erkennen, ob WhatsApp geöffnet ist.“ |
| API-Key fehlt | „API-Key fehlt. Bitte in den Einstellungen eintragen.“ |
| Internet fehlt | „Keine Verbindung. Bitte Internet prüfen.“ |
| KI-Fehler | „Vorschläge konnten nicht erstellt werden. Bitte erneut versuchen.“ |
| Clipboard leer | „Keine kopierte Nachricht erkannt.“ |

Keine Stacktraces in der UI.

---

## 11. Sicherheits- und Datenschutzgrenzen

Diese Architektur darf keine Funktion enthalten, die WhatsApp-Inhalte automatisch ausliest oder Aktionen in WhatsApp ausführt.

Verboten:

- Accessibility Service
- Notification Listener
- Screen Capture
- Kontakte lesen
- SMS lesen/senden
- automatisches Einfügen
- automatisches Senden
- persistente Nachrichtenhistorie

Erlaubt:

- vom Nutzer eingegebene Absicht
- vom Nutzer bestätigter Clipboard-Text
- KI-Vorschläge anzeigen
- Vorschlag per Button kopieren

---

## 12. Testbarkeit

## 12.1 Automatisierbar

Sinnvolle Unit-Tests:

- `PromptBuilder`
- `AiResponseParser`
- `ToneOption`-Mapping
- `ReplyMode`-Mapping
- SettingsStore, soweit praktikabel

## 12.2 Manuell auf Gerät nötig

Muss auf echtem Gerät getestet werden:

- Overlay-Berechtigung
- Usage Access
- Floating Button über WhatsApp
- Dragging
- Panel-Verhalten
- Sperren/Entsperren
- App-Wechsel
- Samsung-Akkuverhalten
- Clipboard-Zugriff

---

## 13. Verbotene Architekturentscheidungen

Nicht einführen:

- Repository-Schicht ohne Datenquelle
- UseCase-Schicht ohne echte Fachlogik
- globale Service-Locator
- EventBus
- große Dependency-Injection-Struktur
- Multi-Provider-KI-Abstraktion vor dem ersten stabilen Provider
- Vollbild-Chat-UI
- permanente Hintergrund-Clipboard-Überwachung
- Accessibility-basierte Fallbacks

---

## 14. MVP-Abschlusskriterien aus Architektur-Sicht

Die Architektur ist ausreichend, wenn:

- Build erzeugt installierbare APK
- MainActivity zeigt Berechtigungen und API-Key-Status
- OverlayController verwaltet alle Views zentral
- keine doppelten Overlay-Views entstehen
- Floating Button erscheint nur bei WhatsApp
- ReplyPanel funktioniert kompakt
- Clipboard wird nur nach Nutzeraktion gelesen
- PromptBuilder hat drei klare Modi
- AiClient liefert 3 Vorschläge oder saubere Fehler
- keine WhatsApp-Automation vorhanden ist
- kein Accessibility Service vorhanden ist
- keine unnötigen Berechtigungen im Manifest stehen
- README und relevante Docs aktualisiert sind

---

## 15. Entscheidungsstand

| Thema | Entscheidung |
|---|---|
| App-Typ | Native Android APK |
| Sprache | Kotlin |
| MainActivity UI | Jetpack Compose |
| Overlay UI | klassische Views für MVP |
| Overlay-API | WindowManager + TYPE_APPLICATION_OVERLAY |
| WhatsApp-Erkennung | UsageStatsManager.queryEvents() |
| Clipboard | nur nach Nutzeraktion |
| KI | ein Provider im MVP |
| Speicherung | DataStore |
| Automatisierung | keine WhatsApp-Automation |
| Accessibility | verboten im MVP |
