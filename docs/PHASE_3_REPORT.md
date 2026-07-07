# Phase 3 Report — Manuelles Overlay und Floating Button

## Summary

Phase 3 fügt einen Foreground Service (`OverlayService`) hinzu, der ausschließlich aus dem
Overlay-Toggle in `SettingsScreen` (sichtbare Nutzeraktion) gestartet wird. Der Service zeigt
über `OverlayController` — der einzigen Klasse mit direktem `WindowManager`-Zugriff — einen
verschiebbaren Floating Button (`FloatingBubbleView`, klassische `FrameLayout`-View,
`TYPE_APPLICATION_OVERLAY`) an. Drag und Tap werden per Touch-Slop sauber getrennt, die
Bubble-Position wird über die bereits in Phase 2 vorbereiteten `SettingsStore`-Felder
(`bubble_x`/`bubble_y`) persistiert. Der Service ruft `startForeground()` unmittelbar auf,
zeigt eine Notification ohne Chat-/KI-Inhalt und läuft mit `START_NOT_STICKY` (kein
Auto-Neustart nach Prozess-Kill). Keine WhatsApp-Erkennung, kein Clipboard, keine KI-Anbindung,
kein Accessibility Service wurden hinzugefügt.

## Files changed

Neu:
- `app/src/main/java/de/disaai/chathilfe/overlay/OverlayService.kt`
- `app/src/main/java/de/disaai/chathilfe/overlay/OverlayController.kt`
- `app/src/main/java/de/disaai/chathilfe/overlay/FloatingBubbleView.kt`
- `app/src/main/res/drawable/bg_floating_bubble.xml`
- `app/src/main/res/drawable/ic_notification_bubble.xml`

Geändert:
- `app/src/main/AndroidManifest.xml` (Permissions + `<service>`)
- `app/src/main/java/de/disaai/chathilfe/settings/PermissionStatus.kt` (`FutureRequirementStatus`-Platzhalter durch echten `checkNotificationPermission()` ersetzt)
- `app/src/main/java/de/disaai/chathilfe/settings/SettingsScreen.kt` (Toggle startet/stoppt den Service, neue Notification-Permission-Karte, Status-Karte zeigt echten Zustand)
- `app/src/main/res/values/strings.xml` (neue/aktualisierte Strings)
- `docs/DECISIONS.md` (offener Punkt "konkreter Foreground-Service-Typ" unter D-002 aufgelöst)

Bewusst nicht erstellt: `overlay/OverlayPositionStore.kt` (Position wird über die bereits
vorhandene `SettingsStore`-API gespeichert, keine parallele Architektur), `ReplyPanelView.kt`,
`ForegroundAppDetector.kt` (beide außerhalb des Phase-3-Scopes).

## Permissions added

- `FOREGROUND_SERVICE` — für jeden Foreground Service ab API 28 erforderlich.
- `FOREGROUND_SERVICE_SPECIAL_USE` — erforderlich für `foregroundServiceType="specialUse"` ab API 34.
- `POST_NOTIFICATIONS` — ab API 33 zur Laufzeit angefragt (im Overlay-Toggle-Flow und über eine eigene Permission-Karte); eine Ablehnung blockiert den Service nicht, da `SYSTEM_ALERT_WINDOW` für den Bubble maßgeblich bleibt.

Keine der verbotenen Berechtigungen (Accessibility, Kontakte, SMS, Kamera, Mikrofon, Standort,
Medien, Notification Listener, Screen Capture) wurde hinzugefügt.

## Service type decision

`foregroundServiceType="specialUse"` — die aktuelle Android-Dokumentation nennt eine
manuell gestartete, verschiebbare Chat-Head-/Floating-Icon-Bubble über anderen Apps als das
Referenzbeispiel für diesen Typ; keiner der übrigen Typen (`mediaPlayback`, `location`,
`dataSync`, `camera`, `microphone`, `phoneCall`, `connectedDevice`, `health`, `mediaProjection`,
`remoteMessaging`, `systemExempted`) passt auf einen reinen Overlay-Button. Das Manifest enthält
zusätzlich die geforderte `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" .../>`
mit einer knappen, ehrlichen Begründung. Die Entscheidung ist in `docs/DECISIONS.md` (D-002)
nachgetragen.

## Validation

### Build-Verifikation (lokal, 2026-07-07)

Durchgeführt auf Linux mit OpenJDK 21, Android SDK (`/home/d/Android/Sdk`, Platformen 33–37,
Build-Tools 34/36), AGP 9.2.0 / Gradle-Wrapper 9.6.1 / Kotlin 2.4.0 / Compose-BOM 2026.06.01.
Siehe auch `docs/BUILD_VALIDATION_REPORT.md`.

