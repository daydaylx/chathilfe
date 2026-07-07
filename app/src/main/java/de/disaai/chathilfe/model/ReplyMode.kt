package de.disaai.chathilfe.model

/**
 * Reply mode selected for a request. The UI decides the mode; the model must not guess.
 *
 * - [REPLY]: answer a copied/pasted message. Uses [ReplyRequest.copiedMessage].
 * - [COMPOSE]: write a new message from the user's intent.
 * - [REWRITE]: rewrite an existing text. Uses [ReplyRequest.originalText].
 *
 * See `docs/PROMPTS.md` ("Modus: Antworten / Formulieren / Umschreiben").
 */
enum class ReplyMode(val label: String) {
    REPLY("Antworten"),
    COMPOSE("Formulieren"),
    REWRITE("Umschreiben"),
}
