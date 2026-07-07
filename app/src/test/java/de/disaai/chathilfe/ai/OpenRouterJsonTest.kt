package de.disaai.chathilfe.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenRouterJsonTest {

    @Test
    fun `request body contains model, max_tokens and message, no sampling params`() {
        val body = OpenRouterJson.buildRequestBody("hallo", "anthropic/claude-sonnet-5", 1024)

        assertTrue(body.contains("\"model\":\"anthropic/claude-sonnet-5\""))
        assertTrue(body.contains("\"max_tokens\":1024"))
        assertTrue(body.contains("\"role\":\"user\""))
        assertTrue(body.contains("\"content\":\"hallo\""))
        assertFalse("temperature must not be sent (Claude Sonnet 5)", body.contains("temperature"))
        assertFalse("top_p must not be sent", body.contains("top_p"))
        assertFalse("top_k must not be sent", body.contains("top_k"))
    }

    @Test
    fun `prompt is escaped - quotes and newlines`() {
        val body = OpenRouterJson.buildRequestBody("er sagte \"hi\"\nund tschüss", "m", 10)

        assertTrue(body.contains("\\\"hi\\\""))
        assertTrue(body.contains("\\n"))
        // raw, unescaped quote inside the content must not survive:
        assertFalse(body.contains("er sagte \"hi\""))
    }

    @Test
    fun `extract content from standard response`() {
        val resp = """{"choices":[{"message":{"role":"assistant","content":"1. Hallo\n2. Hi\n3. Hey"}}]}"""
        assertEquals("1. Hallo\n2. Hi\n3. Hey", OpenRouterJson.extractContent(resp))
    }

    @Test
    fun `extract content unescapes escaped chars`() {
        // Raw string: single backslashes are the literal JSON escapes.
        val resp = """{"choices":[{"message":{"content":"a\"b\nc\td"}}]}"""
        assertEquals("a\"b\nc\td", OpenRouterJson.extractContent(resp))
    }

    @Test
    fun `extract content returns null when choices missing`() {
        assertNull(OpenRouterJson.extractContent("""{"error":{"message":"nope"}}"""))
    }

    @Test
    fun `extract content returns null when content blank`() {
        assertNull(OpenRouterJson.extractContent("""{"choices":[{"message":{"content":""}}]}"""))
    }

    @Test
    fun `extract content returns null on garbage`() {
        assertNull(OpenRouterJson.extractContent("not json at all"))
        assertNull(OpenRouterJson.extractContent(""))
    }

    @Test
    fun `escapeJsonString handles special chars`() {
        assertEquals("\\\\", OpenRouterJson.escapeJsonString("\\"))
        assertEquals("\\n", OpenRouterJson.escapeJsonString("\n"))
        assertEquals("\\\"", OpenRouterJson.escapeJsonString("\""))
        assertEquals("\\t", OpenRouterJson.escapeJsonString("\t"))
        assertEquals("\\r", OpenRouterJson.escapeJsonString("\r"))
    }

    @Test
    fun `escapeJsonString escapes control chars as uXXXX`() {
        assertEquals("\\u0001", OpenRouterJson.escapeJsonString("\u0001"))
    }
}
