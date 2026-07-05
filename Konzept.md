# Konzept.md — WhatsApp Reply Overlay MVP

## 1. Kurzbeschreibung

Die App ist ein privater Android-MVP, der als schwebender Formulierungshelfer über WhatsApp funktioniert.

Wenn WhatsApp geöffnet ist, erscheint ein kleiner Floating Button am Bildschirmrand. Beim Antippen öffnet sich ein kompaktes Eingabefenster. Der Nutzer kann grob beschreiben, was er sagen möchte, optional eine kopierte WhatsApp-Nachricht verwenden und einen gewünschten Ton auswählen. Die KI erzeugt mehrere Antwortvorschläge. Der Nutzer kopiert einen Vorschlag und fügt ihn selbst in WhatsApp ein.

Die App liest keine WhatsApp-Chats automatisch aus und sendet keine Nachrichten selbst.

---

## 2. Ziel

Ziel ist eine kleine, stabile Android-App als private APK.

Die App soll helfen, schwierige oder unklare Chatnachrichten besser zu formulieren, ohne WhatsApp direkt zu automatisieren.

### Hauptnutzen

- Nutzer weiß ungefähr, was er sagen will, aber nicht wie.
- Nutzer kann auf kopierte Nachrichten passend antworten.
- Nutzer kann eigene Texte freundlicher, direkter, kürzer oder deeskalierender formulieren lassen.
- Nutzer bleibt immer selbst verantwortlich: prüfen, kopieren, einfügen, senden.

---

## 3. Nicht-Ziele

Folgende Punkte gehören nicht in den MVP:

- WhatsApp-Nachrichten automatisch auslesen
- vollständige Chatverläufe analysieren
- Kontakte auslesen
- Nachrichten automatisch in WhatsApp einfügen
- Nachrichten automatisch senden
- Accessibility-basierte WhatsApp-Automation
- Multi-App-Support für Telegram, Instagram, SMS usw.
- Account-System
- Cloud-Speicherung
- komplexes Prompt-/Rollen-System
- Play-Store-Veröffentlichung
- Social-Media-App oder Messenger-Ersatz

Diese Einschränkungen sind wichtig, damit der MVP klein, testbar und technisch kontrollierbar bleibt.

---

## 4. Zielplattform

### Primäres Zielgerät

- Samsung Galaxy S25
- moderne Android-Version
- private APK-Installation

### Empfohlener Stack

| Bereich | Empfehlung |
|---|---|
| Sprache | Kotlin |
| UI | Jetpack Compose |
| Overlay | Android `WindowManager` mit `TYPE_APPLICATION_OVERLAY` |
| Hintergrunddienst | Foreground Service, falls nötig |
| App-Erkennung | `UsageStatsManager` |
| KI-Anbieter | OpenRouter oder OpenAI |
| Lokale Speicherung | DataStore |
| Distribution | lokale/private APK |

---

## 5. Grundprinzip

Die App soll nicht versuchen, WhatsApp zu steuern. Sie soll nur ein Hilfsfenster über WhatsApp bereitstellen.

### Ablauf

1. Nutzer öffnet WhatsApp.
2. App erkennt, dass WhatsApp im Vordergrund ist.
3. Floating Button erscheint am rechten Bildschirmrand.
4. Nutzer tippt auf den Button.
5. Mini-Fenster öffnet sich.
6. Nutzer wählt einen Modus:
   - Antworten
   - Formulieren
   - Umschreiben
7. Optional wird ein kopierter Text aus der Zwischenablage verwendet.
8. Nutzer beschreibt, was er ausdrücken möchte.
9. Nutzer wählt Ton/Stil.
10. KI erzeugt 3 Vorschläge.
11. Nutzer kopiert einen Vorschlag.
12. Nutzer fügt ihn selbst in WhatsApp ein und sendet selbst.

---

## 6. Modi

## 6.1 Modus: Antworten

Dieser Modus wird genutzt, wenn der Nutzer auf eine konkrete Nachricht antworten möchte.

### Eingaben

- kopierte Nachricht
- Nutzerabsicht
- gewünschter Ton
- gewünschte Länge optional

### Beispiel

Kopierte Nachricht:

> Warum meldest du dich erst jetzt?

Nutzerabsicht:

> Ich will mich entschuldigen, aber nicht unterwürfig klingen.

Gewünschter Ton:

> ruhig, ehrlich, kurz

Ergebnis:

> Sorry, dass ich mich erst jetzt melde. Ich hatte gerade einiges um die Ohren, wollte dir aber trotzdem ordentlich antworten und nicht nur schnell irgendwas schreiben.

