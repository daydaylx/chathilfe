# ARCHITECTURE.md — ChatHilfe MVP

## Zweck

Dieses Dokument ist die technische Architekturquelle für den ChatHilfe-MVP.

Ziel ist eine kleine native Android-App mit Floating Button über WhatsApp, Mini-Fenster, drei Modi, KI-Vorschlägen und manueller Kopierfunktion.

Die Architektur bleibt absichtlich klein. Overengineering ist hier ein reales Risiko.

---

## Architekturziele

- native Android-App in Kotlin
- klare Trennung zwischen Setup, Overlay, App-Erkennung, Clipboard, KI und Settings
- Overlay-Verwaltung zentral über einen Controller
- keine doppelten oder hängenden Overlay-Views
- keine WhatsApp-Automation
- keine automatische Chat-Auslesung
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
- Account-System
- Backend
- Analytics
- große Clean Architecture

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
OverlayRuntime startet aus sichtbarer Nutzeraktion
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
↓
Nutzer fügt manuell in WhatsApp ein
```

---

## Komponenten

## MainActivity

Aufgaben:

- Berechtigungsstatus anzeigen
- API-Key speichern
- Overlay aktivieren/deaktivieren
- Test-Overlay starten
- kurze Hinweise anzeigen

Nicht Aufgabe:

- Chatverlauf
- KI-Konversation
- komplexe Navigation

---

## SettingsStore

Speichern:

- API-Key
- Overlay aktiv/inaktiv
- bevorzugter Ton
- letzter Modus optional
- Floating-Button-Position

Nicht speichern:

- kopierte WhatsApp-Nachrichten
- Nutzerabsichten
- generierte Vorschläge
- Chatverläufe
- Clipboard-Historie

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

- Clipboard lesen, wenn Nutzer das Panel aktiv öffnet
- Vorschau anzeigen
- Text erst nach Bestätigung verwenden
- generierte Vorschläge kopieren

Verboten:

- Hintergrundüberwachung
- Clipboard-Historie
- Logging von Clipboard-Inhalten

---

## AiClient

MVP-Regel:

- ein Provider
- keine Multi-Provider-Abstraktion vor stabilem MVP
- API-Key nie loggen
- Nutzertexte nie loggen

Fehlerfälle:

- API-Key fehlt
- Netzwerkfehler
- Rate Limit
- leere/ungültige Antwort

---

## PromptBuilder

Die UI entscheidet den Modus. Die KI soll nicht raten.

Modi:

- `Reply`: auf kopierte Nachricht antworten
- `Compose`: neue Nachricht aus Absicht formulieren
- `Rewrite`: vorhandenen Text umschreiben

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
apiKey
isOverlayEnabled
preferredTone
lastMode optional
bubbleX
bubbleY
```

Nicht erlaubt:

```text
clipboardPreview
confirmedClipboardText
currentUserIntent
currentOriginalText
suggestions
```

---

## Testbarkeit

Automatisierbar:

- PromptBuilder
- AiResponseParser
- ReplyMode-/ToneOption-Mapping

Gerätetest nötig:

- Overlay
- Usage Access
- WhatsApp-Erkennung
- Dragging
- Clipboard-Zugriff
- Sperren/Entsperren
- Samsung-Akkuverhalten

---

## Akzeptanzkriterien

Architektur ist ausreichend, wenn:

- App baut als APK
- OverlayController verwaltet alle Overlay-Views zentral
- Floating Button erscheint nur bei WhatsApp
- ReplyPanel funktioniert kompakt
- Clipboard wird nur nach Nutzeraktion gelesen
- KI liefert 3 Vorschläge oder saubere Fehler
- keine WhatsApp-Automation vorhanden ist
- kein Accessibility Service vorhanden ist
- keine unnötigen Berechtigungen im Manifest stehen
