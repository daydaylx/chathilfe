package de.disaai.chathilfe.overlay

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import de.disaai.chathilfe.R
import de.disaai.chathilfe.clipboard.ClipboardHelper
import de.disaai.chathilfe.model.ReplySuggestion
import de.disaai.chathilfe.model.RetryChipSelector
import de.disaai.chathilfe.model.RetryInstruction
import de.disaai.chathilfe.model.ToneOption

/**
 * Classic Android View for the Result-Panel (no Compose, per D-003). Shows exactly one of the
 * 3 dummy suggestions at a time, with pager navigation, copy, and a global/temporary retry area.
 * Never persists or logs suggestion text, retry selections, or clipboard content.
 */
class ResultPanelView(context: Context) : FrameLayout(context) {

    interface Listener {
        fun onClose()
    }

    var listener: Listener? = null

    private var suggestions: List<ReplySuggestion> = emptyList()
    private var originalText: String = ""
    private var tone: ToneOption = ToneOption.DEFAULT
    private val pager = SuggestionPager()
    private val chipSelector = RetryChipSelector()
    private val chipViews = mutableMapOf<RetryInstruction, TextView>()

    private val positionLabel: TextView
    private val bodyText: TextView

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_result_panel)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val previousButton = Button(context).apply {
            text = "<"
            contentDescription = context.getString(R.string.result_panel_previous_description)
            setOnClickListener { pager.previous(); refreshBody() }
        }
        positionLabel = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER
        }
        val nextButton = Button(context).apply {
            text = ">"
            contentDescription = context.getString(R.string.result_panel_next_description)
            setOnClickListener { pager.next(); refreshBody() }
        }
        val closeButton = Button(context).apply {
            text = "x"
            contentDescription = context.getString(R.string.result_panel_close_description)
            setOnClickListener { listener?.onClose() }
        }
        headerRow.addView(previousButton)
        headerRow.addView(positionLabel)
        headerRow.addView(nextButton)
        headerRow.addView(closeButton)

        bodyText = TextView(context).apply {
            setPadding(0, dp(8), 0, dp(8))
        }

        val copyButton = Button(context).apply {
            text = context.getString(R.string.result_panel_copy_button)
            setOnClickListener {
                val current = suggestions.getOrNull(pager.index) ?: return@setOnClickListener
                ClipboardHelper.writeText(context, current.text)
                Toast.makeText(context, R.string.result_panel_copied_toast, Toast.LENGTH_SHORT).show()
            }
        }

        val retryRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        retryRow.addView(
            TextView(context).apply {
                text = context.getString(R.string.result_panel_retry_prompt)
            },
        )
        retryRow.addView(
            Button(context).apply {
                text = context.getString(R.string.result_panel_retry_button)
                setOnClickListener { onRetry() }
            },
        )

        val chipScroll = HorizontalScrollView(context)
        val chipRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        RetryInstruction.entries.filter { it != RetryInstruction.NOCHMAL }.forEach { chip ->
            val chipView = TextView(context).apply {
                text = chip.label
                setPadding(dp(10), dp(6), dp(10), dp(6))
                setOnClickListener {
                    chipSelector.toggle(chip)
                    refreshChips()
                }
            }
            chipViews[chip] = chipView
            chipRow.addView(
                chipView,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { marginEnd = dp(6) },
            )
        }
        chipScroll.addView(chipRow)

        root.addView(headerRow)
        root.addView(bodyText)
        root.addView(copyButton)
        root.addView(retryRow)
        root.addView(chipScroll)
        addView(root)

        refreshChips()
    }

    /** Replaces the shown suggestions and resets pager/retry state. */
    fun show(suggestions: List<ReplySuggestion>, originalText: String, tone: ToneOption) {
        this.suggestions = suggestions
        this.originalText = originalText
        this.tone = tone
        pager.reset(suggestions.size)
        chipSelector.clear()
        refreshBody()
        refreshChips()
    }

    private fun onRetry() {
        val regenerated = DummySuggestionSource.generate(originalText, tone, chipSelector.active())
        chipSelector.clear()
        show(regenerated, originalText, tone)
    }

    private fun refreshBody() {
        positionLabel.text = pager.positionLabel()
        bodyText.text = suggestions.getOrNull(pager.index)?.text.orEmpty()
    }

    private fun refreshChips() {
        chipViews.forEach { (chip, view) ->
            view.background = ContextCompat.getDrawable(
                context,
                if (chipSelector.isActive(chip)) R.drawable.bg_chip_selected else R.drawable.bg_chip,
            )
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
