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

## API-Key-Strategie

Für die private APK wird der OpenRouter-Key lokal beim Build eingebettet.

Regeln:

- echter API-Key niemals ins GitHub-Repo
- echter API-Key niemals in Dokumentation
- echter API-Key niemals in Logs
- echter API-Key nicht im UI anzeigen
- kein API-Key-Feld im MVP
- kein API-Key in DataStore
- lokale Secret-Dateien müssen in `.gitignore` stehen
- Build-Doku darf nur Platzhalter verwenden

Wichtig:

Ein in die APK eingebetteter Key ist nicht wirklich geheim. APKs können dekompiliert werden. Für private Nutzung ist das akzeptabel, wenn die APK nicht öffentlich verteilt wird und der Key ein niedriges Credit-/Usage-Limit hat.

---

## Datenkategorien

Lokal speichern erlaubt:

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

- API-Key in DataStore
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
| `POST_NOTIFICATIONS` | Service-Notification, wenn erforderlich |
| `FOREGROUND_SERVICE` | Overlay-Laufzeit |

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
- manueller Einfügen-Fallback

Verboten:

- dauerhaftes Monitoring
- Hintergrundlesen
- Speichern
- Logging
- automatische KI-Anfrage bei Clipboard-Änderung

---

## KI-Anfragen

Eine KI-Anfrage darf enthalten:

- gewählter Modus
- bestätigter kopierter oder manuell eingefügter Text optional
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

## Akzeptanzkriterien

Datenschutz ist für den MVP akzeptabel, wenn:

- keine verbotenen Berechtigungen vorhanden sind
- kein Accessibility Service vorhanden ist
- Clipboard nur nach Nutzeraktion gelesen wird
- manueller Fallback funktioniert
- Nutzertexte nicht gespeichert werden
- Nutzertexte nicht geloggt werden
- API-Key nicht geloggt wird
- API-Key nicht im Repo steht
- KI-Anfragen nur nach Button-Klick erfolgen
- nur bestätigte Inhalte an KI gesendet werden
- keine automatische WhatsApp-Aktion möglich ist