| Befehl | Ergebnis |
|---|---|
| `./gradlew clean assembleDebug` | ✅ BUILD SUCCESSFUL (Clean-Build, 39 Tasks ausgeführt) |
| `./gradlew test` | ✅ BUILD SUCCESSFUL, NO-SOURCE (keine Unit-Tests vorhanden) |
| `./gradlew lint` | ❌ BUILD FAILED — 1 Error + 9 Warnings (nicht APK-blockierend, siehe unten) |

- **APK:** `app/build/outputs/apk/debug/app-debug.apk` (~31,8 MB), verifiziert via
  `aapt dump badging`: `package=de.disaai.chathilfe`, `versionCode=1`, `versionName=0.1.0`,
  `minSdk=29`, `targetSdk=35`, `compileSdk=37`, Label `ChatHilfe`.
- **Permissions im APK:** `INTERNET`, `SYSTEM_ALERT_WINDOW`, `PACKAGE_USAGE_STATS`,
  `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS` sowie die von
  AndroidX automatisch hinzugefügte receiver-Permission. Keine verbotene Permission
  (keine Kontakte/SMS/Kamera/Mikrofon/Standort/Accessibility/NotificationListener/ScreenCapture).
- **Lint-Fund (offen, nicht Phase-3-blockierend, nicht in diesem Auftrag behoben):**
  - 1 Error: `LocalContextGetResourceValueCall` in `SettingsScreen.kt:136`
    (`context.getString(R.string.overlay_permission_required_toast)`).
  - 9 Warnings: `InlinedApi` (POST_NOTIFICATIONS @ `SettingsScreen.kt:105`), `OldTargetApi`
    (targetSdk 35 < compileSdk 37, erwartet), `ObsoleteSdkInt` (mipmap-anydpi-v26),
    `UnusedResources` ×2, `MonochromeLauncherIcon` ×2, `UseKtx` (`PermissionStatus.kt:54`),
    `ClickableViewAccessibility` (`FloatingBubbleView.kt:50`).
- Kotlin-Kompilierung ohne Fehler; einzige Compile-Warnung ist die harmlose Deprecation
  `unsafeCheckOpNoThrow` in `PermissionStatus.kt:26`.

### Code-Level (statisch, für Acceptance-Punkte ohne Gerät plausibilisiert)

- **Toggle startet/stoppt Service:** `OverlayService.start`/`.stop` werden **ausschließlich** aus
  `SettingsScreen` (`onCheckedChange`, Zeilen 141/150) gerufen; `MainActivity` startet keinen
  Service. Fehlende Overlay-Berechtigung beim Einschalten → Toast `overlay_permission_required_toast`,
  kein `OverlayService.start` (Service bleibt aus).
- **`startForeground()` zeitnah:** wird in `onStartCommand` vor dem Bubble-Aufbau aufgerufen.
- **Foreground-Service-Typ:** Manifest deklariert `foregroundServiceType="specialUse"` +
  `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" .../>` mit Begründung.
- **Kein doppelter Bubble:** `OverlayController.show` kehrt früh zurück, wenn `isAttached`;
  `onStartCommand` baut die Bubble nur auf, wenn `!controller.isAttached`.
- **Bubble verschiebbar / Tap-vs-Drag:** `FloatingBubbleView` löst Drag erst nach Überschreiten des
  `touchSlop` aus; `ACTION_UP` unterscheidet `onDragEnd` vs. `onTap`.
- **Position wird persistiert:** `onDragEnd` → `SettingsStore.setBubblePosition(x,y)` schreibt
  `bubble_x`/`bubble_y` (DataStore). Beim nächsten Start wird die gespeicherte Position gelesen.
- **Sauberes Stoppen:** `ACTION_STOP` und `onDestroy` rufen `controller.remove()`
  (`removeView` + bedingungslos `bubbleView = null` in `finally`) sowie
  `stopForeground(STOP_FOREGROUND_REMOVE)`/`stopSelf()`.
- **Kein Auto-Neustart:** `onStartCommand` liefert `START_NOT_STICKY`; kein `BOOT_COMPLETED`-
  Receiver, kein Autostart-Code. Service läuft nur nach bewusster Nutzeraktion.
- **Tap → nur Phase-3-Toast, kein ReplyPanel:** `onTap` zeigt ausschließlich
  `R.string.bubble_tap_toast`; kein `ReplyPanel`/Eingabe-UI vorhanden.
- **Einzige `WindowManager`-Zugriffsstelle:** `OverlayController`; `FloatingBubbleView` selbst
  greift nie auf `WindowManager` zu. Defensives try/catch um `addView`/`updateViewLayout`/`removeView`.

### Scope-Check (Negativliste, `grep` über `app/src/main`)