---

## 6.2 Modus: Formulieren

Dieser Modus wird genutzt, wenn der Nutzer keinen konkreten Eingangstext hat, sondern selbst etwas sagen möchte.

### Eingaben

- Nutzerabsicht
- gewünschter Ton
- gewünschte Länge optional

### Beispiel

Nutzerabsicht:

> Ich will sagen, dass ich heute Ruhe brauche, aber nicht kalt klingen.

Ergebnis:

> Ich mag dich und es liegt nicht an dir, aber ich merke, dass ich heute einfach etwas Ruhe brauche. Ich wollte dir das lieber ehrlich sagen, statt komisch oder abweisend zu wirken.

---

## 6.3 Modus: Umschreiben

Dieser Modus wird genutzt, wenn der Nutzer schon einen Text hat, dieser aber falsch klingt.

### Eingaben

- Originaltext
- gewünschte Änderung
- gewünschter Ton

### Beispiel

Originaltext:

> Keine Ahnung, mach halt was du willst.

Gewünschte Änderung:

> weniger passiv-aggressiv

Ergebnis:

> Ich bin gerade unsicher, was ich dazu sagen soll. Entscheide du ruhig, aber ich würde später gern nochmal normal darüber reden.

---

## 7. UI-Konzept

## 7.1 Floating Button

### Anforderungen

- nur sichtbar, wenn WhatsApp aktiv ist
- klein und nicht störend
- an den Bildschirmrand andockend
- verschiebbar
- halbtransparent im Ruhezustand
- klar erkennbares Symbol
- verschwindet außerhalb von WhatsApp

### Verhalten

- Tippen öffnet Mini-Fenster
- Ziehen verschiebt Button
- längeres Drücken optional: Overlay deaktivieren

---

## 7.2 Mini-Fenster

Das Mini-Fenster soll kompakt sein und WhatsApp nicht komplett verdecken.

### Inhalt

- Titel: „Antworthelfer“
- Modusauswahl:
  - Antworten
  - Formulieren
  - Umschreiben
- Zwischenablage-Bereich:
  - „Kopierter Text erkannt“
  - Vorschau
  - „Verwenden“
  - „Ignorieren“
- Eingabefeld:
  - „Was willst du sagen?“
- Ton-Auswahl:
  - kurz
  - freundlich
  - direkt
  - entschuldigend
  - deeskalierend
  - klare Grenze
  - flirtend
- Button:
  - „Vorschläge erstellen“
- Ergebnisbereich:
  - Vorschlag 1
  - Vorschlag 2
  - Vorschlag 3
- je Vorschlag:
  - „Kopieren“
  - optional später: „Kürzer“, „Direkter“, „Freundlicher“

---

## 8. Berechtigungen

## 8.1 Notwendig

| Berechtigung | Zweck |
|---|---|
| Über anderen Apps anzeigen | Floating Button und Mini-Fenster anzeigen |
| Nutzungsdatenzugriff | erkennen, ob WhatsApp aktiv ist |
| Internet | KI-Anfrage senden |

## 8.2 Optional

| Berechtigung | Zweck |
|---|---|
| Benachrichtigung | stabilerer Hintergrunddienst |
| Autostart-Hinweis | abhängig von Hersteller/Batterieoptimierung |

## 8.3 Nicht verwenden im MVP

- Kontakte
- SMS
- Mikrofon
- Kamera
- Standort
- Speicherzugriff
- Accessibility Service

Accessibility soll im MVP bewusst nicht verwendet werden, weil die App keine Chat-Inhalte automatisch lesen oder WhatsApp steuern soll.

---

## 9. Datenschutzprinzip

Die App verarbeitet nur Text, den der Nutzer aktiv eingibt oder bewusst aus der Zwischenablage übernimmt.

### Regeln

- keine automatische WhatsApp-Auslesung
- keine Kontaktverarbeitung
- keine Speicherung von Chatinhalten
- keine Analyse vollständiger Verläufe
- keine versteckte Zwischenablage-Überwachung
- API-Key lokal speichern
- nur notwendige Anfrage an KI-Anbieter senden

### KI-Anfrage enthält maximal

- Modus
- kopierte Nachricht, falls aktiv übernommen
- Nutzerabsicht
- Ton
- gewünschte Sprache
- gewünschte Anzahl an Vorschlägen

---

## 10. Interne Prompt-Logik

Die App soll nicht der KI überlassen, was zu tun ist. Die App entscheidet über den Modus und baut daraus einen klaren Prompt.

