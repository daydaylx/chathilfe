# ARCHITECTURE.md — ChatHilfe MVP

## Zweck

Dieses Dokument ist die technische Architekturquelle für den ChatHilfe-MVP.

Ziel ist eine kleine native Android-App mit Floating Button über WhatsApp, schmalem Eingabebalken, kompaktem Ergebnis-Panel, KI-Vorschlägen, kompaktem Retry und manueller Kopierfunktion.

Die Architektur bleibt absichtlich klein. Overengineering ist hier ein reales Risiko.

Technische Grundentscheidungen stehen verbindlich in `docs/DECISIONS.md`. Der visuelle Scope steht zusätzlich in `docs/VISUAL_SCOPE.md`.

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
- InputBar und ResultPanel kontrolliert anzeigen, ersetzen oder größenmäßig aktualisieren

---

## InputBarView

Startzustand des Overlays nach Tap auf die Bubble.

Aufgaben:

- Ton-/Stil-Button anzeigen
- kompaktes Texteingabefeld anzeigen
- Einfügen-Button anzeigen
- Start-Button anzeigen
- Ladezustand kompakt anzeigen

Regeln:

- schmal bleiben
- kein Ergebnisbereich vor KI-Antwort
- Start-Button nicht `Senden` nennen
- erlaubte Start-Labels: `Los`, `Erstellen` oder schlichtes Pfeil-Icon
- bei leerem/blockiertem Clipboard weiter manuell nutzbar bleiben

---

## ResultPanelView

Ergebniszustand nach erfolgreicher oder teilweise erfolgreicher KI-Antwort.

Aufgaben:

- genau einen aktuellen Vorschlag anzeigen
- aktuelle Position anzeigen, zum Beispiel `1/3`
- Wechsel zwischen Vorschlägen anbieten
- Kopieren des sichtbaren Vorschlags ermöglichen
- kompakten Retry-Bereich anzeigen
- Schließen ermöglichen

Regeln:

- keine drei Vorschlagskarten untereinander als Standardansicht
- Swipe ist erlaubt und gewünscht
- Pfeilnavigation oder einfacher Pager ist als MVP-Fallback erlaubt
- Kopieren bezieht sich eindeutig auf den aktuell sichtbaren Vorschlag
- Retry-Bereich bleibt global und klein

Nicht Aufgabe:

- Verlauf anzeigen
- Bewertung einzelner Vorschläge
- freie Feedback-Texteingabe im MVP
- Stiltraining
- Memory/Profile
- Modell- oder Provider-Auswahl

---

## SuggestionPager

Kleine Logik für die Anzeige der drei Vorschläge.

Aufgaben:

- aktuellen Index halten
- `1/3`, `2/3`, `3/3` anzeigen
- Wechsel nach links/rechts ermöglichen
- optional Swipe unterstützen
- sichtbaren Vorschlag für Kopieren bereitstellen

Nicht speichern:

- Vorschlagsverlauf
- Retry-Verlauf
- Nutzungsverhalten

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

- Clipboard lesen, wenn Nutzer das Overlay aktiv öffnet oder eine entsprechende Aktion auslöst
- Text übernehmen, wenn verfügbar
- generierte Vorschläge kopieren

Verboten:

- Hintergrundüberwachung
- Clipboard-Historie
- Logging von Clipboard-Inhalten

Fallback:

- Wenn Clipboard-Lesen leer oder blockiert ist, muss der Nutzer Text manuell ins Overlay eingeben oder einfügen können.

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
- SuggestionPager-Indexlogik

Gerätetest nötig:

- Foreground Service + Overlay
- Usage Access
- WhatsApp-Erkennung
- Dragging
- Clipboard-Zugriff und manueller Fallback
- InputBar + ResultPanel
- Vorschlagswechsel per Swipe, Pfeil oder Pager
- Retry-Bereich
- Sperren/Entsperren
- Samsung-Akkuverhalten

---

## Akzeptanzkriterien

Architektur ist ausreichend, wenn:

- App baut als APK
- Foreground Service startet aus sichtbarer Nutzeraktion
- OverlayController verwaltet alle Overlay-Views zentral
- Floating Button erscheint nur bei WhatsApp
- InputBar öffnet kompakt
- ResultPanel erscheint erst nach KI-Antwort
- genau ein Vorschlag sichtbar ist und zwischen 3 Vorschlägen gewechselt werden kann
- Kopieren den aktuell sichtbaren Vorschlag kopiert
- Retry-Bereich funktioniert ohne Verlauf, Bewertung, Profil oder Gedächtnis
- Clipboard wird nur nach Nutzeraktion gelesen oder manueller Fallback funktioniert
- KI liefert 3 Vorschläge oder saubere Fehler
- API-Key kommt aus lokaler Build-Time-Konfiguration und steht nicht im Repo
- keine API-Key-Eingabe im UI vorhanden ist
- genau ein OpenRouter-Default-Modell genutzt wird
- keine WhatsApp-Automation vorhanden ist
- kein Accessibility Service vorhanden ist
- keine unnötigen Berechtigungen im Manifest stehen
