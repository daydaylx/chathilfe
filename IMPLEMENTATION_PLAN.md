# IMPLEMENTATION_PLAN.md — ChatHilfe MVP

## 1. Zweck

Dieses Dokument definiert die Umsetzungsreihenfolge für den ChatHilfe-MVP.

Ziel ist eine private Android-APK mit Floating Button über WhatsApp, Mini-Fenster, drei Modi, KI-Vorschlägen und manueller Kopierfunktion.

Der Plan ist bewusst phasenweise aufgebaut, damit Coding-Agenten nicht alles gleichzeitig bauen und den Scope nicht aufblasen.

---

## 2. Grundregeln

Vor jeder Umsetzung lesen:

1. `AGENTS.md`
2. `README.md`
3. `ARCHITECTURE.md`
4. `ANDROID_CONSTRAINTS.md`
5. dieses Dokument

Harte Regeln:

- kein Accessibility Service
- kein WhatsApp-Chat-Auslesen
- kein Auto-Einfügen
- kein Auto-Senden
- kein Clipboard-Monitoring im Hintergrund
- keine Kontakte-Berechtigung
- keine unnötige Clean Architecture
- keine Multi-App-Erweiterung im MVP

---

## 3. Zielbild des MVP

Der MVP ist fertig, wenn:

- APK installierbar ist
- App startet
- Berechtigungsstatus sichtbar ist
- API-Key lokal gespeichert werden kann
- Overlay bewusst aktiviert werden kann
- WhatsApp öffnen den Floating Button zeigt
- WhatsApp verlassen den Floating Button versteckt
- Floating Button verschiebbar ist
- Mini-Fenster öffnet
- Modi Antworten, Formulieren und Umschreiben funktionieren
- Clipboard nur nach Nutzeraktion gelesen wird
- KI genau drei Vorschläge erzeugt
- Vorschläge kopierbar sind
- keine WhatsApp-Automation vorhanden ist

---

## 4. Phase 0 — Projektprüfung

## Ziel

Vorhandenes Repo verstehen und Basis festlegen.

## Aufgaben

- Repo-Struktur prüfen
- vorhandene Dateien lesen
- prüfen, ob bereits Android-/Gradle-Projekt existiert
- Branch und Git-Status prüfen
- keine vorhandenen Nutzeränderungen überschreiben

## Abschlusskriterien

- Agent weiß, ob Android-Projekt bereits existiert
- keine Datei wurde unnötig verändert
- nächster Umsetzungsschritt ist klar

## Nicht tun

- kein Code generieren, bevor Projektbasis verstanden wurde
- keine Dependencies hinzufügen
- keine Architektur umbauen

---

## 5. Phase 1 — Android-Projektbasis

## Ziel

Eine minimale native Android-App erstellen, die baut und startet.

## Aufgaben

- Android-Projekt mit Kotlin anlegen
- Jetpack Compose für MainActivity einrichten
- sinnvolle Package-Struktur anlegen
- App-Name setzen: ChatHilfe
- Basis-Theme anlegen, bevorzugt dunkles Design
- Gradle-Setup minimal halten

## Dateien / Bereiche

- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/.../MainActivity.kt`

## Akzeptanzkriterien

- `./gradlew assembleDebug` läuft erfolgreich
- App startet auf Emulator oder Gerät
- MainActivity zeigt eine einfache Startseite

## Nicht tun

- keine Overlay-Logik in dieser Phase
- keine KI-Anbindung
- kein Service
- keine zusätzlichen Screens außer Setup/Status

---

## 6. Phase 2 — Settings und Berechtigungsstatus

## Ziel

Die App zeigt klar, welche Berechtigungen fehlen und erlaubt die lokale API-Key-Eingabe.

## Aufgaben

- `SettingsScreen` bauen
- `PermissionStatus` bauen
- Overlay-Berechtigung prüfen
- Usage-Access prüfen
- Buttons zu Android-Einstellungen anbieten
- `SettingsStore` mit DataStore einrichten
- API-Key lokal speichern
- Overlay aktiv/inaktiv speichern

## Akzeptanzkriterien

- Overlay-Status wird korrekt angezeigt
- Usage-Access-Status wird korrekt angezeigt
- Nutzer kann Einstellungsseiten öffnen
- API-Key kann eingegeben und gespeichert werden
- keine API-Keys werden geloggt

## Nicht tun

- kein Clipboard lesen
- keine KI-Anfrage senden
- kein Hintergrunddienst starten, außer Test-Button wird später explizit genutzt

---

## 7. Phase 3 — Manuelles Overlay

## Ziel

Ein Floating Button kann manuell angezeigt, bewegt und entfernt werden.

## Aufgaben

- `OverlayController` erstellen
- `OverlayService` oder Overlay-Runtime erstellen
- `FloatingBubbleView` erstellen
- `TYPE_APPLICATION_OVERLAY` verwenden
- Button per Test-Button in MainActivity anzeigen
- Button verschiebbar machen
- Tap vs Drag sauber unterscheiden
- Position speichern
- Overlay sauber entfernen

## Akzeptanzkriterien

- Button erscheint über anderen Apps, wenn Overlay-Berechtigung vorhanden ist
- Button ist verschiebbar
- Button löst beim Ziehen keinen Tap aus
- Position bleibt nach Neustart erhalten
- keine doppelten Buttons entstehen
- Button kann deaktiviert werden

## Nicht tun

- noch keine WhatsApp-Erkennung
- keine KI
- kein Clipboard
- kein Auto-Start aus Hintergrund

---

## 8. Phase 4 — WhatsApp-Erkennung

## Ziel

Floating Button erscheint nur, wenn WhatsApp im Vordergrund ist.

## Aufgaben

- `ForegroundAppDetector` bauen
- `UsageStatsManager.queryEvents()` verwenden
- Paket `com.whatsapp` erkennen
- Polling initial auf 1000 ms setzen
- bei fehlendem Usage Access klaren Status zurückgeben
- Button anzeigen/verstecken

## Akzeptanzkriterien

- WhatsApp öffnen → Button erscheint
- WhatsApp verlassen → Button verschwindet
- App-Wechsel erzeugt keine doppelten Views
- fehlender Usage Access wird verständlich erklärt

## Nicht tun

- kein Accessibility-Fallback
- kein Notification Listener
- kein Screen Scraping
- kein Root/ADB-Hack

---

## 9. Phase 5 — Mini-Fenster ohne KI

## Ziel

Der Nutzer kann das ReplyPanel öffnen und Eingaben machen, ohne dass schon KI angebunden ist.

## Aufgaben

- `ReplyPanelView` bauen
- Modusauswahl einbauen:
  - Antworten
  - Formulieren
  - Umschreiben
- Ton-Auswahl einbauen:
  - kurz
  - freundlich
  - direkt
  - entschuldigend
  - deeskalierend
  - klare Grenze
  - flirtend
- Eingabefelder bauen
- Clipboard-Vorschau nur nach Panel-Öffnung lesen
- Clipboard-Text erst nach Bestätigung übernehmen
- statische Dummy-Vorschläge für UI-Test anzeigen
- Kopieren-Button testen

## Akzeptanzkriterien

- Tap auf Bubble öffnet Panel
- Panel ist kompakt und schließbar
- Modus kann gewechselt werden
- Ton kann ausgewählt werden
- Clipboard wird nicht heimlich gelesen
- Vorschläge können kopiert werden

## Nicht tun

- keine echte KI-Anfrage
- kein Speichern von Nutzertexten
- kein Chatverlauf

---

## 10. Phase 6 — PromptBuilder und Parser

## Ziel

Prompts und Antwortauswertung stehen getrennt und testbar bereit.

## Aufgaben

- `PromptBuilder` erstellen
- Prompts aus `PROMPTS.md` übernehmen
- `AiResponseParser` erstellen
- Parser tolerant gegenüber Nummerierung machen
- Unit-Tests für PromptBuilder und Parser schreiben

## Akzeptanzkriterien

- jeder Modus erzeugt passenden Prompt
- Parser extrahiert bis zu 3 Vorschläge
- Parser crasht nicht bei unperfekter KI-Ausgabe
- keine Nutzerinhalte werden geloggt

## Nicht tun

- keine Provider-Abstraktion überbauen
- keine automatische Moduserkennung durch KI

---

## 11. Phase 7 — KI-Anbindung

## Ziel

Echte KI-Vorschläge werden erzeugt und im Panel angezeigt.

## Aufgaben

- `AiClient` bauen
- einen Provider unterstützen, z. B. OpenRouter oder OpenAI
- API-Key aus DataStore lesen
- Anfrage senden
- Ladezustand anzeigen
- Fehler anzeigen
- Antwort parsen
- genau 3 Vorschläge anzeigen, soweit möglich

## Akzeptanzkriterien

- fehlender API-Key erzeugt klare Meldung
- kein Internet erzeugt klare Meldung
- gültige Anfrage zeigt 3 Vorschläge
- Vorschläge können kopiert werden
- keine API-Keys oder Nutzertexte werden geloggt

## Nicht tun

- keine Multi-Provider-Oberfläche
- kein Kosten-/Billing-System
- kein Verlauf
- keine automatische Anfrage beim Öffnen von WhatsApp

---

## 12. Phase 8 — Stabilisierung auf Gerät

## Ziel

Die App ist als private APK brauchbar und nicht nur theoretisch funktionsfähig.

## Aufgaben

- echte APK bauen
- auf Samsung S25 testen
- Overlay-Berechtigung testen
- Usage Access testen
- WhatsApp öffnen/schließen testen
- Rotation testen
- Sperren/Entsperren testen
- App aus Recent Apps entfernen
- Akkuoptimierung dokumentieren
- Fehlerfälle testen

## Akzeptanzkriterien

- Testcheckliste aus `TEST_PLAN.md` weitgehend erfüllt
- bekannte Einschränkungen dokumentiert
- keine verbotenen Permissions im Manifest
- kein Accessibility Service vorhanden
- README aktualisiert

---

## 13. Phase 9 — README und Übergabe

## Ziel

Projekt ist für weitere Agenten und manuelle Nutzung vorbereitet.

## Aufgaben

- README mit Build-Befehlen aktualisieren
- bekannte Einschränkungen dokumentieren
- APK-Installationshinweise ergänzen
- manuelle Testcheckliste verlinken
- offene Risiken nennen

## Abschlussausgabe des Agenten

Jeder Agent soll am Ende berichten:

```text
Summary:
- ...

