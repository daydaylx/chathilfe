package de.disaai.chathilfe.clipboard

import android.content.ClipData
import android.content.Context
import android.content.ClipboardManager

/**
 * Reads/writes the system clipboard only in direct response to an explicit user tap
 * (Einfügen / Kopieren). No background monitoring, no history, no logging of content.
 */
object ClipboardHelper {

    fun readText(context: Context): String? {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return null
        if (!manager.hasPrimaryClip()) return null
        val clip = manager.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        val text = clip.getItemAt(0).coerceToText(context)?.toString()
        return text?.takeIf { it.isNotBlank() }
    }

    fun writeText(context: Context, text: String) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return
        manager.setPrimaryClip(ClipData.newPlainText(LABEL, text))
    }

    private const val LABEL = "ChatHilfe"
}
