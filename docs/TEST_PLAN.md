# TEST_PLAN.md — ChatHilfe MVP

## Zweck

Dieses Dokument definiert die Tests für den ChatHilfe-MVP.

Viele Kernfunktionen sind Geräteverhalten und nicht zuverlässig nur per Unit-Test prüfbar: Overlay, Usage Access, Clipboard, App-Wechsel und Samsung-Hintergrundverhalten müssen auf echtem Gerät getestet werden.

---

## Testziele

Sicherstellen, dass:

- APK installierbar ist
- Berechtigungen korrekt geprüft werden
- Floating Button nur bei WhatsApp erscheint
- Overlay keine doppelten Views erzeugt
- Input-Bar kompakt öffnet
- Result-Panel erst nach KI-Antwort erscheint
- Vorschlagswechsel zwischen 3 Varianten funktioniert
- Clipboard nur nach Nutzeraktion verwendet wird
- bewusst eingefügte WhatsApp-Dialogblöcke nur temporär verarbeitet werden
- Retry-Bereich kompakt funktioniert und nichts speichert
- KI-Vorschläge erzeugt und kopiert werden können
- Fehler verständlich dargestellt werden
- keine verbotenen Funktionen eingebaut wurden

---

## Testumgebung

Primär:

- Samsung Galaxy S25
- Android 15 oder Android 16
- WhatsApp `com.whatsapp`
- Internet
- gültiger lokaler Build-Time-API-Key

Optional:

- Android Emulator für Basistests
- weiteres Android-Gerät

---

## Build-Tests

Sobald Gradle-Projekt existiert:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

Akzeptanz:

- `assembleDebug` erfolgreich
- Tests laufen, wenn vorhanden
- Lint-Fehler bewertet
- keine erfundenen Testergebnisse
- kein echter API-Key im Repo
- lokale Secret-Dateien werden ignoriert

---

## Secret-/API-Key-Test

Prüfen:

- App hat kein API-Key-Eingabefeld
- API-Key wird nicht in DataStore gespeichert
- API-Key kommt aus Build-Time-Konfiguration oder lokaler Environment-Variable
- fehlender Key erzeugt klare Fehlermeldung oder klaren Build-/Runtime-Fehler ohne Secret-Ausgabe
- README/Doku zeigen nur Platzhalter

Erwartet:

- kein echter API-Key im Repo
- kein echter API-Key in Logs
- kein API-Key im UI sichtbar

---

## Permission-/Manifest-Test

Erlaubte Permissions:

- `INTERNET`
- `SYSTEM_ALERT_WINDOW`
- `PACKAGE_USAGE_STATS`
- `POST_NOTIFICATIONS`, nur falls Foreground Service genutzt wird
- `FOREGROUND_SERVICE`, nur falls Foreground Service genutzt wird
- `FOREGROUND_SERVICE_SPECIAL_USE`, falls der Foreground-Service-Typ `specialUse` genutzt wird

Verboten:

- Kontakte
- SMS
- Kamera
- Mikrofon
- Standort
- Medienzugriff
- Accessibility Service
- Notification Listener
- Screen Capture

Akzeptanz:

- keine verbotenen Permissions
- kein Accessibility Service
- keine WhatsApp-Automation

---

## Installationstest

Schritte:

1. Debug-APK bauen.
2. APK installieren.
3. App öffnen.
4. Startscreen prüfen.

Erwartet:

- kein Crash
- Setup-Status sichtbar
- fehlende Berechtigungen verständlich
- API-Key-Status höchstens als Build-Time-Konfiguration sichtbar
- keine API-Key-Eingabe
- keine unnötigen Permission-Prompts

---

## Berechtigungstest

Overlay:

1. App öffnen.
2. Overlay-Status prüfen.
3. Einstellungsseite öffnen.
4. Berechtigung erlauben.
5. Zur App zurückkehren.

Usage Access:

1. App öffnen.
2. Usage-Status prüfen.
3. Einstellungsseite öffnen.
4. Zugriff erlauben.
5. Zur App zurückkehren.

Erwartet:

- Status aktualisiert sich korrekt
- App stürzt nicht ab
- Nutzen der Berechtigung ist verständlich

---

## Overlay-Test

Schritte:

1. Overlay aktivieren.
2. Test-Overlay anzeigen.
3. Button ziehen.
4. Button antippen.
5. Button deaktivieren.

Erwartet:

