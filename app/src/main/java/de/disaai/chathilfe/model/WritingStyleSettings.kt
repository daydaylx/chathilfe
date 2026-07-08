package de.disaai.chathilfe.model

/**
 * User-selected writing-style preferences (Issue #8). Pure style values only — never user text,
 * clipboard content, suggestions or a persona. The fixed app voice is a static prompt rule
 * (docs/DECISIONS.md D-013) and is intentionally NOT a user setting here.
 *
 * Stored locally in DataStore as enum [internalValue] strings (docs/PRIVACY_SECURITY.md); never
 * logged together with user content. See `docs/PROMPTS.md` ("Schreibstil") for the prompt mapping.
 */

/** Reply length. */
enum class AnswerLength(val internalValue: String, val label: String) {
    SHORT("short", "Kurz"),
    NORMAL("normal", "Normal"),
    LONGER("longer", "Etwas länger");

    companion object {
        val DEFAULT = NORMAL
        fun fromInternalValue(value: String?): AnswerLength =
            entries.firstOrNull { it.internalValue == value } ?: DEFAULT
    }
}

/** Emoji usage. Default sparingly per Issue #8. */
enum class EmojiUsage(val internalValue: String, val label: String) {
    NONE("none", "Keine"),
    SPARING("sparing", "Sparsam"),
    NORMAL("normal", "Normal");

    companion object {
        val DEFAULT = SPARING
        fun fromInternalValue(value: String?): EmojiUsage =
            entries.firstOrNull { it.internalValue == value } ?: DEFAULT
    }
}

/** Punctuation style. */
enum class PunctuationStyle(val internalValue: String, val label: String) {
    CLEAN("clean", "Sauber"),
    RELAXED("relaxed", "Locker"),
    VERY_RELAXED("very_relaxed", "Sehr locker");

    companion object {
        val DEFAULT = RELAXED
        fun fromInternalValue(value: String?): PunctuationStyle =
            entries.firstOrNull { it.internalValue == value } ?: DEFAULT
    }
}

/** Capitalization style. */
enum class CapitalizationStyle(val internalValue: String, val label: String) {
    CORRECT("correct", "Korrekt"),
    RELAXED("relaxed", "Locker");

    companion object {
        val DEFAULT = CORRECT
        fun fromInternalValue(value: String?): CapitalizationStyle =
            entries.firstOrNull { it.internalValue == value } ?: DEFAULT
    }
}

/** Naturalness / anti-AI phrasing. Default "less AI" per Issue #8. */
enum class Naturalness(val internalValue: String, val label: String) {
    NORMAL("normal", "Normal"),
    LESS_AI("less_ai", "Weniger KI"),
    VERY_RELAXED("very_relaxed", "Sehr locker");

    companion object {
        val DEFAULT = LESS_AI
        fun fromInternalValue(value: String?): Naturalness =
            entries.firstOrNull { it.internalValue == value } ?: DEFAULT
    }
}

/**
 * The five user-tunable style values. Defaults follow Issue #8
 * (length=normal, emoji=sparingly, punctuation=relaxed, capitalization=correct,
 * naturalness=less AI). No persona field by design (D-013).
 */
data class WritingStyleSettings(
    val length: AnswerLength = AnswerLength.DEFAULT,
    val emojiUsage: EmojiUsage = EmojiUsage.DEFAULT,
    val punctuation: PunctuationStyle = PunctuationStyle.DEFAULT,
    val capitalization: CapitalizationStyle = CapitalizationStyle.DEFAULT,
    val naturalness: Naturalness = Naturalness.DEFAULT,
)
