# PRIVACY_SECURITY.md — ChatHilfe MVP

## 1. Zweck

Dieses Dokument definiert die Datenschutz- und Sicherheitsgrenzen für den ChatHilfe-MVP.

Die App liegt als Overlay über WhatsApp. Dadurch wirkt sie schnell sensibel. Deshalb muss die Umsetzung bewusst minimal, transparent und nutzerkontrolliert bleiben.

Der MVP darf keine WhatsApp-Inhalte automatisch lesen, keine Nachrichten automatisch senden und keine versteckte Überwachung durchführen.

---

## 2. Grundprinzip

Die App verarbeitet nur Inhalte, die der Nutzer aktiv bereitstellt oder bestätigt.

Erlaubt:

- Nutzer tippt selbst eine Absicht ein
- Nutzer öffnet bewusst das ReplyPanel
- Nutzer bestätigt bewusst eine kopierte Nachricht aus der Zwischenablage
- Nutzer startet bewusst die KI-Anfrage
- Nutzer kopiert bewusst einen Vorschlag

Nicht erlaubt:

- automatische Chat-Auslesung
- automatische Clipboard-Überwachung
- automatische Nachrichtenerstellung ohne Nutzeraktion
- automatisches Einfügen
- automatisches Senden
- Speichern von Gesprächsinhalten

---

## 3. Datenkategorien

## 3.1 Lokale Einstellungen

Dürfen lokal gespeichert werden:

- API-Key
- Overlay aktiv/inaktiv
- bevorzugter Ton
- zuletzt gewählter Modus optional
- Floating-Button-Position

## 3.2 Flüchtige Nutzerdaten

Dürfen nur im Speicher während der Nutzung existieren:

- kopierte Nachricht nach Bestätigung
- Nutzerabsicht
- Originaltext für Umschreiben
- generierte Vorschläge
- aktuelle Fehlermeldung

Diese Daten dürfen nicht dauerhaft gespeichert werden.

## 3.3 Verbotene Daten

Nicht speichern, nicht übertragen, nicht loggen:

- vollständige WhatsApp-Chats
- Kontakte
- Telefonnummern
- Medien
- Standort
- SMS
- Benachrichtigungsinhalte
- Bildschirmaufnahmen
- Clipboard-Historie
- Gerätekennungen für Tracking

---

## 4. Berechtigungen

## 4.1 Erlaubte Berechtigungen

| Berechtigung | Zweck |
|---|---|
| `INTERNET` | KI-Anfrage senden |
| `SYSTEM_ALERT_WINDOW` | Floating Button und Mini-Fenster anzeigen |
| `PACKAGE_USAGE_STATS` | erkennen, ob WhatsApp im Vordergrund ist |
| `POST_NOTIFICATIONS` | optional, nur bei Foreground Service |
| `FOREGROUND_SERVICE` | optional, nur bei Foreground Service |

## 4.2 Verbotene Berechtigungen

Nicht verwenden:

- Kontakte
- SMS
- Kamera
- Mikrofon
- Standort
- Medienzugriff
- Accessibility Service
- Notification Listener
- Screen Capture

## 4.3 Begründung

Die App braucht keine Kontakte, keine Kamera, kein Mikrofon, keinen Standort und keine SMS. Jeder zusätzliche Zugriff würde das Risiko erhöhen und den MVP unnötig verdächtig machen.

---

## 5. Clipboard-Regeln

## 5.1 Erlaubtes Verhalten

- Clipboard lesen, wenn der Nutzer das ReplyPanel öffnet
- Clipboard-Vorschau anzeigen
- Text erst verwenden, wenn Nutzer „Verwenden“ antippt
- generierten Vorschlag per Button kopieren

## 5.2 Verbotenes Verhalten

- Clipboard dauerhaft überwachen
- Clipboard lesen, während Panel geschlossen ist
- Clipboard-Inhalt im Hintergrund an KI senden
- Clipboard-Inhalt speichern
- Clipboard-Inhalt loggen

## 5.3 UI-Pflicht

Wenn Clipboard-Text erkannt wird, muss die App klar anzeigen:

```text
Kopierter Text erkannt. Verwenden?
```

Der Nutzer muss entscheiden können:

- Verwenden
- Ignorieren

---

## 6. KI-Anfragen

## 6.1 Erlaubte Inhalte in KI-Anfrage

Eine KI-Anfrage darf enthalten:

- gewählter Modus
- bestätigter kopierter Text, falls vorhanden
- Nutzerabsicht
- gewünschter Ton
- gewünschte Sprache
- gewünschte Anzahl Vorschläge

## 6.2 Nicht erlaubte Inhalte

Nicht senden:

- komplette Chatverläufe
- unbestätigte Clipboard-Inhalte
- Kontakte
- Telefonnummern, sofern nicht vom Nutzer selbst im Text eingegeben
- Gerätekennung
- App-Nutzungsstatistiken
- Standort
- Medien
- Logs

## 6.3 Anfragezeitpunkt

KI-Anfragen dürfen nur entstehen, wenn der Nutzer aktiv auf „Vorschläge erstellen“ tippt.

Nicht erlaubt:

- automatisch beim Öffnen von WhatsApp
- automatisch beim Kopieren einer Nachricht
- automatisch beim Öffnen des Panels
- periodisch im Hintergrund

---

## 7. API-Key-Sicherheit

## 7.1 Speicherung

Der API-Key wird lokal gespeichert.

MVP-Variante:

- DataStore ist akzeptabel

Bessere spätere Variante:

