# TEST_PLAN.md — ChatHilfe MVP

## 1. Zweck

Dieses Dokument definiert, wie der ChatHilfe-MVP getestet werden muss.

Der wichtigste Punkt: Viele Kernfunktionen sind Android-/Geräteverhalten und können nicht zuverlässig nur durch Unit-Tests geprüft werden. Overlay, Usage Access, Clipboard und Samsung-Hintergrundverhalten müssen auf einem echten Gerät getestet werden.

---

## 2. Testziele

Der Testplan soll sicherstellen, dass:

- die APK installierbar ist
- Berechtigungen korrekt geprüft werden
- Floating Button nur bei WhatsApp erscheint
- Overlay keine doppelten Views erzeugt
- Mini-Fenster stabil funktioniert
- Clipboard nur nach Nutzeraktion verwendet wird
- KI-Vorschläge erzeugt und kopiert werden können
- Fehlerfälle verständlich dargestellt werden
- keine verbotenen Funktionen eingebaut wurden

---

## 3. Testumgebung

## Primär

- Samsung Galaxy S25
- moderne Android-Version, ideal Android 15 oder Android 16
- installiertes WhatsApp mit Paket `com.whatsapp`
- Internetverbindung
- gültiger API-Key für den gewählten KI-Anbieter

## Optional

- Android Emulator für Basistests
- zweites Android-Gerät mit anderer Android-Version
- WhatsApp Business später separat, nicht MVP-Pflicht

---

## 4. Build-Tests

Sobald das Android-Projekt existiert:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

## Akzeptanz

- `assembleDebug` muss für eine testbare APK erfolgreich sein
- Unit-Tests müssen laufen, wenn vorhanden
- Lint-Fehler müssen bewertet werden
- Keine Erfolge behaupten, wenn Befehle nicht ausgeführt wurden

---

## 5. Manifest-/Permission-Test

## Prüfen

Erlaubte Permissions:

- `INTERNET`
- `SYSTEM_ALERT_WINDOW`
- `PACKAGE_USAGE_STATS`
- `POST_NOTIFICATIONS`, nur falls Foreground Service genutzt wird
- `FOREGROUND_SERVICE`, nur falls Foreground Service genutzt wird

Verbotene Permissions:

- `READ_CONTACTS`
- `READ_SMS`
- `SEND_SMS`
- `RECORD_AUDIO`
- `CAMERA`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `READ_EXTERNAL_STORAGE`
- `READ_MEDIA_IMAGES`
- `READ_MEDIA_VIDEO`
- `BIND_ACCESSIBILITY_SERVICE`

## Akzeptanz

- keine verbotene Permission vorhanden
- kein Accessibility Service deklariert
- keine Notification-Listener- oder Screen-Capture-Komponente vorhanden

---

## 6. Installationstest

## Schritte

1. Debug-APK bauen.
2. APK auf Samsung S25 installieren.
3. App öffnen.
4. Prüfen, ob Startscreen erscheint.

## Erwartet

- App startet ohne Crash
- App zeigt Projekt-/Setup-Status
- fehlende Berechtigungen werden verständlich angezeigt
- App fordert keine unnötigen Berechtigungen an

---

## 7. Berechtigungstest

## 7.1 Overlay-Berechtigung

Schritte:

1. App öffnen.
2. Overlay-Status prüfen.
3. Button „Overlay-Berechtigung öffnen“ antippen.
4. Android-Einstellung öffnen.
5. Berechtigung erlauben.
6. Zur App zurückkehren.

Erwartet:

- Status wechselt auf erlaubt
- App stürzt nicht ab
- Nutzer versteht, wofür die Berechtigung benötigt wird

## 7.2 Nutzungsdatenzugriff

Schritte:

1. App öffnen.
2. Usage-Access-Status prüfen.
3. Button „Nutzungsdatenzugriff öffnen“ antippen.
4. Android-Einstellung öffnen.
5. Zugriff erlauben.
6. Zur App zurückkehren.

Erwartet:

