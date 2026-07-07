package de.disaai.chathilfe.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryInstructionTest {

    @Test
    fun `toggle activates a chip`() {
        val selector = RetryChipSelector()
        selector.toggle(RetryInstruction.KUERZER)
        assertTrue(selector.isActive(RetryInstruction.KUERZER))
    }

    @Test
    fun `toggle again deactivates the chip`() {
        val selector = RetryChipSelector()
        selector.toggle(RetryInstruction.KUERZER)
        selector.toggle(RetryInstruction.KUERZER)
        assertFalse(selector.isActive(RetryInstruction.KUERZER))
    }

    @Test
    fun `at most two chips stay active, oldest evicted first`() {
        val selector = RetryChipSelector(maxActive = 2)
        selector.toggle(RetryInstruction.KUERZER)
        selector.toggle(RetryInstruction.DIREKTER)
        selector.toggle(RetryInstruction.SANFTER)

        assertEquals(2, selector.active().size)
        assertFalse(selector.isActive(RetryInstruction.KUERZER))
        assertTrue(selector.isActive(RetryInstruction.DIREKTER))
        assertTrue(selector.isActive(RetryInstruction.SANFTER))
    }

    @Test
    fun `clear removes all active chips`() {
        val selector = RetryChipSelector()
        selector.toggle(RetryInstruction.KUERZER)
        selector.clear()
        assertTrue(selector.active().isEmpty())
    }
}