- Button erscheint über Apps
- Button ist verschiebbar
- Drag löst keinen Tap aus
- Position bleibt erhalten
- kein doppelter Button
- Overlay entfernbar

---

## WhatsApp-Erkennungstest

Schritte:

1. Overlay aktivieren.
2. WhatsApp öffnen.
3. Button erscheint.
4. Andere App öffnen.
5. Button verschwindet.
6. Mehrfach wechseln.

Erwartet:

- Button nur bei `com.whatsapp`
- keine doppelten Views
- Verzögerung akzeptabel

---

## Input-Bar-Test

Schritte:

1. WhatsApp öffnen.
2. Floating Button antippen.
3. Prüfen, dass zuerst nur die Input-Bar erscheint.
4. Ton/Stil öffnen und ändern.
5. Text eingeben.
6. Einfügen-Button antippen.
7. Start-Button antippen.
8. Input-Bar schließen oder Overlay beenden.

Erwartet:

- Startzustand ist kein großes Formular
- Input-Bar ist schmal und verdeckt WhatsApp möglichst wenig
- Ton/Stil ist links oder klar sichtbar erreichbar
- Textfeld ist direkt nutzbar
- im Antworten-Modus zeigt vorhandener Text eine kompakte Kontext-Vorschau
- Kontext-Vorschau kann per „Entfernen" geleert werden
- Einfügen ist optional und blockiert manuelle Eingabe nicht
- Start-Button heißt nicht `Senden`
- keine Eingabe wird dauerhaft gespeichert

---

## Result-Panel- und Vorschlagswechsel-Test

Schritte:

1. Input-Bar mit Dummy-Daten nutzen.
2. Dummy-Vorschläge anzeigen.
3. Prüfen, dass sich das Result-Panel erst nach Vorschlägen öffnet.
4. Prüfen, dass nur ein Vorschlag sichtbar ist.
5. Zwischen Vorschlag 1, 2 und 3 wechseln.
6. Sichtbaren Vorschlag kopieren.
7. Panel schließen.

Erwartet:

- Result-Panel erscheint nicht vor Vorschlägen
- keine drei Vorschläge untereinander als Standardansicht
- Anzeige wie `1/3`, Pfeile, Punkte oder Swipe ist vorhanden
- Wechsel zwischen allen 3 Vorschlägen funktioniert
- Kopieren kopiert den aktuell sichtbaren Vorschlag
- WhatsApp bleibt im Hintergrund sichtbar
- kein Vorschlagsverlauf wird gespeichert

---

## Clipboard-Test

Ohne Clipboard:

- Input-Bar öffnen
- keine falsche Vorschau anzeigen
- manuelle Eingabe bleibt nutzbar

Mit Clipboard:

1. WhatsApp-Nachricht manuell kopieren.
2. Input-Bar öffnen.
3. Einfügen antippen.
4. Übernommenen Text prüfen.

Erwartet:

- Clipboard erst nach Nutzeraktion
- Text erst nach Nutzeraktion übernehmen
- im Antworten-Modus wird der übernommene Text als Antwort-Kontext sichtbar
- „Kontext entfernen" leert nur die aktuelle Eingabe transient
- kein Speichern
- kein Logging

---

## WhatsApp-Dialogblock-Test

Quelle: `docs/WHATSAPP_DIALOG_CONTEXT.md`.

### Unit-Tests

Testfall 1 — Dialog mit zwei Sprechern:

```text
[1.7., 18:02] D: Hey wie arbeitest du morgen?
[1.7., 19:11] Anke Grunerr: Ich bin morgen zur Trauerfeier von Marco seiner mama
[1.7., 22:16] D: Ach mist müssen morgen nämlich zur nach Untersuchung der Katzen weil die gestern kastriert wurden und weiß nicht ob die Bahn wieder fahren
[1.7., 22:17] Anke Grunerr: Strassenbahn fährt im moment von taucha nach paunsdorf
[1.7., 22:18] Anke Grunerr: Sbahn fährt
[1.7., 22:43] D: Das reicht mir ka
[3.7., 16:16] Anke Grunerr: Ihr habt noch eine kühltasche mit essen im garten
```

Erwartet:

- Dialog erkannt
- Sprecher `D` und `Anke Grunerr` extrahiert
- letzte Nachricht von `Anke Grunerr` als aktueller Antwortanlass erkannt, soweit heuristisch möglich
- alter Verlauf nur als `conversationContext`