- Status wechselt auf erlaubt
- App kann Vordergrund-App erkennen
- Fehlender Zugriff wird verständlich erklärt

---

## 8. Settings-Test

Schritte:

1. API-Key eintragen.
2. Speichern antippen.
3. App schließen.
4. App neu öffnen.
5. Prüfen, ob API-Key-Status erhalten bleibt.
6. Overlay aktivieren/deaktivieren.

Erwartet:

- API-Key wird lokal gespeichert
- API-Key wird nicht sichtbar geloggt
- Overlay-Status bleibt erhalten
- keine Nutzertexte werden gespeichert

---

## 9. Manuelles Overlay-Testen

Schritte:

1. Overlay-Berechtigung erlauben.
2. Test-Overlay aus der App starten.
3. Floating Button anzeigen lassen.
4. Button ziehen.
5. Button antippen.
6. Button entfernen/deaktivieren.

Erwartet:

- Button erscheint über anderen Apps
- Button ist verschiebbar
- Drag löst keinen Tap aus
- Position bleibt erhalten
- kein doppelter Button entsteht
- Overlay kann sauber entfernt werden

---

## 10. WhatsApp-Erkennungstest

Schritte:

1. Overlay aktivieren.
2. WhatsApp öffnen.
3. Beobachten, ob Floating Button erscheint.
4. Zu Launcher oder anderer App wechseln.
5. Beobachten, ob Button verschwindet.
6. Zurück zu WhatsApp wechseln.
7. Wiederholen mit mehreren App-Wechseln.

Erwartet:

- Button erscheint nur bei `com.whatsapp`
- Button verschwindet außerhalb von WhatsApp
- App-Wechsel erzeugt keine doppelten Views
- Verzögerung bleibt akzeptabel

Fehler notieren:

- Button erscheint zu spät
- Button bleibt in anderer App sichtbar
- Button erscheint doppelt
- Button verschwindet nicht

---

## 11. ReplyPanel-Test

Schritte:

1. WhatsApp öffnen.
2. Floating Button antippen.
3. Panel öffnet sich.
4. Modus wechseln.
5. Ton wechseln.
6. Text eingeben.
7. Panel schließen.
8. Panel erneut öffnen.

Erwartet:

- Panel ist kompakt
- WhatsApp wird nicht komplett verdeckt
- Panel ist schließbar
- Modusauswahl funktioniert
- Ton-Auswahl funktioniert
- Eingabe funktioniert
- keine Eingabe wird dauerhaft gespeichert

---

## 12. Clipboard-Test

## 12.1 Kein Clipboard

Schritte:

1. Clipboard leeren oder keinen Text kopieren.
2. Panel öffnen.

Erwartet:

- App zeigt keine falsche kopierte Nachricht
- Modus Formulieren bleibt nutzbar

## 12.2 Clipboard mit WhatsApp-Nachricht

Schritte:

1. In WhatsApp eine Nachricht manuell kopieren.
2. Floating Button antippen.
3. Panel öffnen.
4. Clipboard-Vorschau prüfen.
5. „Verwenden“ antippen.

Erwartet:

- Clipboard wird erst beim Panel-Öffnen/Bestätigen genutzt
- Vorschau ist sichtbar
- Text wird erst nach Bestätigung übernommen
- Text wird nicht gespeichert
- Text wird nicht geloggt

---

## 13. Modus-Tests

## 13.1 Antworten

Input:

```text
Kopierte Nachricht:
Warum meldest du dich erst jetzt?

Nutzerabsicht:
Ich will mich entschuldigen, aber nicht unterwürfig klingen.

Ton:
ruhig, ehrlich, kurz
```

Erwartet:

- 3 direkte Antwortvorschläge
- keine Erklärung
- keine Analyse
- keine automatische Aktion

## 13.2 Formulieren

Input:

```text
Ich will sagen, dass ich heute Ruhe brauche, aber nicht kalt klingen.
```

Erwartet:

