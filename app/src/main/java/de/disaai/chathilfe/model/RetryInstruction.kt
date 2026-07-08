package de.disaai.chathilfe.model

/** Global, temporary retry/change chips shown after dummy suggestions. Never persisted. */
enum class RetryInstruction(val internalValue: String, val label: String) {
    NOCHMAL("nochmal", "Nochmal"),
    KUERZER("kuerzer", "Kürzer"),
    LOCKERER("lockerer", "Lockerer"),
    DIREKTER("direkter", "Direkter"),
    SANFTER("sanfter", "Sanfter"),
    KLARER("klarer", "Klarer"),
    WENIGER_KUENSTLICH("weniger_kuenstlich", "Weniger künstlich"),
    AUSFUEHRLICHER("ausfuehrlicher", "Ausführlicher"),
}

/**
 * Tracks which change chips are currently active. Instance-scoped to a single Result-Panel
 * session and cleared on close or after "Nochmal" - never persisted or logged (D-008).
 */
class RetryChipSelector(private val maxActive: Int = 2) {

    private val active = linkedSetOf<RetryInstruction>()

    fun toggle(chip: RetryInstruction): Set<RetryInstruction> {
        if (!active.remove(chip)) {
            if (active.size >= maxActive) {
                active.remove(active.first())
            }
            active.add(chip)
        }
        return active()
    }

    fun isActive(chip: RetryInstruction): Boolean = chip in active

    fun active(): Set<RetryInstruction> = active.toSet()

    fun clear() {
        active.clear()
    }
}