Testfall 2 — Einzeltext-Fallback:

```text
Kannst du mir das heute noch schicken?
```

Erwartet:

- kein Dialogparser-Modus nötig
- bestehender Antwortmodus bleibt unverändert

Testfall 3 — Teilweise kaputtes Format:

```text
[1.7., 18:02] D: Hey
Irgendeine freie Zeile
[1.7., 18:05] Anke: Ja passt
```

Erwartet:

- kein Crash
- freie Zeile wird an vorherige Nachricht gehängt oder kontrollierter Einzeltext-Fallback

### PromptBuilder-Test

Erwartet:

- `conversationContext` erscheint nur, wenn Dialog erkannt wurde
- Prompt trennt bisherigen Verlauf von aktueller Nachricht
- Prompt enthält Regel, nicht auf jede alte Nachricht einzeln zu antworten
- normale Einzeltexte erzeugen weiterhin den bisherigen Prompt ohne Kontextsektion

### Datenschutz-Test

Erwartet:

- kein Speichern des Dialogauszugs
- kein Logging von Namen, Texten oder Vorschlägen
- keine neue Permission
- kein Accessibility Service
- kein Kontaktzugriff

---

## Modus-Tests

Antworten:

```text
Kopierte Nachricht:
Warum meldest du dich erst jetzt?

Nutzerabsicht:
Ich will mich entschuldigen, aber nicht unterwürfig klingen.

Ton:
ruhig, ehrlich, kurz
```

Formulieren:

```text
Ich will sagen, dass ich heute Ruhe brauche, aber nicht kalt klingen.
```

Umschreiben:

```text
Original:
Keine Ahnung, mach halt was du willst.

Änderung:
weniger passiv-aggressiv
```

Erwartet:

- genau 3 Varianten soweit möglich
- keine Erklärung
- natürliche Sprache
- direkt kopierbar

---

## Antwortqualitäts-Testset (A/B)

Quelle: `docs/RESPONSE_QUALITY_AUDIT.md`. Dieses Testset ist die Grundlage, um
Prompt-/Persona-Kalibrierung zu bewerten und später Modelle zu vergleichen
(offener Punkt in `docs/DECISIONS.md`). Es ist eine **manuelle** Bewertungsliste,
kein automatisiertes A/B-Framework.

### Bewertungsraster (jedes Kriterium 1–5)

| Kriterium | Frage |
|---|---|
| Natürlichkeit | Klingt es wie WhatsApp? |
| Kürze | Ist es kurz genug (1–2 Sätze)? |
| Passung | Reagiert es wirklich auf den Kontext? |
| Ton | Passt der gewählte Ton? |
| Nicht-KI-Gefühl | Klingt es nicht nach ChatGPT/Brief/E-Mail? |
| Kopierbarkeit | Kann man es direkt senden? |
| Kosten/Latenz | Ist es für private Nutzung bezahlbar und schnell genug? |

### Modellkandidaten (Slugs geprüft am 2026-07-08)

Geprüft via OpenRouter `/api/v1/models`; noch **nicht** A/B-getestet:

| Rolle | Slug | A/B-Status |
|---|---|---|
| Default | `deepseek/deepseek-v4-flash` | gepinnt, noch nicht manuell bewertet |
| Referenz | `anthropic/claude-sonnet-5` | nicht erneut bewertet nach Schreibstil-Fix |
| Haiku | `anthropic/claude-haiku-4.5` | offen |
| GPT Mini | `openai/gpt-5-mini` | offen |
| optionaler Mini-Kandidat | `openai/gpt-4.1-mini` | offen |

Aliases wie `~anthropic/claude-haiku-latest` oder `~openai/gpt-mini-latest`
dürfen zum Explorieren genutzt, aber nicht dauerhaft als Release-Default gepinnt
werden.

DeepSeek ist aktuell gepinnt. Ein erneuter Modellwechsel (siehe offener Punkt in
DECISIONS.md) erfolgt nur, wenn ein Kandidat im Durchschnitt **deutlich** besser
abschneidet als DeepSeek nach Prompt-/Persona-/Schreibstil-Fix.

### Antworten (REPLY) — 8 Fälle

