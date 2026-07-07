package de.disaai.chathilfe.overlay

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import de.disaai.chathilfe.R
import de.disaai.chathilfe.clipboard.ClipboardHelper
import de.disaai.chathilfe.model.ReplySuggestion
import de.disaai.chathilfe.model.RetryChipSelector
import de.disaai.chathilfe.model.RetryInstruction
import de.disaai.chathilfe.model.ToneOption

/**
 * Classic Android View for the Result-Panel (no Compose, per D-003). Shows exactly one of the
 * 3 suggestions at a time, with pager navigation, copy, and a global/temporary retry area.
 * Never persists or logs suggestion text, retry selections, or clipboard content.
 *
 * Glass look via OverlayStyle / res/drawable. Navigation, copy and retry behavior unchanged.
 */
class ResultPanelView(context: Context) : FrameLayout(context) {

    interface Listener {
        fun onClose()
        fun onRetry(chips: Set<RetryInstruction>)
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
    private val statusText: TextView

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_result_panel)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }

        // --- Header: ‹  |  1/3 (pill)  |  ›  |  close ---
        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val previousButton = iconButton(R.drawable.ic_chevron_left, R.string.result_panel_previous_description) {
            pager.previous(); refreshBody()
        }
        positionLabel = TextView(context).apply {
            OverlayStyle.applyTextPill(this)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(6)
                marginEnd = dp(6)
            }
        }
        val nextButton = iconButton(R.drawable.ic_chevron_right, R.string.result_panel_next_description) {
            pager.next(); refreshBody()
        }
        val closeButton = iconButton(R.drawable.ic_close, R.string.result_panel_close_description) {
            listener?.onClose()
        }
        headerRow.addView(previousButton)
        headerRow.addView(positionLabel)
        headerRow.addView(nextButton)
        headerRow.addView(closeButton)

        // --- Body: capped, internally scrollable suggestion text ---
        bodyText = TextView(context).apply {
            OverlayStyle.applyBodyText(this)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
        val bodyScroll = MaxHeightScrollView(context).apply {
            maxHeightPx = (resources.displayMetrics.heightPixels * 0.4f).toInt()
            isVerticalScrollBarEnabled = true
            addView(bodyText)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }

        // --- Copy: primary accent pill with copy icon + label ---
        val copyDrawable = ContextCompat.getDrawable(context, R.drawable.ic_copy)?.mutate()
        copyDrawable?.let { DrawableCompat.setTint(it, OverlayStyle.color(context, OverlayStyle.onAccent)) }
        val copyButton = TextView(context).apply {
            OverlayStyle.applyAccentPill(this)
            text = context.getString(R.string.result_panel_copy_button)
            gravity = Gravity.CENTER
            setCompoundDrawablesRelativeWithIntrinsicBounds(copyDrawable, null, null, null)
            compoundDrawablePadding = dp(8)
            setOnClickListener {
                val current = suggestions.getOrNull(pager.index) ?: return@setOnClickListener
                ClipboardHelper.writeText(context, current.text)
                Toast.makeText(context, R.string.result_panel_copied_toast, Toast.LENGTH_SHORT).show()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(8)
            }
        }

        // --- Retry: global + compact ("Nicht passend?" … Nochmal) ---
        val retryRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(10) }
        }
        retryRow.addView(
            TextView(context).apply {
                text = context.getString(R.string.result_panel_retry_prompt)
                setTextColor(OverlayStyle.color(context, OverlayStyle.textMuted))
                textSize = OverlayStyle.TEXT_SIZE_HINT
            },
        )
        retryRow.addView(
            View(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
            },
        )
        retryRow.addView(
            TextView(context).apply {
                OverlayStyle.applyTextPill(this)
                text = context.getString(R.string.result_panel_retry_button)
                setOnClickListener { onRetry() }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { marginStart = dp(8) }
            },
        )

        // --- Change chips: horizontally scrollable, global, max 1-2 active ---
        val chipScroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(8) }
        }
        val chipRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        RetryInstruction.entries.filter { it != RetryInstruction.NOCHMAL }.forEach { chip ->
            val chipView = TextView(context).apply {
                OverlayStyle.applyChip(this, chipSelector.isActive(chip))
                text = chip.label
                setOnClickListener {
                    chipSelector.toggle(chip)
                    refreshChips()
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { marginEnd = dp(6) }
            }
            chipViews[chip] = chipView
            chipRow.addView(chipView)
        }
        chipScroll.addView(chipRow)

        statusText = TextView(context).apply {
            setTextColor(OverlayStyle.color(context, OverlayStyle.textMuted))
            textSize = OverlayStyle.TEXT_SIZE_HINT
            setPadding(0, dp(8), 0, 0)
            visibility = GONE
        }

        root.addView(headerRow)
        root.addView(bodyScroll)
        root.addView(copyButton)
        root.addView(retryRow)
        root.addView(chipScroll)
        root.addView(statusText)
        addView(root)
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

    /** Swaps suggestions for a retry while keeping stored text/tone; resets pager + chips. */
    fun replaceSuggestions(suggestions: List<ReplySuggestion>) {
        this.suggestions = suggestions
        pager.reset(suggestions.size)
        chipSelector.clear()
        refreshBody()
        refreshChips()
    }

    /** Compact muted loading hint during a retry; previous suggestions stay visible. */
    fun setLoading(active: Boolean) {
        statusText.text = if (active) context.getString(R.string.result_panel_loading) else ""
        statusText.setTextColor(OverlayStyle.color(context, OverlayStyle.textMuted))
        statusText.visibility = if (active) VISIBLE else GONE
    }

    /** Compact error message in error color (previous suggestions stay visible); null clears it. */
    fun showError(message: String?) {
        statusText.text = message ?: ""
        statusText.setTextColor(OverlayStyle.color(context, OverlayStyle.error))
        statusText.visibility = if (message != null) VISIBLE else GONE
    }

    private fun onRetry() {
        listener?.onRetry(chipSelector.active())
    }

    private fun refreshBody() {
        positionLabel.text = pager.positionLabel()
        bodyText.text = suggestions.getOrNull(pager.index)?.text.orEmpty()
    }

    private fun refreshChips() {
        chipViews.forEach { (chip, view) -> OverlayStyle.applyChip(view, chipSelector.isActive(chip)) }
    }

    private fun iconButton(
        iconRes: Int,
        descriptionRes: Int,
        onClick: () -> Unit,
    ): ImageView = ImageView(context).apply {
        OverlayStyle.applyIconButton(this, iconRes)
        contentDescription = context.getString(descriptionRes)
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(dp(OverlayStyle.ICON_SIZE_DP), dp(OverlayStyle.ICON_SIZE_DP))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
