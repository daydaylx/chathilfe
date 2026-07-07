package de.disaai.chathilfe.ai

import de.disaai.chathilfe.model.ReplyMode
import de.disaai.chathilfe.model.ReplyRequest
import de.disaai.chathilfe.model.RetryInstruction
import de.disaai.chathilfe.model.ToneOption

/**
 * Builds the prompt string for a single AI request from a [ReplyRequest], following the
 * templates in `docs/PROMPTS.md`.
 *
 * Pure function: no logging, no persistence. The returned string is transient and lives only
 * for the next request; nothing here is stored or interpreted as a user profile. Sampling or
 * thinking parameters are intentionally not added here (see
 * `docs/PROMPT_PARAMETER_POLICY.md` — those are provider/model-specific and belong to Phase 7).
 */
object PromptBuilder {

    fun build(request: ReplyRequest): String {
        val tone = TONE_MEANINGS[request.tone] ?: request.tone.label
        val retry = retryText(request.retryInstructions)
        return templateFor(request.mode)
            .replace("{{copied_message}}", request.copiedMessage.orEmpty())
            .replace("{{original_text}}", request.originalText.orEmpty())
            .replace("{{user_intent}}", request.userIntent)
            .replace("{{tone}}", tone)
            .replace("{{retry_instruction}}", retry)
    }

    private fun templateFor(mode: ReplyMode): String = when (mode) {
        ReplyMode.REPLY -> TEMPLATE_REPLY
        ReplyMode.COMPOSE -> TEMPLATE_COMPOSE
        ReplyMode.REWRITE -> TEMPLATE_REWRITE
    }

    /**
     * Joins the meanings of active change chips. [RetryInstruction.NOCHMAL] means
     * "regenerate with identical inputs" and contributes no text. Order is deterministic by
     * enum ordinal so the output is stable and testable.
     */
    private fun retryText(chips: Set<RetryInstruction>): String =
        chips
            .filter { it != RetryInstruction.NOCHMAL }
            .sortedBy { it.ordinal }
            .joinToString("; ") { chip -> RETRY_MEANINGS[chip] ?: chip.label }
            .ifEmpty { "" }

    /** Tone meanings from the table in `docs/PROMPTS.md`. */
    private val TONE_MEANINGS: Map<ToneOption, String> = mapOf(
        ToneOption.KURZ to "knapp, direkt, nicht hart",
        ToneOption.FREUNDLICH to "warm, angenehm, nicht unterwürfig",
        ToneOption.DIREKT to "klar, ehrlich, ohne Ausschmückung",
        ToneOption.ENTSCHULDIGEND to "Verantwortung übernehmen, nicht kriechen",
        ToneOption.DEESKALIEREND to "beruhigend, konfliktmindernd",
        ToneOption.KLARE_GRENZE to "bestimmt, respektvoll, nicht aggressiv",
        ToneOption.FLIRTEND to "leicht verspielt, nicht peinlich",
    )

    /** Retry meanings from the table in `docs/PROMPTS.md`. [RetryInstruction.NOCHMAL] is excluded. */
    private val RETRY_MEANINGS: Map<RetryInstruction, String> = mapOf(
        RetryInstruction.KUERZER to "kompakter, weniger Wörter",
        RetryInstruction.LOCKERER to "weniger steif, natürlicher Alltagston",
        RetryInstruction.DIREKTER to "klarer, weniger weichgespült",
        RetryInstruction.SANFTER to "vorsichtiger, weniger hart",
        RetryInstruction.KLARER to "weniger schwammig, konkreter formuliert",
        RetryInstruction.WENIGER_KUENSTLICH to "keine typischen KI-Formulierungen, natürlicher Chatstil",
    )

    // Templates are verbatim copies of docs/PROMPTS.md. They contain no '$' characters, so
    // they are safe inside Kotlin raw strings.

    private val TEMPLATE_REPLY: String = """
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
- Die Antwort soll zur kopierten Nachricht passen.
- Berücksichtige, was der Nutzer ausdrücken will.
- Wenn Informationen fehlen, formuliere neutral statt Dinge zu erfinden.
- Wenn ein Änderungswunsch für einen neuen Versuch vorhanden ist, berücksichtige ihn still.
- Erkläre nicht, was geändert wurde.

Kopierte Nachricht:
{{copied_message}}

Was der Nutzer ausdrücken will:
{{user_intent}}

Gewünschter Ton:
{{tone}}

Änderungswunsch für neuen Versuch, falls vorhanden:
{{retry_instruction}}

Ausgabeformat:
1. ...
2. ...
3. ...
    """.trimIndent()

    private val TEMPLATE_COMPOSE: String = """
Du bist ein Formulierungsassistent für private Chatnachrichten.

Aufgabe:
Formuliere aus dem Wunsch des Nutzers 3 sendbare Chatnachrichten.

Regeln:
- Natürlich und menschlich schreiben.
- Keine Erklärung ausgeben.
- Keine Analyse ausgeben.
- Keine übertriebene Höflichkeit.
- Keine künstliche Therapiesprache.
- Keine unnötig langen Nachrichten.
- Jede Variante soll direkt kopierbar sein.
- Wenn der Wunsch emotional ist, bleibe klar und ruhig.
- Erfinde keine Details, die der Nutzer nicht genannt hat.
- Wenn ein Änderungswunsch für einen neuen Versuch vorhanden ist, berücksichtige ihn still.
- Erkläre nicht, was geändert wurde.

Nutzerwunsch:
{{user_intent}}

Gewünschter Ton:
{{tone}}

Änderungswunsch für neuen Versuch, falls vorhanden:
{{retry_instruction}}

Ausgabeformat:
1. ...
2. ...
3. ...
    """.trimIndent()

    private val TEMPLATE_REWRITE: String = """
Du bist ein Formulierungsassistent für private Chatnachrichten.

Aufgabe:
Schreibe den vorhandenen Text passend um.

Regeln:
- Bedeutung möglichst erhalten.
- Ton gemäß Vorgabe anpassen.
- 3 Varianten erzeugen.
- Keine Erklärung ausgeben.
- Jede Variante soll direkt kopierbar sein.
- Nicht unnötig lang werden.
- Keine neuen Fakten erfinden.
- Wenn der Originaltext aggressiv klingt, entschärfe ihn ohne den Kern zu verlieren.
- Wenn ein Änderungswunsch für einen neuen Versuch vorhanden ist, berücksichtige ihn still.
- Erkläre nicht, was geändert wurde.

Originaltext:
{{original_text}}

Gewünschte Änderung:
{{user_intent}}

Gewünschter Ton:
{{tone}}

Änderungswunsch für neuen Versuch, falls vorhanden:
{{retry_instruction}}

Ausgabeformat:
1. ...
2. ...
3. ...
    """.trimIndent()
}
