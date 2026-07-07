package de.disaai.chathilfe.overlay

import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.disaai.chathilfe.R
import de.disaai.chathilfe.clipboard.ClipboardHelper
import de.disaai.chathilfe.model.ToneOption

/**
 * Classic Android View for the Input-Bar (no Compose, per D-003). Narrow start state of the
 * overlay: tone button, compact text field, paste button, start button. Start button is never
 * called "Senden". Never logs or stores typed text or clipboard content.
 */
class InputBarView(context: Context) : FrameLayout(context) {

    interface Listener {
        fun onToneSelected(tone: ToneOption)
        fun onStart(text: String, tone: ToneOption)
        fun onClose()
    }

    var listener: Listener? = null

    private var currentTone: ToneOption = ToneOption.DEFAULT

    private val toneButton: Button
    private val editText: EditText
    private val toneRow: LinearLayout

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_input_bar)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val mainRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        toneButton = Button(context).apply {
            text = currentTone.label
            contentDescription = context.getString(R.string.input_bar_tone_button_description)
            setOnClickListener {
                toneRow.visibility = if (toneRow.visibility == VISIBLE) GONE else VISIBLE
            }
        }

        editText = EditText(context).apply {
            hint = context.getString(R.string.input_bar_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            maxLines = 3
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val pasteButton = Button(context).apply {
            text = context.getString(R.string.input_bar_paste_button)
            setOnClickListener {
                val pasted = ClipboardHelper.readText(context)
                if (!pasted.isNullOrBlank()) {
                    editText.setText(pasted)
                    editText.setSelection(editText.text.length)
                }
            }
        }

        val startButton = Button(context).apply {
            text = context.getString(R.string.input_bar_start_button)
            setOnClickListener {
                listener?.onStart(editText.text.toString(), currentTone)
            }
        }

        val closeButton = Button(context).apply {
            text = context.getString(R.string.input_bar_close_button)
            contentDescription = context.getString(R.string.input_bar_close_description)
            setOnClickListener { listener?.onClose() }
        }

        mainRow.addView(toneButton)
        mainRow.addView(editText)
        mainRow.addView(pasteButton)
        mainRow.addView(startButton)
        mainRow.addView(closeButton)

        toneRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            visibility = GONE
            setPadding(dp(8), 0, dp(8), dp(8))
            ToneOption.entries.forEach { tone ->
                addView(
                    TextView(context).apply {
                        text = tone.label
                        setPadding(dp(8), dp(4), dp(8), dp(4))
                        setOnClickListener {
                            currentTone = tone
                            toneButton.text = tone.label
                            toneRow.visibility = GONE
                            listener?.onToneSelected(tone)
                        }
                    },
                )
            }
        }

        root.addView(mainRow)
        root.addView(toneRow)
        addView(root)
    }

    /** Sets the initial tone (e.g. restored from settings) without notifying the listener. */
    fun setTone(tone: ToneOption) {
        currentTone = tone
        toneButton.text = tone.label
    }

    fun currentText(): String = editText.text.toString()

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
