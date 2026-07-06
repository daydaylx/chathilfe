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

- Code-Review aller neuen/geänderten Dateien manuell durchgeführt (kein Compiler verfügbar,
  siehe "Not validated"): Ressourcen-Referenzen (`R.string.*`, `R.drawable.*`) gegen
  `strings.xml`/`drawable/` abgeglichen — vollständig konsistent. Keine verwaisten Referenzen
  auf den entfernten `FutureRequirementStatus`-Platzhalter im Repo gefunden (`grep` bestätigt).
- Manifest-Struktur manuell gegen Android-FGS-Dokumentation geprüft (Permission + Service-Typ +
  Property wie oben begründet).
- Statisch geprüft: kein echter API-Key im Repo, kein Accessibility Service, kein
  Clipboard-Zugriff, keine KI-Anbindung, keine WhatsApp-/UsageStats-Erkennung im Diff — nur
  bereits bestehende `PACKAGE_USAGE_STATS`-Permission unverändert übernommen.
- `OverlayController`/`FloatingBubbleView` erfüllen die Architekturvorgaben: einzige
  `WindowManager`-Zugriffsstelle, Attached-State-Tracking, defensive try/catch um
  `addView`/`updateViewLayout`/`removeView`, `bubbleView = null` unconditional in `finally` bei
  `remove()`, `FloatingBubbleView` ruft nie `WindowManager` direkt auf.

## Not validated

- **`./gradlew assembleDebug`/`test`/`lint` konnten in dieser Sandbox nicht ausgeführt werden.**
  Ursache eindeutig identifiziert (nicht umgangen, wie in den Vorgaben gefordert):
  1. Die im Wrapper gepinnte Gradle-Distribution (`gradle-9.6.1-bin.zip`) kann nicht von
     `services.gradle.org`/`github.com` geladen werden (`HTTP 403`, Egress-Restriktion dieser
     Sandbox — identisch zum in Phase 0–2 dokumentierten Verhalten).
  2. Ein system-weit installiertes Gradle (8.14.3) ist vorhanden, aber AGP 9.2.0 verlangt
     mindestens Gradle 9.4.1 (`Failed to apply plugin 'com.android.internal.version-check'`) —
     bestätigt per direktem Testlauf.
  3. Kein Android SDK in dieser Sandbox installiert.
  Die Toolchain-Version wurde bewusst **nicht** geändert, um von der in `docs/DECISIONS.md`
  (D-004) gepinnten Konfiguration abzuweichen.
- Kein Gerätetest durchgeführt (kein physisches Android-Gerät in dieser Sandbox verfügbar).
  Die vollständige Gerätetest-Checkliste (Overlay-Berechtigung, Bubble-Anzeige/-Drag,
  Notification-Inhalt, POST_NOTIFICATIONS-Ablehnung, Toggle aus/an, Force-Stop-Verhalten,
  Sperren/Entsperren, mehrfaches Toggeln) ist unten dokumentiert und muss vor Phase 4 auf einem
  echten Gerät (Samsung S25) nachgeholt werden — das ist ein Pflicht-Gate laut `docs/DECISIONS.md` (D-006).
- Kein Kotlin-Compiler-Lauf, daher keine Garantie gegen Tippfehler/Typfehler über das manuelle
  Review hinaus.

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

Gerätetest gemäß obiger Checkliste auf echtem Android-Gerät durchführen (Pflicht-Gate laut
`docs/DECISIONS.md` D-006), danach `./gradlew assembleDebug`/`test`/`lint` auf einer Umgebung
mit Internetzugriff zur Gradle-Distribution und installiertem Android SDK nachholen. Erst nach
bestätigtem Gerätetest mit Phase 4 (WhatsApp-Erkennung über `ForegroundAppDetector`) beginnen.
