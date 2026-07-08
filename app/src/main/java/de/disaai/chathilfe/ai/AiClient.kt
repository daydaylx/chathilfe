package de.disaai.chathilfe.ai

import de.disaai.chathilfe.model.ReplyRequest
import de.disaai.chathilfe.model.WritingStyleSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sends a single OpenRouter Chat Completions request for a [ReplyRequest] and maps the result to
 * a [ParseResult]. One provider, one pinned model (see [AiConfig], D-012). No retries, no
 * fallback, no model routing. suspend + [Dispatchers.IO] for the network call; the caller is
 * responsible for not issuing parallel requests.
 *
 * Privacy: never logs the API key, the prompt (user text), retry instructions or the returned
 * suggestions. On HTTP errors only a coarse, fixed German message is returned — never the response
 * body, which could echo input or carry provider-internal detail.
 */
class AiClient {

    suspend fun request(
        req: ReplyRequest,
        style: WritingStyleSettings = WritingStyleSettings(),
    ): ParseResult = withContext(Dispatchers.IO) {
        if (!AiConfig.isKeyConfigured) {
            return@withContext ParseResult.Error("Kein API-Key konfiguriert.")
        }
        val prompt = PromptBuilder.build(req, style)
        val body = OpenRouterJson.buildRequestBody(prompt, AiConfig.MODEL, AiConfig.MAX_TOKENS)

        var conn: HttpURLConnection? = null
        try {
            conn = (URL(AiConfig.ENDPOINT).openConnection() as HttpURLConnection).apply {
                connectTimeout = AiConfig.TIMEOUT_MS
                readTimeout = AiConfig.TIMEOUT_MS
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Authorization", "Bearer ${AiConfig.apiKey}")
                setRequestProperty("Content-Type", "application/json")
            }
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            if (code in 200..299) {
                val raw = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
                val content = OpenRouterJson.extractContent(raw)
                if (content.isNullOrBlank()) {
                    ParseResult.Error("Leere Antwort der KI.")
                } else {
                    AiResponseParser.parse(content)
                }
            } else {
                ParseResult.Error(httpErrorMessage(code))
            }
        } catch (e: IOException) {
            ParseResult.Error("Kein Internet oder Zeitüberschreitung.")
        } catch (e: Throwable) {
            ParseResult.Error("Anfrage fehlgeschlagen.")
        } finally {
            conn?.disconnect()
        }
    }

    private fun httpErrorMessage(code: Int): String = when (code) {
        401, 403 -> "API-Key nicht gültig."
        429 -> "Zu viele Anfragen. Bitte später erneut versuchen."
        in 500..599 -> "KI-Anbieter antwortet nicht."
        else -> "Anfrage fehlgeschlagen."
    }
}
