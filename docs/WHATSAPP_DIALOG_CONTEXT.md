# WHATSAPP_DIALOG_CONTEXT.md — Eingefügte WhatsApp-Dialogblöcke

## Zweck

Dieses Dokument beschreibt die schlanke Umsetzung für Issue #19: Mehrere kopierte WhatsApp-Nachrichten sollen als Dialog-Kontext erkannt werden.

Die Funktion soll die Antwortqualität verbessern, ohne ein Chatverlauf-, Profil-, Import- oder WhatsApp-Auslese-System zu bauen.

---

## Aufwand/Nutzen-Entscheidung

Bewertung:

```text
Nutzen:      7/10
Aufwand MVP: 4–5/10
Risiko:      mittel
Empfehlung:  ja, aber nur als Parser + Prompt-Kontext + Tests
```

Begründung:

- Der Nutzen entsteht, weil die KI nicht mehr aus einem rohen WhatsApp-Block erraten muss, wer spricht und worauf geantwortet werden soll.
- Der Aufwand bleibt sinnvoll, wenn die Umsetzung als kleiner Parser mit Prompt-Kontext erfolgt.
- Der Aufwand kippt, wenn daraus UI, Verlauf, Profile, Import oder automatische WhatsApp-Auslesung werden.

---

## Harte Grenzen

Nicht bauen:

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

Diese Funktion verarbeitet ausschließlich Text, den der Nutzer bewusst einfügt oder bestätigt.

---

## Architektur

Die Verarbeitung liegt als kleine Parser-Schicht zwischen InputBar/OverlayService und PromptBuilder:

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

Die KI soll nicht allein aus dem rohen WhatsApp-Block raten. Der Kotlin-Code soll vor dem Prompt eine einfache Struktur herstellen.

---

## Neue Parser-Schicht

Neue pure Kotlin-Datei ohne Android-Abhängigkeiten:

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

Die Modelle sind nur transient für die aktuelle Anfrage. Keine Persistenz, keine Logs.

---

## Erkennung

Der Parser soll nur aktiv werden, wenn mindestens zwei passende WhatsApp-Zeilen erkannt werden.

Grundmuster:

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

Regel:

- tolerant gegenüber WhatsApp-Datumsvarianten
- nicht aggressiv beliebigen Text als Chatverlauf klassifizieren
- normale Einzeltexte bleiben im bestehenden Flow

---

## Mehrzeilige Nachrichten

WhatsApp-Nachrichten können mehrzeilig sein.

Regel:

- Eine neue Nachricht beginnt nur, wenn eine Zeile dem WhatsApp-Muster entspricht.
- Freie Zeilen nach einer erkannten Nachricht werden an die vorherige Nachricht angehängt.
- Freie Zeilen vor der ersten erkannten Nachricht werden ignoriert oder führen kontrolliert zum Einzeltext-Fallback.
- Kaputte Blöcke dürfen nicht crashen.

---

## Sprecher-Heuristik

Die App weiß nicht sicher, wer der Nutzer ist. Deshalb darf die Sprechererkennung nur vorsichtig arbeiten.

MVP-Heuristik:

- Wenn genau zwei Sprecher vorkommen und einer sehr kurz/eigen wirkt, z. B. `D`, `Ich`, Initialen, kann dieser als `likelySelfSender` markiert werden.
- Der andere Sprecher kann dann `likelyOtherSender` sein.
- Wenn die Zuordnung unsicher ist, keine harte Behauptung treffen.

Wichtig:

- Feldname bewusst `likelySelfSender`, nicht `selfSender`.
- Keine Namen speichern.
- Keine Nutzerprofile daraus bauen.

---

## Antwortanlass bestimmen

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
- Die KI antwortet nicht auf jede alte Nachricht einzeln.

---

## ReplyRequest-Erweiterung

`ReplyRequest` erhält optional:

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

---

## PromptBuilder-Regeln

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

---

## UI-Regel

Kein neuer großer Modus.

Der Nutzer bleibt im bestehenden Antwortmodus. Wenn ein WhatsApp-Dialogblock erkannt wird, kann optional ein kleiner Hinweis erscheinen:

```text
Chatverlauf erkannt
```

Mehr UI ist für das MVP nicht nötig.

---

## Umsetzung in kleinen Schritten

### Schritt 1 — Parser + Tests

- `WhatsAppChatParser` anlegen
- `ParsedChatMessage` / `ParsedChatContext` anlegen
- Unit-Tests mit realistischem WhatsApp-Block
- Einzeltext-Fallback testen
- kaputtes/teilweise passendes Format testen

### Schritt 2 — Request-/Prompt-Anbindung

- `ReplyRequest.conversationContext` ergänzen
- `PromptBuilder` um Kontextsektion erweitern
- Tests für Prompt mit Dialog-Kontext ergänzen
- normale Einzeltexte unverändert halten

### Schritt 3 — Overlay-Integration

- In `OverlayService.buildRequest()` bei `ReplyMode.REPLY` den eingefügten Text prüfen
- Wenn Parser Dialog erkennt:
  - `latestOtherMessage.text` als `copiedMessage`
  - kompakten Verlauf als `conversationContext`
- Optional kleinen Hinweis in der InputBar anzeigen

---

## Abschlusskriterien

- WhatsApp-Dialogblock mit mehreren Nachrichten wird erkannt.
- Sprecher, Datum/Uhrzeit und Nachricht werden extrahiert.
- Letzte relevante Nachricht des Gegenübers wird als Antwortanlass genutzt, soweit heuristisch möglich.
- Alter Verlauf wird nur als Kontext genutzt.
- Normale Einzeltexte funktionieren unverändert.
- Keine Chattexte, Namen oder Vorschläge werden gespeichert oder geloggt.
- Parser- und PromptBuilder-Tests sind vorhanden.
- `./gradlew test`, `./gradlew lint` und `./gradlew assembleDebug` laufen oder blockierende Gründe sind dokumentiert.
