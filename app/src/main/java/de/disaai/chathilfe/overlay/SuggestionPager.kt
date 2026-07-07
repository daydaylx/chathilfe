package de.disaai.chathilfe.overlay

/**
 * Holds the current index into a fixed-size list of suggestions. Pure logic, no Android
 * dependencies, so it is unit-testable. Never persists suggestion or usage history.
 */
class SuggestionPager(size: Int = 0) {

    var size: Int = size
        private set

    var index: Int = 0
        private set

    fun reset(newSize: Int) {
        size = newSize
        index = 0
    }

    fun next(): Int {
        if (size > 0) index = (index + 1) % size
        return index
    }

    fun previous(): Int {
        if (size > 0) index = (index - 1 + size) % size
        return index
    }

    fun positionLabel(): String = if (size == 0) "0/0" else "${index + 1}/$size"
}
