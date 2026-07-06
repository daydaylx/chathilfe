# ARCHITECTURE.md — ChatHilfe MVP

## Zweck

Dieses Dokument ist die technische Architekturquelle für den ChatHilfe-MVP.

Ziel ist eine kleine native Android-App mit Floating Button über WhatsApp, Mini-Fenster, drei Modi, KI-Vorschlägen, kompaktem Retry und manueller Kopierfunktion.

Die Architektur bleibt absichtlich klein. Overengineering ist hier ein reales Risiko.

Technische Grundentscheidungen stehen verbindlich in `docs/DECISIONS.md`.

---

## Festgelegte Architekturentscheidungen

| Thema | Entscheidung |
|---|---|
| MainActivity | Jetpack Compose |
| Overlay Bubble | klassische Android View |
| ReplyPanel im Overlay | klassische Android View |
| Overlay-API | `WindowManager` + `TYPE_APPLICATION_OVERLAY` |
| Runtime | Foreground Service, aus sichtbarer Nutzeraktion gestartet |
| App-Erkennung | `UsageStatsManager.queryEvents()` |
| KI-Provider | OpenRouter, ein Provider im MVP |
| KI-Modell | ein OpenRouter-Default-Modell, vor Phase 7 in `AiConfig` pinnen |
| Modellrouting | nicht im MVP |
| API-Key | Build-Time-Konfiguration, nicht im Repo, nicht im UI |
| Einstellungen | DataStore für UI-/Overlay-Settings, nicht für API-Key oder Texte |
| Retry | temporäre `RetryInstruction`, nicht persistent |

Kein ComposeView im Overlay-MVP. ComposeView im `WindowManager` ist erst nach stabilem MVP erlaubt und braucht eine eigene Entscheidung.

---

## Architekturziele

- native Android-App in Kotlin
- klare Trennung zwischen Setup, Overlay, App-Erkennung, Clipboard, KI und Settings
- Overlay-Verwaltung zentral über einen Controller
- keine doppelten oder hängenden Overlay-Views
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

---

## Empfohlene Struktur

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
    ├── RetryInstruction.kt
    ├── ReplyRequest.kt
    └── ReplySuggestion.kt
