# Phase 4 Report — WhatsApp-Erkennung

## Summary

Phase 4 fügt `ForegroundAppDetector` (`app/.../detection/ForegroundAppDetector.kt`) hinzu, der über
`UsageStatsManager.queryEvents(beginTime, endTime)` erkennt, ob `com.whatsapp` die aktuelle
Vordergrund-App ist. Der `OverlayService` zeigt die Bubble nicht mehr sofort beim Start, sondern
startet stattdessen das Detektor-Polling (1000 ms) und attaches die Floating Bubble **nur, solange
WhatsApp im Vordergrund ist**; verlässt der Nutzer WhatsApp, wird die Bubble entfernt, ohne den
Service zu stoppen. Die Bubble-Position bleibt über show/hide-Zyklen erhalten (persistiert via
`SettingsStore`). Fehlender Usage Access wird sauber gemeldet: der Overlay-Toggle startet den
Service nur, wenn neben `SYSTEM_ALERT_WINDOW` auch `PACKAGE_USAGE_STATS` erteilt ist (Toast
`usage_permission_required_toast`); zusätzlich liefert der Detektor defensiv `State.NoUsageAccess`.

Kein Accessibility-Fallback, kein Notification Listener, kein Screen Scraping, kein Clipboard,
keine KI-Anbindung, kein `com.whatsapp.w4b` (Post-MVP), keine Phase-5-UI.

## Files changed

Neu:
- `app/src/main/java/de/disaai/chathilfe/detection/ForegroundAppDetector.kt`
- `docs/PHASE_4_REPORT.md` (dieser Bericht)

Geändert:
- `app/src/main/java/de/disaai/chathilfe/overlay/OverlayService.kt`
  (Detektor instanziieren, Polling statt sofortigem `showBubble`, zustandsgetriebenes
  show/hide, sauberes Stoppen des Pollings in `ACTION_STOP`/`onDestroy`; Bubble-Listener als
  Feld extrahiert; `isBubbleShown`-Guard gegen doppelte Views).
