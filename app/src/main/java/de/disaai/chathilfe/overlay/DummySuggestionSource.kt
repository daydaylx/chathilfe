package de.disaai.chathilfe.overlay

import de.disaai.chathilfe.model.ReplySuggestion
import de.disaai.chathilfe.model.RetryInstruction
import de.disaai.chathilfe.model.ToneOption

/**
 * Phase 5 placeholder only: produces 3 static dummy suggestions so the overlay flow can be
 * built and tested without a real AI request. Replace the call site with a real AiClient in a
 * later phase and delete this file then. Never logs or stores the input text.
 */
object DummySuggestionSource {

    fun generate(
        originalText: String,
        tone: ToneOption,
        chips: Set<RetryInstruction> = emptySet(),
    ): List<ReplySuggestion> {
        val base = originalText.trim().ifEmpty { "deine Nachricht" }
        val qualifier = chips.joinToString(", ") { it.label }.ifEmpty { null }

        val variants = listOf(
            "(${tone.label}) Vorschlag 1 zu \"$base\"",
            "(${tone.label}) Vorschlag 2 zu \"$base\"",
            "(${tone.label}) Vorschlag 3 zu \"$base\"",
        )

        return variants.map { text ->
            ReplySuggestion(if (qualifier != null) "$text [$qualifier]" else text)
        }
    }
}