```

Keine Repository-/UseCase-/Interactor-Schicht einführen, solange kein konkreter Nutzen entsteht.

---

## Laufzeitmodell

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
ForegroundAppDetector prüft Vordergrund-App
↓
Wenn com.whatsapp aktiv:
    Floating Button anzeigen
Sonst:
    Floating Button ausblenden
↓
Nutzer tippt Floating Button
↓
ReplyPanel öffnet sich
↓
Nutzer wählt Modus, Ton und Eingabe
↓
AiClient erzeugt 3 Vorschläge
↓
Nutzer kopiert Vorschlag
oder nutzt einen bewussten Retry mit optionaler RetryInstruction
↓
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

Nicht Aufgabe:

- KI-Dauerjobs
- Clipboard-Lesen im Hintergrund
- automatische App-Starts aus dem Hintergrund
- Persistieren von Texten, Vorschlägen oder Retries

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
- beim Stop alle Views entfernen
- Drag/Tap sauber trennen

---

## ReplyPanelView

Aufgaben:

- Modusauswahl anzeigen
- Ton-Auswahl anzeigen
- Eingabefelder anzeigen
- Clipboard-Vorschau nur nach Nutzeraktion ermöglichen
- 3 Vorschlagskarten anzeigen
- Kopieren pro Vorschlag ermöglichen
- kompakten Retry-Bereich nach Ergebnissen anzeigen

Retry-Regeln:

- Retry-Bereich erst nach Vorschlägen anzeigen
- `Nochmal` ohne zusätzliche RetryInstruction senden
- Änderungs-Chips als temporäre `RetryInstruction` für die nächste Anfrage setzen
- maximal 1–2 Retry-Chips gleichzeitig aktiv
- RetryInstruction nach neuer Anfrage oder Panel-Schließen verwerfen

Nicht Aufgabe:

- Verlauf anzeigen
- Bewertung einzelner Vorschläge
- freie Feedback-Texteingabe im MVP
- Stiltraining
- Memory/Profile
- Modell- oder Provider-Auswahl

---

## ForegroundAppDetector

Aufgaben:

- aktuelle Vordergrund-App über `UsageStatsManager.queryEvents()` erkennen
- `com.whatsapp` prüfen
- fehlenden Usage Access sauber melden

Default:

- Polling 1000 ms
- maximal 500 ms, wenn sichtbar zu träge

Kein Accessibility-Fallback.

---

## ClipboardHelper

Erlaubt:

- Clipboard lesen, wenn Nutzer das Panel aktiv öffnet oder im Panel eine entsprechende Aktion auslöst
- Vorschau anzeigen, wenn Text verfügbar ist
- Text erst nach Bestätigung verwenden
- generierte Vorschläge kopieren

Verboten:

- Hintergrundüberwachung
- Clipboard-Historie
- Logging von Clipboard-Inhalten

Fallback:

- Wenn Clipboard-Lesen leer oder blockiert ist, muss der Nutzer Text manuell ins Panel einfügen können.

---

## AiConfig

Aufgaben:

- OpenRouter Endpoint definieren
- eine Modell-ID definieren
- API-Key aus Build-Time-Konfiguration bereitstellen

Regeln:

- echter API-Key darf nicht im Repo stehen
- lokale Secret-Dateien müssen ignoriert werden
- kein API-Key im UI
- kein API-Key in DataStore
- kein Modellrouting im MVP

---

## AiClient

MVP-Regel:

- OpenRouter als einziger Provider
- genau ein Default-Modell
- kein Multi-Provider-System vor stabilem MVP
- kein automatisches Modell-Fallback im MVP
- API-Key aus `AiConfig` / BuildConfig lesen
- API-Key nie loggen
- Nutzertexte nie loggen
- Retry-Anweisungen nie loggen

Fehlerfälle:

- API-Key fehlt im lokalen Build
- Netzwerkfehler
- Rate Limit
- leere/ungültige Antwort

---

## PromptBuilder

Die UI entscheidet den Modus. Die KI soll nicht raten.

Modi:

- `Reply`: auf kopierte oder manuell eingefügte Nachricht antworten
- `Compose`: neue Nachricht aus Absicht formulieren
- `Rewrite`: vorhandenen Text umschreiben

Optionale Eingabe:

- `RetryInstruction`: nur für die nächste Anfrage, nicht persistent

Prompts liegen in `docs/PROMPTS.md`.

---

## AiResponseParser

Pflichten:

- 3 Vorschläge tolerant extrahieren
- Nummerierungsfehler tolerieren
- bei schlechter Antwort nicht crashen
- Fallback anzeigen oder klare Fehlermeldung liefern

---

## Persistenter State

Erlaubt:

```text
isOverlayEnabled
preferredTone
lastMode optional
bubbleX
bubbleY
```

Nicht erlaubt:

```text
apiKey
clipboardPreview
confirmedClipboardText
currentUserIntent
currentOriginalText
retryInstruction
suggestions
suggestionHistory
retryHistory
memoryData
profileData
```

---

## Testbarkeit

Automatisierbar:

- PromptBuilder
- AiResponseParser
- ReplyMode-/ToneOption-/RetryInstruction-Mapping

Gerätetest nötig:

- Foreground Service + Overlay
- Usage Access
- WhatsApp-Erkennung
- Dragging
- Clipboard-Zugriff und manueller Fallback
- ReplyPanel + Retry-Bereich
- Sperren/Entsperren
- Samsung-Akkuverhalten

---

## Akzeptanzkriterien

Architektur ist ausreichend, wenn:

- App baut als APK
- Foreground Service startet aus sichtbarer Nutzeraktion
- OverlayController verwaltet alle Overlay-Views zentral
- Floating Button erscheint nur bei WhatsApp
- ReplyPanel funktioniert kompakt
- Retry-Bereich funktioniert ohne Verlauf, Bewertung, Profil oder Gedächtnis
- Clipboard wird nur nach Nutzeraktion gelesen oder manueller Fallback funktioniert
- KI liefert 3 Vorschläge oder saubere Fehler
- API-Key kommt aus lokaler Build-Time-Konfiguration und steht nicht im Repo
- keine API-Key-Eingabe im UI vorhanden ist
- genau ein OpenRouter-Default-Modell genutzt wird
- keine WhatsApp-Automation vorhanden ist
- kein Accessibility Service vorhanden ist
- keine unnötigen Berechtigungen im Manifest stehen
