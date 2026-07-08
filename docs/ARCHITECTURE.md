# ARCHITECTURE.md — ChatHilfe MVP

## Zweck

Dieses Dokument ist die technische Architekturquelle für den ChatHilfe-MVP.

Ziel ist eine kleine native Android-App mit Floating Button über WhatsApp, schmalem Eingabebalken, kompaktem Ergebnis-Panel, KI-Vorschlägen, kompaktem Retry und manueller Kopierfunktion.

Die Architektur bleibt absichtlich klein. Overengineering ist hier ein reales Risiko.

Technische Grundentscheidungen stehen verbindlich in `docs/DECISIONS.md`. Der visuelle Scope steht zusätzlich in `docs/VISUAL_SCOPE.md`.

Für bewusst eingefügte WhatsApp-Dialogblöcke gilt zusätzlich `docs/WHATSAPP_DIALOG_CONTEXT.md`.

---

## Festgelegte Architekturentscheidungen

| Thema | Entscheidung |
|---|---|
| MainActivity | Jetpack Compose |
| Overlay Bubble | klassische Android View |
| Input-Bar im Overlay | klassische Android View |
| Result-Panel im Overlay | klassische Android View |
| Overlay-API | `WindowManager` + `TYPE_APPLICATION_OVERLAY` |
| Runtime | Foreground Service, aus sichtbarer Nutzeraktion gestartet |
| App-Erkennung | `UsageStatsManager.queryEvents()` |
| KI-Provider | OpenRouter, ein Provider im MVP |
| KI-Modell | ein OpenRouter-Default-Modell, vor Phase 7 in `AiConfig` pinnen |
| Modellrouting | nicht im MVP |
| API-Key | Build-Time-Konfiguration, nicht im Repo, nicht im UI |
| Einstellungen | DataStore für UI-/Overlay-Settings, nicht für API-Key oder Texte |
| Retry | temporäre `RetryInstruction`, nicht persistent |
| Dialogauszug | optionaler transienter Parser-Kontext aus bewusst eingefügtem Text, nicht persistent |
| Vorschlagsanzeige | ein sichtbarer Vorschlag, Wechsel per Swipe, Pfeil oder Pager |

Kein ComposeView im Overlay-MVP. ComposeView im `WindowManager` ist erst nach stabilem MVP erlaubt und braucht eine eigene Entscheidung.

---

## Architekturziele

- native Android-App in Kotlin
- klare Trennung zwischen Setup, Overlay, App-Erkennung, Clipboard, KI und Settings
- Overlay-Verwaltung zentral über einen Controller
- keine doppelten oder hängenden Overlay-Views
- schmaler Eingabebalken als Startzustand
- Ergebnis-Panel erst nach KI-Antwort
- immer nur ein sichtbarer Vorschlag im Ergebnis-Panel
- bewusst eingefügte Dialogauszüge nur transient strukturieren
- keine WhatsApp-Automation
- keine automatische Chat-Auslesung
- keine Speicherung von Nutzertexten, Vorschlägen, Retry-Anweisungen oder Verläufen
- keine unnötigen Architektur-Schichten
- erster Zielzustand: stabile private APK

---

## Nicht-Ziele

Nicht bauen:

- Accessibility Service
- automatisches Senden
- automatisches Einfügen
- WhatsApp-Chat-Reader
- Notification Scraping
- Screen Scraping
- Multi-Messenger-Framework
- Multi-Provider-System
- Modellrouting nach Tonfall
- Account-System
- Backend
- Analytics
- Gedächtnis-/Memory-System
- Verlauf für Nutzertexte, Vorschläge oder Retries
- Personen-, Kontakt-, Beziehungs- oder Stilprofile
- große Clean Architecture
- API-Key-Eingabe im MVP
- großes Formular als Startzustand
- drei Vorschläge untereinander als Standardansicht

---

## Empfohlene Struktur

