package de.disaai.chathilfe.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsAppChatParserTest {

    private val realisticDialog = """
        [1.7., 18:02] D: Hey wie arbeitest du morgen?
        [1.7., 19:11] Anke Grunerr: Ich bin morgen zur Trauerfeier von Marco seiner mama
        [1.7., 22:16] D: Ach mist müssen morgen nämlich zur nach Untersuchung der Katzen weil die gestern kastriert wurden und weiß nicht ob die Bahn wieder fahren
        [1.7., 22:17] Anke Grunerr: Strassenbahn fährt im moment von taucha nach paunsdorf
        [1.7., 22:18] Anke Grunerr: Sbahn fährt
        [1.7., 22:43] D: Das reicht mir ka
        [3.7., 16:16] Anke Grunerr: Ihr habt noch eine kühltasche mit essen im garten
    """.trimIndent()

    @Test
    fun `realistic dialog is parsed with roles and latest other message`() {
        val parsed = WhatsAppChatParser.parse(realisticDialog)
        assertNotNull(parsed)
        parsed!!
        assertEquals(7, parsed.messages.size)
        assertEquals(listOf("D", "Anke Grunerr"), parsed.messages.map { it.sender }.distinct())
        assertEquals("D", parsed.likelySelfSender)
        assertEquals("Anke Grunerr", parsed.likelyOtherSender)
        // The latest message from the counterpart is the reply trigger.
        assertNotNull(parsed.latestOtherMessage)
        assertEquals("Ihr habt noch eine kühltasche mit essen im garten", parsed.latestOtherMessage!!.text)
    }

    @Test
    fun `format context produces an ordered transcript`() {
        val parsed = WhatsAppChatParser.parse(realisticDialog)!!
        val context = parsed.formatContext()
        assertNotNull(context)
        assertTrue(context!!.contains("D: Hey wie arbeitest du morgen?"))
        assertTrue(context.contains("Anke Grunerr: Ihr habt noch eine kühltasche mit essen im garten"))
        // Order preserved: the first D line precedes the last Anke line.
        assertTrue(context.indexOf("D: Hey wie arbeitest du morgen?") < context.indexOf("kühltasche"))
    }

    @Test
    fun `plain single text falls back to null`() {
        assertNull(WhatsAppChatParser.parse("Hallo, wie geht es dir?"))
        assertNull(WhatsAppChatParser.parse(""))
    }

    @Test
    fun `single matching line still falls back to null`() {
        // Need at least two WhatsApp-style lines to treat input as a dialog.
        assertNull(WhatsAppChatParser.parse("[1.7., 18:02] D: Hey wie arbeitest du morgen?"))
    }

    @Test
    fun `non-matching header lines before the first match are ignored`() {
        val text = "Nachrichten exportiert\n\n[1.7., 18:02] D: Hi\n[1.7., 18:03] Anke: Hey"
        val parsed = WhatsAppChatParser.parse(text)
        assertNotNull(parsed)
        assertEquals(2, parsed!!.messages.size)
    }

    @Test
    fun `multiline continuation lines attach to the previous message`() {
        val text = """
            [1.7., 18:02] Anke Grunerr: Zeile 1
            Zeile 2
            [1.7., 18:03] D: ok
        """.trimIndent()
        val parsed = WhatsAppChatParser.parse(text)!!
        assertEquals(2, parsed.messages.size)
        // "D" is the short/self-like sender, so Anke is the counterpart.
        assertEquals("Anke Grunerr", parsed.likelyOtherSender)
        val anke = parsed.messages.first { it.sender == "Anke Grunerr" }
        assertEquals("Zeile 1\nZeile 2", anke.text)
        assertEquals("Zeile 1\nZeile 2", parsed.latestOtherMessage!!.text)
    }

    @Test
    fun `three senders yield no confident role split`() {
        val text = """
            [1.7., 18:02] Anna: x
            [1.7., 18:03] Bernd: y
            [1.7., 18:04] Carla: z
        """.trimIndent()
        val parsed = WhatsAppChatParser.parse(text)!!
        assertNull(parsed.likelySelfSender)
        assertNull(parsed.likelyOtherSender)
        // Best effort: the most recent message is the trigger.
        assertEquals("z", parsed.latestOtherMessage!!.text)
    }

    @Test
    fun `two long names yield no confident role split`() {
        val text = """
            [1.7., 18:02] Anna: x
            [1.7., 18:03] Bernd: y
        """.trimIndent()
        val parsed = WhatsAppChatParser.parse(text)!!
        assertNull(parsed.likelySelfSender)
        assertNull(parsed.likelyOtherSender)
        assertEquals("y", parsed.latestOtherMessage!!.text)
    }

    @Test
    fun `date and time variants are tolerated`() {
        val text = """
            [01.07.26, 18:02] D: a
            [01.07.26, 18:03] Anke: b
        """.trimIndent()
        val parsed = WhatsAppChatParser.parse(text)!!
        assertEquals("01.07.26", parsed.messages.first().rawDate)
        assertEquals("18:02", parsed.messages.first().rawTime)

        val seconds = WhatsAppChatParser.parse(
            """
            [1.7.2026, 18:02:33] D: a
            [1.7.2026, 18:03:00] Anke: b
            """.trimIndent()
        )!!
        assertEquals("18:02:33", seconds.messages.first().rawTime)
    }

    @Test
    fun `garbage and partial input does not crash`() {
        assertNull(WhatsAppChatParser.parse("[unfertig"))
        assertNull(WhatsAppChatParser.parse("[] :"))
        assertNull(WhatsAppChatParser.parse("kein chat\nnur text\nund noch eine zeile"))
    }
}
