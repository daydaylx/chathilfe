package de.disaai.chathilfe.ai

import de.disaai.chathilfe.model.AnswerLength
import de.disaai.chathilfe.model.CapitalizationStyle
import de.disaai.chathilfe.model.EmojiUsage
import de.disaai.chathilfe.model.Naturalness
import de.disaai.chathilfe.model.PunctuationStyle
import de.disaai.chathilfe.model.ReplyMode
import de.disaai.chathilfe.model.ReplyRequest
import de.disaai.chathilfe.model.RetryInstruction
import de.disaai.chathilfe.model.ToneOption
import de.disaai.chathilfe.model.WritingStyleSettings

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

    /**
     * Builds the prompt for [request], injecting the user's [WritingStyleSettings] (Issue #8) as
     * a "Schreibstil" block. Defaults to [WritingStyleSettings] when omitted, so callers without
     * explicit style still get the documented MVP defaults.
     */
    fun build(request: ReplyRequest, style: WritingStyleSettings = WritingStyleSettings()): String {
        val tone = TONE_MEANINGS[request.tone] ?: request.tone.label
        val retry = retryText(request.retryInstructions)
        return templateFor(request.mode)
            .replace("{{conversation_context_section}}", contextSection(request))
            .replace("{{copied_message}}", request.copiedMessage.orEmpty())
            .replace("{{original_text}}", request.originalText.orEmpty())
            .replace("{{user_intent}}", request.userIntent)
            .replace("{{tone}}", tone)
            .replace("{{retry_instruction}}", retry)
            .replace("{{style_rules}}", styleRules(style))
    }

    /**
     * Optional conversation-context section for the reply template (Issue #19 /
     * docs/WHATSAPP_DIALOG_CONTEXT.md). Returns "" when no pasted dialog block is present so
     * the prompt stays clean; otherwise the documented `Bisheriger Chatverlauf, falls vorhanden:`
     * block. Transient only — never stored or logged.
     */
    private fun contextSection(request: ReplyRequest): String {
        val context = request.conversationContext
        return if (context.isNullOrBlank()) "" else "Bisheriger Chatverlauf, falls vorhanden:\n$context\n\n"
    }

    /**
     * Builds the deterministic "Schreibstil" prompt block from the user's style settings
     * (Issue #8). Pure style values only — no user text, no persona (D-013).
     */
    private fun styleRules(style: WritingStyleSettings): String = listOf(
        "- Länge: " + (LENGTH_MEANINGS[style.length] ?: style.length.label),
        "- Emojis: " + (EMOJI_MEANINGS[style.emojiUsage] ?: style.emojiUsage.label),
        "- Satzzeichen: " + (PUNCTUATION_MEANINGS[style.punctuation] ?: style.punctuation.label),
        "- Groß-/Kleinschreibung: " + (CAPITALIZATION_MEANINGS[style.capitalization] ?: style.capitalization.label),
        "- Natürlichkeit: " + (NATURALNESS_MEANINGS[style.naturalness] ?: style.naturalness.label),
    ).joinToString("\n")

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
        RetryInstruction.AUSFUEHRLICHER to "etwas ausführlicher, 2–4 natürliche WhatsApp-Sätze, aber kein Roman",
    )

    // --- Writing-style meanings from the "Schreibstil" table in `docs/PROMPTS.md` (Issue #8). ---

    private val LENGTH_MEANINGS: Map<AnswerLength, String> = mapOf(
        AnswerLength.SHORT to "sehr kurz, möglichst ein Satz",
        AnswerLength.NORMAL to "knapp halten, 1–2 kurze Sätze",
        AnswerLength.LONGER to "etwas ausführlicher darf sein, aber nicht lang",
    )

    private val EMOJI_MEANINGS: Map<EmojiUsage, String> = mapOf(
        EmojiUsage.NONE to "keine Emojis",
        EmojiUsage.SPARING to "sparsam Emojis, nur wenn es natürlich passt",
        EmojiUsage.NORMAL to "Emojis dürfen, aber nicht überladen",
    )

    private val PUNCTUATION_MEANINGS: Map<PunctuationStyle, String> = mapOf(
        PunctuationStyle.CLEAN to "saubere Satzzeichen",
        PunctuationStyle.RELAXED to "lockere Satzzeichen, wie im echten Chat",
        PunctuationStyle.VERY_RELAXED to "sehr lockere Satzzeichen, oft ohne Punkte",
    )

    private val CAPITALIZATION_MEANINGS: Map<CapitalizationStyle, String> = mapOf(
        CapitalizationStyle.CORRECT to "Groß-/Kleinschreibung korrekt",
        CapitalizationStyle.RELAXED to "Groß-/Kleinschreibung darf locker sein",
    )

    private val NATURALNESS_MEANINGS: Map<Naturalness, String> = mapOf(
        Naturalness.NORMAL to "natürliche Sprache",
        Naturalness.LESS_AI to "natürlicher Chatstil, keine typischen KI-Formulierungen",
        Naturalness.VERY_RELAXED to "sehr lockerer, umgangssprachlicher Stil",
    )

    // Templates follow docs/PROMPTS.md. The reply template uses a {{conversation_context_section}}
    // placeholder so the optional pasted-dialog block (Issue #19) is injected only when present;
    // the wording otherwise matches the doc. Templates contain no '$' characters, so they are
    // safe inside Kotlin raw strings.

    private val TEMPLATE_REPLY: String = """
Du bist ein Formulierungsassistent für private Chatnachrichten.

Stimme:
Die Antworten sollen klingen, als hätte sie eine alltägliche Person geschrieben – eine Frau Anfang 30 mit normaler Bildung, natürlicher Alltagssprache, nicht zu akademisch, nicht zu geschäftlich, nicht zu jugendlich, nicht zu künstlich perfekt. Das ist eine feste App-Vorgabe und kein gespeichertes Profil.

Schreibstil (Nutzervorgabe):
{{style_rules}}

Aufgabe:
Formuliere passende Antwortvorschläge auf die aktuelle Nachricht.

Regeln:
- Schreibe wie eine WhatsApp-Nachricht, nicht wie eine E-Mail oder ein Brief.
- Standard: 1–2 kurze Sätze pro Vorschlag.
- Nur wenn der Änderungswunsch ausdrücklich „ausführlicher" enthält: 2–4 kurze WhatsApp-Sätze, weiterhin natürlich und direkt kopierbar, kein Roman.
- Antworte in natürlichem, alltäglichem Deutsch.
- Reagiere direkt auf die aktuelle Nachricht, rede nicht drumherum.
- Nutze den bisherigen Chatverlauf nur als Kontext, falls vorhanden.
- Antworte nicht auf jede alte Nachricht einzeln.
- Wenn im Verlauf ein Themenwechsel vorkommt, reagiere auf die aktuelle Nachricht.
- Keine Floskeln wie „Vielen Dank für deine Nachricht".
- Keine Sätze wie „Ich verstehe, dass…".
- Keine künstliche Therapie- oder Coachingsprache.
- Keine übertriebene oder formelle Höflichkeit.
- Keine Analyse, keine Erklärung, kein Meta-Kommentar.
- Nicht künstlich oder zu perfekt klingen, lieber normal als glatt.
- Keine Nachricht automatisch senden oder vorgeben, gesendet zu haben.
- Erzeuge genau 3 Varianten, jede direkt kopierbar.
- Berücksichtige, was der Nutzer ausdrücken will.
- Wenn Informationen fehlen, formuliere neutral statt Dinge zu erfinden.
- Wenn ein Änderungswunsch für einen neuen Versuch vorhanden ist, berücksichtige ihn still.
- Erkläre nicht, was geändert wurde.
{{conversation_context_section}}Aktuelle Nachricht, auf die geantwortet werden soll:
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

Stimme:
Die Antworten sollen klingen, als hätte sie eine alltägliche Person geschrieben – eine Frau Anfang 30 mit normaler Bildung, natürlicher Alltagssprache, nicht zu akademisch, nicht zu geschäftlich, nicht zu jugendlich, nicht zu künstlich perfekt. Das ist eine feste App-Vorgabe und kein gespeichertes Profil.

Schreibstil (Nutzervorgabe):
{{style_rules}}

Aufgabe:
Formuliere aus dem Wunsch des Nutzers 3 sendbare Chatnachrichten.

Regeln:
- Schreibe wie eine WhatsApp-Nachricht, nicht wie eine E-Mail oder ein Brief.
- Standard: 1–2 kurze Sätze pro Vorschlag.
- Nur wenn der Änderungswunsch ausdrücklich „ausführlicher" enthält: 2–4 kurze WhatsApp-Sätze, weiterhin natürlich und direkt kopierbar, kein Roman.
- Natürliches, alltägliches Deutsch, wie Menschen wirklich schreiben.
- Keine Floskeln wie „Vielen Dank für deine Nachricht“.
- Keine Sätze wie „Ich verstehe, dass…“.
- Keine künstliche Therapie- oder Coachingsprache.
- Keine übertriebene oder formelle Höflichkeit.
- Keine Analyse, keine Erklärung, kein Meta-Kommentar.
- Nicht unnötig lang, lieber normal als perfekt.
- Jede Variante direkt kopierbar.
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

Stimme:
Die Antworten sollen klingen, als hätte sie eine alltägliche Person geschrieben – eine Frau Anfang 30 mit normaler Bildung, natürlicher Alltagssprache, nicht zu akademisch, nicht zu geschäftlich, nicht zu jugendlich, nicht zu künstlich perfekt. Das ist eine feste App-Vorgabe und kein gespeichertes Profil.

Schreibstil (Nutzervorgabe):
{{style_rules}}

Aufgabe:
Schreibe den vorhandenen Text passend um.

Regeln:
- Schreibe wie eine WhatsApp-Nachricht, nicht wie eine E-Mail oder ein Brief.
- Standard: 1–2 kurze Sätze pro Vorschlag.
- Nur wenn der Änderungswunsch ausdrücklich „ausführlicher" enthält: 2–4 kurze WhatsApp-Sätze, weiterhin natürlich und direkt kopierbar, kein Roman.
- Bedeutung möglichst erhalten.
- Ton gemäß Vorgabe anpassen.
- Keine Floskeln wie „Vielen Dank für deine Nachricht“.
- Keine Sätze wie „Ich verstehe, dass…“.
- Keine künstliche Therapie- oder Coachingsprache.
- Keine übertriebene oder formelle Höflichkeit.
- Keine Analyse, keine Erklärung, kein Meta-Kommentar.
- Nicht unnötig lang, lieber normal als perfekt.
- 3 Varianten, jede direkt kopierbar.
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