```text
app/
  MainActivity.kt
  settings/
    SettingsScreen.kt
    SettingsStore.kt
    PermissionStatus.kt
  overlay/
    OverlayService.kt
    OverlayController.kt
    FloatingBubbleView.kt
    InputBarView.kt
    ResultPanelView.kt
    SuggestionPager.kt
    OverlayPositionStore.kt
  detection/
    ForegroundAppDetector.kt
  clipboard/
    ClipboardHelper.kt
  chat/
    WhatsAppChatParser.kt
  ai/
    AiClient.kt
    PromptBuilder.kt
    AiResponseParser.kt
    AiConfig.kt
  model/
    ReplyMode.kt
    ToneOption.kt
    RetryInstruction.kt
    ReplyRequest.kt
    ReplySuggestion.kt
    ParsedChatMessage.kt
    ParsedChatContext.kt
```

Keine Repository-/UseCase-/Interactor-Schicht einführen, solange kein konkreter Nutzen entsteht.

---

## Laufzeitmodell

```text
Nutzer öffnet MainActivity
Nutzer gewährt Berechtigungen
Nutzer aktiviert Overlay bewusst
Foreground Service startet
ForegroundAppDetector prüft Vordergrund-App
Wenn com.whatsapp aktiv ist, erscheint der Floating Button
Nutzer tippt Floating Button
InputBar öffnet sich
Nutzer wählt Ton, gibt Text ein oder nutzt Einfügen
Optional: eingefügter WhatsApp-Dialogblock wird transient strukturiert
AiClient erzeugt 3 Vorschläge
ResultPanel zeigt Vorschlag 1 von 3
Nutzer wechselt Vorschlag per Pager, Swipe oder Pfeilnavigation
Nutzer kopiert den sichtbaren Vorschlag oder nutzt Retry
Nutzer fügt manuell in WhatsApp ein
```

---

## Komponenten

## MainActivity

Aufgaben:

- Berechtigungsstatus anzeigen
- API-Key-Konfigurationsstatus anzeigen, falls sinnvoll
- Overlay aktivieren/deaktivieren
- Test-Overlay starten
- kurze Hinweise anzeigen

Nicht Aufgabe:

- Chatverlauf
- KI-Konversation
- komplexe Navigation
- API-Key-Eingabe
- Modell-Auswahl
- Provider-Auswahl
- Gedächtnis-/Profilverwaltung

---

## OverlayService

Aufgaben:

- Foreground Service für die Overlay-Laufzeit hosten
- aus sichtbarer Nutzeraktion starten
- `startForeground()` zeitnah aufrufen
- OverlayController und ForegroundAppDetector koordinieren
- bei Stop alle Overlay-Views entfernen lassen
- `ReplyRequest` aus bewusst bereitgestellten Eingaben bauen
- optional bewusst eingefügten Dialogauszug über `WhatsAppChatParser` strukturieren

Nicht Aufgabe:

- KI-Dauerjobs
- Clipboard-Lesen im Hintergrund
- automatische App-Starts aus dem Hintergrund
- Persistieren von Texten, Vorschlägen oder Retries
- Speichern von Dialogauszügen

---

## WhatsAppChatParser

Pure Kotlin-Komponente für bewusst eingefügte WhatsApp-Dialogblöcke.

Aufgaben:

- WhatsApp-Zeilen im Muster `[Datum, Uhrzeit] Sprecher: Nachricht` erkennen
- erst ab mindestens zwei passenden Zeilen aktiv werden
- mehrzeilige Nachrichten tolerant behandeln
- `ParsedChatContext` erzeugen
- letzte relevante Nachricht des Gegenübers heuristisch bestimmen

Nicht Aufgabe:

- WhatsApp auslesen
- Clipboard überwachen
- Kontakte erkennen
- Namen speichern
- Profile bauen
- Chatverlauf persistieren

Siehe: `docs/WHATSAPP_DIALOG_CONTEXT.md`.

---

## SettingsStore

Speichern:

- Overlay aktiv/inaktiv
- bevorzugter Ton
- letzter Modus optional
- Floating-Button-Position

Nicht speichern:

- API-Key
- kopierte WhatsApp-Nachrichten
- Nutzerabsichten
- Originaltexte
- generierte Vorschläge
- Retry-Anweisungen
- Chatverläufe
- Dialogauszüge
- Clipboard-Historie
- Profile oder Gedächtnisdaten

---

## OverlayController

Zentrale Klasse für alle `WindowManager`-Operationen.

Nur hier erlaubt:

- `addView`
- `removeView`
- `updateViewLayout`

Pflichten:

- doppelte Views verhindern
- vor `addView` Attached-State prüfen
- vor `removeView` Attached-State prüfen
