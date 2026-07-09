package de.disaai.chathilfe.overlay

import android.content.Context
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
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
 * suggestions at a time, with pager navigation, copy, and a global/temporary retry area.
 * Never persists or logs suggestion text, retry selections, or clipboard content.
 *
 * Messenger-native capsule: compact pager row, readable suggestion body, primary Copy action,
 * then a small retry strip. No large branded card/header.
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
    /**
     * Per-pager-index local edits for the current panel session (Issue #21). Never persisted;
     * cleared on [show] / [replaceSuggestions]. Falls back to the original suggestion text when
     * no edit exists for an index.
     */
    private val editedTexts = mutableMapOf<Int, String>()

    private val editWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {
            val idx = pager.index
            if (idx >= 0) editedTexts[idx] = s?.toString().orEmpty()
        }
    }
    private val chipSelector = RetryChipSelector()
    private val chipViews = mutableMapOf<RetryInstruction, TextView>()

    private val positionLabel: TextView
    private val bodyEdit: EditText
    private val statusText: TextView
    private val retryButton: TextView
    private val retryProgress: ProgressBar

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_result_panel)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(OverlayStyle.SPACE_M), dp(OverlayStyle.SPACE_M), dp(OverlayStyle.SPACE_M), dp(OverlayStyle.SPACE_M))
        }

        // --- Pager row: ‹ 1/3 › … close ---
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
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                marginStart = dp(OverlayStyle.SPACE_XS)
                marginEnd = dp(OverlayStyle.SPACE_XS)
            }
        }
        val nextButton = iconButton(R.drawable.ic_chevron_right, R.string.result_panel_next_description) {
            pager.next(); refreshBody()
        }
        val spacer = View(context).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) }
        val closeButton = iconButton(R.drawable.ic_close, R.string.result_panel_close_description) {
            listener?.onClose()
        }
        headerRow.addView(previousButton)
        headerRow.addView(positionLabel)
        headerRow.addView(nextButton)
        headerRow.addView(spacer)
        headerRow.addView(closeButton)

        // --- Suggestion body: directly editable (Issue #21), capped and internally scrollable (#20) ---
        bodyEdit = EditText(context).apply {
            OverlayStyle.applyBodyText(this)
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine(false)
            setHorizontallyScrolling(false)
            isVerticalScrollBarEnabled = true
            // No underline: looks like body text but stays editable on tap (no separate edit mode).
            background = null
            isCursorVisible = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
        bodyEdit.addTextChangedListener(editWatcher)
        val bodyScroll = MaxHeightScrollView(context).apply {
            maxHeightPx = (resources.displayMetrics.heightPixels * (OverlayStyle.BODY_MAX_HEIGHT_FRACTION_PERCENT / 100f)).toInt()
            isVerticalScrollBarEnabled = true
            addView(bodyEdit)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(OverlayStyle.SPACE_S) }
        }

        // --- Copy: primary accent pill with copy icon + label ---
        val copyDrawable = ContextCompat.getDrawable(context, R.drawable.ic_copy)?.mutate()
        copyDrawable?.let { DrawableCompat.setTint(it, OverlayStyle.color(context, OverlayStyle.onAccent)) }
        val copyButton = TextView(context).apply {
            OverlayStyle.applyAccentPill(this)
            text = context.getString(R.string.result_panel_copy_button)
            gravity = Gravity.CENTER
            setCompoundDrawablesRelativeWithIntrinsicBounds(copyDrawable, null, null, null)
            compoundDrawablePadding = dp(OverlayStyle.SPACE_S)
            setOnClickListener {
                // Issue #21: copy the currently visible, possibly edited text — not the stored original.
                ClipboardHelper.writeText(context, bodyEdit.text.toString())
                Toast.makeText(context, R.string.result_panel_copied_toast, Toast.LENGTH_SHORT).show()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(OverlayStyle.SPACE_S) }
        }

        // --- Retry: compact strip ---
        val retryRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(OverlayStyle.SPACE_M), 0, 0)
        }
        retryRow.addView(
            TextView(context).apply {
                OverlayStyle.applySectionLabel(this)
                text = context.getString(R.string.result_panel_retry_prompt)
            },
        )
        retryRow.addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) })
        retryProgress = ProgressBar(context, null, android.R.attr.progressBarStyleSmall).apply {
            indeterminateTintList = android.content.res.ColorStateList.valueOf(OverlayStyle.color(context, OverlayStyle.text))
            visibility = GONE
            layoutParams = LinearLayout.LayoutParams(
                dp(OverlayStyle.ICON_SM),
                dp(OverlayStyle.ICON_SM),
            ).apply { marginStart = dp(OverlayStyle.SPACE_S) }
        }
        retryRow.addView(retryProgress)
        retryButton = TextView(context).apply {
            OverlayStyle.applyTextPill(this)
            text = context.getString(R.string.result_panel_retry_button)
            setOnClickListener { onRetry() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { marginStart = dp(OverlayStyle.SPACE_S) }
        }
        retryRow.addView(retryButton)

        val chipScroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
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
                ).apply { marginEnd = dp(OverlayStyle.SPACE_S) }
            }
            chipViews[chip] = chipView
            chipRow.addView(chipView)
        }
        chipScroll.addView(chipRow)

        statusText = TextView(context).apply {
            OverlayStyle.applySectionLabel(this)
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
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

    fun show(suggestions: List<ReplySuggestion>, originalText: String, tone: ToneOption) {
        this.suggestions = suggestions
        this.originalText = originalText
        this.tone = tone
        pager.reset(suggestions.size)
        chipSelector.clear()
        editedTexts.clear()
        refreshBody()
        refreshChips()
        // Avoid stealing focus / popping the IME the moment results appear.
        bodyEdit.clearFocus()
    }

    fun replaceSuggestions(suggestions: List<ReplySuggestion>) {
        this.suggestions = suggestions
        pager.reset(suggestions.size)
        chipSelector.clear()
        editedTexts.clear()
        refreshBody()
        refreshChips()
    }

    fun setLoading(active: Boolean) {
        retryButton.isEnabled = !active
        retryButton.alpha = if (active) 0.5f else 1f
        retryButton.text = context.getString(
            if (active) R.string.result_panel_retry_button_loading else R.string.result_panel_retry_button,
        )
        retryProgress.visibility = if (active) VISIBLE else GONE
        chipViews.values.forEach {
            it.isEnabled = !active
            it.alpha = if (active) 0.5f else 1f
        }
        statusText.text = if (active) context.getString(R.string.result_panel_loading) else ""
        statusText.setTextColor(OverlayStyle.color(context, OverlayStyle.textMuted))
        statusText.visibility = if (active) VISIBLE else GONE
    }

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
        val idx = pager.index
        val text = editedTexts[idx] ?: suggestions.getOrNull(idx)?.text.orEmpty()
        bodyEdit.removeTextChangedListener(editWatcher)
        bodyEdit.setText(text)
        if (text.isNotEmpty()) {
            bodyEdit.setSelection(text.length)
        }
        bodyEdit.addTextChangedListener(editWatcher)
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
        layoutParams = LinearLayout.LayoutParams(dp(OverlayStyle.ICON_BUTTON), dp(OverlayStyle.ICON_BUTTON))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