Files changed:
- ...

Validation:
- ...

Not validated:
- ...

Risks:
- ...

Next sensible step:
- ...
```

---

## 14. Priorisierung

## Muss zuerst funktionieren

1. App startet
2. Berechtigungen sichtbar
3. Overlay manuell testbar
4. Button nur bei WhatsApp
5. Panel öffnet
6. Clipboard bewusst übernehmen
7. KI-Vorschläge erzeugen
8. Kopieren

## Darf warten

- schönes Icon
- Animationen
- WhatsApp Business
- Modellauswahl
- Kostenlimit
- Verlauf
- Export/Import
- Play-Store-Vorbereitung

## Nicht bauen

- Auto-Senden
- Auto-Einfügen
- Chat-Auslesen
- Accessibility
- Notification-Auswertung

---

## 15. Qualitätskriterien

Eine Phase gilt nicht als abgeschlossen, wenn:

- Code nicht baut
- verbotene Berechtigungen eingeführt wurden
- Nutzertexte geloggt werden
- Overlay doppelt erscheint
- Service nicht sauber stoppt
- der Agent Tests behauptet, die nicht ausgeführt wurden
- der Scope erweitert wurde, ohne es klar zu markieren

---

## 16. Schwierigkeit

Gesamtprojekt:

Schwierigkeiten: 6/10 | Thinking: high
