# ANDROID_CONSTRAINTS.md — Android 15/16 Constraints für ChatHilfe

## 1. Zweck dieses Dokuments

Dieses Dokument hält die Android-spezifischen technischen Grenzen für den ChatHilfe-MVP fest.

Es soll verhindern, dass Coding-Agenten alte, unsichere oder nicht mehr passende Android-Patterns verwenden.

Besonders kritisch sind:

- Overlay-Berechtigung
- `TYPE_APPLICATION_OVERLAY`
- Foreground-Service-Regeln
- Android 15/16-Verhalten
- `UsageStatsManager`
- Clipboard-Zugriff
- Samsung-/Hersteller-Batterielimits

---

## 2. Ziel-SDK-Strategie

## 2.1 Empfohlene Startwerte

Für den MVP:

| Einstellung | Empfehlung |
|---|---|
| `compileSdk` | 36 |
| `targetSdk` | 35 für den ersten stabilen MVP |
| späteres Ziel | targetSdk 36 nach Stabilisierung |
| `minSdk` | 29 oder höher |
| primäres Testgerät | Samsung Galaxy S25 |

## 2.2 Begründung

`compileSdk 36` erlaubt aktuelle APIs und Prüfung gegen Android 16.

`targetSdk 35` ist als erster MVP-Schritt pragmatischer, weil zuerst Overlay, UsageStats und Service-Lifecycle stabil laufen müssen.

Nach stabiler MVP-Funktion soll `targetSdk 36` getestet und gesetzt werden.

## 2.3 Regel

Die Target-SDK-Strategie darf nicht beiläufig geändert werden.

Änderungen an `targetSdk` müssen dokumentiert und auf echtem Gerät getestet werden.

---

## 3. Overlay-Berechtigung

## 3.1 Erlaubte Technik

Für das Floating Overlay verwenden:

- `SYSTEM_ALERT_WINDOW`
- `Settings.canDrawOverlays()`
- `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`
- `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`

## 3.2 Verbotene Window-Typen

Nicht verwenden:

- `TYPE_PHONE`
- `TYPE_SYSTEM_ALERT`
- `TYPE_SYSTEM_OVERLAY`
- `TYPE_SYSTEM_ERROR`
- `TYPE_TOAST` als Overlay-Ersatz

Diese alten Systemfenster-Typen sind für normale Apps seit API 26 nicht mehr passend. Für normale App-Overlays muss `TYPE_APPLICATION_OVERLAY` verwendet werden.

## 3.3 UX-Regeln

Das Overlay muss:

- klein sein
- verschiebbar sein
- außerhalb von WhatsApp verschwinden
- nicht dauerhaft die Eingabe blockieren
- nicht wie ein Vollbilddialog wirken
- sauber entfernt werden können

## 3.4 Technische Regeln

- Alle `WindowManager`-Operationen laufen über `OverlayController`.
- Keine Klasse außer `OverlayController` ruft `addView`, `removeView` oder `updateViewLayout` auf.
- Vor `addView` prüfen, ob die View schon attached ist.
- Vor `removeView` prüfen, ob die View attached ist.
- Bei Service-Ende alle Overlay-Views entfernen.
- Overlay darf keine Tastatur oder Systemfenster unnötig verdecken.

---

## 4. Foreground-Service-Regeln

## 4.1 Grundproblem

Android schränkt Foreground Services seit Android 12 deutlich ein. Apps dürfen Foreground Services nicht beliebig aus dem Hintergrund starten.

Für den MVP bedeutet das:

- Overlay-Runtime aus sichtbarer Nutzeraktion starten
- keinen heimlichen Background-Start bauen
- ForegroundServiceStartNotAllowedException vermeiden

## 4.2 Erlaubter Startfluss

Gewünscht:

```text
Nutzer öffnet MainActivity
↓
Nutzer aktiviert Overlay bewusst
↓
App prüft Berechtigungen
↓
Overlay-Runtime startet aus sichtbarer Nutzeraktion
↓
Button wird abhängig von WhatsApp angezeigt/versteckt
```

Nicht gewünscht:

```text
System/Receiver startet App im Hintergrund
↓
Service startet ungefragt
↓
Overlay erscheint heimlich
```

## 4.3 Wenn ein Foreground Service verwendet wird

Dann gilt:

- `FOREGROUND_SERVICE` nur deklarieren, wenn wirklich nötig
- bei targetSdk 34+ passenden Service-Typ deklarieren
- passende spezifische Foreground-Service-Permission deklarieren, falls erforderlich
- `startForeground()` zeitnah aufrufen
- klare Notification anzeigen
- keine KI-Netzwerkaufrufe dauerhaft im Service laufen lassen
- keine Clipboard-Abfragen im Hintergrund

## 4.4 Android 15 Relevanz

Apps mit `SYSTEM_ALERT_WINDOW` dürfen Foreground Services aus dem Hintergrund nur dann unter dieser Ausnahme starten, wenn bereits ein sichtbares Overlay-Fenster vorhanden ist oder eine andere Ausnahme greift.

