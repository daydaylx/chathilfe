package de.disaai.chathilfe.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiConfigTest {

    @Test
    fun `placeholder key is not configured`() {
        assertFalse(isOpenRouterKeyConfigured(OPENROUTER_KEY_PLACEHOLDER))
        assertFalse(isOpenRouterKeyConfigured("replace_me_locally"))
    }

    @Test
    fun `blank or null key is not configured`() {
        assertFalse(isOpenRouterKeyConfigured(""))
        assertFalse(isOpenRouterKeyConfigured("   "))
        assertFalse(isOpenRouterKeyConfigured(null))
    }

    @Test
    fun `real-looking key is configured`() {
        assertTrue(isOpenRouterKeyConfigured("sk-or-v1-abc123"))
    }

    @Test
    fun `model and endpoint are pinned`() {
        assertEquals("anthropic/claude-sonnet-5", AiConfig.MODEL)
        assertTrue(AiConfig.ENDPOINT.startsWith("https://openrouter.ai/api/v1/"))
        assertTrue(AiConfig.MAX_TOKENS > 0)
    }
}
