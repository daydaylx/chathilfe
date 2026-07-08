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
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import de.disaai.chathilfe.R
import de.disaai.chathilfe.clipboard.ClipboardHelper
import de.disaai.chathilfe.model.ReplyMode
import de.disaai.chathilfe.model.ToneOption

/**
 * Classic Android View for the Input-Bar (no Compose, per D-003).
 *
 * Messenger-native capsule v2:
 * - top row: mode segmented control (Antworten / Schreiben) + close
 * - input row: tone pill + text field + paste
 * - reply-only intent chips: Zustimmen, Absagen, Entschuldigen, Nachfragen, Beruhigen, Grenze
 * - primary Los action
 *
 * Never logs or stores typed text, clipboard content, selected reply intents or suggestions.
 */
class InputBarView(context: Context) : FrameLayout(context) {

    interface Listener {
        fun onModeSelected(mode: ReplyMode)
        fun onToneSelected(tone: ToneOption)
        fun onStart(text: String, tone: ToneOption, mode: ReplyMode, replyIntent: String?)
        fun onClose()
    }

    private enum class ReplyIntentChip(val label: String, val promptText: String) {
        ZUSTIMMEN("Zustimmen", "zustimmen"),
        ABSAGEN("Absagen", "absagen"),
        ENTSCHULDIGEN("Entschuldigen", "sich entschuldigen"),
        NACHFRAGEN("Nachfragen", "nachfragen"),
        BERUHIGEN("Beruhigen", "beruhigen und deeskalieren"),
        GRENZE("Grenze", "eine klare Grenze setzen"),
    }

    var listener: Listener? = null

    private val visibleModes = listOf(ReplyMode.REPLY, ReplyMode.COMPOSE)
    private var currentMode: ReplyMode = ReplyMode.REPLY
    private var currentTone: ToneOption = ToneOption.DEFAULT
    private var selectedReplyIntent: ReplyIntentChip? = null