## 10.1 Prompt: Antworten

```text
Du bist ein Formulierungsassistent für private Chatnachrichten.

Aufgabe:
Formuliere passende Antwortvorschläge auf die kopierte Nachricht.

Regeln:
- Antworte in natürlichem Deutsch.
- Schreibe wie eine normale Chatnachricht.
- Keine langen Erklärungen.
- Keine Analyse ausgeben.
- Nicht künstlich oder übertrieben höflich klingen.
- Keine Nachricht automatisch senden.
- Erzeuge genau 3 Varianten.
- Jede Variante soll direkt kopierbar sein.

Kopierte Nachricht:
{{copied_message}}

Was der Nutzer ausdrücken will:
{{user_intent}}

Gewünschter Ton:
{{tone}}

Ausgabeformat:
1. ...
2. ...
3. ...
```

## 10.2 Prompt: Formulieren

```text
Du bist ein Formulierungsassistent für private Chatnachrichten.

Aufgabe:
Formuliere aus dem Wunsch des Nutzers 3 sendbare Chatnachrichten.

Regeln:
- Natürlich und menschlich schreiben.
- Keine Erklärung ausgeben.
- Keine Analyse ausgeben.
- Keine übertriebene Höflichkeit.
- Keine künstliche Therapiesprache.
- Jede Variante soll direkt kopierbar sein.

Nutzerwunsch:
{{user_intent}}

Gewünschter Ton:
{{tone}}

Ausgabeformat:
1. ...
2. ...
3. ...
```

## 10.3 Prompt: Umschreiben

```text
Du bist ein Formulierungsassistent für private Chatnachrichten.

Aufgabe:
Schreibe den vorhandenen Text passend um.

Regeln:
- Bedeutung erhalten.
- Ton gemäß Vorgabe anpassen.
- 3 Varianten erzeugen.
- Keine Erklärung ausgeben.
- Jede Variante soll direkt kopierbar sein.

Originaltext:
{{original_text}}

Gewünschte Änderung:
{{user_intent}}

Gewünschter Ton:
{{tone}}

Ausgabeformat:
1. ...
2. ...
3. ...
```

---

## 11. Technische Architektur

```text
ReplyOverlayApp
├── MainActivity
│   ├── zeigt Berechtigungsstatus
│   ├── API-Key-Eingabe
│   ├── Overlay starten/stoppen
│   └── Testbereich
│
├── OverlayService
│   ├── Floating Button anzeigen
│   ├── Mini-Fenster anzeigen
│   ├── Overlay schließen
│   └── Position speichern
│
├── ForegroundAppDetector
│   ├── prüft aktive Vordergrund-App
│   └── erkennt com.whatsapp
│
├── ClipboardHelper
│   ├── liest Zwischenablage nur bei aktiver Nutzeraktion
│   └── kopiert generierte Vorschläge
│
├── AiClient
│   ├── sendet Anfrage an KI-Anbieter
│   ├── verarbeitet Fehler
│   └── liefert Antwortvarianten zurück
│
├── PromptBuilder
│   ├── baut Prompt für Antworten
│   ├── baut Prompt für Formulieren
│   └── baut Prompt für Umschreiben
│
├── SettingsStore
│   ├── API-Key
│   ├── bevorzugter Ton
│   ├── Overlay aktiv/inaktiv
│   └── Button-Position
│
└── Domain
    ├── Mode
    ├── Tone
    ├── AiRequest
    └── AiSuggestion
```

---

## 12. Fehlerfälle

Die App muss folgende Fälle sauber behandeln:

| Fall | Erwartetes Verhalten |
|---|---|
| Overlay-Berechtigung fehlt | Hinweis mit Button zu Einstellungen |
| Nutzungsdatenzugriff fehlt | Hinweis mit Button zu Einstellungen |
| API-Key fehlt | KI-Funktion deaktiviert, Eingabe anbieten |
| WhatsApp nicht aktiv | Floating Button ausblenden |
| Zwischenablage leer | Modus „Formulieren“ vorschlagen |
| KI-Anfrage schlägt fehl | verständliche Fehlermeldung |
| Internet fehlt | Offline-Hinweis |
| Antwort leer/ungültig | erneute Anfrage anbieten |
| Bildschirm gesperrt/entsperrt | Overlay stabil wiederherstellen |
| App wird vom System beendet | Dienst sauber neu startbar machen |

---

## 13. MVP-Abschlusskriterien

Der MVP gilt als fertig, wenn:

