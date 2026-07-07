package de.disaai.chathilfe.overlay

import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.disaai.chathilfe.R
import de.disaai.chathilfe.clipboard.ClipboardHelper
import de.disaai.chathilfe.model.ToneOption

/**
 * Classic Android View for the Input-Bar (no Compose, per D-003). Narrow start state of the
 * overlay. Two compact rows so the text field stays usable on phone width:
 *
 *   row 1: [Ton] [================ Textfeld ================] [×]
 *   row 2: [Einfügen] ……… [Los]
 *
 * Start button is never called "Senden". Never logs or stores typed text or clipboard content.
 * Glass look via OverlayStyle / res/drawable; behavior unchanged from the visual refresh.
 */
class InputBarView(context: Context) : FrameLayout(context) {

    interface Listener {
        fun onToneSelected(tone: ToneOption)
        fun onStart(text: String, tone: ToneOption)
        fun onClose()
    }

    var listener: Listener? = null

    private var currentTone: ToneOption = ToneOption.DEFAULT

    private val toneButton: TextView
    private val editText: EditText
    private val toneScroll: HorizontalScrollView
    private val toneChips = mutableListOf<TextView>()
    private val pasteButton: ImageView
    private val startButton: ImageView
    private val statusText: TextView

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_input_bar)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        // --- Row 1: tone | text field (dominant) | close ---
        val inputRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(10), dp(6), dp(4))
        }

        toneButton = TextView(context).apply {
            OverlayStyle.applyTextPill(this)
            text = currentTone.label
            contentDescription = context.getString(R.string.input_bar_tone_button_description)
            setOnClickListener { toneScroll.visibility = if (toneScroll.visibility == VISIBLE) GONE else VISIBLE }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { marginEnd = dp(6) }
        }

        editText = EditText(context).apply {
            hint = context.getString(R.string.input_bar_hint)
            setHintTextColor(OverlayStyle.color(context, OverlayStyle.textMuted))
            setTextColor(OverlayStyle.color(context, OverlayStyle.text))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            maxLines = 3
            minHeight = dp(40)
            background = ContextCompat.getDrawable(context, R.drawable.bg_input_field)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            textSize = OverlayStyle.TEXT_SIZE_BODY
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(2)
                marginEnd = dp(4)
            }
        }

        val closeButton = iconButton(R.drawable.ic_close, R.string.input_bar_close_description) {
            listener?.onClose()
        }

        inputRow.addView(toneButton)
        inputRow.addView(editText)
        inputRow.addView(closeButton)

        // --- Row 2: paste (left) … start (right, primary accent) ---
        val actionRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), 0, dp(6), dp(10))
        }

        pasteButton = iconButton(R.drawable.ic_paste, R.string.input_bar_paste_button) {
            val pasted = ClipboardHelper.readText(context)
            if (!pasted.isNullOrBlank()) {
                editText.setText(pasted)
                editText.setSelection(editText.text.length)
            }
        }

        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }

        startButton = iconButton(R.drawable.ic_arrow_right, R.string.input_bar_start_button, accent = true) {
            listener?.onStart(editText.text.toString(), currentTone)
        }

        actionRow.addView(pasteButton)
        actionRow.addView(spacer)
        actionRow.addView(startButton)

        // --- Tone picker (horizontally scrollable, hidden until tone pill is tapped) ---
        toneScroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            visibility = GONE
            setPadding(dp(10), 0, dp(6), dp(6))
        }
        val toneRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        ToneOption.entries.forEach { tone ->
            val chip = TextView(context).apply {
                OverlayStyle.applyChip(this, tone == currentTone)
                text = tone.label
                setOnClickListener {
                    currentTone = tone
                    toneButton.text = tone.label
                    refreshToneChips()
                    toneScroll.visibility = GONE
                    listener?.onToneSelected(tone)
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { marginEnd = dp(6) }
            }
            toneChips.add(chip)
            toneRow.addView(chip)
        }
        toneScroll.addView(toneRow)

        statusText = TextView(context).apply {
            setTextColor(OverlayStyle.color(context, OverlayStyle.textMuted))
            textSize = OverlayStyle.TEXT_SIZE_HINT
            setPadding(dp(12), 0, dp(12), dp(8))
            visibility = GONE
        }

        root.addView(inputRow)
        root.addView(actionRow)
        root.addView(toneScroll)
        root.addView(statusText)
        addView(root)
    }

    /** Disables start/paste and shows a compact muted loading hint while an AI request is in flight. */
    fun setLoading(active: Boolean) {
        startButton.isEnabled = !active
        pasteButton.isEnabled = !active
        statusText.text = if (active) context.getString(R.string.input_bar_loading) else ""
        statusText.setTextColor(OverlayStyle.color(context, OverlayStyle.textMuted))
        statusText.visibility = if (active) VISIBLE else GONE
    }

    /** Shows a compact error message below the input row; null clears it. */
    fun showError(message: String?) {
        statusText.text = message ?: ""
        statusText.setTextColor(OverlayStyle.color(context, OverlayStyle.error))
        statusText.visibility = if (message != null) VISIBLE else GONE
    }

    /** Sets the initial tone (e.g. restored from settings) without notifying the listener. */
    fun setTone(tone: ToneOption) {
        currentTone = tone
        toneButton.text = tone.label
        refreshToneChips()
    }

    fun currentText(): String = editText.text.toString()

    private fun refreshToneChips() {
        toneChips.forEach { OverlayStyle.applyChip(it, it.text == currentTone.label) }
    }

    private fun iconButton(
        iconRes: Int,
        descriptionRes: Int,
        accent: Boolean = false,
        onClick: () -> Unit,
    ): ImageView = ImageView(context).apply {
        OverlayStyle.applyIconButton(this, iconRes, accent)
        contentDescription = context.getString(descriptionRes)
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(dp(OverlayStyle.ICON_SIZE_DP), dp(OverlayStyle.ICON_SIZE_DP)).apply {
            marginStart = dp(2)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
