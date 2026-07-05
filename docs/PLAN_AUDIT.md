# ChatHilfe Plan-Audit

> **Kontext:** Ergebnis eines reinen Plan-/Audit-Auftrags. Es wurde **kein Anwendungscode geschrieben und keine bestehende Datei geändert** — dieses Dokument ist das einzige Deliverable und wird als Repo-Dokument abgelegt: ein belastbares Audit + Umsetzungsplan, damit ein Coding-Agent danach ohne eigene Strategiearbeit starten kann. Grundlage: vollständiges Lesen aller 11 Repo-Dokumente auf Branch `claude/chathilfe-audit-plan-088czj`.

---

## 1. Kurzurteil

- Das Repo ist **reines Dokumentations-Repo** — es existiert **noch kein Android-/Gradle-Projekt**. Nichts ist baubar. Das ist der eigentliche Startzustand, kein Defekt.
- Die Dokumentation ist für ein privates MVP **überdurchschnittlich gut, konsistent und diszipliniert**. Scope-Grenzen, Datenschutz, Nicht-Ziele und Abschlusskriterien sind sauber definiert. Der jüngste `docs/`-Move wurde sauber durchgezogen — **keine kaputten Pfad-Referenzen** gefunden.
- Der Scope ist **realistisch und richtig klein**. Die dokumentierte Selbsteinschätzung („Schwierigkeit liegt nicht in der KI, sondern in Android-Overlay/Service/Berechtigungen") ist korrekt.
- **Die drei größten Risiken sind Android-Runtime-Themen, nicht Produkt- oder KI-Themen:** (a) Clipboard-Lesen aus einem Overlay-/Service-Kontext ist ab Android 10 fokusgebunden und liefert sonst leer; (b) Overlay + UsageStats-Polling überleben ohne Foreground Service auf Samsung nicht zuverlässig — die Docs lassen „Foreground Service, falls nötig" offen, was das Thema unterschätzt; (c) die Overlay-UI-Technik (klassische Views vs. `ComposeView`) ist nicht entschieden, obwohl `ComposeView` in `WindowManager` eigene Lifecycle-Owner braucht und sonst crasht.
- **Vor dem ersten Code müssen 4 Entscheidungen fixiert werden:** KI-Provider (einer!), Foreground-Service-Strategie, Overlay-UI-Technik, `applicationId` + Toolchain-Versionen. Alle vier sind heute offen gelassen.
- Empfehlung: Phasenreihenfolge der bestehenden `IMPLEMENTATION_PLAN.md` **unverändert übernehmen** (sie deckt sich exakt mit der geforderten Reihenfolge), aber ein **Entscheidungs-Gate vor Phase 1** ergänzen und **Gerätetest früh** (nach Overlay/Erkennung) statt nur am Ende einplanen.

---

## 2. Repo-Status

- **Android-Projekt vorhanden:** nein. Keine `settings.gradle.kts`, kein Root-`build.gradle.kts`, kein `app/build.gradle.kts`, kein Kotlin-Code, kein `AndroidManifest.xml`, kein `gradlew`.
- **Dokumentation vorhanden:** ja, vollständig und konsistent. Root: `README.md`, `AGENTS.md`, `Konzept.md`, `Arbeitsauftrag.md`, `LICENSE`. Unter `docs/`: `ARCHITECTURE.md`, `ANDROID_CONSTRAINTS.md`, `IMPLEMENTATION_PLAN.md`, `TEST_PLAN.md`, `PRIVACY_SECURITY.md`, `PROMPTS.md`, `UI_UX_SPEC.md`.
- **Dokumentenlage:** Steuer-/Regeldokumente im Root (`AGENTS.md` als Priorität 1), Fachdocs unter `docs/`. Source-of-truth-Tabelle + Konfliktpriorität in `AGENTS.md` vorhanden. Cross-Referenzen zeigen nach dem Move korrekt auf `docs/…`.
- **Git:** Branch `claude/chathilfe-audit-plan-088czj`, working tree clean. Letzte Commits = sauberer Docs-Move Root → `docs/` inkl. Entfernen der Root-Duplikate.
- **Hauptproblem vor Umsetzung:** Es existiert schlicht **kein Projektgerüst**. Alles Weitere (Overlay, Erkennung, KI) hängt daran. Dazu 4 offene technische Grundsatzentscheidungen (siehe §3).

---

## 3. Blockierende Probleme vor Umsetzung

| Problem | Auswirkung | Empfehlung | Priorität |
|---|---|---|---|
| Kein Android-/Gradle-Projekt vorhanden | Nichts baubar/testbar; alle Phasen blockiert | Phase 1 (Scaffold) als erste Codearbeit; Toolchain-Versionen dabei fixieren | blockierend (erwarteter Startzustand) |
| Overlay-UI-Technik nicht entschieden (Compose-Docs vs. `…View`-Klassennamen) | `ComposeView` im `WindowManager` crasht ohne `ViewTreeLifecycleOwner`/`SavedStateRegistry`/`ViewModelStoreOwner`; falsche Wahl = instabiles Overlay | Entscheidung vor Phase 3: **klassische Views/XML für Bubble+Panel** (robust, empfohlen) ODER `ComposeView` mit explizit gesetzten Lifecycle-Ownern. MainActivity bleibt Compose | hoch |
| Foreground-Service-Strategie nur „falls nötig / optional" | Ohne FGS killt Samsung Service → Polling stoppt, Overlay verschwindet; „optional" unterschätzt das | Entscheidung vor Phase 3/4: **ein FGS hostet Overlay-Laufzeit + Polling**, Start aus Nutzeraktion, `startForeground()` sofort, passender Service-Typ für targetSdk 34+ prüfen (`specialUse`) | hoch |
| Clipboard-Lesen aus Overlay/Service ab Android 10 fokusgebunden | `getPrimaryClip()` liefert null/leer, wenn nicht die fokussierte App liest → „Kopierten Text verwenden" funktioniert evtl. nie | Früh auf Gerät verifizieren; Panel-Fenster beim Lesen fokussierbar machen; **Fallback im Scope**: Nutzer fügt Text selbst in ein Panel-Feld ein (kein Auto-Monitoring) | hoch |
| KI-Provider nicht entschieden („OpenRouter oder OpenAI") | Blockiert Phase 7 (`AiClient`, `AiConfig`, Endpoint, Modell-ID, Auth-Header) | **Genau einen Provider + ein Default-Modell festlegen**, in `AiConfig`/Doc verankern. Kein Multi-Provider (bereits korrekt verboten) | mittel → blockierend ab Phase 7 |
| `applicationId`/Package-Name + Toolchain (AGP/Gradle/Kotlin/Compose-BOM) nicht festgelegt | Phase 1 kann sonst inkonsistent scaffolden | Vor Phase 1 fixieren: z. B. `applicationId`, `compileSdk 36`, `targetSdk 35`, `minSdk 29`, aktuelle stabile AGP/Kotlin/Compose-BOM | niedrig-mittel |

---

## 4. Dokumentations-Konsistenz

Insgesamt hohe Konsistenz. Positiv bestätigt: **keine kaputten Pfade**, Abschlusskriterien vorhanden (`Konzept §13`, `README`), manuelle Testschritte vorhanden (`TEST_PLAN.md`), Berechtigungslisten über alle Docs deckungsgleich, 7 Tonoptionen über `Konzept`/`PROMPTS`/`UI_UX_SPEC` identisch.

| Fund | Datei/Ort | Problem | Korrektur | Priorität |
|---|---|---|---|---|
| Foreground Service durchgängig „optional/falls nötig" | `Konzept.md:64,551`, `ANDROID_CONSTRAINTS.md:53-83`, `IMPLEMENTATION_PLAN.md:104`, `PRIVACY_SECURITY.md:76-77`, `TEST_PLAN.md:70-71` | Kernentscheidung offen gelassen; unterschätzt, dass FGS für dauerhaftes Polling+Overlay real nötig ist | Eine klare Aussage verankern: „Overlay-Laufzeit läuft in einem Foreground Service (aus Nutzeraktion gestartet)"; `POST_NOTIFICATIONS`/`FOREGROUND_SERVICE` damit als benötigt (nicht optional) markieren | mittel |
| KI-Provider unbestimmt | `Konzept.md:66`, `README.md:107`, `AGENTS.md:99`, `Arbeitsauftrag.md:81` | „OpenRouter **oder** OpenAI" nie aufgelöst | Einen Provider + Default-Modell wählen und festschreiben (z. B. in `ARCHITECTURE.md`/`AiConfig`) | mittel |
| Architektur-Baum divergiert | `Konzept.md:390-435` vs. `docs/ARCHITECTURE.md:44-73` | Modellnamen unterschiedlich (`Mode/Tone/AiRequest/AiSuggestion` vs. `ReplyMode/ToneOption/ReplyRequest/ReplySuggestion`); Konzept-Baum ohne `OverlayController`, `AiResponseParser`, `AiConfig` | `ARCHITECTURE.md` ist laut `AGENTS.md`-Priorität maßgeblich. In `Konzept §11` einen Hinweis ergänzen „Struktur maßgeblich: `docs/ARCHITECTURE.md`" oder den Baum dort auf ARCHITECTURE angleichen | niedrig-mittel |
| Overlay-UI-Technik nicht benannt | `IMPLEMENTATION_PLAN.md:54` (Compose nur für MainActivity) vs. `ARCHITECTURE.md:56-57` (`…View`) | Kein Doc legt fest, ob Bubble/Panel klassische Views oder `ComposeView` sind | Entscheidung dokumentieren (Empfehlung: klassische Views/XML fürs Overlay) | mittel |
| „Android 15/16" vs. `targetSdk 35` | `README.md:16`, `Konzept.md`-Umfeld vs. `ANDROID_CONSTRAINTS.md:13-17` | Leicht missverständlich, faktisch konsistent | Einzeiler: „`targetSdk 35` = Android-15-Baseline; Android 16 nur laufzeitgetestet, `targetSdk 36` später" | niedrig |
| `applicationId`/Toolchain fehlen | überall | Kein Package-Name, keine AGP/Gradle/Kotlin/Compose-Versionen | Vor Phase 1 festlegen und in `README`/`ARCHITECTURE` notieren | niedrig |
| `PACKAGE_USAGE_STATS` Manifest-Detail | `ANDROID_CONSTRAINTS.md:150` | Als normale `uses-permission` gelistet; ist Sonderzugriff, im Manifest oft `tools:ignore="ProtectedPermissions"` nötig | Implementierungshinweis in `ANDROID_CONSTRAINTS.md` ergänzen | niedrig |

**Pfad-Frage (Root vs. `docs/`):** bereits gelöst. Der Move ist vollzogen, Referenzen stimmen. **Empfehlung: dabei bleiben** — Steuerdocs im Root (`AGENTS.md`/`README.md`/`Konzept.md`/`Arbeitsauftrag.md`), Fachdocs in `docs/`. Kein weiterer Umzug nötig; das wäre reines Risiko ohne Nutzen.

> Hinweis: Alle Korrekturen oben sind **nur Plan**, nicht ausgeführt. Format je Vorschlag: *Empfohlene Änderung / Betroffene Datei / Grund / Priorität* — hier kompakt in der Tabelle abgebildet.

---

## 5. Architektur-Risiken

| Risiko | Wahrscheinlichkeit | Auswirkung | Gegenmaßnahme | MVP-Entscheidung |
|---|---|---|---|---|
| Clipboard-Lesen aus Overlay/Service ab Android 10 fokusgebunden → leer | hoch | hoch | Panel-Fenster beim Lesen fokussierbar; **einmaliges** Lesen per Nutzer-Tap; Fallback: manuelles Einfügen in Panel-Feld | Früh auf Gerät testen; Fallback ist scope-konform → verbindlich vorsehen |
| Overlay + Polling überleben ohne FGS nicht (Samsung Kill) | hoch | hoch | Ein Foreground Service hostet Overlay-Laufzeit + Detector; `startForeground()` sofort; keine Anti-Kill-Hacks | FGS verbindlich, aus Nutzeraktion gestartet |
| `ComposeView` im `WindowManager` ohne Lifecycle-Owner → Crash | mittel | hoch | Klassische Views/XML fürs Overlay **oder** `ComposeView` mit gesetztem `ViewTree*Owner` + `SavedStateRegistry` | Empfehlung: klassische Views fürs Overlay im MVP |
| Doppelte/hängende Overlay-Views | mittel | hoch | Alle `addView/removeView/updateViewLayout` nur über `OverlayController`; Attached-State prüfen; bei Stop alle Views entfernen | Bereits als Pflicht dokumentiert — strikt einhalten |
| Drag-vs-Tap-Verwechslung | mittel | mittel | `ViewConfiguration` touch slop + Zeit-/Distanzschwelle; Drag unterdrückt Tap | Standardlösung, in Phase 3 fest einbauen |
| Android 15/16 Background-Start-Regeln | mittel | mittel | Overlay-Start nur aus sichtbarer Nutzeraktion; kein WorkManager/Job aus dem Overlay-Service | Bereits korrekt vorgegeben |
| FGS-Typ ab targetSdk 34+ | mittel | mittel | Passenden Service-Typ deklarieren (voraussichtlich `specialUse` mit Begründung); Verhalten auf Gerät prüfen | In Phase 4 klären, dokumentieren |
| `UsageStatsManager.queryEvents()` Latenz/Akku bei 1000 ms-Polling | mittel | niedrig-mittel | Intervall 1000 ms Start, min. 500 ms; Verzögerung akzeptieren, nicht mit Accessibility umgehen | Bereits korrekt vorgegeben |
| Samsung-/Akkuoptimierung stoppt Dienst nach Sperren | mittel-hoch | mittel | Einschränkung dokumentieren; „Akku-Optimierung ignorieren"-Hinweis; manuelle Reaktivierung; kein Autostart-Hack | Bereits korrekt; ehrlich als Limit ausweisen |
| API-Key im DataStore im Klartext | mittel | mittel | Nicht loggen/committen/anzeigen; Keystore/verschlüsselt später | DataStore für privaten MVP akzeptabel |
| Logging sensibler Inhalte | niedrig | hoch | Nur Statusflags loggen (`overlay_visible`, `ai_request_failed=http_429`), nie Texte/Key | Bereits klar geregelt |
| MainActivity vs. Overlay Verantwortungen | niedrig | niedrig | MainActivity = nur Setup; Overlay = nur Hilfsfenster | Trennung bereits sauber |
| Zu frühe Provider-Abstraktion / zu große Architektur | niedrig | mittel | Ein Provider, keine Repository/UseCase-Schicht ohne Nutzen | Bereits explizit verboten — gut abgesichert |

---

## 6. Geschärfter Phasenplan

Die vorhandene `docs/IMPLEMENTATION_PLAN.md` (Phasen 0–9) deckt sich **exakt** mit der geforderten Reihenfolge (Projektbasis prüfen → Grundprojekt → Settings/Berechtigungen → manuelles Overlay → WhatsApp-Erkennung → Mini-Fenster ohne KI → PromptBuilder/Parser → KI-Anbindung → Gerätetest → README/Übergabe). **Keine Umsortierung nötig.** Zwei Schärfungen: (a) Phase 0 wird zum **Entscheidungs-Gate**, (b) **Gerätetest wird nach Phase 4/5 vorgezogen** statt nur am Ende.

### Phase 0 — Projektprüfung + Entscheidungs-Gate
**Ziel:** Repo verstanden, 4 offene Grundsatzentscheidungen fixiert.
**Aufgaben:** Dateien/Branch/Status prüfen (erledigt in diesem Audit); **entscheiden & notieren:** (1) KI-Provider + Default-Modell, (2) FGS-Strategie, (3) Overlay-UI-Technik (Views vs. ComposeView), (4) `applicationId` + Toolchain-Versionen.
**Nicht tun:** kein Code, keine Dependencies, Scope nicht erweitern.
**Akzeptanzkriterien:** 4 Entscheidungen schriftlich fixiert; kein Widerspruch zu `AGENTS.md`/`ANDROID_CONSTRAINTS.md`.
**Validierung:** Review der Entscheidungen gegen MVP-Grenzen.
**Abbruchbedingungen:** Eine Entscheidung verletzt eine Nicht-Ziel-Grenze → stoppen, Rückfrage.

### Phase 1 — Android-Projektbasis
**Ziel:** minimale App baut und startet. **Dateien:** `settings.gradle.kts`, Root-`build.gradle.kts`, `app/build.gradle.kts`, `AndroidManifest.xml`, `MainActivity.kt`, Theme.
**Aufgaben:** Kotlin+Compose-Scaffold; Package-Struktur laut `ARCHITECTURE.md`; App-Name „ChatHilfe"; dunkles Basis-Theme; `compileSdk 36`/`targetSdk 35`/`minSdk 29`.
**Nicht tun:** kein Overlay, keine KI, kein Service, keine Zusatz-Dependencies.
**Akzeptanzkriterien:** `./gradlew assembleDebug` grün; App startet; MainActivity zeigt Setup-Platzhalter.
**Validierung:** Build lokal ausführen (nur echte Ergebnisse behaupten).
**Typische Fehler:** AGP/Kotlin/Compose-Versionsmismatch; falscher `applicationId`.
**Abbruchbedingungen:** Build nicht reproduzierbar grün.

### Phase 2 — Settings & Berechtigungen
**Ziel:** Nutzer sieht, was fehlt. **Dateien:** `settings/SettingsScreen.kt`, `SettingsStore.kt`, `PermissionStatus.kt`.
**Aufgaben:** Overlay-Permission (`canDrawOverlays`) + Usage Access prüfen; Einstellungsseiten öffnen (`ACTION_MANAGE_OVERLAY_PERMISSION`, Usage-Access-Intent); DataStore für `apiKey`, `isOverlayEnabled`, `preferredTone`; Statuskarten.
**Nicht tun:** Key nie loggen; keine verbotenen Permissions.
**Akzeptanzkriterien:** Status korrekt; Key speicherbar; kein Key im Log.
**Validierung:** Unit-Test SettingsStore möglich; Permission-Flow auf Gerät.
**Typische Fehler:** Statuskarte aktualisiert nicht nach Rückkehr aus Settings (onResume-Refresh vergessen).
**Abbruchbedingungen:** Key landet im Log/Crashreport.

### Phase 3 — Manuelles Overlay
**Ziel:** Floating Button manuell testbar. **Dateien:** `overlay/OverlayController.kt`, `OverlayService.kt`, `FloatingBubbleView`, `OverlayPositionStore.kt`.
**Aufgaben:** `TYPE_APPLICATION_OVERLAY`; alle WindowManager-Ops nur im Controller; Start aus Nutzeraktion via FGS (`startForeground()` sofort); Dragging + Tap/Drag-Trennung; Position speichern; sauberes Entfernen bei Stop.
**Nicht tun:** keine WhatsApp-Erkennung, keine KI, kein Clipboard.
**Akzeptanzkriterien:** Button über Apps sichtbar; verschiebbar; keine doppelten Buttons; deaktivierbar.
**Validierung:** **Gerätetest Pflicht** (Overlay ist nicht per Unit-Test prüfbar).
**Typische Fehler:** doppelte Views; `ComposeView`-Crash ohne Lifecycle-Owner; Drag löst Tap aus; Overlay bleibt nach Stop hängen.
**Abbruchbedingungen:** Overlay-Berechtigung fehlt und wird nicht sauber abgefangen.

### Phase 4 — WhatsApp-Erkennung
**Ziel:** Bubble nur bei WhatsApp. **Dateien:** `detection/ForegroundAppDetector.kt`.
**Aufgaben:** `UsageStatsManager.queryEvents()`; `com.whatsapp` erkennen; Polling 1000 ms; Button zeigen/verstecken; fehlenden Usage Access melden; FGS-Typ verifizieren.
**Nicht tun:** kein Accessibility-Fallback, kein Notification Listener.
**Akzeptanzkriterien:** WhatsApp auf → Button; weg → Button verschwindet; keine doppelten Views.
**Validierung:** **Gerätetest Pflicht**; App-Wechsel mehrfach.
**Typische Fehler:** Erkennung reagiert nach Sperren/Entsperren nicht mehr (Polling gestoppt); Latenz als Bug missverstanden.
**Abbruchbedingungen:** Dienst wird nach Sperren dauerhaft gekillt und nicht reaktivierbar → als Limit dokumentieren, nicht mit Hacks umgehen.

### Phase 5 — ReplyPanel ohne KI
**Ziel:** UI mit Dummy-Daten. **Dateien:** `overlay/ReplyPanelView`.
**Aufgaben:** Modi Antworten/Formulieren/Umschreiben; Ton-Chips; Eingabefelder; **Clipboard nur bei Panel-Öffnung/Tap lesen, erst nach „Verwenden" übernehmen**; Dummy-Vorschläge; Kopieren.
**Nicht tun:** kein Hintergrund-Clipboard, keine Speicherung von Texten, keine KI.
**Akzeptanzkriterien:** Panel öffnet/schließt; Modus/Ton wählbar; Clipboard nicht heimlich; Vorschläge kopierbar.
**Validierung:** **Clipboard-Verhalten auf Gerät Pflicht** (Fokus-Restriktion!); Fallback „manuell einfügen" testen.
**Typische Fehler:** `getPrimaryClip()` liefert leer aus Overlay; Panel verdeckt WhatsApp komplett; Eingaben werden persistiert.
**Abbruchbedingungen:** Clipboard aus Overlay technisch nicht lesbar → auf manuelles Einfügen umstellen (im Scope).

### Phase 6 — PromptBuilder & Parser
**Ziel:** KI-Logik ohne Provider testbar. **Dateien:** `ai/PromptBuilder.kt`, `AiResponseParser.kt`, `model/*`.
**Aufgaben:** Prompts 1:1 aus `docs/PROMPTS.md`; Parser tolerant (`1.`/`1)`/`-`/3 Absätze); Unit-Tests.
**Nicht tun:** kein Netzwerk, keine Provider-Abstraktion.
**Akzeptanzkriterien:** jeder Modus → korrekter Prompt; Parser robust; kein Crash bei schlechter Antwort.
**Validierung:** **Unit-Tests Pflicht** (`./gradlew test`) — voll automatisierbar.
**Typische Fehler:** Parser crasht bei <3 Varianten statt Teilmenge anzuzeigen.
**Abbruchbedingungen:** —

### Phase 7 — KI-Anbindung
**Ziel:** echte Vorschläge. **Dateien:** `ai/AiClient.kt`, `AiConfig.kt`.
**Aufgaben:** **ein** in Phase 0 gewählter Provider + Modell; Key aus DataStore; Ladezustand; Fehlerfälle (Key fehlt/kein Netz/Rate Limit/leere Antwort); Antwort parsen → 3 Vorschläge.
**Nicht tun:** kein Multi-Provider, kein Verlauf, keine automatische Anfrage.
**Akzeptanzkriterien:** klare Meldungen bei Fehlern; gültige Anfrage → Vorschläge; keine Texte/Keys im Log; Anfrage nur per Button.
**Validierung:** Fehlerpfade + ein echter Call mit gültigem Key auf Gerät.
**Typische Fehler:** Key/Prompt landet im Log; Timeout ohne UI-Feedback.
**Abbruchbedingungen:** Provider-Wahl aus Phase 0 fehlt.

### Phase 8 — Stabilisierung auf Gerät
**Ziel:** private APK real nutzbar. **Aufgaben:** APK auf Samsung S25; Overlay/Usage/App-Wechsel/Sperren-Entsperren/Internetfehler/fehlende Berechtigungen; Akkuoptimierung dokumentieren.
**Akzeptanzkriterien:** `TEST_PLAN.md` weitgehend erfüllt; keine verbotenen Permissions; kein Accessibility.
**Validierung:** Testbericht im `TEST_PLAN.md`-Format; nur echte Ergebnisse.
**Abbruchbedingungen:** reproduzierbarer Crash bei Sperren/Entsperren.

### Phase 9 — Übergabe
**Ziel:** Repo weiter nutzbar. **Aufgaben:** README/Build-Befehle/bekannte Limits/Teststatus aktualisieren; Abschlussformat aus `AGENTS.md`.

---

## 7. Erste Umsetzungstickets

### Ticket 1 — Grundsatzentscheidungen fixieren (Entscheidungs-Gate)
**Ziel:** Vier offene Entscheidungen verbindlich festlegen, bevor Code entsteht.
**Kontext:** Docs lassen KI-Provider, FGS-Strategie, Overlay-UI-Technik, `applicationId`/Toolchain offen. Ohne Fixierung scaffoldet ein Agent inkonsistent.
**Dateien/Bereiche:** nur Doku (Vorschlag: kurze Ergänzung in `docs/ARCHITECTURE.md` bzw. `README.md`).
**Aufgaben:** Provider + Default-Modell wählen; „Overlay-Laufzeit läuft in FGS aus Nutzeraktion" festschreiben; „Overlay = klassische Views" (empfohlen) festschreiben; `applicationId`, `compileSdk 36`/`targetSdk 35`/`minSdk 29` + AGP/Kotlin/Compose-BOM benennen.
**Nicht tun:** kein Multi-Provider; keine Accessibility/Automation; kein Scope-Wachstum.
**Akzeptanzkriterien:** alle vier schriftlich; kein Konflikt zu `AGENTS.md`.
**Validierung:** Abgleich gegen §Verifikation der MVP-Grenzen.
**Risiken:** falsche Provider/Toolchain-Wahl → früh, billig korrigierbar.
**Abschlussausgabe:** Summary / Files changed / Validation / Not validated / Risks / Next sensible step.

### Ticket 2 — Android-Projektbasis (Gradle + MainActivity)
**Ziel:** `assembleDebug` grün, App startet mit Setup-Platzhalter.
**Kontext:** Kein Projektgerüst vorhanden (Phase 1).
**Dateien/Bereiche:** Gradle-Dateien, `AndroidManifest.xml`, `MainActivity.kt`, Theme, Package-Struktur laut `ARCHITECTURE.md`.
**Aufgaben:** Compose-Scaffold; dunkles Theme; SDK/Toolchain aus Ticket 1; nur `INTERNET` initial im Manifest.
**Nicht tun:** kein Overlay/Service/KI; keine unnötigen Dependencies.
**Akzeptanzkriterien:** Build grün; App startet ohne Crash.
**Validierung:** `./gradlew assembleDebug` real ausführen.
**Risiken:** Versionsmismatch.
**Abschlussausgabe:** Standardformat.

### Ticket 3 — SettingsStore (DataStore) + API-Key
**Ziel:** `apiKey`/`isOverlayEnabled`/`preferredTone` persistierbar, Key nie geloggt.
**Dateien/Bereiche:** `settings/SettingsStore.kt` (+ Test).
**Aufgaben:** DataStore-Keys; Key speichern/lesen; Logging-Verbot einhalten.
**Nicht tun:** keine Chat-/Vorschlag-/Clipboard-Persistenz.
**Akzeptanzkriterien:** Werte überleben Neustart; kein Key im Log.
**Validierung:** Unit-Test für Read/Write.
**Risiken:** Key im Klartext (für MVP akzeptabel, Keystore später).
**Abschlussausgabe:** Standardformat.

### Ticket 4 — Permission-Status-UI (Overlay + Usage Access)
**Ziel:** Nutzer sieht fehlende Berechtigungen und kann Settings öffnen.
**Dateien/Bereiche:** `settings/PermissionStatus.kt`, `SettingsScreen.kt`.
**Aufgaben:** `canDrawOverlays()` + Usage-Access prüfen; Intents zu Settings; Statuskarten (OK/fehlt/optional); `onResume`-Refresh.
**Nicht tun:** keine verbotenen Permissions.
**Akzeptanzkriterien:** Status korrekt vor/nach Grant; kein Crash.
**Validierung:** Gerätetest (Permission-Flow).
**Risiken:** Status aktualisiert nicht nach Rückkehr.
**Abschlussausgabe:** Standardformat.

### Ticket 5 — OverlayController + FloatingBubble (manuell)
**Ziel:** Verschiebbarer Floating Button über Apps, manuell an/aus.
**Dateien/Bereiche:** `overlay/OverlayController.kt`, `OverlayService.kt`, `FloatingBubbleView`, `OverlayPositionStore.kt`.
**Aufgaben:** `TYPE_APPLICATION_OVERLAY`; alle WindowManager-Ops im Controller; FGS-Host aus Nutzeraktion; sauberes add/remove mit Attached-Check.
**Nicht tun:** keine WhatsApp-Erkennung/KI/Clipboard.
**Akzeptanzkriterien:** Button sichtbar/verschiebbar; keine doppelten Views; entfernbar.
**Validierung:** **Gerätetest Pflicht.**
**Risiken:** doppelte/hängende Views; ComposeView-Crash.
**Abschlussausgabe:** Standardformat.

### Ticket 6 — Drag/Tap-Trennung + Position speichern
**Ziel:** Ziehen verschiebt, Tippen öffnet (später Panel); Position bleibt erhalten.
**Dateien/Bereiche:** `FloatingBubbleView`, `OverlayPositionStore.kt`.
**Aufgaben:** touch slop/Zeit-Schwelle; Rand-Andocken; `bubbleX/bubbleY` speichern.
**Nicht tun:** kein Panel-Inhalt, keine KI.
**Akzeptanzkriterien:** Drag löst keinen Tap aus; Position überlebt Neustart.
**Validierung:** **Gerätetest Pflicht.**
**Risiken:** Fehlkalibrierte Schwelle.
**Abschlussausgabe:** Standardformat.

### Ticket 7 — ForegroundAppDetector (WhatsApp-Erkennung)
**Ziel:** Bubble nur bei `com.whatsapp`.
**Dateien/Bereiche:** `detection/ForegroundAppDetector.kt`, Verdrahtung mit Controller.
**Aufgaben:** `queryEvents()`-Polling 1000 ms; Bubble zeigen/verstecken; fehlenden Usage Access melden; FGS-Typ verifizieren.
**Nicht tun:** kein Accessibility/Notification Listener.
**Akzeptanzkriterien:** WhatsApp auf → Bubble; weg → Bubble weg; keine doppelten Views.
**Validierung:** **Gerätetest Pflicht** (App-Wechsel, Sperren/Entsperren).
**Risiken:** Polling stoppt nach Sperren; Latenz.
**Abschlussausgabe:** Standardformat.

### Ticket 8 — ReplyPanel-Gerüst ohne KI
**Ziel:** Panel mit Modi/Ton/Eingabe + bewusstem Clipboard + Dummy-Vorschlägen.
**Dateien/Bereiche:** `overlay/ReplyPanelView`.
**Aufgaben:** Modi/Ton-Chips/Eingabefelder laut `UI_UX_SPEC.md`; Clipboard nur bei Öffnung/Tap lesen, erst nach „Verwenden" nutzen; Dummy-Vorschläge + Kopieren; **Fallback manuelles Einfügen**.
**Nicht tun:** kein Hintergrund-Clipboard, keine Persistenz von Texten, keine KI.
**Akzeptanzkriterien:** Panel öffnet/schließt; Modus/Ton wählbar; Clipboard freiwillig; kopierbar.
**Validierung:** **Clipboard-Read auf Gerät Pflicht** (Fokus-Restriktion).
**Risiken:** `getPrimaryClip()` leer aus Overlay → Fallback nutzen.
**Abschlussausgabe:** Standardformat.

---

## 8. Modell-/Agenten-Empfehlung

- **Fable (planen/reviewen):** die drei harten Android-Runtime-Entscheidungen — (1) FGS-/Service-Lifecycle-Strategie inkl. Service-Typ ab targetSdk 34+, (2) Overlay-UI-Technik (klassische Views vs. `ComposeView` mit Lifecycle-Ownern), (3) Clipboard-aus-Overlay-Strategie + Fallback. Zusätzlich Review von Phase 3–5 gegen Android-15/16-Verhalten und gegen die MVP-Grenzen.
- **Sonnet (umsetzen):** die mechanischen, gut spezifizierten Tickets — Gradle-Scaffold (T2), DataStore/SettingsStore (T3), Permission-UI (T4), `PromptBuilder`/`AiResponseParser` + Unit-Tests (Phase 6), `AiClient` (Phase 7). Diese sind determiniert und teilweise per Unit-Test validierbar.
- **Mensch (zwingender Gerätetest):** alles Overlay-, UsageStats-, Clipboard- und Samsung-Akku-bezogene — Tickets 5–8 und Phase 8. Overlay-Lifecycle, WhatsApp-Erkennung, Clipboard-Lesen aus dem Overlay, Verhalten bei Sperren/Entsperren und Batterie-Kills sind **nicht** ohne echtes Samsung-S25-Gerät verifizierbar. Kein Agent darf hier Geräteerfolg behaupten.

---

## 9. Finale Empfehlung

- Die Dokumentation ist stark genug, um direkt umzusetzen — **das Nadelöhr ist nicht Planung, sondern Android-Laufzeitverhalten**. Vor dem ersten Commit die **vier Entscheidungen** aus §3/Ticket 1 fixieren; damit verschwinden die meisten Blocker.
- **Phasenreihenfolge der bestehenden `IMPLEMENTATION_PLAN.md` unverändert übernehmen.** Sie entspricht exakt der geforderten Sequenz. Einzige Schärfungen: Entscheidungs-Gate in Phase 0, und **Gerätetest früh** (nach Overlay/Erkennung, Phase 3–5), nicht erst in Phase 8.
- **Drei Themen früh auf echtem Gerät verifizieren**, bevor darauf aufgebaut wird: Clipboard-Lesen aus dem Overlay, FGS-Überleben bei Samsung, `ComposeView`/Overlay-Rendering. Alle drei können sonst spät teure Rückbauten erzwingen.
- **Alle MVP-Grenzen eingehalten:** keine WhatsApp-Automation, kein Auto-Lesen/-Einfügen/-Senden, kein Accessibility/Notification Listener/Screen Scraping, kein Hintergrund-Clipboard, keine Message-Persistenz, ein KI-Provider, `TYPE_APPLICATION_OVERLAY`, Overlay- und KI-Start nur aus Nutzeraktion. Der empfohlene Clipboard-Fallback (manuelles Einfügen) bleibt innerhalb dieser Grenzen.
- **Nächster sinnvoller Schritt:** Ticket 1 (Entscheidungen fixieren) → Ticket 2 (Scaffold) durch einen Coding-Agenten; danach linear T3–T8.

> Es wurde kein Anwendungscode umgesetzt, keine bestehende Datei geändert, kein Scope erweitert und keine verbotene WhatsApp-Automation empfohlen. Dieses Audit ist das vollständige Deliverable; committet wird ausschließlich dieses Dokument.