- App lässt sich als APK installieren.
- Startscreen zeigt alle notwendigen Berechtigungen.
- Nutzer kann Overlay-Berechtigung öffnen.
- Nutzer kann Nutzungsdatenzugriff öffnen.
- Nutzer kann API-Key speichern.
- WhatsApp öffnen führt zum Anzeigen des Floating Buttons.
- WhatsApp schließen führt zum Ausblenden des Floating Buttons.
- Button ist verschiebbar.
- Button-Position wird gespeichert.
- Klick auf Button öffnet Mini-Fenster.
- Nutzer kann Modus wählen.
- Nutzer kann kopierten Text verwenden oder ignorieren.
- Nutzer kann eine Absicht eingeben.
- Nutzer kann Ton auswählen.
- KI erzeugt genau 3 Vorschläge.
- Vorschläge können einzeln kopiert werden.
- App liest keine WhatsApp-Chats automatisch.
- App sendet keine Nachrichten.
- App nutzt keinen Accessibility Service.
- App crasht nicht bei Sperren/Entsperren.
- App funktioniert stabil auf Samsung S25.

---

## 14. Empfohlene Entwicklungsphasen

## Phase 1: Projektbasis

- Android-Projekt mit Kotlin und Jetpack Compose erstellen
- Paketstruktur sauber anlegen
- MainActivity mit Grundscreen bauen
- DataStore vorbereiten
- API-Key lokal speicherbar machen

## Phase 2: Berechtigungen

- Overlay-Berechtigung prüfen
- Nutzungsdatenzugriff prüfen
- passende Einstellungsseiten öffnen
- Status im UI anzeigen

## Phase 3: Overlay

- OverlayService bauen
- Floating Button anzeigen
- Button verschiebbar machen
- Button-Position speichern
- Mini-Fenster öffnen und schließen

## Phase 4: WhatsApp-Erkennung

- ForegroundAppDetector bauen
- `com.whatsapp` erkennen
- Button nur bei WhatsApp anzeigen
- Polling/Überwachung ressourcenschonend umsetzen

## Phase 5: Mini-Fenster

- Modusauswahl bauen
- Eingabefeld bauen
- Ton-Auswahl bauen
- Zwischenablage auf aktive Nutzeraktion lesen
- Ergebnisbereich bauen
- Kopierfunktion einbauen

## Phase 6: KI-Anbindung

- AiClient bauen
- PromptBuilder bauen
- Fehlerbehandlung
- Ladezustand
- 3 Vorschläge parsen und anzeigen

## Phase 7: Stabilisierung

- Tests auf echtem Gerät
- Verhalten bei Sperrbildschirm prüfen
- Batterieoptimierung prüfen
- Crash-Fälle reduzieren
- UI kompakt und nicht nervig machen

---

## 15. Risiken

| Risiko | Einschätzung | Gegenmaßnahme |
|---|---:|---|
| Overlay wird als störend empfunden | mittel | klein, transparent, verschiebbar, abschaltbar |
| UsageStats-Erkennung ist nicht perfekt | mittel | Intervall sauber wählen, manuelle Overlay-Aktivierung optional |
| Samsung beendet Hintergrunddienst | mittel | Foreground Service optional, Hinweise zur Akkuoptimierung |
| API-Key unsicher gespeichert | mittel | lokal speichern, keine Logs mit Key |
| KI-Ausgabe klingt künstlich | mittel | klare Prompts, kurze Varianten, Ton-Auswahl |
| Scope wächst zu stark | hoch | kein Auto-Senden, kein Chat-Auslesen, kein Multi-App-Support |

---

## 16. Spätere Erweiterungen

Nicht im MVP, aber später möglich:

- Unterstützung für Telegram
- Unterstützung für Instagram DMs
- Quick-Actions: „kürzer“, „wärmer“, „direkter“
- Favorisierte Tonprofile
- eigene Standardformulierung des Nutzers
- lokaler Verlauf der letzten Vorschläge, optional
- KI-Modellauswahl
- Kostenlimit pro Tag
- Export/Import der Einstellungen
- eigenes Keyboard als Alternative zum Overlay

---

## 17. Realistisches Urteil

Der Scope ist technisch sinnvoll und als privater Android-MVP gut machbar.

Die größte Schwierigkeit liegt nicht in der KI, sondern in Android-Overlay, Berechtigungen, Hintergrundverhalten und sauberem UX-Verhalten.

Die App sollte bewusst klein bleiben:
Floating Button, Mini-Fenster, Moduswahl, KI-Vorschläge, Kopieren.

Alles darüber hinaus erhöht Risiko und Entwicklungsaufwand deutlich.