- `app/src/main/java/de/disaai/chathilfe/settings/SettingsScreen.kt`
  (Toggle prüft vor `OverlayService.start` zusätzlich Usage Access → Toast; beide Toasts auf die
  `Toast.makeText(context, resId, duration)`-Überladung umgestellt, siehe „Lint").
- `app/src/main/res/values/strings.xml` (neu: `usage_permission_required_toast`).
- `app/src/main/AndroidManifest.xml` (`specialUse`-Subtype-Begründung präzisiert: Bubble erscheint
  nur bei WhatsApp im Vordergrund, erkannt via Usage Stats; keine neue Permission).

## Permissions

Keine neue Berechtigung. Das Manifest enthielt bereits `PACKAGE_USAGE_STATS` (seit Phase 1/2),
`FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS`, `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE`,
`INTERNET`. Die APK bestätigt genau diese Menge plus die von AndroidX automatisch hinzugefügte
`DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Keine verbotene Permission (keine Kontakte/SMS/Kamera/
Mikrofon/Standort/Medien/Accessibility/NotificationListener/ScreenCapture).

## Service type decision

`foregroundServiceType="specialUse"` bleibt korrekt. Phase 4 ändert die Natur des Service nicht
(weiterhin ein manuell aus sichtbarer Nutzeraktion gestarteter, verschiebbarer Floating-Bubble-Overlay).
Die Subtype-Property-Begründung wurde um „appears only while WhatsApp is in the foreground (detected
via usage stats)" ergänzt, um die jetzt genutzte Fähigkeit ehrlich abzubilden.

## Validation

### Build-Verifikation (lokal, 2026-07-07)

Linux, OpenJDK 21, Android SDK (`/home/d/Android/Sdk`, Platformen 33–37, Build-Tools 34/36),
AGP 9.2.0 / Gradle-Wrapper 9.6.1 / Kotlin 2.4.0 / Compose-BOM 2026.06.01.

| Befehl | Ergebnis |
|---|---|
| `./gradlew assembleDebug` | ✅ BUILD SUCCESSFUL |
| `./gradlew assembleDebug lint` | ✅ BUILD SUCCESSFUL (lint 0 errors, 9 warnings) |
| `./gradlew test` | ✅ BUILD SUCCESSFUL (keine Unit-Test-Quellen; UsageStatsManager ist nicht unit-testbar) |

- **APK:** `app/build/outputs/apk/debug/app-debug.apk` (~30,8 MB / 32.328.292 Bytes), verifiziert via
  `aapt dump badging`: `package=de.disaai.chathilfe`, `versionCode=1`, `versionName=0.1.0`,
  `minSdk=29`, `targetSdk=35`, `compileSdk=37`, Label `ChatHilfe`.
- **Lint:** `0 errors, 9 warnings`. Die 9 Warnings sind die bereits aus Phase 3 bekannten
  (`InlinedApi`, `OldTargetApi`, `ObsoleteSdkInt`, `UnusedResources` ×2, `MonochromeLauncherIcon` ×2,
  `UseKtx`, `ClickableViewAccessibility`); keine stammt aus Phase-4-Code. Siehe unten „Lint".

### Code-Level (statisch, da kein Gerät)

- **WhatsApp in den Vordergrund → Bubble:** `ForegroundAppDetector` liefert pro Poll
  `State.WhatsappForeground(true)`; `OverlayService.onDetectionState` ruft `showBubble()`, das die
  Position aus `SettingsStore` liest und `controller.show(...)` aufruft (nur wenn `!isBubbleShown`).
- **WhatsApp verlassen → Bubble weg:** Detektor liefert `false` → `hideBubble()` → `controller.remove()`.
- **Kein doppelter View:** `OverlayController.show` bricht früh ab, wenn `isAttached`;
  zusätzlich ist `showBubble()` durch `isBubbleShown` und `remove()` idempotent.
- **Polling 1000 ms:** Konstante `POLL_INTERVAL_MS = 1000L`; nicht aggressiver (max. 500 ms nur
  dokumentiert, nicht aktiviert).
- **Fehlender Usage Access sauber gemeldet:** Toggle-Toast `usage_permission_required_toast` +
  Detektor liefert `State.NoUsageAccess` (Service hält Bubble dann ausgeblendet).
- **Service stoppt Polling sauber:** `ACTION_STOP` und `onDestroy` rufen `stopDetection()`
  (`job.cancel()`), `hideBubble()` (`controller.remove()`) und `stopForeground`/`stopSelf`.
- **Positionserhalt:** Drag-Ende persistiert `bubbleX/bubbleY`; jeder show-Zyklus liest sie neu.
- **Kein Auto-Neustart:** unverändert `START_NOT_STICKY`; der Detektor wird nur aus
  `onStartCommand` (nach sichtbarer Nutzeraktion) gestartet.
- **Einzige `WindowManager`-Zugriffsstelle:** weiterhin `OverlayController`; `ForegroundAppDetector`
  greift nie auf WindowManager zu.

### Scope-Check (Negativliste, `grep` über `app/src/main`)

Kein `AccessibilityService`/`BIND_ACCESSIBILITY`, kein `NotificationListenerService`, kein
`MediaProjection`/`ScreenCapture`, kein Clipboard (`ClipboardManager`/`getPrimaryClip`), kein
AI/HTTP (`OkHttp`/`Retrofit`/`Ktor`/`HttpURLConnection`/`AiClient`/`OpenRouter`), kein Phase-5-UI
(`InputBarView`/`ResultPanelView`/`ReplyPanel`/`SuggestionPager`), kein `com.whatsapp.w4b`.
`UsageStatsManager`/`queryEvents` nur in `ForegroundAppDetector`; `com.whatsapp` nur als
Paketkonstante. `OPENROUTER_API_KEY` unverändert nur als `BuildConfig`-Platzhalter-Vergleich
(`!= "replace_me_locally"`), kein echter Key, kein API-Aufruf.

## Lint

Vor Phase 4: 1 Error (`LocalContextGetResourceValueCall` in `SettingsScreen.kt`,
`context.getString(R.string.overlay_permission_required_toast)`) + 9 Warnings. Die Phase-4-Änderung
fügte anfangs eine **zweite** Instanz desselben Errors hinzu (der neue Usage-Access-Toast nutzte
ebenfalls `context.getString(...)`). Behebung: beide Toasts auf die idiomaticsche
`Toast.makeText(context, resId, duration)`-Überladung umgestellt (kein `context.getString` mehr).
Damit ist der Error verschwunden — auch der bereits aus Phase 3 bekannte. **Ergebnis: 0 errors,
9 warnings.** Diese kleine Überschreitung des ursprünglichen Phase-4-Nicht-Ziels („bestehenden
Lint-Error nicht fixen") war nötig, weil die eigene Phase-4-Änderung denselben Fehlertyp neu
ausgelöst hat; die Korrektur ist minimal und betrifft ausschließlich Code, den Phase 4 ohnehin
ändert.

## Not validated

- **Kein Gerätetest in dieser Sitzung** (kein Android-Gerät verbunden; bewusst keine
  Emulator-Nutzung; Nutzerentscheidung). Damit sind **alle Laufzeit-Acceptance-Punkte** nur
  code-seitig plausibilisiert, **nicht** empirisch bestätigt:
  - Bubble erscheint tatsächlich nur über WhatsApp zur Laufzeit.
  - Bubble verschwindet zuverlässig beim Verlassen von WhatsApp (Timing/Verzögerung der
    UsageStats-Ereignisse, OEM-abhängig).
  - Keine doppelten Views bei schnellem App-Wechseln und Sperren/Entsperren.
  - Polling läuft stabil im Foreground Service (Samsung-Akkulimits können es stoppen).
  - Usage-Access-fehlt-Pfad verhält sich an der Toast-/Service-Grenze korrekt.
  - Phase-4-Gerätetest bleibt offen. Nach `docs/DECISIONS.md` D-006 und
    `docs/DEVICE_TEST_POLICY.md` ist er **empfohlen, aber nicht blockierend**;
    finale Gerätevalidierung gebündelt in Phase 8.

### Gerätetest-Checkliste (für den nächsten Schritt mit echtem Gerät)

1. Overlay+Usage Access erteilen, Toggle an → Foreground-Notification, **noch keine** Bubble (nicht in WhatsApp).
2. WhatsApp öffnen → Bubble erscheint (rechts/vertikal mittig oder an zuletzt gezogener Position), Verzögerung messen.
3. Andere App (z. B. Einstellungen) öffnen → Bubble verschwindet.
4. Mehrfach zwischen WhatsApp und anderen Apps wechseln → zuverlässiges erscheinen/verschwinden, keine doppelten Views.
5. Bubble innerhalb WhatsApp ziehen → Position bleibt; WhatsApp verlassen/wieder öffnen → Position erhalten.
6. Tap ohne Drag → Toast (keine Bewegung).
7. Usage Access entziehen (Android-Einstellungen) → beim nächsten Toggle-Versuch Toast; Service startet nicht.
8. Toggle aus → Bubble sofort weg, Notification weg.
9. Sperren/Entsperren → kein hängendes Overlay, keine doppelten Views; ggf. Bubble-Logik nach Aufwecken prüfen.
10. Längere WhatsApp-Nutzung + Force-Stop der App → kein Auto-Neustart, kein Polling ohne erneute Nutzeraktion.

## Risks

- **Gerätetest offen (nicht blockierend).** Alle o.g. Laufzeitpunkte sind nicht empirisch bestätigt.
  Nach `docs/DECISIONS.md` D-006 / `docs/DEVICE_TEST_POLICY.md` sind Gerätetests nach Phase 4
  empfohlen, aber nicht blockierend; finale Gerätevalidierung erfolgt gebündelt in Phase 8.
- **UsageStats ist nicht echtzeit-tauglich** (Verzögerung ≥ 1 s, OEM-/Geräteabhängig). Laut
  `docs/ANDROID_CONSTRAINTS.md` akzeptiert und dokumentiert; **nicht** mit Accessibility umgangen.
- **Bootstrap-/Screen-off-Edge-Case:** Detektor nutzt beim ersten Poll ein 30-s-Look-back-Fenster und
  hält `lastForegroundPackage` über Polls; beim Ausschalten des Bildschirms kann u. U. kein neues
  `ACTIVITY_RESUMED` ausgelöst werden, sodass der Detektor noch „WhatsApp" meldet, bis die nächste
  Activity resumed. Für den MVP akzeptiert (selbstkorrigierend beim nächsten App-Wechsel).
- **Samsung-/Batterielimits** können das Polling stoppen. Keine Anti-Kill-Tricks; Nutzer kann das
  Overlay manuell reaktivieren (Toggle aus/an).
- **`specialUse`-Foreground-Services** sind eine vergleichsweise junge API-Kategorie; die
  Subtype-Begründung im Manifest ist nun präziser (WhatsApp-only via Usage Stats).

## Next sensible step

Build ist lokal verifiziert (`assembleDebug` ✅, `lint` ✅ 0 errors). Der Phase-4-Gerätetest
bleibt offen, ist laut `docs/DECISIONS.md` D-006 / `docs/DEVICE_TEST_POLICY.md` aber **nicht
blockierend** (finale Gerätevalidierung gebündelt in Phase 8; siehe Checkliste oben). Damit kann
mit **Phase 5 — Input-Bar und Result-Panel ohne KI** (Dummy-Daten) gemäß `docs/IMPLEMENTATION_PLAN.md`
fortgefahren werden. Parallel (separater Auftrag, nicht Phase 4) möglich: die verbleibenden
9 Lint-Warnings bewerten.
