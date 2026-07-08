package de.disaai.chathilfe.ai

import de.disaai.chathilfe.BuildConfig

/**
 * OpenRouter configuration for the single MVP AI request path (D-001, D-007, D-009, D-012).
 *
 * One provider (OpenRouter), one pinned model ([MODEL]). No routing, no fallback. The API key is
 * read from Build-Time configuration only ([BuildConfig.OPENROUTER_API_KEY]) — never from
 * DataStore, never from UI, never logged. The real key lives only in the local, git-ignored
 * `local.properties`; a placeholder value means "not configured" (clear runtime message, no secret
 * leak).
 *
 * Privacy: this object never logs the key.
 */
object AiConfig {

    /** OpenRouter Chat Completions endpoint (OpenAI-compatible shape). */
    const val ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"

    /**
     * The single pinned MVP model: DeepSeek V4 Flash via OpenRouter (D-012).
     *
     * Verified against OpenRouter model metadata (2026-07-08): `deepseek/deepseek-v4-flash` is
     * available and supports `max_tokens`. Although this model also exposes sampling and reasoning
     * parameters, the MVP still sends no non-default temperature/top_p/top_k and no reasoning
     * parameter; style is controlled only via prompt, tone chips, writing-style settings and retry
     * chips.
     */
    const val MODEL = "deepseek/deepseek-v4-flash"

    /**
     * Output budget for exactly 3 short chat replies. `max_tokens` is an allowed output-budget
     * parameter (PROMPT_PARAMETER_POLICY.md: "ausreichend Output-Budget"); no sampling params.
     */
    const val MAX_TOKENS = 1024

    /** Connect/read timeout for the OpenRouter HTTP call, in milliseconds. */
    const val TIMEOUT_MS = 15_000

    /** Build-Time API key. Placeholder value when the local key is absent. */
    val apiKey: String = BuildConfig.OPENROUTER_API_KEY

    /** True only when a real (non-placeholder, non-blank) key is embedded in this build. */
    val isKeyConfigured: Boolean = isOpenRouterKeyConfigured(BuildConfig.OPENROUTER_API_KEY)
}

/** Placeholder emitted by the build when `OPENROUTER_API_KEY` is absent locally. */
internal const val OPENROUTER_KEY_PLACEHOLDER = "replace_me_locally"

/** A key counts as configured only if it is non-blank and not the build placeholder. */
internal fun isOpenRouterKeyConfigured(raw: String?): Boolean =
    !raw.isNullOrBlank() && raw != OPENROUTER_KEY_PLACEHOLDER