    private val toneButton: TextView
    private val editText: EditText
    private val toneScroll: HorizontalScrollView
    private val replyIntentScroll: HorizontalScrollView
    private val toneChips = mutableListOf<TextView>()
    private val segmentViews = mutableListOf<TextView>()
    private val intentChipViews = mutableMapOf<ReplyIntentChip, TextView>()
    private val pasteButton: ImageView
    private val startButton: TextView
    private val startProgress: ProgressBar
    private val statusText: TextView

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_input_bar)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(OverlayStyle.SPACE_M),
                dp(OverlayStyle.SPACE_S),
                dp(OverlayStyle.SPACE_M),
                dp(OverlayStyle.SPACE_M),
            )
        }

        // --- Top capsule row: [Antworten | Schreiben] … [close] ---
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val modeRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            background = ContextCompat.getDrawable(context, R.drawable.bg_segmented)
            contentDescription = context.getString(R.string.input_bar_mode_description)
            setPadding(dp(OverlayStyle.SPACE_XS), dp(OverlayStyle.SPACE_XS), dp(OverlayStyle.SPACE_XS), dp(OverlayStyle.SPACE_XS))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        visibleModes.forEachIndexed { index, mode ->
            val label = if (mode == ReplyMode.COMPOSE) context.getString(R.string.input_bar_mode_compose_short) else mode.label
            val segment = TextView(context).apply {
                OverlayStyle.applySegment(this, mode == currentMode)
                text = label
                setOnClickListener {
                    if (currentMode != mode) {
                        currentMode = mode
                        if (mode != ReplyMode.REPLY) selectedReplyIntent = null
                        refreshSegments()
                        refreshReplyIntentChips()
                        applyModeState()
                        listener?.onModeSelected(mode)
                    }
                }
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = if (index == visibleModes.lastIndex) 0 else dp(OverlayStyle.SPACE_XS)
                }
            }
            segmentViews.add(segment)
            modeRow.addView(segment)
        }
        val closeButton = iconButton(R.drawable.ic_close, R.string.input_bar_close_description) {
            listener?.onClose()
        }.apply {
            layoutParams = LinearLayout.LayoutParams(dp(OverlayStyle.ICON_BUTTON), dp(OverlayStyle.ICON_BUTTON)).apply {
                marginStart = dp(OverlayStyle.SPACE_S)
            }
        }
        topRow.addView(modeRow)
        topRow.addView(closeButton)

        // --- Input row: tone | message/intent field | paste ---
        val inputRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
        }
        toneButton = TextView(context).apply {
            OverlayStyle.applyTextPill(this)
            text = currentTone.label
            contentDescription = context.getString(R.string.input_bar_tone_button_description)
            setOnClickListener {
                toneScroll.visibility = if (toneScroll.visibility == VISIBLE) GONE else VISIBLE
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { marginEnd = dp(OverlayStyle.SPACE_S) }
        }
        editText = EditText(context).apply {
            setHintTextColor(OverlayStyle.color(context, OverlayStyle.textMuted))
            setTextColor(OverlayStyle.color(context, OverlayStyle.text))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            maxLines = 3
            minHeight = dp(OverlayStyle.ICON_BUTTON)
            background = ContextCompat.getDrawable(context, R.drawable.bg_input_field)
            setPadding(dp(OverlayStyle.SPACE_M), dp(OverlayStyle.SPACE_S), dp(OverlayStyle.SPACE_M), dp(OverlayStyle.SPACE_S))
            textSize = OverlayStyle.TEXT_BODY
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(OverlayStyle.SPACE_S)
            }
        }
        pasteButton = iconButton(R.drawable.ic_paste, R.string.input_bar_paste_button) {
            val pasted = ClipboardHelper.readText(context)
            if (!pasted.isNullOrBlank()) {
                editText.setText(pasted)
                editText.setSelection(editText.text.length)
            }
        }
        inputRow.addView(toneButton)
        inputRow.addView(editText)
        inputRow.addView(pasteButton)

        // --- Reply intent chips (only visible in Antworten mode) ---
        replyIntentScroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
        }
        val replyIntentRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        ReplyIntentChip.entries.forEach { chip ->
            val chipView = TextView(context).apply {
                OverlayStyle.applyChip(this, selectedReplyIntent == chip)
                text = chip.label
                setOnClickListener {
                    selectedReplyIntent = if (selectedReplyIntent == chip) null else chip
                    refreshReplyIntentChips()
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { marginEnd = dp(OverlayStyle.SPACE_S) }
            }
            intentChipViews[chip] = chipView
            replyIntentRow.addView(chipView)
        }
        replyIntentScroll.addView(replyIntentRow)

        // --- Tone picker (hidden until tone pill is tapped) ---
        toneScroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            visibility = GONE
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
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
                ).apply { marginEnd = dp(OverlayStyle.SPACE_S) }
            }
            toneChips.add(chip)
            toneRow.addView(chip)
        }
        toneScroll.addView(toneRow)

        // --- Action row: … Los → ---
        val actionRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
        }
        startButton = TextView(context).apply {
            OverlayStyle.applyAccentPill(this)
            text = context.getString(R.string.input_bar_start_button)
            val arrowDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_right)?.mutate()
            arrowDrawable?.let { DrawableCompat.setTint(it, OverlayStyle.color(context, OverlayStyle.onAccent)) }
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, arrowDrawable, null)
            compoundDrawablePadding = dp(OverlayStyle.SPACE_XS)
            setOnClickListener {
                listener?.onStart(
                    editText.text.toString(),
                    currentTone,
                    currentMode,
                    selectedReplyIntent?.promptText,
                )
            }
        }
        startProgress = ProgressBar(context, null, android.R.attr.progressBarStyleSmall).apply {
            indeterminateTintList = android.content.res.ColorStateList.valueOf(OverlayStyle.color(context, OverlayStyle.onAccent))
            visibility = GONE
            layoutParams = LinearLayout.LayoutParams(
                dp(OverlayStyle.ICON_SM),
                dp(OverlayStyle.ICON_SM),
            ).apply { marginEnd = dp(OverlayStyle.SPACE_S) }
        }
        actionRow.addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) })
        actionRow.addView(startProgress)
        actionRow.addView(startButton)

        statusText = TextView(context).apply {
            OverlayStyle.applySectionLabel(this)
            setPadding(dp(OverlayStyle.SPACE_XS), dp(OverlayStyle.SPACE_S), dp(OverlayStyle.SPACE_XS), 0)
            visibility = GONE
        }

        root.addView(topRow)
        root.addView(inputRow)
        root.addView(replyIntentScroll)
        root.addView(toneScroll)
        root.addView(actionRow)
        root.addView(statusText)
        addView(root)

        applyModeState()
    }

    fun setLoading(active: Boolean) {
        startButton.isEnabled = !active
        startButton.text = context.getString(
            if (active) R.string.input_bar_start_button_loading else R.string.input_bar_start_button,
        )
        startProgress.visibility = if (active) VISIBLE else GONE
        pasteButton.isEnabled = !active
        statusText.text = if (active) context.getString(R.string.input_bar_loading) else ""
        statusText.setTextColor(OverlayStyle.color(context, OverlayStyle.textMuted))
        statusText.visibility = if (active) VISIBLE else GONE
    }

    fun showError(message: String?) {
        statusText.text = message ?: ""
        statusText.setTextColor(OverlayStyle.color(context, OverlayStyle.error))
        statusText.visibility = if (message != null) VISIBLE else GONE
    }

    fun setTone(tone: ToneOption) {
        currentTone = tone
        toneButton.text = tone.label
        refreshToneChips()
    }

    fun setMode(mode: ReplyMode) {
        currentMode = if (mode in visibleModes) mode else ReplyMode.REPLY
        if (currentMode != ReplyMode.REPLY) selectedReplyIntent = null
        refreshSegments()
        refreshReplyIntentChips()
        applyModeState()
    }

    fun currentText(): String = editText.text.toString()

    private fun applyModeState() {
        editText.hint = context.getString(
            when (currentMode) {
                ReplyMode.REPLY -> R.string.input_bar_hint_reply
                ReplyMode.COMPOSE -> R.string.input_bar_hint
                ReplyMode.REWRITE -> R.string.input_bar_hint
            },
        )
        replyIntentScroll.visibility = if (currentMode == ReplyMode.REPLY) VISIBLE else GONE
    }

    private fun refreshSegments() {
        segmentViews.forEachIndexed { index, view ->
            OverlayStyle.applySegment(view, visibleModes[index] == currentMode)
        }
    }

    private fun refreshReplyIntentChips() {
        intentChipViews.forEach { (chip, view) ->
            OverlayStyle.applyChip(view, selectedReplyIntent == chip)
        }
    }

    private fun refreshToneChips() {
        toneChips.forEachIndexed { index, view ->
            OverlayStyle.applyChip(view, ToneOption.entries[index] == currentTone)
        }
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
        layoutParams = LinearLayout.LayoutParams(dp(OverlayStyle.ICON_BUTTON), dp(OverlayStyle.ICON_BUTTON))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
