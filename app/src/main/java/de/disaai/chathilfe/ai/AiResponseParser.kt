package de.disaai.chathilfe.ai

import de.disaai.chathilfe.model.ReplySuggestion

/**
 * Result of parsing a raw model response into reply suggestions.
 *
 * The parser never throws: unrecoverable input becomes [Error], a usable subset becomes
 * [Partial]. This gives the AI client (Phase 7) a clear contract for "show 3", "show fewer"
 * and "show an error message" (see `docs/PROMPTS.md` "Parser-Regeln").
 */
sealed class ParseResult {
    /** The expected number of suggestions (3 in the MVP). */
    data class Success(val suggestions: List<ReplySuggestion>) : ParseResult()

    /** Fewer usable suggestions than requested, but at least one. The caller may still show them. */
    data class Partial(val suggestions: List<ReplySuggestion>, val info: String) : ParseResult()

    /** No usable suggestion could be extracted. */
    data class Error(val message: String) : ParseResult()
}

/**
 * Tolerant parser for AI reply suggestions. Accepts `1.`, `1)`, `-`, `*`, `•` markers and
 * falls back to blank-line paragraphs, then to single non-empty lines. Deduplicates identical
 * items and never crashes on bad model output.
 *
 * Does not log or persist any input.
 */
object AiResponseParser {

    private const val EXPECTED = 3

    // Anchored on a whole single line (parser feeds one trimmed line at a time).
    private val NUMBERED = Regex("""^\d+[.)]\s+(.+)$""")
    private val BULLETED = Regex("""^[-*•·–]\s+(.+)$""")

    fun parse(raw: String): ParseResult = try {
        val items = extractItems(raw)
        val cleaned = clean(items).map { ReplySuggestion(it) }
        when {
            cleaned.size >= EXPECTED -> ParseResult.Success(cleaned.take(EXPECTED))
            cleaned.isNotEmpty() -> ParseResult.Partial(
                cleaned,
                "Nur ${cleaned.size} von $EXPECTED Vorschlägen erkannt.",
            )
            else -> ParseResult.Error("Keine Vorschläge erkannt.")
        }
    } catch (t: Throwable) {
        ParseResult.Error("Antwort konnte nicht gelesen werden.")
    }

    private fun extractItems(raw: String): List<String> {
        val lines = raw.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // Prefer structured (numbered/bulleted) items. If at least one is found, ignore intro
        // lines entirely so "Hier sind drei Vorschläge:" is never treated as a suggestion.
        val structured = lines.mapNotNull { structuredText(it) }.filter { it.isNotEmpty() }
        if (structured.isNotEmpty()) return structured

        // Fallback: blank-line-separated paragraphs.
        val paragraphs = raw
            .split(Regex("\\n\\s*\\n"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (paragraphs.size >= EXPECTED) return paragraphs

        // Last resort: every non-empty line.
        return lines
    }

    private fun structuredText(line: String): String? =
        (NUMBERED.find(line)?.groupValues?.get(1) ?: BULLETED.find(line)?.groupValues?.get(1))?.trim()

    private fun clean(items: List<String>): List<String> {
        val seen = LinkedHashSet<String>()
        for (item in items) {
            val trimmed = item.trim()
            if (trimmed.isNotEmpty()) seen.add(trimmed)
        }
        return seen.toList()
    }
}
