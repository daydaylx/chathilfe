# ANDROID_CONSTRAINTS.md — ChatHilfe MVP

## Zweck

Dieses Dokument ist die Quelle für Android-spezifische Einschränkungen.

Es verhindert alte oder riskante Android-Patterns bei Overlay, Foreground Service, UsageStats, Clipboard und Android 15/16.

Verbindliche Entscheidungen stehen zusätzlich in `docs/DECISIONS.md`.

---

## SDK-Strategie

| Einstellung | MVP-Default |
|---|---|
| `applicationId` | `de.disaai.chathilfe` |
| `compileSdk` | 36 |
| `targetSdk` | 35 für ersten stabilen MVP |
| späteres Ziel | targetSdk 36 nach Stabilisierung |
| `minSdk` | 29 |
| Testgerät | Samsung Galaxy S25 |

`targetSdk` darf nicht nebenbei geändert werden. Jede Änderung muss dokumentiert und auf echtem Gerät getestet werden.

AGP-/Gradle-/Kotlin-/Compose-Versionen müssen beim Scaffolden anhand aktueller offizieller Release- und Kompatibilitätsinformationen gepinnt werden.

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

Entscheidung:

- Die Overlay-Laufzeit läuft in einem Foreground Service.
- Der Service wird ausschließlich aus sichtbarer Nutzeraktion gestartet.
- `startForeground()` wird zeitnah aufgerufen.

Erlaubter Flow:

```text
MainActivity sichtbar
↓
Nutzer aktiviert Overlay
↓
App prüft Berechtigungen
↓
Foreground Service startet
↓
Overlay-Runtime läuft
```

Manifest-Regeln:

- `FOREGROUND_SERVICE` ist für die Overlay-Laufzeit erforderlich.
- `POST_NOTIFICATIONS` ist erforderlich, wenn die Ziel-Android-Version eine sichtbare Service-Notification absichert.
- Der konkrete Foreground-Service-Typ muss beim Scaffolden/Implementieren gegen aktuelle Android-Doku geprüft werden.
- Wenn `specialUse` verwendet wird, muss eine klare Manifest-Begründung ergänzt und dokumentiert werden.

Nicht erlaubt:

- versteckter Background-Start
- Autostart-Hack
- WorkManager-/JobScheduler-Ketten aus dem Overlay-Service
- KI-Dauerjobs im Service
- Clipboard-Abfragen im Hintergrund

Android 15: Nicht darauf verlassen, dass `SYSTEM_ALERT_WINDOW` allein Background-Starts erlaubt.

Android 16: Keine Background-Job-Ketten aus dem Overlay-Service starten.

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

- Lesen beim Öffnen des Panels oder durch explizite Nutzeraktion im Panel
- Vorschau anzeigen, wenn Text verfügbar ist
- Text erst nach Nutzerbestätigung verwenden
- Vorschlag per Button kopieren
- manueller Fallback: Nutzer kann Text selbst ins Panel einfügen

Verboten:

- dauerhaftes Monitoring
- Hintergrundlesen
- Speicherung von Clipboard-Inhalten
- Logging von Clipboard-Inhalten
- automatische KI-Anfrage bei Clipboard-Änderung

Wichtig:

- Clipboard-Lesen kann auf modernen Android-Versionen leer oder blockiert sein, wenn die App nicht fokussiert ist.
- Der MVP darf deshalb nicht davon abhängen, dass Clipboard-Lesen immer funktioniert.
- Der manuelle Einfügen-Fallback ist Pflicht.

---

## Manifest-Regeln

Erlaubt / benötigt:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

Wichtig zu `PACKAGE_USAGE_STATS`:

- Es ist ein Sonderzugriff und wird vom Nutzer über Android-Einstellungen freigegeben.
- Beim Manifest kann je nach Tooling ein `tools:ignore="ProtectedPermissions"` nötig sein.

Optional je nach Ziel-Android und Service-Notification:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
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
