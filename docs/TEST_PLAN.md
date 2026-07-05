# TEST_PLAN.md — ChatHilfe MVP

## Zweck

Dieses Dokument definiert die Tests für den ChatHilfe-MVP.

Viele Kernfunktionen sind Geräteverhalten und nicht zuverlässig nur per Unit-Test prüfbar: Overlay, Usage Access, Clipboard, App-Wechsel und Samsung-Hintergrundverhalten müssen auf echtem Gerät getestet werden.

---

## Testziele

Sicherstellen, dass:

- APK installierbar ist
- Berechtigungen korrekt geprüft werden
- Floating Button nur bei WhatsApp erscheint
- Overlay keine doppelten Views erzeugt
- ReplyPanel stabil funktioniert
- Clipboard nur nach Nutzeraktion verwendet wird
- KI-Vorschläge erzeugt und kopiert werden können
- Fehler verständlich dargestellt werden
- keine verbotenen Funktionen eingebaut wurden

---

## Testumgebung

Primär:

- Samsung Galaxy S25
- Android 15 oder Android 16
- WhatsApp `com.whatsapp`
- Internet
- gültiger API-Key

Optional:

- Android Emulator für Basistests
- weiteres Android-Gerät

---

## Build-Tests

Sobald Gradle-Projekt existiert:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

Akzeptanz:

- `assembleDebug` erfolgreich
- Tests laufen, wenn vorhanden
- Lint-Fehler bewertet
- keine erfundenen Testergebnisse

---

## Permission-/Manifest-Test

Erlaubte Permissions:

- `INTERNET`
- `SYSTEM_ALERT_WINDOW`
- `PACKAGE_USAGE_STATS`
- `POST_NOTIFICATIONS`, nur falls Foreground Service genutzt wird
- `FOREGROUND_SERVICE`, nur falls Foreground Service genutzt wird

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

Akzeptanz:

- keine verbotenen Permissions
- kein Accessibility Service
- keine WhatsApp-Automation

---

## Installationstest

Schritte:

1. Debug-APK bauen.
2. APK installieren.
3. App öffnen.
4. Startscreen prüfen.

Erwartet:

- kein Crash
- Setup-Status sichtbar
- fehlende Berechtigungen verständlich
- keine unnötigen Permission-Prompts

---

## Berechtigungstest

Overlay:

1. App öffnen.
2. Overlay-Status prüfen.
3. Einstellungsseite öffnen.
4. Berechtigung erlauben.
5. Zur App zurückkehren.

Usage Access:

1. App öffnen.
2. Usage-Status prüfen.
3. Einstellungsseite öffnen.
4. Zugriff erlauben.
5. Zur App zurückkehren.

Erwartet:

- Status aktualisiert sich korrekt
- App stürzt nicht ab
- Nutzen der Berechtigung ist verständlich

---

## Overlay-Test

Schritte:

1. Overlay aktivieren.
2. Test-Overlay anzeigen.
3. Button ziehen.
4. Button antippen.
5. Button deaktivieren.

Erwartet:

- Button erscheint über Apps
- Button ist verschiebbar
- Drag löst keinen Tap aus
- Position bleibt erhalten
- kein doppelter Button
- Overlay entfernbar

---

## WhatsApp-Erkennungstest

Schritte:

1. Overlay aktivieren.
2. WhatsApp öffnen.
3. Button erscheint.
4. Andere App öffnen.
5. Button verschwindet.
6. Mehrfach wechseln.

Erwartet:

- Button nur bei `com.whatsapp`
- keine doppelten Views
- Verzögerung akzeptabel

---

## ReplyPanel-Test

Schritte:

1. WhatsApp öffnen.
2. Floating Button antippen.
3. Panel öffnet.
4. Modus wechseln.
5. Ton wechseln.
6. Text eingeben.
7. Panel schließen.

Erwartet:

- Panel kompakt
- WhatsApp nicht komplett verdeckt
- Modus/Ton/Eingabe funktionieren
- keine Eingabe wird dauerhaft gespeichert

---

## Clipboard-Test

Ohne Clipboard:

- Panel öffnen
- keine falsche Vorschau anzeigen
- Formulieren-Modus bleibt nutzbar

Mit Clipboard:

1. WhatsApp-Nachricht manuell kopieren.
2. Panel öffnen.
3. Vorschau prüfen.
4. „Verwenden“ antippen.

Erwartet:

- Clipboard erst nach Nutzeraktion
- Text erst nach Bestätigung
- kein Speichern
- kein Logging

---

## Modus-Tests

Antworten:

```text
Kopierte Nachricht:
Warum meldest du dich erst jetzt?

Nutzerabsicht:
Ich will mich entschuldigen, aber nicht unterwürfig klingen.

Ton:
ruhig, ehrlich, kurz
```

Formulieren:

```text
Ich will sagen, dass ich heute Ruhe brauche, aber nicht kalt klingen.
```

Umschreiben:

```text
Original:
Keine Ahnung, mach halt was du willst.

Änderung:
weniger passiv-aggressiv
```

Erwartet:

- genau 3 Varianten soweit möglich
- keine Erklärung
- natürliche Sprache
- direkt kopierbar

---

## KI-Fehlertests

Testen:

- API-Key fehlt
- Internet fehlt
- ungültige Modellantwort
- HTTP-Fehler / Rate Limit

Erwartet:

- klare Fehlermeldung
- kein Crash
- keine sensiblen Daten in UI/Logs

---

## Lifecycle-Tests

Testen:

- Sperren/Entsperren
- App aus Recent Apps entfernen
- Rotation
- längere Inaktivität
- Samsung-Akkuoptimierung

Erwartet:

- keine doppelten Views
- kein hängendes Overlay
- Einschränkungen dokumentiert

---

## Datenschutz-Test

Prüfen:

- keine Chatnachrichten in DataStore
- keine generierten Antworten gespeichert
- keine Nutzerabsicht gespeichert
- keine Clipboard-Historie
- keine API-Keys in Logs
- keine Nutzertexte in Logs

---

## Abschluss-Testmatrix

| Bereich | Pflicht |
|---|---|
| Build | ja |
| Installation | ja |
| Overlay Permission | ja |
| Usage Access | ja |
| WhatsApp-Erkennung | ja |
| Dragging | ja |
| Panel öffnen/schließen | ja |
| Clipboard bewusst übernehmen | ja |
| alle 3 Modi | ja |
| KI-Fehlerfälle | ja |
| Kopieren | ja |
| Sperren/Entsperren | ja |
| keine verbotenen Permissions | ja |
| kein Accessibility | ja |

---

## Testbericht-Format

```text
Test environment:
- Gerät:
- Android-Version:
- App-Version/Commit:

Passed:
- ...

Failed:
- ...

Not tested:
- ...

Risks:
- ...

Next fixes:
- ...
```