Konsequenz:

- nicht darauf verlassen, dass `SYSTEM_ALERT_WINDOW` allein Background-Starts erlaubt
- Service-Start an sichtbare Nutzeraktion koppeln

## 4.5 Android 16 Relevanz

Auf Android 16 gelten zusätzliche Quoten für Background-Jobs, die aus Foreground Services gestartet werden.

Konsequenz:

- keine WorkManager-/JobScheduler-Ketten aus dem Overlay-Service starten
- KI-Anfragen nur durch Nutzeraktion auslösen
- keine dauerhafte Sync-/Background-Job-Logik bauen

---

## 5. WhatsApp-Erkennung mit UsageStatsManager

## 5.1 Erlaubte Technik

Verwenden:

- `UsageStatsManager.queryEvents(beginTime, endTime)`
- Berechtigung: `PACKAGE_USAGE_STATS`
- Nutzer muss Usage Access in Android-Einstellungen aktiv freigeben

Nicht verwenden:

- Accessibility Service
- Screen Scraping
- Notification Listener
- WhatsApp-Datenbankzugriff
- Root-/ADB-Hacks

## 5.2 Paketnamen

Pflicht im MVP:

```text
com.whatsapp
```

Optional später:

```text
com.whatsapp.w4b
```

WhatsApp Business ist kein Pflichtumfang.

## 5.3 Polling-Intervall

Startwert:

```text
1000 ms
```

Optional bei sichtbarer Verzögerung:

```text
500 ms
```

Nicht aggressiver pollen, solange kein echter Grund besteht.

## 5.4 Verhalten

- Wenn `com.whatsapp` Vordergrund-App ist: Bubble anzeigen.
- Wenn andere App Vordergrund-App ist: Bubble ausblenden.
- Wenn Usage Access fehlt: klare Meldung in MainActivity.
- Wenn Device gesperrt oder User nicht unlocked ist: robust mit `null`/leerer Antwort umgehen.

## 5.5 Einschränkung

UsageStats ist keine perfekte Echtzeit-API. Leichte Verzögerungen oder herstellerabhängiges Verhalten sind möglich.

Diese Einschränkung ist zu dokumentieren, nicht mit Accessibility zu umgehen.

---

## 6. Clipboard-Regeln

## 6.1 Erlaubt

- Clipboard lesen, wenn Nutzer das ReplyPanel aktiv öffnet
- kopierten Text als Vorschau anzeigen
- Text erst nach Nutzerbestätigung verwenden
- Vorschlag per Button in Clipboard kopieren

## 6.2 Verboten

- Clipboard dauerhaft überwachen
- Clipboard im Hintergrund lesen
- Clipboard-Historie speichern
- Clipboard-Inhalt loggen
- Clipboard-Inhalt automatisch an KI senden

## 6.3 Datenschutz-Grund

Clipboard kann sensible Inhalte enthalten. Der MVP darf daher nur mit aktiv bestätigtem Clipboard-Text arbeiten.

---

## 7. Android 16 UI-Constraints

## 7.1 Edge-to-edge

Für Apps mit targetSdk 36 kann Android 16 Edge-to-edge nicht mehr einfach per Opt-out abschalten.

Konsequenz:

- MainActivity muss mit Systemleisten/Insets sauber umgehen
- Inhalte dürfen nicht hinter Statusbar oder Navigationbar unlesbar werden
- Compose-Screen sollte Insets berücksichtigen

## 7.2 Große Displays und Rotation

Für Apps mit targetSdk 36 ignoriert Android 16 auf großen Displays ab `sw600dp` bestimmte Orientation-, Resizability- und Aspect-Ratio-Beschränkungen.

Konsequenz:

- UI nicht hart auf Portrait fixieren
- State bei Rotation erhalten
- ReplyPanel-Größen nicht starr auf ein einzelnes Smartphone-Layout annehmen
- Samsung S25 bleibt primäres Ziel, aber Foldables/Tablets dürfen nicht komplett kaputt aussehen

---

## 8. Manifest-Regeln

## 8.1 Erlaubte Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
```

Optional nur bei tatsächlicher Nutzung:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

Bei targetSdk 34+ und tatsächlichem Foreground Service müssen passende spezifische Foreground-Service-Permissions geprüft werden.

## 8.2 Verbotene Permissions im MVP

Nicht hinzufügen:

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
```

## 8.3 Accessibility-Verbot

Kein Accessibility Service im MVP.

Auch nicht als Fallback für:

- WhatsApp-Erkennung
- Chat-Auslesen
- Text-Einfügen
- Button-Klicks
- automatisches Senden

Wenn eine Funktion Accessibility benötigt, gehört sie nicht in den MVP.

---

## 9. Notification-Regeln

Wenn ein Foreground Service verwendet wird, muss die Notification:

- klar benennen, dass ChatHilfe aktiv ist
- nicht irreführend sein
- eine Stop-/Deaktivierungsoption ermöglichen, falls sinnvoll
- keine Nachrichtentexte anzeigen
- keine KI-Inhalte anzeigen