- 3 sendbare Chatnachrichten
- natürliches Deutsch
- kein übertrieben künstlicher Ton

## 13.3 Umschreiben

Input:

```text
Original:
Keine Ahnung, mach halt was du willst.

Änderung:
weniger passiv-aggressiv
```

Erwartet:

- 3 bessere Varianten
- Bedeutung bleibt grob erhalten
- Ton ist weniger passiv-aggressiv

---

## 14. KI-Fehlertests

## 14.1 API-Key fehlt

Erwartet:

- klare Meldung
- keine Anfrage wird gesendet
- kein Crash

## 14.2 Internet fehlt

Erwartet:

- klare Meldung
- Retry möglich
- kein Crash

## 14.3 Ungültige KI-Antwort

Erwartet:

- Parser crasht nicht
- App zeigt sinnvollen Fallback oder Fehlermeldung

## 14.4 Rate Limit / HTTP-Fehler

Erwartet:

- kurze verständliche Fehlermeldung
- API-Key wird nicht angezeigt
- keine sensiblen Daten in Logs

---

## 15. Kopier-Test

Schritte:

1. Vorschläge erzeugen.
2. Vorschlag 1 kopieren.
3. In WhatsApp-Eingabefeld manuell einfügen.
4. Vorschlag 2 kopieren.
5. Prüfen, ob Clipboard aktualisiert wurde.

Erwartet:

- kopierter Text entspricht Vorschlag
- Nutzer muss manuell einfügen
- nichts wird automatisch gesendet

---

## 16. Lifecycle-Tests

## 16.1 Sperren/Entsperren

Schritte:

1. Overlay aktivieren.
2. WhatsApp öffnen.
3. Bildschirm sperren.
4. Bildschirm entsperren.
5. WhatsApp erneut prüfen.

Erwartet:

- App crasht nicht
- Overlay erscheint nicht doppelt
- Zustand bleibt nachvollziehbar

## 16.2 App aus Recent Apps entfernen

Erwartet:

- Service/Overlay stoppt sauber oder bleibt nur, wenn bewusst so implementiert
- keine hängende View
- Nutzer kann Overlay wieder aktivieren

## 16.3 Rotation

Erwartet:

- MainActivity bleibt nutzbar
- Overlay erzeugt keine doppelten Views
- Position bleibt sinnvoll

---

## 17. Samsung-/Batterietest

Schritte:

1. Overlay aktivieren.
2. WhatsApp nutzen.
3. Gerät längere Zeit sperren.
4. Entsperren und WhatsApp öffnen.
5. Prüfen, ob Overlay noch funktioniert.

Erwartet:

- bekannte Einschränkungen werden dokumentiert
- keine aggressiven Anti-Kill-Tricks
- Nutzer kann Overlay manuell reaktivieren

---

## 18. Datenschutz-Test

Prüfen:

- keine Chatnachrichten in DataStore
- keine generierten Antworten gespeichert
- keine Nutzerabsicht gespeichert
- keine Clipboard-Historie
- keine API-Keys in Logs
- keine Nutzertexte in Logs
- keine verbotenen Permissions

Akzeptanz:

- App verarbeitet nur aktiv bestätigte Inhalte
- nichts Sensibles wird dauerhaft gespeichert

---

## 19. Abschluss-Testmatrix

| Testbereich | Pflicht vor MVP-Fertigstellung |
|---|---|
| Build | ja |
| Installation | ja |
| Overlay Permission | ja |
| Usage Access | ja |
| WhatsApp-Erkennung | ja |
| Floating Button Drag | ja |
| Panel öffnen/schließen | ja |
| Clipboard bewusst übernehmen | ja |
| Antworten-Modus | ja |
| Formulieren-Modus | ja |
| Umschreiben-Modus | ja |
| KI-Fehlerfälle | ja |
| Kopieren | ja |
| Sperren/Entsperren | ja |
| keine verbotenen Permissions | ja |
| kein Accessibility | ja |

---

## 20. Testbericht-Format

Nach Tests ausgeben:

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
