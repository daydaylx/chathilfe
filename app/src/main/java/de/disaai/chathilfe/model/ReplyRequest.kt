package de.disaai.chathilfe.model

/**
 * All inputs for a single AI request.
 *
 * Transient only: never persisted or logged (see `docs/PRIVACY_SECURITY.md`).
 * [retryInstructions] refine only the next request and are discarded afterwards — they are
 * not stored, not logged and never interpreted as a user profile.
 *
 * @property mode reply mode; determines which fields are used.
 * @property userIntent what the user wants to express ("Nutzerabsicht").
 * @property tone desired tone/style.
 * @property retryInstructions optional change chips for the next attempt;
 *           [RetryInstruction.NOCHMAL] means "regenerate with identical inputs" and adds no
 *           text to the prompt.
 * @property copiedMessage the copied/pasted message; used for [ReplyMode.REPLY].
 * @property originalText the text to rewrite; used for [ReplyMode.REWRITE].
 */
data class ReplyRequest(
    val mode: ReplyMode,
    val userIntent: String,
    val tone: ToneOption,
    val retryInstructions: Set<RetryInstruction> = emptySet(),
    val copiedMessage: String? = null,
    val originalText: String? = null,
)
