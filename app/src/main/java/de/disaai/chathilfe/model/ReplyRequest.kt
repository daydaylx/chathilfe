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
 * @property conversationContext optional transcript of a pasted WhatsApp dialog block
 *           (Issue #19 / docs/WHATSAPP_DIALOG_CONTEXT.md). Transient only: used solely as
 *           prompt context for the next request, never stored or logged. When present, the
 *           counterpart's latest message is carried via [copiedMessage] as the reply trigger.
 */
data class ReplyRequest(
    val mode: ReplyMode,
    val userIntent: String,
    val tone: ToneOption,
    val retryInstructions: Set<RetryInstruction> = emptySet(),
    val copiedMessage: String? = null,
    val originalText: String? = null,
    val conversationContext: String? = null,
)
