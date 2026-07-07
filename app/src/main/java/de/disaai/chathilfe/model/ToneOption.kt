package de.disaai.chathilfe.model

/** Tone/style options for the Input-Bar. Exactly one is active at a time; default is [FREUNDLICH]. */
enum class ToneOption(val internalValue: String, val label: String) {
    KURZ("kurz", "Kurz"),
    FREUNDLICH("freundlich", "Freundlich"),
    DIREKT("direkt", "Direkt"),
    ENTSCHULDIGEND("entschuldigend", "Sorry"),
    DEESKALIEREND("deeskalierend", "Sanft"),
    KLARE_GRENZE("klare_grenze", "Grenze"),
    FLIRTEND("flirtend", "Flirtend");

    companion object {
        val DEFAULT = FREUNDLICH

        fun fromInternalValue(value: String?): ToneOption =
            entries.firstOrNull { it.internalValue == value } ?: DEFAULT
    }
}
