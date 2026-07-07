package de.disaai.chathilfe.ai

import de.disaai.chathilfe.model.ReplyMode
import de.disaai.chathilfe.model.ReplyRequest
import de.disaai.chathilfe.model.RetryInstruction
import de.disaai.chathilfe.model.ToneOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    private fun baseRequest(
        mode: ReplyMode = ReplyMode.COMPOSE,
        userIntent: String = "Ich brauche heute Ruhe",
        tone: ToneOption = ToneOption.DEFAULT,
        retryInstructions: Set<RetryInstruction> = emptySet(),
        copiedMessage: String? = null,
        originalText: String? = null,
    ) = ReplyRequest(mode, userIntent, tone, retryInstructions, copiedMessage, originalText)

    @Test
    fun `reply mode contains copied message, intent and tone meaning`() {
        val prompt = PromptBuilder.build(
            baseRequest(
                mode = ReplyMode.REPLY,
                userIntent = "mich entschuldigen",
                tone = ToneOption.KURZ,
                copiedMessage = "Warum meldest du dich erst jetzt?",
            )
        )
        assertTrue(prompt.contains("Warum meldest du dich erst jetzt?"))
        assertTrue(prompt.contains("mich entschuldigen"))
        assertTrue(prompt.contains("knapp, direkt, nicht hart")) // KURZ meaning
        assertTrue(prompt.contains("Ausgabeformat"))
        assertFalse(prompt.contains("{{"))
    }

    @Test
    fun `compose mode has no copied message or original text sections`() {
        val prompt = PromptBuilder.build(
            baseRequest(
                mode = ReplyMode.COMPOSE,
                userIntent = "Ruhe heute betonen",
                tone = ToneOption.FREUNDLICH,
            )
        )
        assertTrue(prompt.contains("Ruhe heute betonen"))
        assertTrue(prompt.contains("warm, angenehm, nicht unterwürfig")) // FREUNDLICH meaning
        assertFalse(prompt.contains("Kopierte Nachricht"))
        assertFalse(prompt.contains("Originaltext"))
        assertFalse(prompt.contains("{{"))
    }

    @Test
    fun `rewrite mode contains original text`() {
        val prompt = PromptBuilder.build(
            baseRequest(
                mode = ReplyMode.REWRITE,
                userIntent = "weniger passiv-aggressiv",
                tone = ToneOption.DIREKT,
                originalText = "Keine Ahnung, mach halt was du willst.",
            )
        )
        assertTrue(prompt.contains("Keine Ahnung, mach halt was du willst."))
        assertTrue(prompt.contains("weniger passiv-aggressiv"))
        assertTrue(prompt.contains("klar, ehrlich, ohne Ausschmückung")) // DIREKT meaning
        assertFalse(prompt.contains("{{"))
    }

    @Test
    fun `retry text appears only when a change chip is active`() {
        val without = PromptBuilder.build(baseRequest())
        val with = PromptBuilder.build(
            baseRequest(retryInstructions = setOf(RetryInstruction.KUERZER))
        )
        assertFalse(without.contains("kompakter, weniger Wörter"))
        assertTrue(with.contains("kompakter, weniger Wörter"))
    }

    @Test
    fun `nochmal alone contributes no retry text`() {
        val plain = PromptBuilder.build(baseRequest())
        val withNochmal = PromptBuilder.build(
            baseRequest(retryInstructions = setOf(RetryInstruction.NOCHMAL))
        )
        assertEquals(plain, withNochmal)
        assertFalse(withNochmal.contains("nochmal"))
        assertFalse(withNochmal.contains("kompakter"))
    }

    @Test
    fun `nochmal plus change chip only applies the change chip`() {
        val prompt = PromptBuilder.build(
            baseRequest(
                retryInstructions = setOf(RetryInstruction.NOCHMAL, RetryInstruction.KUERZER)
            )
        )
        assertTrue(prompt.contains("kompakter, weniger Wörter"))
        assertFalse(prompt.contains("nochmal", ignoreCase = true))
    }

    @Test
    fun `multiple change chips are joined in deterministic ordinal order`() {
        val prompt = PromptBuilder.build(
            baseRequest(
                retryInstructions = setOf(RetryInstruction.KLARER, RetryInstruction.KUERZER)
            )
        )
        val idxKuerzer = prompt.indexOf("kompakter, weniger Wörter")
        val idxKlarer = prompt.indexOf("weniger schwammig, konkreter formuliert")
        assertTrue("KUERZER missing", idxKuerzer >= 0)
        assertTrue("KLARER missing", idxKlarer >= 0)
        assertTrue("ordinal order broken", idxKuerzer < idxKlarer)
    }

    @Test
    fun `empty retry set leaves no placeholder and no change wording`() {
        val prompt = PromptBuilder.build(baseRequest())
        assertFalse(prompt.contains("{{"))
        assertFalse(prompt.contains("kompakter"))
        assertFalse(prompt.contains("weniger steif"))
    }
}