1. kopiert: „Hab gehört ihr haut morgen ab. Können wir uns nochmal treffen oder passt das eher schlecht?“ / Absicht: sagen dass es knapp ist aber vielleicht kurz geht / Erwartung: kurz, normal, nicht zu förmlich.
2. kopiert: „Warum meldest du dich nie richtig?“ / Absicht: sagen dass es nicht böse gemeint war und ich gerade viel um die Ohren habe / Erwartung: ruhig, nicht defensiv, nicht therapeutisch.
3. kopiert: „Kannst du mir das heute noch schicken?“ / Absicht: sagen ja später wenn ich zuhause bin / Erwartung: kurz und alltagstauglich.
4. kopiert: „Bist du noch sauer wegen gestern?“ / Absicht: klar machen dass nicht mehr, ohne Drama / Erwartung: normal, nicht übererklärend.
5. kopiert: „Wir sehen uns doch heute, oder?“ / Absicht: absagen aber freundlich bleiben / Erwartung: kurz, nicht zu entschuldigend.
6. kopiert: „Hast du das eigentlich schon erledigt?“ / Absicht: nachfragen, ob es klappt, ohne Druck / Erwartung: locker, nicht fordernd.
7. kopiert: „Alles klar bei dir?“ / Absicht: kurz beruhigen, dass alles ok ist / Erwartung: ganz normaler Chat-Satz.
8. kopiert: „Können wir später telefonieren?“ / Absicht: sagen dass es gerade nicht geht, sondern später / Erwartung: direkt, freundlich, kurz.

### Formulieren (COMPOSE) — 6 Fälle

9. Absicht: fragen ob sie gut angekommen ist / Erwartung: normale kurze WhatsApp-Frage.
10. Absicht: absagen weil ich zu müde bin, aber freundlich bleiben / Erwartung: nicht zu entschuldigend, nicht zu förmlich.
11. Absicht: jemandem alles Gute zum Geburtstag wünschen, ohne kitschig zu sein / Erwartung: warm, aber kurz.
12. Absicht: nachfragen, ob am Wochenende noch was geplant ist / Erwartung: locker, unaufdringlich.
13. Absicht: mich für etwas Kleinigkeiten bedanken / Erwartung: kurz und ehrlich, nicht formell.
14. Absicht: vorschlagen, sich nächste Woche mal zu treffen / Erwartung: direkt, ohne lange Vorrede.

### Umschreiben (REWRITE) — 4 Fälle (Modus aktuell ausgeblendet, nur für Testzwecke)

15. Original: „Keine Ahnung, mach halt was du willst.“ / Änderung: sanfter, aber trotzdem genervt / Erwartung: Kern bleibt, weniger hart.
16. Original: „Schon gut, ist ja nicht so wichtig.“ / Änderung: klarer sagen was man will / Erwartung: konkreter, nicht passiv-aggressiv.
17. Original: „Ja klar, kein Problem, mache ich sofort.“ / Änderung: ehrlicher, wenn es eigentlich nicht passt / Erwartung: höflich, aber ehrlich.
18. Original: „Mir egal, such du aus.“ / Änderung: mitentscheiden, ohne Druck / Erwartung: aktiv, freundlich.

### Negativ-Leitplanken (müssen bei jedem Fall erfüllt sein)

- keine Floskeln wie „Vielen Dank für deine Nachricht“
- keine Sätze wie „Ich verstehe, dass…“
- keine E-Mail-/Brief-/Therapiesprache
- maximal 1–2 kurze Sätze pro Vorschlag
- die feste App-Stimme erkennbar (alltäglich, nicht geschäftlich/akademisch)

---

## Retry-Tests

UI-Test:

1. 3 Vorschläge im Result-Panel anzeigen.
2. Prüfen, dass der Retry-Bereich darunter erscheint.
3. `Nochmal` antippen.
4. Einen Änderungs-Chip wählen.
5. Zwei Änderungs-Chips wählen.
6. Mehr als zwei Änderungs-Chips versuchen.
7. Panel schließen und neu öffnen.

Erwartet:

- Retry-Bereich erscheint nicht vor Ergebnissen
- `Nochmal` startet neue Anfrage mit gleichen Eingaben
- Änderungs-Chips wirken nur auf die nächste Anfrage
- maximal 1-2 Chips gleichzeitig aktiv
- Retry-Auswahl wird nach Schließen verworfen
- kein Verlauf der Retry-Versuche
- keine Bewertung einzelner Vorschläge
- kein freies Feedbackfeld

Prompt-/Parser-Test:

