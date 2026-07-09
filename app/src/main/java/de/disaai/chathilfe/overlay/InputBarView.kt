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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import de.disaai.chathilfe.R
import de.disaai.chathilfe.clipboard.ClipboardHelper
import de.disaai.chathilfe.model.AnswerLength
import de.disaai.chathilfe.model.CapitalizationStyle
import de.disaai.chathilfe.model.EmojiUsage
import de.disaai.chathilfe.model.Naturalness
import de.disaai.chathilfe.model.PunctuationStyle
import de.disaai.chathilfe.model.ReplyMode
import de.disaai.chathilfe.model.ToneOption
import de.disaai.chathilfe.model.WritingStyleSettings

/**
 * Messenger-native capsule Input-Bar (classic Android Views, per D-003).
 *
 * Layout:
 * - top row: [Ton/Stil] [Antworten|Schreiben] [×]
 * - style panel (collapsible): compact chip rows for tone + 5 writing-style dimensions
 * - context preview (reply mode only)
 * - intent chips (reply mode only): single-select intent hints
 * - input row: text field + paste + arrow start
 * - status line for loading/error
 *
 * Never logs or stores typed text, clipboard content or suggestions.
 */
class InputBarView(context: Context) : FrameLayout(context) {

    interface Listener {
        fun onModeSelected(mode: ReplyMode)
        fun onStart(text: String, mode: ReplyMode, intentHint: String?)
        fun onStyleChanged(style: WritingStyleSettings)
        fun onToneSelected(tone: ToneOption)
        fun onClose()
    }

    var listener: Listener? = null

    private val visibleModes = listOf(ReplyMode.REPLY, ReplyMode.COMPOSE)
    private var currentMode: ReplyMode = ReplyMode.REPLY

    // Transient writing-style state — initialised from defaults, mutated via chip taps,
    // reported to the service through onStyleChanged.
    private var styleState = WritingStyleSettings()
    private var currentTone: ToneOption = ToneOption.DEFAULT

    // Single-select intent hint for reply mode. null = none selected.
    private var selectedIntentHint: String? = null

    // --- Views ---
    private val editText: EditText
    private val pasteButton: ImageView
    private val startButton: TextView
    private val startProgress: ProgressBar
    private val statusText: TextView
    private val contextPreviewRow: LinearLayout
    private val contextCaption: TextView
    private val segmentViews = mutableListOf<TextView>()

    // Style panel
    private val stylePanel: LinearLayout
    private val stylePanelVisible: Boolean get() = stylePanel.isVisible
    private val toneChipViews = mutableMapOf<ToneOption, TextView>()

    // Dimension chip rows: each maps enum entries → chip views
    private val lengthChipViews = mutableMapOf<AnswerLength, TextView>()
    private val emojiChipViews = mutableMapOf<EmojiUsage, TextView>()
    private val punctuationChipViews = mutableMapOf<PunctuationStyle, TextView>()
    private val capitalizationChipViews = mutableMapOf<CapitalizationStyle, TextView>()
    private val naturalnessChipViews = mutableMapOf<Naturalness, TextView>()

    // Intent chips
    private val intentChipRow: LinearLayout
    private val intentChipViews = mutableMapOf<String, TextView>()

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

        // --- Top row: [Ton/Stil] [Antworten | Schreiben] [×] ---
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Ton/Stil button — toggles the style sub-panel
        val styleButton = TextView(context).apply {
            OverlayStyle.applyTextPill(this)
            text = context.getString(R.string.input_bar_style_button)
            setOnClickListener { toggleStylePanel() }
        }