Kein `ForegroundAppDetector`, keine WhatsApp-/UsageStats-Erkennung (`queryEvents`/
`UsageStatsManager`/`com.whatsapp`), kein Clipboard-Zugriff (`ClipboardManager`/`getPrimaryClip`),
kein AI-HTTP-Client (`OkHttp`/`Retrofit`/`Ktor`/`HttpURLConnection`/`AiClient`), kein
`ReplyPanel`, kein Accessibility Service, kein Notification Listener, kein Screen Capture.
`OPENROUTER_API_KEY` taucht im Code nur als `BuildConfig`-Platzhalter-Vergleich
(`!= "replace_me_locally"`) auf — kein echter Key, kein API-Aufruf. Quelle: nur
`local.properties` (gitignored), Wert `replace_me_locally`.

## Not validated

- **Kein Gerätetest in dieser Sitzung** (kein Android-Gerät verbunden; `adb devices` leer,
  bewusst keine Emulator-Nutzung). Damit sind alle **Laufzeit**-Acceptance-Punkte nur code-seitig
  plausibilisiert, **nicht** empirisch bestätigt:
  - Bubble erscheint tatsächlich über Apps (WindowManager-Anzeige zur Laufzeit).
  - Drag folgt flüssig dem Finger / kein Sprung beim Loslassen.
  - Foreground-Notification wird (geräte-/OEM-abhängig) sichtbar.
  - Keine doppelten Views bei schnellem mehrfachem Toggeln (zur Laufzeit).
  - Kein Crash beim Sperren/Entsperren und nach Force-Stop.
  - `POST_NOTIFICATIONS`-Ablehnung blockiert den Bubble nicht (Laufzeit).
  Die vollständige Gerätetest-Checkliste steht unten und bleibt ein Pflicht-Gate laut
  `docs/DECISIONS.md` (D-006) vor Phase 4.
- **`lint` ist nicht grün** (1 Error + 9 Warnings). Nicht APK-blockierend und nicht in diesem
  Auftrag behoben (kein Refactoring). Siehe oben.

### Gerätetest-Checkliste (für den nächsten Schritt mit echtem Gerät)

1. Toggle an ohne Overlay-Berechtigung → Toast, kein Start.
2. Overlay-Berechtigung erteilen, Toggle an → Bubble erscheint rechts/vertikal mittig; Notification ohne Chat-/KI-Inhalt; POST_NOTIFICATIONS-Prompt auf API 33+.
3. POST_NOTIFICATIONS ablehnen → Bubble läuft trotzdem, kein Crash.
4. Bubble ziehen → folgt dem Finger flüssig, kein Sprung beim Loslassen.
5. Toggle aus/an → Bubble erscheint an zuletzt gezogener Position.
6. Tap ohne Drag → Toast, keine Bewegung.
7. Toggle aus → Bubble und Notification verschwinden sofort.
8. Force-Stop der App → kein Auto-Neustart ohne erneute Nutzeraktion.
9. Sperren/Entsperren → Bubble bleibt sichtbar.
10. Mehrfaches schnelles Toggeln → keine doppelten Views, keine `addView`/`removeView`-Exceptions in Logcat.

## Risks

- Da kein Build/Gerätetest möglich war, sind Laufzeitrisiken (z. B. `WindowManager.BadTokenException`
  bei ungewöhnlichen OEM-Overlay-Implementierungen, Touch-Slop-Kalibrierung, Samsung-Akkulimits)
  weiterhin nur dokumentiert, nicht empirisch bestätigt.
- `specialUse`-Foreground-Services sind eine vergleichsweise junge API-Kategorie; sollte sich
  die Android-Dokumentation zum Subtype-Property-Format ändern, muss die Manifest-Begründung
  nachgezogen werden.
- Die Notification-Permission-Karte startet bei dauerhafter Ablehnung ("Nicht mehr fragen") auf
  manchen OEMs keinen erneuten System-Dialog mehr — dieser Fall zeigt dann nur weiterhin
  "Nicht erlaubt" an, ohne Weiterleitung zu den App-Einstellungen. Für den MVP akzeptiert, da
  der Bubble davon unabhängig funktioniert.

## Next sensible step

Build ist lokal verifiziert (`assembleDebug` ✅, APK vorhanden). Offen bleibt der **Gerätetest**
auf einem echten Android-Gerät (Samsung S25, siehe Checkliste oben) — Pflicht-Gate laut
`docs/DECISIONS.md` D-006. Parallel (separater Auftrag, nicht Phase 3) empfohlen: den
Lint-Error in `SettingsScreen.kt:136` beheben, damit `lint` grün wird. Erst nach bestätigtem
Gerätetest mit Phase 4 (WhatsApp-Erkennung über `ForegroundAppDetector`) beginnen.
