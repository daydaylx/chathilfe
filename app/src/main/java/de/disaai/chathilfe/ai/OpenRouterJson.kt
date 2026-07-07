package de.disaai.chathilfe.ai

/**
 * Minimal, dependency-free JSON build + tolerant content extraction for the single OpenRouter
 * Chat Completions request/response this app makes. No `org.json`, no external lib — keeps the
 * MVP dependency-free (AGENTS.md) and the logic unit-testable on a plain JVM.
 *
 * Scope is intentionally narrow (one user message; one `content` field). This is NOT a general
 * JSON parser: request inputs are escaped per RFC 8259, response extraction finds the first
 * `"content"` string value and unescapes it, returning null on any problem (never throws).
 *
 * Privacy: never logs the prompt (user text) or the extracted content (suggestions).
 */
object OpenRouterJson {

    /**
     * Builds the OpenRouter Chat Completions request body.
     *
     * Contains `model`, `max_tokens` and a single user `message` whose content is the prompt.
     * Deliberately omits `temperature`, `top_p`, `top_k` (Claude Sonnet 5 + PROMPT_PARAMETER_POLICY)
     * and any reasoning/thinking parameter.
     */
    fun buildRequestBody(prompt: String, model: String, maxTokens: Int): String =
        "{\"model\":\"" + escapeJsonString(model) + "\"" +
            ",\"max_tokens\":" + maxTokens.toString() +
            ",\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJsonString(prompt) + "\"}]}"

    /**
     * Extracts the reply text from a standard OpenRouter Chat Completions response
     * (`choices[0].message.content`). Returns null if the field is missing, empty, or the body is
     * malformed. Never throws.
     */
    fun extractContent(responseBody: String): String? = try {
        val key = "\"content\""
        val idx = responseBody.indexOf(key)
        if (idx < 0) null else readStringValue(responseBody, idx + key.length)?.takeIf { it.isNotBlank() }
    } catch (t: Throwable) {
        null
    }

    /** Escapes [s] as a JSON string value (without surrounding quotes). RFC 8259. */
    internal fun escapeJsonString(s: String): String {
        val sb = StringBuilder(s.length + 8)
        for (ch in s) {
            when (ch) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\u0008' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (ch.code < 0x20) {
                    sb.append("\\u").append(String.format("%04x", ch.code))
                } else {
                    sb.append(ch)
                }
            }
        }
        return sb.toString()
    }

    /**
     * Starting at [from], skips whitespace and a single `:`, then reads a JSON string literal and
     * unescapes it. Returns null if no string literal starts there or it is malformed/terminated.
     */
    private fun readStringValue(body: String, from: Int): String? {
        var i = from
        val n = body.length
        while (i < n && body[i].isWhitespace()) i++
        if (i < n && body[i] == ':') i++
        while (i < n && body[i].isWhitespace()) i++
        if (i >= n || body[i] != '"') return null
        i++ // skip opening quote
        val sb = StringBuilder()
        while (i < n) {
            val c = body[i]
            if (c == '"') return sb.toString()
            if (c == '\\' && i + 1 < n) {
                when (val e = body[i + 1]) {
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    '/' -> sb.append('/')
                    'b' -> sb.append('\u0008')
                    'f' -> sb.append('\u000C')
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    't' -> sb.append('\t')
                    'u' -> {
                        if (i + 5 < n) {
                            val code = body.substring(i + 2, i + 6).toIntOrNull(16) ?: return null
                            sb.append(Char(code))
                            i += 4
                        } else return null
                    }
                    else -> return null
                }
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return null // unterminated string
    }
}