        val modeRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            background = ContextCompat.getDrawable(context, R.drawable.bg_segmented)
            contentDescription = context.getString(R.string.input_bar_mode_description)
            setPadding(
                dp(OverlayStyle.SPACE_XS), dp(OverlayStyle.SPACE_XS),
                dp(OverlayStyle.SPACE_XS), dp(OverlayStyle.SPACE_XS),
            )
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(OverlayStyle.SPACE_S)
            }
        }
        visibleModes.forEachIndexed { index, mode ->
            val label = if (mode == ReplyMode.COMPOSE)
                context.getString(R.string.input_bar_mode_compose_short) else mode.label
            val segment = TextView(context).apply {
                OverlayStyle.applySegment(this, mode == currentMode)
                text = label
                setOnClickListener {
                    if (currentMode != mode) {
                        currentMode = mode
                        refreshSegments()
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

        val closeButton = ImageView(context).apply {
            OverlayStyle.applyIconButton(this, R.drawable.ic_close)
            contentDescription = context.getString(R.string.input_bar_close_description)
            setOnClickListener { listener?.onClose() }
            layoutParams = LinearLayout.LayoutParams(
                dp(OverlayStyle.ICON_BUTTON), dp(OverlayStyle.ICON_BUTTON),
            ).apply { marginStart = dp(OverlayStyle.SPACE_S) }
        }

        topRow.addView(styleButton)
        topRow.addView(modeRow)
        topRow.addView(closeButton)

        // --- Style panel: compact, collapsible chip grid for tone + writing-style dimensions ---
        stylePanel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = GONE
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
        }
        stylePanel.addView(buildStylePanelContent())

        // --- Context preview (Antworten only) ---
        contextPreviewRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = GONE
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
        }
        contextCaption = TextView(context).apply {
            OverlayStyle.applySectionLabel(this)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(OverlayStyle.SPACE_S)
            }
        }
        val contextRemoveButton = ImageView(context).apply {
            OverlayStyle.applyIconButton(this, R.drawable.ic_close)
            contentDescription = context.getString(R.string.input_bar_context_remove)
            setOnClickListener {
                editText.setText("")
                updateContextPreview()
            }
        }
        contextPreviewRow.addView(contextCaption)
        contextPreviewRow.addView(contextRemoveButton)

        // --- Intent chips (Antworten mode only) ---
        intentChipRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            visibility = GONE
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
        }
        val intentScroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
        REPLY_INTENT_CHIPS.forEach { intentLabel ->
            val chip = TextView(context).apply {
                OverlayStyle.applyChip(this, selected = false)
                text = intentLabel
                setOnClickListener {
                    val wasSelected = selectedIntentHint == intentLabel
                    selectedIntentHint = if (wasSelected) null else intentLabel
                    refreshIntentChips()
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { marginEnd = dp(OverlayStyle.SPACE_S) }
            }
            intentChipViews[intentLabel] = chip
            intentChipRow.addView(chip)
        }
        intentScroll.addView(intentChipRow)

        // --- Input row: [EditText] [Einfügen] [→] ---
        val inputRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
        }
        editText = EditText(context).apply {
            setHintTextColor(OverlayStyle.color(context, OverlayStyle.textMuted))
            setTextColor(OverlayStyle.color(context, OverlayStyle.text))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            // Issue #20: let pasted WhatsApp dialogs grow up to 5 lines, then scroll internally.
            maxLines = 5
            isVerticalScrollBarEnabled = true
            minHeight = dp(OverlayStyle.ICON_BUTTON)
            background = ContextCompat.getDrawable(context, R.drawable.bg_input_field)
            setPadding(
                dp(OverlayStyle.SPACE_M), dp(OverlayStyle.SPACE_S),
                dp(OverlayStyle.SPACE_M), dp(OverlayStyle.SPACE_S),
            )
            textSize = OverlayStyle.TEXT_BODY
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(OverlayStyle.SPACE_S)
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    if (currentMode == ReplyMode.REPLY) updateContextPreview()
                }
            })
        }
        pasteButton = ImageView(context).apply {
            OverlayStyle.applyIconButton(this, R.drawable.ic_paste)
            contentDescription = context.getString(R.string.input_bar_paste_button)
            setOnClickListener {
                val pasted = ClipboardHelper.readText(context)
                if (!pasted.isNullOrBlank()) {
                    editText.setText(pasted)
                    editText.setSelection(editText.text.length)
                }
            }
            layoutParams = LinearLayout.LayoutParams(
                dp(OverlayStyle.ICON_BUTTON), dp(OverlayStyle.ICON_BUTTON),
            ).apply { marginEnd = dp(OverlayStyle.SPACE_S) }
        }
        startButton = TextView(context).apply {
            OverlayStyle.applyAccentPill(this)
            text = context.getString(R.string.input_bar_start_button)
            val arrowDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_right)?.mutate()
            arrowDrawable?.let { DrawableCompat.setTint(it, OverlayStyle.color(context, OverlayStyle.onAccent)) }
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, arrowDrawable, null)
            compoundDrawablePadding = dp(OverlayStyle.SPACE_XS)
            setOnClickListener {
                listener?.onStart(editText.text.toString(), currentMode, selectedIntentHint)
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
        inputRow.addView(editText)
        inputRow.addView(pasteButton)
        inputRow.addView(startProgress)
        inputRow.addView(startButton)

        // --- Status text ---
        statusText = TextView(context).apply {
            OverlayStyle.applySectionLabel(this)
            setPadding(dp(OverlayStyle.SPACE_XS), dp(OverlayStyle.SPACE_S), dp(OverlayStyle.SPACE_XS), 0)
            visibility = GONE
        }

        root.addView(topRow)
        root.addView(stylePanel)
        root.addView(contextPreviewRow)
        root.addView(intentScroll)
        root.addView(inputRow)
        root.addView(statusText)
        addView(root)

        applyModeState()
        refreshAllStyleChips()
    }

    // --- Public API ---

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

    fun setMode(mode: ReplyMode) {
        currentMode = if (mode in visibleModes) mode else ReplyMode.REPLY
        refreshSegments()
        applyModeState()
    }

    fun currentText(): String = editText.text.toString()

    /** Returns the current transient style state. */
    fun currentStyle(): WritingStyleSettings = styleState

    /** Applies an external style (e.g. restored from SettingsStore on open). */
    fun setStyle(style: WritingStyleSettings) {
        styleState = style
        refreshAllStyleChips()
    }

    /** Applies an external tone (e.g. restored preferred tone from SettingsStore on open). */
    fun setTone(tone: ToneOption) {
        currentTone = tone
        refreshChipRow(toneChipViews) { it == currentTone }
    }

    // --- Private helpers ---

    private fun applyModeState() {
        editText.hint = context.getString(
            when (currentMode) {
                ReplyMode.REPLY -> R.string.input_bar_hint_reply
                ReplyMode.COMPOSE -> R.string.input_bar_hint
                ReplyMode.REWRITE -> R.string.input_bar_hint
            },
        )
        intentChipRow.parent?.let { parent ->
            (parent as? View)?.isVisible = currentMode == ReplyMode.REPLY
        }
        if (currentMode != ReplyMode.REPLY) {
            selectedIntentHint = null
            refreshIntentChips()
        }
        updateContextPreview()
    }

    private fun updateContextPreview() {
        val show = currentMode == ReplyMode.REPLY && editText.text.isNotBlank()
        contextPreviewRow.isVisible = show
        if (show) {
            contextCaption.text = context.getString(
                R.string.input_bar_context_value, editText.text.toString(),
            )
        }
    }

    private fun refreshSegments() {
        segmentViews.forEachIndexed { index, view ->
            OverlayStyle.applySegment(view, visibleModes[index] == currentMode)
        }
    }

    // --- Style panel ---

    private fun buildStylePanelContent(): View {
        val panel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(context, R.drawable.bg_input_field)
            setPadding(dp(OverlayStyle.SPACE_M), dp(OverlayStyle.SPACE_S), dp(OverlayStyle.SPACE_M), dp(OverlayStyle.SPACE_M))
        }

        // Header: "Schreibstil" label + close button
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(TextView(context).apply {
            OverlayStyle.applySectionLabel(this)
            text = context.getString(R.string.input_bar_style_panel_description)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        header.addView(ImageView(context).apply {
            OverlayStyle.applyIconButton(this, R.drawable.ic_close)
            contentDescription = context.getString(R.string.input_bar_style_panel_close)
            setOnClickListener { toggleStylePanel() }
            layoutParams = LinearLayout.LayoutParams(dp(OverlayStyle.ICON_XS), dp(OverlayStyle.ICON_XS))
        })
        panel.addView(header)

        // Tone row
        panel.addView(buildChipRow(
            label = context.getString(R.string.settings_style_tone),
            entries = ToneOption.entries.toList(),
            chipViews = toneChipViews,
            defaultGetter = { ToneOption.DEFAULT },
            currentGetter = { currentTone },
            labelOf = { it.label },
            onSelect = { tone ->
                currentTone = tone
                refreshChipRow(toneChipViews) { it == currentTone }
                listener?.onToneSelected(tone)
            },
        ))

        // Writing-style dimension rows
        panel.addView(buildChipRow(
            label = context.getString(R.string.settings_style_length),
            entries = AnswerLength.entries.toList(),
            chipViews = lengthChipViews,
            defaultGetter = { AnswerLength.DEFAULT },
            currentGetter = { styleState.length },
            labelOf = { it.label },
            onSelect = { v ->
                styleState = styleState.copy(length = v)
                refreshChipRow(lengthChipViews) { it == styleState.length }
                listener?.onStyleChanged(styleState)
            },
        ))

        panel.addView(buildChipRow(
            label = context.getString(R.string.settings_style_emoji),
            entries = EmojiUsage.entries.toList(),
            chipViews = emojiChipViews,
            defaultGetter = { EmojiUsage.DEFAULT },
            currentGetter = { styleState.emojiUsage },
            labelOf = { it.label },
            onSelect = { v ->
                styleState = styleState.copy(emojiUsage = v)
                refreshChipRow(emojiChipViews) { it == styleState.emojiUsage }
                listener?.onStyleChanged(styleState)
            },
        ))

        panel.addView(buildChipRow(
            label = context.getString(R.string.settings_style_punctuation),
            entries = PunctuationStyle.entries.toList(),
            chipViews = punctuationChipViews,
            defaultGetter = { PunctuationStyle.DEFAULT },
            currentGetter = { styleState.punctuation },
            labelOf = { it.label },
            onSelect = { v ->
                styleState = styleState.copy(punctuation = v)
                refreshChipRow(punctuationChipViews) { it == styleState.punctuation }
                listener?.onStyleChanged(styleState)
            },
        ))

        panel.addView(buildChipRow(
            label = context.getString(R.string.settings_style_capitalization),
            entries = CapitalizationStyle.entries.toList(),
            chipViews = capitalizationChipViews,
            defaultGetter = { CapitalizationStyle.DEFAULT },
            currentGetter = { styleState.capitalization },
            labelOf = { it.label },
            onSelect = { v ->
                styleState = styleState.copy(capitalization = v)
                refreshChipRow(capitalizationChipViews) { it == styleState.capitalization }
                listener?.onStyleChanged(styleState)
            },
        ))

        panel.addView(buildChipRow(
            label = context.getString(R.string.settings_style_naturalness),
            entries = Naturalness.entries.toList(),
            chipViews = naturalnessChipViews,
            defaultGetter = { Naturalness.DEFAULT },
            currentGetter = { styleState.naturalness },
            labelOf = { it.label },
            onSelect = { v ->
                styleState = styleState.copy(naturalness = v)
                refreshChipRow(naturalnessChipViews) { it == styleState.naturalness }
                listener?.onStyleChanged(styleState)
            },
        ))

        return panel
    }

    /** Builds a labelled horizontal chip row wrapped in a [HorizontalScrollView]. */
    private fun <T> buildChipRow(
        label: String,
        entries: List<T>,
        chipViews: MutableMap<T, TextView>,
        defaultGetter: () -> T,
        currentGetter: () -> T,
        onSelect: (T) -> Unit,
        labelOf: (T) -> String,
    ): LinearLayout {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(OverlayStyle.SPACE_S), 0, 0)
        }
        container.addView(TextView(context).apply {
            OverlayStyle.applySectionLabel(this)
            text = label
            setPadding(0, 0, 0, dp(OverlayStyle.SPACE_XS))
        })
        val scroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
        }
        val chipRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        entries.forEach { entry ->
            val chip = TextView(context).apply {
                OverlayStyle.applyChip(this, selected = currentGetter() == entry)
                text = labelOf(entry)
                setOnClickListener { onSelect(entry) }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { marginEnd = dp(OverlayStyle.SPACE_S) }
            }
            chipViews[entry] = chip
            chipRow.addView(chip)
        }
        scroll.addView(chipRow)
        container.addView(scroll)
        return container
    }

    /** Refreshes a single chip row's selected state. */
    private fun <T> refreshChipRow(chipViews: Map<T, TextView>, isSelected: (T) -> Boolean) {
        chipViews.forEach { (entry, view) ->
            OverlayStyle.applyChip(view, isSelected(entry))
        }
    }

    private fun refreshAllStyleChips() {
        refreshChipRow(toneChipViews) { it == currentTone }
        refreshChipRow(lengthChipViews) { it == styleState.length }
        refreshChipRow(emojiChipViews) { it == styleState.emojiUsage }
        refreshChipRow(punctuationChipViews) { it == styleState.punctuation }
        refreshChipRow(capitalizationChipViews) { it == styleState.capitalization }
        refreshChipRow(naturalnessChipViews) { it == styleState.naturalness }
    }

    private fun toggleStylePanel() {
        stylePanel.isVisible = !stylePanel.isVisible
    }

    // --- Intent chips ---

    private fun refreshIntentChips() {
        intentChipViews.forEach { (label, view) ->
            OverlayStyle.applyChip(view, selected = label == selectedIntentHint)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        /** Alltags-Set for reply intent chips (Decision Brief). Single-select, never stored. */
        val REPLY_INTENT_CHIPS = listOf(
            "Zustimmen", "Absagen", "Entschuldigen",
            "Nachfragen", "Beruhigen", "Grenze",
        )
    }
}
