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
     * The single pinned MVP model: Anthropic Claude Sonnet 5 via OpenRouter (D-012).
     *
     * Verified against OpenRouter model metadata (2026-07-07): `supported_parameters` contains
     * `max_tokens`/`max_completion_tokens` but NOT `temperature`/`top_p`/`top_k` — which matches
     * `docs/PROMPT_PARAMETER_POLICY.md` ("keine non-default temperature/top_p/top_k für Claude
     * Sonnet 5"). Style is controlled only via prompt, tone chips and retry chips.
     */
    const val MODEL = "anthropic/claude-sonnet-5"

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