- `RetryInstruction.KUERZER` erzeugt Prompt mit temporärer Änderungsanweisung
- `RetryInstruction.WENIGER_KUENSTLICH` erzeugt Prompt ohne Meta-Erklärung
- Antwort enthält 3 Vorschläge und keine Erklärung wie „Diesmal kürzer“

---

## KI-Fehlertests

Testen:

- API-Key fehlt
- Internet fehlt
- ungültige Modellantwort
- HTTP-Fehler / Rate Limit
- Retry schlägt fehl

Erwartet:

- klare Fehlermeldung
- kein Crash
- bei Retry-Fehler bleiben bisherige Vorschläge sichtbar

---

## Abschluss-Testmatrix

| Bereich | Pflicht |
|---|---|
| Build | ja |
| Installation | ja |
| Secret-/API-Key-Strategie | ja |
| Overlay Permission | ja |
| Usage Access | ja |
| WhatsApp-Erkennung | ja |
| Dragging | ja |
| Input-Bar | ja |
| Result-Panel | ja |
| Vorschlagswechsel | ja |
| Clipboard bewusst übernehmen | ja |
| alle 3 Modi | ja |
| Antwortqualität (A/B-Testset) | ja, manuell |
| Retry-Bereich | ja |
| KI-Fehlerfälle | ja |
| Kopieren sichtbarer Vorschlag | ja |
| Sperren/Entsperren | ja |
| keine verbotenen Permissions | ja |
| kein Accessibility | ja |
| kein Verlauf/Gedächtnis/Profile | ja |
| visueller Scope erfüllt | ja |

---

## Testbericht-Format

```text
Test environment:
- Gerät:
- Android-Version:
- App-Version/Commit:

Passed:
- ...

Failed:
- ...

Not tested:
- ...

Risks:
- ...

Next fixes:
- ...
```
=======
- keine sensiblen Daten in UI/Logs

---

## Lifecycle-Tests

Testen:

- Sperren/Entsperren
- App aus Recent Apps entfernen
- Rotation
- längere Inaktivität
- Samsung-Akkuoptimierung

Erwartet:

- keine doppelten Views
- kein hängendes Overlay
- Einschränkungen dokumentiert

---

## Datenschutz-Test

Prüfen:

- keine Chatnachrichten in DataStore
- keine generierten Antworten gespeichert
- keine Nutzerabsicht gespeichert
- keine Clipboard-Historie
- keine Retry-Anweisungen gespeichert
- Schreibstil-Settings speichern nur neutrale Enum-Werte, keine Persona und keine Texte
- keine API-Keys in Logs
- keine Nutzertexte in Logs
- keine Retry-Anweisungen in Logs
- kein Verlauf
- kein Gedächtnis
- keine Profile

---

## Visueller Scope-Test

Prüfen gegen `docs/VISUAL_SCOPE.md`:

- Floating Button klein und verschiebbar
- erster Zustand ist Input-Bar
- Result-Panel erst nach KI-Antwort
- ein sichtbarer Vorschlag statt drei gestapelter Karten
- Vorschlagswechsel sichtbar und nutzbar
- Retry klein und global
- keine Modell-/Provider-/Prompt-Technik im Overlay
- kein Dashboard

---

## Abschluss-Testmatrix

| Bereich | Pflicht |
|---|---|
| Build | ja |
| Installation | ja |
| Secret-/API-Key-Strategie | ja |
| Overlay Permission | ja |
| Usage Access | ja |
| WhatsApp-Erkennung | ja |
| Dragging | ja |
| Input-Bar | ja |
| Result-Panel | ja |
| Vorschlagswechsel | ja |
| Clipboard bewusst übernehmen | ja |
| alle 3 Modi | ja |
| Antwortqualität (A/B-Testset) | ja, manuell |
| Retry-Bereich | ja |
| KI-Fehlerfälle | ja |
| Kopieren sichtbarer Vorschlag | ja |
| Sperren/Entsperren | ja |
| keine verbotenen Permissions | ja |
| kein Accessibility | ja |
| kein Verlauf/Gedächtnis/Profile | ja |
| visueller Scope erfüllt | ja |

---

## Testbericht-Format

```text
Test environment:
- Gerät:
- Android-Version:
- App-Version/Commit:

Passed:
- ...

Failed:
- ...

Not tested:
- ...

Risks:
- ...

Next fixes:
- ...
```
>>>>>>> 4c292de (feat(overlay): writing-style menu, intent chips, and UX refinements)
