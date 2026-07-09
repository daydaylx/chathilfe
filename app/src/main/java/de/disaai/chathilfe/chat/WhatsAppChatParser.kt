package de.disaai.chathilfe.chat

/**
 * Pure-Kotlin parser for WhatsApp chat blocks a user consciously pastes into the Input-Bar
 * (Issue #19 / docs/WHATSAPP_DIALOG_CONTEXT.md).
 *
 * It detects the WhatsApp export layout `[date, time] sender: message`, tolerates a few
 * date/time variants and multi-line messages, and best-effort labels who is "self" vs "other" —
 * without ever claiming a hard identity. The result is transient: it lives only for the next
 * request and is never stored or logged (see docs/PRIVACY_SECURITY.md).
 *
 * The parser activates only when at least two lines match the WhatsApp pattern; otherwise it
 * returns `null` and the caller falls back to the normal single-text reply flow, so a normal
 * pasted message is never misread as a chat transcript.
 */
object WhatsAppChatParser {

    // `[date, time] sender: message`. Date is kept deliberately permissive (1.7., 01.07.26,
    // 1.7.2026, …); time is h:mm or h:mm:ss. Sender runs up to the first colon, so names with
    // spaces work; the message body is everything after the first colon (may itself contain ":").
    private val LINE = Regex(
        """^\[(.+?),\s*(\d{1,2}:\d{2}(?::\d{2})?)\]\s*([^:]+):\s*(.*)${'$'}"""
    )

    // Lowercase tokens that read like the user referring to themselves.
    private val SELF_KEYWORDS = setOf("ich", "me", "i", "mich", "mein", "meine")

    /**
     * Parses [text]. Returns a [ParsedChatContext] only when at least two WhatsApp-style lines
     * are found; otherwise `null` (single-text fallback).
     */
    fun parse(text: String): ParsedChatContext? {
        val messages = mutableListOf<ParsedChatMessage>()
        for (rawLine in text.split("\n")) {
            val line = rawLine.trimEnd('\r').trim()
            val match = LINE.find(line)
            if (match != null) {
                val (date, time, sender, body) = match.destructured
                messages.add(
                    ParsedChatMessage(
                        rawDate = date.trim(),
                        rawTime = time.trim(),
                        sender = sender.trim(),
                        text = body.trim(),
                    )
                )
            } else if (messages.isNotEmpty()) {
                // Continuation line of the previous (multi-line) message. Blank lines are skipped.
                if (line.isBlank()) continue
                val last = messages.last()
                val appended = if (last.text.isEmpty()) line else "${last.text}\n$line"
                messages[messages.lastIndex] = last.copy(text = appended)
            }
            // Lines before the first match are ignored; with <2 matches we fall back anyway.
        }

        if (messages.size < 2) return null

        val senders = messages.map { it.sender }.distinct()
        val (self, other) = guessRoles(senders)

        val latestOther = if (other != null) {
            messages.lastOrNull { it.sender == other }
        } else {
            // No confident role split: best effort, the most recent message is the trigger.
            messages.lastOrNull()
        }

        return ParsedChatContext(
            messages = messages,
            likelySelfSender = self,
            likelyOtherSender = other,
            latestOtherMessage = latestOther,
        )
    }

    /**
     * Conservative role guess. Returns `(self, other)` or `(null, null)` when uncertain.
     * Rule: with exactly two senders, a very short or self-like name (initial, "Ich", "Me", …)
     * is treated as the user's own sender. Never claims identity otherwise — the field stays
     * `likely*` on purpose.
     */
    private fun guessRoles(senders: List<String>): Pair<String?, String?> {
        if (senders.size != 2) return null to null
        val a = senders[0]
        val b = senders[1]
        fun looksLikeSelf(name: String): Boolean =
            name.length <= 2 || name.lowercase() in SELF_KEYWORDS
        return when {
            looksLikeSelf(a) && !looksLikeSelf(b) -> a to b
            looksLikeSelf(b) && !looksLikeSelf(a) -> b to a
            else -> null to null
        }
    }

    /** Compact transcript line for the prompt, e.g. `Anke: …`. */
    fun formatMessage(message: ParsedChatMessage): String =
        "${message.sender}: ${message.text}"
}

/**
 * One parsed WhatsApp line. Transient only — never persisted or logged.
 *
 * @property rawDate raw date token as written by WhatsApp (e.g. `1.7.`, `01.07.26`).
 * @property rawTime raw time token (e.g. `18:02`, `18:02:33`).
 * @property sender display name as exported; may be a self-reference heuristic only.
 * @property text message body, multi-line messages joined with `\n`.
 */
data class ParsedChatMessage(
    val rawDate: String,
    val rawTime: String,
    val sender: String,
    val text: String,
)

/**
 * Structured view of a pasted chat block. Transient only — never persisted or logged.
 *
 * @property messages parsed lines in order.
 * @property likelySelfSender heuristic guess for the user's own sender, or null if uncertain.
 * @property likelyOtherSender heuristic guess for the counterpart, or null if uncertain.
 * @property latestOtherMessage best guess for the message to reply to (last message of the
 *           counterpart when roles are clear, otherwise the most recent message).
 */
data class ParsedChatContext(
    val messages: List<ParsedChatMessage>,
    val likelySelfSender: String?,
    val likelyOtherSender: String?,
    val latestOtherMessage: ParsedChatMessage?,
) {
    /**
     * Compact transcript for the prompt's conversation-context section, or null if there is
     * nothing usable. Order follows [messages]; each line is `sender: text`.
     */
    fun formatContext(): String? =
        messages
            .joinToString("\n") { WhatsAppChatParser.formatMessage(it) }
            .takeIf { it.isNotBlank() }
}