Nicht erlaubt:

- Notification-Inhalte aus WhatsApp lesen
- Notification Listener verwenden
- WhatsApp-Benachrichtigungen analysieren

---

## 10. Samsung-/Herstellerlimits

Auf Samsung-Geräten kann Hintergrundverhalten durch Akkuoptimierung eingeschränkt werden.

Mögliche Auswirkungen:

- Overlay verschwindet nach einiger Zeit
- Service wird beendet
- Polling stoppt
- Neustart nach Sperren/Entsperren ist nötig

MVP-Regel:

- App soll verständlich dokumentieren, wenn Akkuoptimierung Probleme macht
- keine aggressiven Anti-Kill-Tricks bauen
- kein Autostart-Hack
- kein Accessibility-Fallback
- Nutzer kann Overlay manuell wieder aktivieren

---

## 11. Netzwerk und KI

KI-Anfragen dürfen nur nach Nutzeraktion entstehen:

- Nutzer tippt „Vorschläge erstellen“

Nicht erlaubt:

- automatische KI-Anfragen beim Öffnen von WhatsApp
- automatische KI-Anfragen beim Clipboard-Wechsel
- Hintergrund-Sync
- Verlaufsauswertung

API-Key-Regeln:

- lokal speichern
- nicht loggen
- nicht committen
- nicht in Crashlogs schreiben

---

## 12. Logs

Nicht loggen:

- API-Key
- Clipboard-Inhalt
- Nutzerabsicht
- kopierte WhatsApp-Nachricht
- generierte Antwort
- Kontakte
- Gerätekennung

Erlaubt:

- technische Statuslogs ohne Inhalte, z. B. `overlay_visible=true`
- Fehlerklasse ohne sensible Daten
- Permission-State ohne Nutzerdaten

---

## 13. Build- und Test-Matrix

Mindestens testen:

| Bereich | Test |
|---|---|
| Berechtigung | Overlay Permission fehlt/vorhanden |
| Berechtigung | Usage Access fehlt/vorhanden |
| Overlay | Button erscheint über WhatsApp |
| Overlay | Button verschwindet außerhalb WhatsApp |
| Overlay | Button ist verschiebbar |
| Overlay | keine doppelten Views nach Rotation/App-Wechsel |
| Clipboard | Clipboard wird nur nach Panel-Öffnung gelesen |
| Clipboard | Text wird erst nach Bestätigung verwendet |
| KI | fehlender API-Key |
| KI | kein Internet |
| KI | ungültige Antwort |
| Android | Sperren/Entsperren |
| Android | App aus Recent Apps entfernen |
| Android | Batterieoptimierung aktiv |
| Android 16 | Edge-to-edge/Insets MainActivity |
| Android 16 | Rotation und größere Fenster soweit möglich |

---

## 14. Quellen für Agenten

Agenten sollen bei Android-Fragen zuerst offizielle Android-Dokumentation prüfen.

Relevante offizielle Quellen:

- Android `WindowManager.LayoutParams`: https://developer.android.com/reference/android/view/WindowManager.LayoutParams
- Android `Manifest.permission`: https://developer.android.com/reference/android/Manifest.permission
- Foreground-Service-Restriktionen: https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
- Foreground-Service-Änderungen: https://developer.android.com/develop/background-work/services/fgs/changes
- Android 16 Behavior Changes: https://developer.android.com/about/versions/16/behavior-changes-16
- `UsageStatsManager`: https://developer.android.com/reference/android/app/usage/UsageStatsManager
- Secure Clipboard Handling: https://developer.android.com/privacy-and-security/risks/secure-clipboard-handling

Wenn StackOverflow, alte Blogposts oder KI-Vorschläge diesen Quellen widersprechen, gelten die offiziellen Android-Dokumente.

---

## 15. Harte Stop-Bedingungen

Stoppen und explizit Rückfrage stellen, wenn eine Änderung eines davon verlangt:

- Accessibility Service hinzufügen
- WhatsApp-Chats automatisch lesen
- Nachrichten automatisch in WhatsApp einfügen
- Nachrichten automatisch senden
- Notification Listener hinzufügen
- Screen Capture hinzufügen
- Root-/ADB-Abhängigkeit einführen
- Kontaktzugriff hinzufügen
- Clipboard-Monitoring im Hintergrund einführen
- Target-SDK-Strategie ändern
- Foreground Service aus dem Hintergrund starten
- Android-Berechtigungen außerhalb der erlaubten Liste hinzufügen

---

## 16. Kurzfazit

Der MVP ist auf modernen Android-Versionen machbar, aber nur mit disziplinierter Umsetzung:

- `TYPE_APPLICATION_OVERLAY` statt alter Window-Typen
- UsageStats statt Accessibility
- Service-Start aus Nutzeraktion
- Clipboard nur nach Nutzeraktion
- keine WhatsApp-Automation
- keine unnötigen Berechtigungen
- echte Tests auf Samsung-Gerät

Wenn diese Regeln eingehalten werden, bleibt das Projekt technisch realistisch und privacy-sicher.
