package de.disaai.chathilfe.ai

import de.disaai.chathilfe.model.ReplyMode
import de.disaai.chathilfe.model.ReplyRequest
import de.disaai.chathilfe.model.RetryInstruction
import de.disaai.chathilfe.model.ToneOption
import de.disaai.chathilfe.model.WritingStyleSettings
import de.disaai.chathilfe.model.AnswerLength
import de.disaai.chathilfe.model.EmojiUsage
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
        conversationContext: String? = null,
    ) = ReplyRequest(mode, userIntent, tone, retryInstructions, copiedMessage, originalText, conversationContext)

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
    fun `fixed app voice is present in every mode`() {
        val reply = PromptBuilder.build(
            baseRequest(mode = ReplyMode.REPLY, copiedMessage = "Hallo?")
        )
        val compose = PromptBuilder.build(baseRequest(mode = ReplyMode.COMPOSE))
        val rewrite = PromptBuilder.build(
            baseRequest(mode = ReplyMode.REWRITE, originalText = "Test.")
        )
        // Static app voice rule (see docs/PROMPTS.md + docs/PRIVACY_SECURITY.md).
        listOf(reply, compose, rewrite).forEach { prompt ->
            assertTrue("voice missing", prompt.contains("Frau Anfang 30"))
            assertTrue("voice marked as profile rule", prompt.contains("feste App-Vorgabe und kein gespeichertes Profil"))
        }
    }

    @Test
    fun `hardened whatsapp style rules are present`() {
        val prompt = PromptBuilder.build(baseRequest(mode = ReplyMode.COMPOSE))
        assertTrue(prompt.contains("wie eine WhatsApp-Nachricht, nicht wie eine E-Mail oder ein Brief"))
        assertTrue(prompt.contains("Standard: 1–2 kurze Sätze"))
    }

    @Test
    fun `default length stays short and does not mention ausfuehrlicher wording`() {
        val prompt = PromptBuilder.build(baseRequest())
        assertTrue(prompt.contains("Standard: 1–2 kurze Sätze pro Vorschlag."))
        assertFalse(prompt.contains("etwas ausführlicher, 2–4 natürliche WhatsApp-Sätze, aber kein Roman"))
    }

    @Test
    fun `ausfuehrlicher chip adds longer-form retry wording`() {
        val prompt = PromptBuilder.build(
            baseRequest(retryInstructions = setOf(RetryInstruction.AUSFUEHRLICHER))
        )
        assertTrue(prompt.contains("etwas ausführlicher, 2–4 natürliche WhatsApp-Sätze, aber kein Roman"))
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
    fun `ausfuehrlicher combined with another chip sorts after it by ordinal`() {
        val prompt = PromptBuilder.build(
            baseRequest(
                retryInstructions = setOf(RetryInstruction.AUSFUEHRLICHER, RetryInstruction.KUERZER)
            )
        )
        val idxKuerzer = prompt.indexOf("kompakter, weniger Wörter")
        val idxAusfuehrlicher = prompt.indexOf("etwas ausführlicher, 2–4 natürliche WhatsApp-Sätze, aber kein Roman")
        assertTrue("KUERZER missing", idxKuerzer >= 0)
        assertTrue("AUSFUEHRLICHER missing", idxAusfuehrlicher >= 0)
        assertTrue("ordinal order broken", idxKuerzer < idxAusfuehrlicher)
    }

    @Test
    fun `empty retry set leaves no placeholder and no change wording`() {
        val prompt = PromptBuilder.build(baseRequest())
        assertFalse(prompt.contains("{{"))
        assertFalse(prompt.contains("kompakter"))
        assertFalse(prompt.contains("weniger steif"))
    }

    @Test
    fun `default writing style block is present and placeholder free`() {
        val prompt = PromptBuilder.build(baseRequest(mode = ReplyMode.COMPOSE))
        // Defaults from Issue #8: length=normal, emoji=sparing, punctuation=relaxed,
        // capitalization=correct, naturalness=less AI.
        assertTrue(prompt.contains("Schreibstil (Nutzervorgabe)"))
        assertTrue(prompt.contains("Länge: knapp halten, 1–2 kurze Sätze"))
        assertTrue(prompt.contains("Emojis: sparsam Emojis, nur wenn es natürlich passt"))
        assertTrue(prompt.contains("Natürlichkeit: natürlicher Chatstil, keine typischen KI-Formulierungen"))
        assertFalse(prompt.contains("{{"))
    }

    @Test
    fun `default style equals explicit default writing style`() {
        val a = PromptBuilder.build(baseRequest(mode = ReplyMode.COMPOSE))
        val b = PromptBuilder.build(baseRequest(mode = ReplyMode.COMPOSE), WritingStyleSettings())
        assertEquals(a, b)
    }

    @Test
    fun `custom writing style changes the style block`() {
        val prompt = PromptBuilder.build(
            baseRequest(mode = ReplyMode.REPLY, copiedMessage = "Hallo?"),
            WritingStyleSettings(
                length = AnswerLength.SHORT,
                emojiUsage = EmojiUsage.NONE,
            ),
        )
        assertTrue(prompt.contains("Länge: sehr kurz, möglichst ein Satz"))
        assertTrue(prompt.contains("Emojis: keine Emojis"))
        // Non-overridden values keep their defaults.
        assertTrue(prompt.contains("Groß-/Kleinschreibung korrekt"))
        assertFalse(prompt.contains("{{"))
    }

    @Test
    fun `style block is present in all three modes`() {
        val reply = PromptBuilder.build(baseRequest(mode = ReplyMode.REPLY, copiedMessage = "?"))
        val compose = PromptBuilder.build(baseRequest(mode = ReplyMode.COMPOSE))
        val rewrite = PromptBuilder.build(baseRequest(mode = ReplyMode.REWRITE, originalText = "x"))
        listOf(reply, compose, rewrite).forEach {
            assertTrue("style block missing", it.contains("Schreibstil (Nutzervorgabe)"))
            assertFalse("placeholder left", it.contains("{{"))
        }
    }

    @Test
    fun `reply with conversation context includes history and current message section`() {
        val prompt = PromptBuilder.build(
            baseRequest(
                mode = ReplyMode.REPLY,
                userIntent = "zusage",
                copiedMessage = "Ihr habt noch eine kühltasche mit essen im garten",
                conversationContext = "D: Hey\nAnke Grunerr: ja klar",
            )
        )
        assertTrue(prompt.contains("Bisheriger Chatverlauf, falls vorhanden:"))
        assertTrue(prompt.contains("D: Hey"))
        assertTrue(prompt.contains("Anke Grunerr: ja klar"))
        assertTrue(prompt.contains("Aktuelle Nachricht, auf die geantwortet werden soll:"))
        assertTrue(prompt.contains("Ihr habt noch eine kühltasche mit essen im garten"))
        assertFalse(prompt.contains("{{"))
    }

    @Test
    fun `reply without conversation context omits history section`() {
        val prompt = PromptBuilder.build(
            baseRequest(mode = ReplyMode.REPLY, copiedMessage = "Hallo?")
        )
        assertFalse(prompt.contains("Bisheriger Chatverlauf"))
        assertFalse(prompt.contains("{{"))
        assertTrue(prompt.contains("Aktuelle Nachricht, auf die geantwortet werden soll:"))
    }

    @Test
    fun `reply with empty user intent still builds a clean prompt`() {
        // Issue #22: a pasted message alone is enough; the user intent may be empty.
        val prompt = PromptBuilder.build(
            baseRequest(
                mode = ReplyMode.REPLY,
                userIntent = "",
                copiedMessage = "Wann hast du Zeit?",
            )
        )
        assertTrue(prompt.contains("Wann hast du Zeit?"))
        assertFalse(prompt.contains("{{"))
    }
}
