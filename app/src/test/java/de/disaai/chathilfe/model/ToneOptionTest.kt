package de.disaai.chathilfe.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ToneOptionTest {

    @Test
    fun `default tone is Freundlich`() {
        assertEquals(ToneOption.FREUNDLICH, ToneOption.DEFAULT)
    }

    @Test
    fun `fromInternalValue resolves a known value`() {
        assertEquals(ToneOption.DIREKT, ToneOption.fromInternalValue("direkt"))
    }

    @Test
    fun `fromInternalValue falls back to default for null`() {
        assertEquals(ToneOption.DEFAULT, ToneOption.fromInternalValue(null))
    }

    @Test
    fun `fromInternalValue falls back to default for unknown value`() {
        assertEquals(ToneOption.DEFAULT, ToneOption.fromInternalValue("unbekannt"))
    }
}
