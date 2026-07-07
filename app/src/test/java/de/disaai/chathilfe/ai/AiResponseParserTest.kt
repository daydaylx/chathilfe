package de.disaai.chathilfe.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiResponseParserTest {

    @Test
    fun `numbered with dot yields three success`() {
        val result = AiResponseParser.parse("1. Hallo\n2. Hi\n3. Hey")
        assertTrue("expected Success, was $result", result is ParseResult.Success)
        assertEquals(
            listOf("Hallo", "Hi", "Hey"),
            (result as ParseResult.Success).suggestions.map { it.text },
        )
    }

    @Test
    fun `numbered with parenthesis yields three success`() {
        val result = AiResponseParser.parse("1) Hallo\n2) Hi\n3) Hey")
        assertTrue(result is ParseResult.Success)
        assertEquals(3, (result as ParseResult.Success).suggestions.size)
    }

    @Test
    fun `bulleted list yields three success`() {
        val result = AiResponseParser.parse("- Hallo\n- Hi\n- Hey")
        assertTrue(result is ParseResult.Success)
        assertEquals(
            listOf("Hallo", "Hi", "Hey"),
            (result as ParseResult.Success).suggestions.map { it.text },
        )
    }

    @Test
    fun `intro line before three numbered items is ignored`() {
        val result = AiResponseParser.parse(
            "Hier sind drei Vorschläge:\n1. A\n2. B\n3. C"
        )
        assertTrue(result is ParseResult.Success)
        assertEquals(
            listOf("A", "B", "C"),
            (result as ParseResult.Success).suggestions.map { it.text },
        )
    }

    @Test
    fun `more than three numbered items takes the first three`() {
        val result = AiResponseParser.parse("1. A\n2. B\n3. C\n4. D")
        assertTrue(result is ParseResult.Success)
        assertEquals(3, (result as ParseResult.Success).suggestions.size)
    }

    @Test
    fun `two items yield partial`() {
        val result = AiResponseParser.parse("1. Nur einer\n2. Noch einer")
        assertTrue("expected Partial, was $result", result is ParseResult.Partial)
        assertEquals(2, (result as ParseResult.Partial).suggestions.size)
    }

    @Test
    fun `blank-line paragraph fallback yields three success`() {
        val result = AiResponseParser.parse("Erste Idee\n\nZweite Idee\n\nDritte Idee")
        assertTrue(result is ParseResult.Success)
        assertEquals(3, (result as ParseResult.Success).suggestions.size)
    }

    @Test
    fun `single-line fallback yields three success`() {
        val result = AiResponseParser.parse("Erste Idee\nZweite Idee\nDritte Idee")
        assertTrue(result is ParseResult.Success)
        assertEquals(3, (result as ParseResult.Success).suggestions.size)
    }

    @Test
    fun `empty input yields error`() {
        assertTrue(AiResponseParser.parse("") is ParseResult.Error)
    }

    @Test
    fun `blank-only input yields error`() {
        assertTrue(AiResponseParser.parse("   \n  \n") is ParseResult.Error)
    }

    @Test
    fun `duplicate items are deduplicated`() {
        val result = AiResponseParser.parse("1. Gleich\n2. Gleich\n3. Anders")
        assertTrue("expected Partial after dedupe, was $result", result is ParseResult.Partial)
        assertEquals(
            listOf("Gleich", "Anders"),
            (result as ParseResult.Partial).suggestions.map { it.text },
        )
    }

    @Test
    fun `malformed input never throws`() {
        // A representative set of hostile/odd inputs must not raise an exception.
        AiResponseParser.parse("1.")
        AiResponseParser.parse("\u0000\u0001\u0002")
        AiResponseParser.parse("\n\n\n")
        AiResponseParser.parse("x".repeat(100_000))
        AiResponseParser.parse("1. A\n2. B\n3. C\n".repeat(5000))
    }
}
