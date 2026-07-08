# Implementations

Dieses Dokument sammelt technische Umsetzungsentscheidungen, die nicht als große Produktfeatures missverstanden werden sollen.

## WhatsApp-Dialogblöcke als Kontext erkennen

Referenz-Issue: #19 — Mehrere kopierte WhatsApp-Nachrichten als Dialog-Kontext erkennen

### Ziel

Wenn der Nutzer mehrere WhatsApp-Nachrichten kopiert und in ChatHilfe einfügt, soll die App den Text automatisch als Dialog/Chatverlauf erkennen und daraus besseren Antwortkontext für die KI bauen.

Die Funktion soll helfen, den aktuellen Antwortanlass sauber zu erkennen, ohne daraus ein gespeichertes Chatverlauf-, Profil- oder Import-System zu machen.

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
