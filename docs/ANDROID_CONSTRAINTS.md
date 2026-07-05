# ANDROID_CONSTRAINTS.md — ChatHilfe MVP

## Zweck

Dieses Dokument ist die Quelle für Android-spezifische Einschränkungen.

Es verhindert alte oder riskante Android-Patterns bei Overlay, Foreground Service, UsageStats, Clipboard und Android 15/16.

---

## SDK-Strategie

| Einstellung | MVP-Default |
|---|---|
| `compileSdk` | 36 |
| `targetSdk` | 35 für ersten stabilen MVP |
| späteres Ziel | targetSdk 36 nach Stabilisierung |
| `minSdk` | 29 oder höher |
| Testgerät | Samsung Galaxy S25 |

`targetSdk` darf nicht nebenbei geändert werden. Jede Änderung muss dokumentiert und auf echtem Gerät getestet werden.

---

## Overlay

Erlaubt:

- `SYSTEM_ALERT_WINDOW`
- `Settings.canDrawOverlays()`
- `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`
- `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`

Verboten:

- `TYPE_PHONE`
- `TYPE_SYSTEM_ALERT`
- `TYPE_SYSTEM_OVERLAY`
- `TYPE_SYSTEM_ERROR`
- `TYPE_TOAST` als Overlay-Ersatz

Regeln:

- alle `WindowManager`-Operationen über `OverlayController`
- keine doppelten Views
- Overlay bei Stop sauber entfernen
- Bubble klein und verschiebbar halten
- außerhalb von WhatsApp ausblenden
- keine Vollbild-Überlagerung im MVP

---

## Foreground Service

Grundregel:

- keinen versteckten, ungefragten Hintergrundstart bauen
- Overlay-Runtime aus sichtbarer Nutzeraktion starten

Erlaubter Flow:

```text
MainActivity sichtbar
↓
Nutzer aktiviert Overlay
↓
App prüft Berechtigungen
↓
Overlay-Runtime startet
```

Falls ein Foreground Service verwendet wird:

- `FOREGROUND_SERVICE` nur dann deklarieren
- bei targetSdk 34+ passenden Service-Typ prüfen
- `startForeground()` zeitnah aufrufen
- klare Notification anzeigen
- keine KI-Dauerjobs im Service
- keine Clipboard-Abfragen im Hintergrund

Android 15: Nicht darauf verlassen, dass `SYSTEM_ALERT_WINDOW` allein Background-Starts erlaubt.

Android 16: Keine Job-/WorkManager-Ketten aus dem Overlay-Service starten.

---

## WhatsApp-Erkennung

Erlaubt:

- `UsageStatsManager.queryEvents(beginTime, endTime)`
- `PACKAGE_USAGE_STATS`

Nicht erlaubt:

- Accessibility Service
- Notification Listener
- Screen Scraping
- Root/ADB-Hacks
- WhatsApp-Datenbankzugriff

Paketname für MVP:

```text
com.whatsapp
```

Optional später:

```text
com.whatsapp.w4b
```

Polling:

- Startwert: 1000 ms
- maximal auf 500 ms senken, wenn UX zu träge ist
- nicht aggressiver pollen

UsageStats ist nicht perfekte Echtzeit. Verzögerungen dokumentieren, nicht mit Accessibility umgehen.

---

## Clipboard

Erlaubt:

- Lesen beim Öffnen des Panels
- Vorschau anzeigen
- Text erst nach Nutzerbestätigung verwenden
- Vorschlag per Button kopieren

Verboten:

- dauerhaftes Monitoring
- Hintergrundlesen
- Speicherung von Clipboard-Inhalten
- Logging von Clipboard-Inhalten
- automatische KI-Anfrage bei Clipboard-Änderung

---

## Manifest-Regeln

Erlaubt:

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

Verboten im MVP:

```xml
READ_CONTACTS
READ_SMS
SEND_SMS
RECORD_AUDIO
CAMERA
ACCESS_FINE_LOCATION
ACCESS_COARSE_LOCATION
READ_EXTERNAL_STORAGE
READ_MEDIA_IMAGES
READ_MEDIA_VIDEO
BIND_ACCESSIBILITY_SERVICE
```

---

## Android 16 UI-Hinweise

Wenn targetSdk 36 gesetzt wird:

- MainActivity muss Insets/Edge-to-edge sauber behandeln
- keine harte Annahme, dass Content nie unter Systemleisten liegt
- Rotation und größere Fenster dürfen nicht komplett brechen

Samsung S25 bleibt Primärziel.

---

## Samsung-/Batterielimits

Mögliche Probleme:

- Service wird beendet
- Overlay verschwindet
- Polling stoppt nach Sperren/Entsperren

Regeln:

- Einschränkungen dokumentieren
- keine aggressiven Anti-Kill-Tricks
- kein Autostart-Hack
- Nutzer kann Overlay manuell reaktivieren

---

## Quellen für Agenten

Bei Android-Fragen zuerst offizielle Android-Doku prüfen:

- `WindowManager.LayoutParams`
- `Manifest.permission`
- Foreground-Service-Restriktionen
- Android 16 Behavior Changes
- `UsageStatsManager`
- Secure Clipboard Handling

Wenn alte Blogposts oder KI-Vorschläge widersprechen, gelten die offiziellen Android-Dokumente.

---

## Stop-Bedingungen

Stoppen und Rückfrage stellen, wenn eine Änderung verlangt:

- Accessibility Service
- Auto-Senden
- Auto-Einfügen
- automatische Chat-Auslesung
- Notification Listener
- Screen Capture
- Root/ADB-Abhängigkeit
- Hintergrund-Clipboard-Monitoring
- Target-SDK-Strategie ändern
- neue Berechtigung außerhalb der erlaubten Liste
