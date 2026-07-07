package de.disaai.chathilfe.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestionPagerTest {

    @Test
    fun `starts at first index`() {
        val pager = SuggestionPager(3)
        assertEquals(0, pager.index)
        assertEquals("1/3", pager.positionLabel())
    }

    @Test
    fun `next wraps around to the first suggestion`() {
        val pager = SuggestionPager(3)
        pager.next()
        pager.next()
        assertEquals(2, pager.index)
        pager.next()
        assertEquals(0, pager.index)
    }

    @Test
    fun `previous wraps around to the last suggestion`() {
        val pager = SuggestionPager(3)
        pager.previous()
        assertEquals(2, pager.index)
    }

    @Test
    fun `reset changes size and returns to the first index`() {
        val pager = SuggestionPager(3)
        pager.next()
        pager.reset(5)
        assertEquals(0, pager.index)
        assertEquals("1/5", pager.positionLabel())
    }

    @Test
    fun `empty pager reports zero of zero and never moves`() {
        val pager = SuggestionPager(0)
        assertEquals("0/0", pager.positionLabel())
        pager.next()
        pager.previous()
        assertEquals(0, pager.index)
    }
}