- Android Keystore / verschlüsselte Speicherung prüfen

## 7.2 Verboten

- API-Key ins Repository committen
- API-Key in Logs ausgeben
- API-Key in Fehlermeldungen anzeigen
- API-Key in Crashreports senden
- API-Key hart im Code hinterlegen

## 7.3 UI

API-Key-Eingabe soll als sensibel behandelt werden:

- nicht unnötig im Klartext anzeigen
- Status anzeigen: vorhanden / fehlt
- Key löschen oder ersetzen können

---

## 8. Logging-Regeln

## 8.1 Nicht loggen

- API-Key
- kopierte Nachricht
- Nutzerabsicht
- Originaltext
- generierte Antwort
- Clipboard-Inhalt
- Telefonnummern
- Kontakte
- vollständige Requests
- vollständige Responses

## 8.2 Erlaubte technische Logs

Erlaubt sind technische Statusinformationen ohne Inhalte:

```text
overlay_visible=true
usage_access_granted=false
ai_request_failed=http_429
panel_opened=true
```

## 8.3 Fehlerlogs

Fehlerlogs dürfen enthalten:

- Fehlerklasse
- HTTP-Statuscode
- technische Ursache ohne Payload

Fehlerlogs dürfen keine Nutzerdaten enthalten.

---

## 9. Speicherung und Retention

## 9.1 Persistente Speicherung

Nur Einstellungen speichern.

Keine Chat- oder KI-Inhalte speichern.

## 9.2 Speicherorte

Erlaubt:

- DataStore für Einstellungen

Nicht erlaubt:

- lokale Datenbank für Nachrichten
- Dateien mit Gesprächsinhalten
- Export von Nutzertexten
- Cloud-Speicher

## 9.3 Verlauf

Ein Verlauf der Vorschläge ist nicht Teil des MVP.

Später nur optional und nur mit klarer Nutzerentscheidung.

---

## 10. UI-Transparenz

Die App muss in der Einrichtung klar erklären:

| Berechtigung | Erklärung |
|---|---|
| Über anderen Apps anzeigen | damit der Button über WhatsApp sichtbar ist |
| Nutzungsdatenzugriff | damit die App erkennt, ob WhatsApp geöffnet ist |
| Internet | damit die KI Vorschläge erstellen kann |

Die App darf nicht so wirken, als würde sie heimlich Chats lesen.

---

## 11. Verbotene Sicherheitsumgehungen

Nicht einbauen:

- Accessibility als Fallback
- Notification Listener als Fallback
- Screen Capture als Fallback
- Root-Zugriff
- ADB-Abhängigkeit
- Overlay-Typen für Systemapps
- Akku-Optimierung aggressiv umgehen
- Autostart-Hacks

Wenn eine Funktion ohne diese Umgehungen nicht möglich ist, gehört sie nicht in den MVP.

---

## 12. Threat Model

## 12.1 Risiko: Nutzer glaubt, App liest Chats

Gegenmaßnahme:

- UI erklärt klar: keine Chat-Auslesung
- Clipboard nur nach Bestätigung
- keine Accessibility-Berechtigung

## 12.2 Risiko: API-Key leakt

Gegenmaßnahme:

- nicht loggen
- nicht committen
- lokale Speicherung
- später Keystore prüfen

## 12.3 Risiko: sensible Nachricht wird versehentlich an KI gesendet

Gegenmaßnahme:

- Clipboard-Vorschau
- explizite Bestätigung
- keine automatische Anfrage

## 12.4 Risiko: App wirkt wie Spyware

Gegenmaßnahme:

- minimale Berechtigungen
- keine unnötigen Systemrechte
- klare Erklärungen
- kein heimlicher Hintergrundbetrieb

## 12.5 Risiko: generierte Nachricht ist unpassend

Gegenmaßnahme:

- Nutzer muss manuell kopieren, einfügen und senden
- App sendet nie automatisch
- 3 Varianten statt eine erzwungene Antwort

---

## 13. Datenschutz-Akzeptanzkriterien

Der MVP ist datenschutzseitig akzeptabel, wenn:

- keine verbotenen Berechtigungen vorhanden sind
- kein Accessibility Service vorhanden ist
- Clipboard nur nach Nutzeraktion gelesen wird
- Nutzertexte nicht gespeichert werden
- Nutzertexte nicht geloggt werden
- API-Key nicht geloggt wird
- KI-Anfragen nur nach Button-Klick erfolgen
- nur bestätigte Inhalte an KI gesendet werden
- keine automatische WhatsApp-Aktion möglich ist

---

## 14. Prüfliste für Agenten

Vor Abschluss jeder Aufgabe prüfen:

- Wurde eine neue Permission hinzugefügt?
- Wird Clipboard irgendwo im Hintergrund gelesen?
- Werden Nutzertexte gespeichert?
- Werden Nutzertexte geloggt?
- Wird WhatsApp automatisiert?
- Wurde Accessibility eingeführt?
- Wird eine KI-Anfrage ohne Nutzeraktion ausgelöst?
- Wird der API-Key sicher behandelt?

Wenn eine Antwort kritisch ist, Änderung stoppen und dokumentieren.

---

## 15. Kurzfazit

Der Datenschutz des MVP steht und fällt mit drei Regeln:

1. Keine automatische WhatsApp-Auslesung.
2. Clipboard nur nach aktiver Nutzerentscheidung.
3. Keine automatische Nachricht in WhatsApp.

Diese Regeln dürfen nicht aufgeweicht werden.
