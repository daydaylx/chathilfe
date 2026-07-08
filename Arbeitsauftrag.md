# Arbeitsauftrag.md — Startauftrag für externe Coding-Agenten

## Einordnung

Dieses Dokument ist ein direkt nutzbarer Startauftrag für einen externen Coding-Agenten.

Für dauerhafte Repo-Regeln gelten vorrangig:

1. [`AGENTS.md`](AGENTS.md)
2. [`CLAUDE.md`](CLAUDE.md), wenn Claude Code genutzt wird
3. [`Konzept.md`](Konzept.md)
4. [`docs/DECISIONS.md`](docs/DECISIONS.md)
5. [`docs/AGENT_MODEL_POLICY.md`](docs/AGENT_MODEL_POLICY.md)
6. [`docs/PROMPT_PARAMETER_POLICY.md`](docs/PROMPT_PARAMETER_POLICY.md)
7. [`docs/DEVICE_TEST_POLICY.md`](docs/DEVICE_TEST_POLICY.md)
8. [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
9. [`docs/ANDROID_CONSTRAINTS.md`](docs/ANDROID_CONSTRAINTS.md)
10. [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md)
11. taskrelevante Fachdocs in [`docs/`](docs/)

Wenn dieser Arbeitsauftrag einer Fachdatei widerspricht, gilt die Fachdatei.

---

## Rolle

Du bist ein erfahrener Android-Entwickler mit Fokus auf Kotlin, Jetpack Compose, Android-Systemberechtigungen, Overlays, Datenschutz, sauberer App-Struktur und pragmatischer MVP-Umsetzung.

Arbeite kritisch. Baue keinen unnötigen Ballast. Halte dich strikt an den MVP-Scope.

---

## Modell- und Thinking-Hinweis

Für Coding-Agenten gelten zusätzlich die Regeln aus `docs/AGENT_MODEL_POLICY.md`.

Empfohlen:

- Claude Sonnet 5: `high` für normale Umsetzung, `xhigh` für Architektur, Android-Lifecycle, Datenschutz, Berechtigungen, Security, Multi-Datei-Refactors und harte Fehlersuche.
- GLM-5.2: Max-Effort für lange oder riskante Coding-Aufgaben; High nur bewusst für kleinere oder latenzsensiblere Teilaufgaben.
- Keine non-default `temperature`, `top_p` oder `top_k` mit Claude Sonnet 5 verwenden.
- Modellregeln gelten nur für Coding-Agenten, nicht als App-Feature.

Für Prompt- und Providerparameter gilt zusätzlich `docs/PROMPT_PARAMETER_POLICY.md`.

---

## Ziel

Erstelle oder erweitere eine private Android-App als MVP.

Die App soll über WhatsApp als schwebender Formulierungshelfer funktionieren:

- Wenn WhatsApp geöffnet ist, erscheint ein kleiner Floating Button.
- Beim Antippen öffnet sich zuerst eine schmale Input-Bar.
- Die Input-Bar enthält Ton/Stil, Texteingabe, Einfügen und Start.
- Der Start-Button darf nicht `Senden` heißen.
- Optional kann eine kopierte Nachricht bewusst eingefügt werden.
- Falls Clipboard nicht lesbar ist, kann der Nutzer Text manuell eingeben oder einfügen.
- Der Nutzer beschreibt grob, was er sagen möchte.
- Die KI erzeugt 3 Antwortvorschläge.
- Nach der KI-Antwort erscheint ein kompaktes Result-Panel.
- Das Result-Panel zeigt immer nur einen Vorschlag.
- Der Nutzer wechselt zwischen 3 Vorschlägen per Swipe, Pfeil oder Pager.
- Der Nutzer kann den sichtbaren Vorschlag kopieren.
- Der Nutzer fügt den Text selbst in WhatsApp ein und sendet selbst.
- Retry ist erlaubt über `Nochmal` und kompakte temporäre Änderungs-Chips.

---

## Harte Nicht-Ziele

Baue ausdrücklich nicht:

- kein automatisches Auslesen von WhatsApp-Chats
- kein Lesen vollständiger Chatverläufe
- kein Zugriff auf Kontakte
- kein automatisches Einfügen in WhatsApp
- kein automatisches Senden
- keine Accessibility-basierte WhatsApp-Steuerung
- kein Notification Scraping
- kein Screen Scraping
- kein Account-System
- keine Cloud-Speicherung
- kein Play-Store-Release-Setup
- kein Multi-App-Support
- keine überdimensionierte Architektur
- keine API-Key-Eingabe im UI
- kein Modellrouting im MVP
- keine Modell- oder Provider-Auswahl im Overlay
- kein Verlauf
- kein Gedächtnis
- kein Stiltraining
- keine Profile
- kein großes Formular als Startzustand
- keine drei Vorschläge untereinander als Standardansicht

Wenn eine Funktion eines dieser Themen benötigt, stoppe und erkläre die Konsequenz.

---

## Technische Entscheidungen

Übernehme `docs/DECISIONS.md` verbindlich.

Kurzfassung:

- Provider: OpenRouter im MVP
- App-Modellstrategie: genau ein OpenRouter-Default-Modell im MVP, vor Phase 7 pinnen
- Overlay-Laufzeit: Foreground Service aus sichtbarer Nutzeraktion
- MainActivity: Jetpack Compose
- Overlay Bubble, Input-Bar und Result-Panel: klassische Android Views
- `applicationId`: `de.disaai.chathilfe`
- SDK-Basis: `compileSdk 37`, `targetSdk 35`, `minSdk 29`
- Clipboard-Fallback: manuelles Eingeben oder Einfügen im Overlay
- API-Key: lokaler Build-Time-Key, kein UI-Feld, nicht in DataStore
- Gerätetest: gesammelt in Phase 8, nicht als Zwischen-Gate

---

## Vorgehen

Arbeite strikt nach [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md).

Aktuelle Kurzfassung:

1. Repo prüfen
2. offene Toolchain-Details und aktuelle Entscheidungen prüfen
3. Android-Projektbasis prüfen oder anlegen
4. Settings und Berechtigungen bauen
5. Foreground Service + manuelles Overlay bauen
6. WhatsApp-Erkennung bauen
7. Input-Bar und Result-Panel ohne KI bauen
8. PromptBuilder und Parser bauen
9. KI-Anbindung bauen
10. Stabilisierung und gebündelter Gerätetest in Phase 8
11. README und Teststatus aktualisieren

Keine Phase überspringen, wenn dadurch ungetestete oder unklare Grundfunktionalität verdeckt wird.

---

## Zusatzauftrag: WhatsApp-Dialogblöcke als Kontext erkennen

Referenz-Issue: #19 — Mehrere kopierte WhatsApp-Nachrichten als Dialog-Kontext erkennen

### Ziel

Wenn der Nutzer mehrere WhatsApp-Nachrichten kopiert und in ChatHilfe einfügt, soll die App den Text automatisch als Dialog/Chatverlauf erkennen und daraus besseren Antwortkontext für die KI bauen.

Die Funktion soll nur helfen, den aktuellen Antwortanlass sauber zu erkennen. Sie darf nicht zu einem gespeicherten Chatverlauf-, Profil-, Import- oder WhatsApp-Auslese-System werden.

### Aufwand/Nutzen-Entscheidung

Diese Funktion ist sinnvoll, aber nur in einer schlanken Umsetzung.

Bewertung:

```text
Nutzen:      7/10
Aufwand MVP: 4–5/10
Risiko:      mittel
Empfehlung:  ja, aber nur als Parser + Prompt-Kontext + Tests
```

Die Verbesserung entsteht dadurch, dass die KI nicht mehr einen rohen, langen WhatsApp-Block interpretieren muss. Stattdessen bekommt sie strukturierte Felder:

```text
Bisheriger Chatverlauf:
...

Aktuelle Nachricht, auf die geantwortet werden soll:
...

Was der Nutzer ausdrücken will:
...
```

### Nicht als großes Feature bauen

Nicht umsetzen:

- kein Chatverlauf-Import
- kein Accessibility Service
- kein automatisches WhatsApp-Lesen
- kein Hintergrund-Monitoring
- kein Kontaktprofil
- kein Beziehungsprofil
- kein Verlauf/Gedächtnis in der App
- keine Speicherung von Chattexten
- kein neues großes UI-Menü
- keine automatische Sprecheridentität mit harter Behauptung

Diese Funktion bleibt eine temporäre Verarbeitung des vom Nutzer bewusst eingefügten Textes.

### Empfohlene Architektur

Die Verarbeitung soll als kleine Parser-Schicht zwischen InputBar/OverlayService und PromptBuilder liegen:

```text
Eingefügter Text
↓
WhatsAppChatParser
↓
ParsedChatContext
↓
ReplyRequest mit conversationContext + latestOtherMessage
↓
PromptBuilder
↓
KI
```

Die KI soll nicht allein aus dem rohen WhatsApp-Block raten, wer spricht und worauf geantwortet werden soll. Der Kotlin-Code soll vor dem Prompt eine einfache Struktur herstellen.

### Neue Parser-Schicht

Neue pure Kotlin-Datei, ohne Android-Abhängigkeiten:

```text
app/src/main/java/de/disaai/chathilfe/chat/WhatsAppChatParser.kt
```

Mögliche Datenmodelle:

```kotlin
data class ParsedChatMessage(
    val rawDate: String,
    val rawTime: String,
    val sender: String,
    val text: String,
)

data class ParsedChatContext(
    val messages: List<ParsedChatMessage>,
    val likelySelfSender: String?,
    val likelyOtherSender: String?,
    val latestOtherMessage: ParsedChatMessage?,
)
```

Die Modelle dürfen nicht persistiert werden. Sie gelten nur für die aktuelle Anfrage.

### Erkennung

Der Parser soll nur aktiv werden, wenn mindestens zwei passende WhatsApp-Zeilen erkannt werden. Dadurch wird verhindert, dass normale Einzeltexte fälschlich als Chatverlauf behandelt werden.

Erkennbares Grundmuster:

```text
[Datum, Uhrzeit] Sprecher: Nachricht
```

Beispiele:

```text
[1.7., 18:02] D: Text
[01.07.26, 18:02] Name: Text
[1.7.2026, 18:02:33] Name: Text
```

Mögliche Regex-Grundlage:

```text
^\[(.+?),\s*(\d{1,2}:\d{2}(?::\d{2})?)\]\s*([^:]+):\s*(.*)$
```

Die Erkennung soll tolerant sein, aber nicht aggressiv beliebigen Text als WhatsApp-Chat klassifizieren.

### Mehrzeilige Nachrichten

WhatsApp-Nachrichten können mehrzeilig sein.

Regel:

- Eine neue Nachricht beginnt nur, wenn eine Zeile dem WhatsApp-Muster entspricht.
- Freie Zeilen nach einer erkannten Nachricht werden an die vorherige Nachricht angehängt.
- Wenn vor der ersten erkannten Nachricht freie Zeilen stehen, entweder ignorieren oder kontrolliert auf Einzeltext-Fallback gehen.
- Kaputte Blöcke dürfen nicht crashen.

### Sprecher-Heuristik

Die App weiß nicht sicher, wer der Nutzer ist. Deshalb darf die Sprechererkennung nur vorsichtig arbeiten.

MVP-Heuristik:

- Wenn genau zwei Sprecher vorkommen und einer sehr kurz/eigen wirkt, z. B. `D`, `Ich`, Initialen, kann dieser als `likelySelfSender` markiert werden.
- Der andere Sprecher kann dann `likelyOtherSender` sein.
- Wenn die Zuordnung unsicher ist, keine harte Behauptung treffen.

Wichtig:

- Feldname bewusst `likelySelfSender`, nicht `selfSender`.
- Keine Namen speichern.
- Keine Nutzerprofile daraus bauen.

### Antwortanlass bestimmen

Wenn ein Dialogblock erkannt wurde:

- letzte relevante Nachricht des Gegenübers als aktuelle Nachricht nutzen
- vorherige Nachrichten als kompakten Verlaufskontext übergeben
- eigene letzte Antwort berücksichtigen, damit die KI nicht redundant antwortet
- bei Themenwechseln die letzte Nachricht priorisieren

Beispiel:

```text
[1.7., 18:02] D: Hey wie arbeitest du morgen?
[1.7., 19:11] Anke Grunerr: Ich bin morgen zur Trauerfeier von Marco seiner mama
[1.7., 22:16] D: Ach mist müssen morgen nämlich zur nach Untersuchung der Katzen weil die gestern kastriert wurden und weiß nicht ob die Bahn wieder fahren
[1.7., 22:17] Anke Grunerr: Strassenbahn fährt im moment von taucha nach paunsdorf
[1.7., 22:18] Anke Grunerr: Sbahn fährt
[1.7., 22:43] D: Das reicht mir ka
[3.7., 16:16] Anke Grunerr: Ihr habt noch eine kühltasche mit essen im garten
```

Erwartung:

- Dialog erkannt.
- Sprecher erkannt: `D`, `Anke Grunerr`.
- Letzte Nachricht von `Anke Grunerr` ist aktueller Antwortanlass.
- Katzen/Bahn/Trauerfeier sind nur Verlaufskontext.
- Die KI soll nicht auf jede alte Nachricht einzeln antworten.

### ReplyRequest-Erweiterung

`ReplyRequest` sollte ein optionales Kontextfeld erhalten:

```kotlin
val conversationContext: String? = null
```

Bei erkanntem Dialog:

```kotlin
ReplyRequest(
    mode = ReplyMode.REPLY,
    copiedMessage = latestOtherMessage.text,
    userIntent = pendingReplyIntent.orEmpty(),
    tone = pendingTone,
    conversationContext = formattedContext,
)
```

Bei normalen Einzeltexten bleibt der bestehende Flow unverändert.

### PromptBuilder-Regeln

Wenn `conversationContext` vorhanden ist, soll der Prompt klar getrennte Abschnitte erhalten:

```text
Bisheriger Chatverlauf:
{{conversation_context}}

Aktuelle Nachricht, auf die geantwortet werden soll:
{{copied_message}}

Was der Nutzer ausdrücken will:
{{user_intent}}
```

Zusätzliche Regeln:

```text
- Nutze den Chatverlauf nur als Kontext.
- Antworte nicht auf jede alte Nachricht einzeln.
- Priorisiere die letzte relevante Nachricht des Gegenübers.
- Wenn im Verlauf ein Themenwechsel vorkommt, reagiere auf die aktuelle Nachricht.
- Keine Details erfinden.
```

### UI-Regel

Kein neuer großer Modus.

Der Nutzer bleibt im bestehenden Antwortmodus. Wenn ein WhatsApp-Dialogblock erkannt wird, kann optional ein kleiner Hinweis erscheinen:

```text
Chatverlauf erkannt
```

Mehr UI ist für das MVP nicht nötig.

### Umsetzung in kleinen Schritten

#### Schritt 1 — Parser + Tests

- `WhatsAppChatParser` anlegen
- `ParsedChatMessage` / `ParsedChatContext` anlegen
- Unit-Tests mit realistischem WhatsApp-Block
- Einzeltext-Fallback testen
- kaputtes/teilweise passendes Format testen

#### Schritt 2 — Request-/Prompt-Anbindung

- `ReplyRequest.conversationContext` ergänzen
- `PromptBuilder` um Kontextsektion erweitern
- Tests für Prompt mit Dialog-Kontext ergänzen
- Sicherstellen, dass normale Einzeltexte unverändert bleiben

#### Schritt 3 — Overlay-Integration

- In `OverlayService.buildRequest()` bei `ReplyMode.REPLY` den eingefügten Text prüfen
- Wenn Parser Dialog erkennt:
  - `latestOtherMessage.text` als `copiedMessage`
  - kompakten Verlauf als `conversationContext`
- Optional kleinen Hinweis in der InputBar anzeigen

### Abschlusskriterien

- WhatsApp-Dialogblock mit mehreren Nachrichten wird erkannt.
- Sprecher, Datum/Uhrzeit und Nachricht werden extrahiert.
- Letzte relevante Nachricht des Gegenübers wird als Antwortanlass genutzt, soweit heuristisch möglich.
- Alter Verlauf wird nur als Kontext genutzt.
- Normale Einzeltexte funktionieren unverändert.
- Keine Chattexte, Namen oder Vorschläge werden gespeichert oder geloggt.
- Parser- und PromptBuilder-Tests sind vorhanden.
- `./gradlew test`, `./gradlew lint` und `./gradlew assembleDebug` laufen oder blockierende Gründe sind dokumentiert.

---

## Änderungsregeln

- Vor Änderungen relevante Source-of-Truth-Dateien lesen.
- Änderungen klein und fokussiert halten.
- Keine neuen Produktionsdependencies ohne Begründung und Freigabe.
- Keine echten Secrets schreiben.
- Keine Nutzertexte, Clipboard-Texte, generierten Vorschläge oder Retry-Anweisungen speichern oder loggen.
- Keine verbotenen Android-Berechtigungen einführen.
- Keine Entscheidungen still ändern; bei Bedarf `docs/DECISIONS.md` aktualisieren.
- Visual Scope gegen `docs/VISUAL_SCOPE.md` prüfen.
- Device-Test-Flow gegen `docs/DEVICE_TEST_POLICY.md` prüfen.
- Provider- und Promptparameter gegen `docs/PROMPT_PARAMETER_POLICY.md` prüfen.

---

## Verifikation

Vor Abschluss prüfen:

- Build läuft, wenn Gradle-Projekt und Umgebung verfügbar sind
- relevante Tests laufen, wenn vorhanden
- Lint geprüft oder begründet nicht geprüft
- keine verbotenen Permissions im Manifest
- kein Accessibility Service
- keine WhatsApp-Automation
- kein Hintergrund-Clipboard-Monitoring
- keine Nutzertexte/API-Keys/Retry-Anweisungen in Logs
- Overlay erzeugt keine doppelten Views
- README und relevante Fachdocs sind aktuell, wenn die Änderung Doku betrifft

Geräteverhalten ehrlich als `nicht validiert` markieren, wenn kein echtes Gerät genutzt wurde. Bis Phase 8 bleibt echter Gerätetest ein offenes Risiko.

---

## Ausgabeformat

Am Ende liefern:

```text
Summary:
- ...

Files changed:
- ...

Validation:
- ...

Not validated:
- ...

Risks:
- ...

Next sensible step:
- ...
```

---

Schwierigkeiten: 6/10 | Thinking: high
