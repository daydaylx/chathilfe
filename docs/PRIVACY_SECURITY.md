# PRIVACY_SECURITY.md — ChatHilfe MVP

## Zweck

Dieses Dokument definiert Datenschutz- und Sicherheitsgrenzen für den MVP.

Die App liegt als Overlay über WhatsApp. Deshalb muss sie minimal, transparent und nutzerkontrolliert bleiben.

---

## Grundprinzip

Die App verarbeitet nur Inhalte, die der Nutzer aktiv bereitstellt oder bestätigt.

Erlaubt:

- Nutzer tippt eine Absicht ein
- Nutzer öffnet das ReplyPanel bewusst
- Nutzer bestätigt eine kopierte Nachricht
- Nutzer startet die KI-Anfrage bewusst
- Nutzer kopiert einen Vorschlag bewusst

Nicht erlaubt:

- automatische Chat-Auslesung
- automatische Clipboard-Überwachung
- automatische Nachrichtenerstellung ohne Nutzeraktion
- automatisches Einfügen
- automatisches Senden
- Speichern von Gesprächsinhalten

---

## Datenkategorien

Lokal speichern erlaubt:

- API-Key
- Overlay aktiv/inaktiv
- bevorzugter Ton
- letzter Modus optional
- Floating-Button-Position

Nur flüchtig im Speicher erlaubt:

- bestätigte kopierte Nachricht
- Nutzerabsicht
- Originaltext für Umschreiben
- generierte Vorschläge
- aktuelle Fehlermeldung

Verboten:

- vollständige WhatsApp-Chats
- Kontakte
- Telefonnummern aus Kontaktbuch
- Medien
- Standort
- SMS
- Benachrichtigungsinhalte
- Bildschirmaufnahmen
- Clipboard-Historie
- Gerätekennungen für Tracking

---

## Berechtigungen

Erlaubt:

| Berechtigung | Zweck |
|---|---|
| `INTERNET` | KI-Anfrage |
| `SYSTEM_ALERT_WINDOW` | Overlay |
| `PACKAGE_USAGE_STATS` | WhatsApp-Vordergrund erkennen |
| `POST_NOTIFICATIONS` | optional bei Foreground Service |
| `FOREGROUND_SERVICE` | optional bei Foreground Service |

Verboten:

- Kontakte
- SMS
- Kamera
- Mikrofon
- Standort
- Medienzugriff
- Accessibility Service
- Notification Listener
- Screen Capture

---

## Clipboard-Regeln

Erlaubt:

- Clipboard lesen, wenn Nutzer das Panel öffnet
- Vorschau anzeigen
- Text erst nach „Verwenden“ nutzen
- Vorschlag per Button kopieren

Verboten:

- dauerhaftes Monitoring
- Hintergrundlesen
- Speichern
- Logging
- automatische KI-Anfrage bei Clipboard-Änderung

UI-Pflicht:

```text
Kopierter Text erkannt. Verwenden?
[Verwenden] [Ignorieren]
```

---

## KI-Anfragen

Eine KI-Anfrage darf enthalten:

- gewählter Modus
- bestätigter kopierter Text optional
- Nutzerabsicht
- Ton
- Sprache
- gewünschte Anzahl Vorschläge

Nicht senden:

- komplette Chatverläufe
- unbestätigtes Clipboard
- Kontakte
- Gerätekennung
- Standort
- Screenshots
- Logs

KI-Anfragen dürfen nur nach Tippen auf „Vorschläge erstellen“ entstehen.

---

## API-Key

Regeln:

- lokal speichern
- nicht committen
- nicht loggen
- nicht in Fehlermeldungen anzeigen
- nicht in Crashreports senden
- nicht hart im Code hinterlegen

MVP: DataStore ist akzeptabel.

Später prüfen: Android Keystore oder verschlüsselte Speicherung.

---

## Logging

Nie loggen:

- API-Key
- Clipboard-Inhalt
- kopierte Nachricht
- Nutzerabsicht
- Originaltext
- generierte Antwort
- vollständige Requests/Responses

Erlaubt:

```text
overlay_visible=true
usage_access_granted=false
ai_request_failed=http_429
```

---

## Threat Model

Risiko: App wirkt wie Spyware.

Gegenmaßnahmen:

- minimale Berechtigungen
- keine Accessibility
- Clipboard nur nach Bestätigung
- klare UI-Hinweise

Risiko: sensible Nachricht wird versehentlich an KI gesendet.

Gegenmaßnahmen:

- Vorschau
- explizites „Verwenden“
- KI-Anfrage nur per Button

Risiko: API-Key leakt.

Gegenmaßnahmen:

- nicht loggen
- nicht committen
- später Keystore prüfen

Risiko: KI schreibt unpassend.

Gegenmaßnahmen:

- Nutzer kopiert und sendet selbst
- drei Varianten
- keine automatische WhatsApp-Aktion

---

## Akzeptanzkriterien

Datenschutz ist für den MVP akzeptabel, wenn:

- keine verbotenen Berechtigungen vorhanden sind
- kein Accessibility Service vorhanden ist
- Clipboard nur nach Nutzeraktion gelesen wird
- Nutzertexte nicht gespeichert werden
- Nutzertexte nicht geloggt werden
- API-Key nicht geloggt wird
- KI-Anfragen nur nach Button-Klick erfolgen
- nur bestätigte Inhalte an KI gesendet werden
- keine automatische WhatsApp-Aktion möglich ist
