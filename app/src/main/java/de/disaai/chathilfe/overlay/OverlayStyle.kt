package de.disaai.chathilfe.overlay

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import de.disaai.chathilfe.R

/**
 * Central style layer for the WindowManager overlay (classic Views, per D-003). One source of
 * truth for the glass palette, the spacing/type/radius scales and the small apply-helpers used
 * by [FloatingBubbleView], [InputBarView] and [ResultPanelView]. Does NOT create shapes itself
 * — backgrounds live in res/drawable XML; this object only references them and applies text/
 * tint/padding. Keeps the Setup/MainActivity Compose palette ([R.color.chathilfe_*]) untouched.
 */
object OverlayStyle {

    // --- Glass palette (color resource IDs) ---
    val glass = R.color.overlay_glass
    val surface = R.color.overlay_surface
    val field = R.color.overlay_field
    val contour = R.color.overlay_contour
    val divider = R.color.overlay_divider
    val text = R.color.overlay_text
    val textMuted = R.color.overlay_text_muted
    val accent = R.color.overlay_accent
    val error = R.color.overlay_error
    val onAccent = R.color.overlay_on_accent

    // --- Spacing scale (dp) ---
    const val SPACE_XS = 4
    const val SPACE_S = 8
    const val SPACE_M = 12
    const val SPACE_L = 16
    const val SPACE_XL = 20
    const val SPACE_XXL = 24

    // --- Radius scale (dp) ---
    const val RADIUS_SM = 10
    const val RADIUS_MD = 14
    const val RADIUS_LG = 20
    const val RADIUS_PILL = 999

    // --- Icon sizes (dp) ---
    const val ICON_XS = 16
    const val ICON_SM = 20
    const val ICON_MD = 24
    const val ICON_BUTTON = 40

    // --- Content layout ---
    const val CONTENT_MAX_WIDTH_DP = 560
    const val CONTENT_MARGIN_DP = 16
    const val BODY_MAX_HEIGHT_FRACTION_PERCENT = 40

    // --- Type scale (sp) ---
    const val TEXT_CAPTION = 12f
    const val TEXT_LABEL = 13f
    const val TEXT_BODY = 14f
    const val TEXT_TITLE = 16f

    fun color(context: Context, resId: Int): Int = ContextCompat.getColor(context, resId)

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun sp(context: Context, value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.resources.displayMetrics)

    /** Styles a 40dp icon button (clickable [ImageView]): glass-contour bg, light tint, centered. */
    fun applyIconButton(view: ImageView, iconRes: Int, tintAccent: Boolean = false) {
        val ctx = view.context
        view.background = ContextCompat.getDrawable(ctx, R.drawable.bg_chip)
        view.setImageResource(iconRes)
        ImageViewCompat.setImageTintList(
            view,
            ContextCompat.getColorStateList(ctx, if (tintAccent) accent else text),
        )
        view.scaleType = ImageView.ScaleType.CENTER
        view.setPadding(dp(ctx, SPACE_S), dp(ctx, SPACE_S), dp(ctx, SPACE_S), dp(ctx, SPACE_S))
        view.isClickable = true
        view.isFocusable = true
    }

    /** Styles the app wordmark in a header (slightly larger, bright). */
    fun applyWordmark(view: TextView) {
        val ctx = view.context
        view.setTextColor(color(ctx, text))
        view.textSize = TEXT_LABEL
    }

    /** Styles a section/page title in a header. */
    fun applyHeaderTitle(view: TextView) {
        val ctx = view.context
        view.setTextColor(color(ctx, text))
        view.textSize = TEXT_TITLE
    }

    /** Styles a small muted section label (e.g. "Nicht passend?"). */
    fun applySectionLabel(view: TextView) {
        val ctx = view.context
        view.setTextColor(color(ctx, textMuted))
        view.textSize = TEXT_CAPTION
    }

    /** Styles a tonal glass pill (e.g. tone selector, 1/3 position indicator). */
    fun applyTextPill(view: TextView) {
        val ctx = view.context
        view.background = ContextCompat.getDrawable(ctx, R.drawable.bg_pill_glass)
        view.setTextColor(color(ctx, text))
        view.textSize = TEXT_LABEL
        view.setPadding(dp(ctx, SPACE_M), dp(ctx, 7), dp(ctx, SPACE_M), dp(ctx, 7))
    }

    /** Styles a primary accent pill (e.g. Copy button, Los). Text sits dark on green. */
    fun applyAccentPill(view: TextView) {
        val ctx = view.context
        view.background = ContextCompat.getDrawable(ctx, R.drawable.bg_pill_accent)
        view.setTextColor(color(ctx, onAccent))
        view.textSize = TEXT_LABEL
        view.setPadding(dp(ctx, SPACE_L), dp(ctx, 9), dp(ctx, SPACE_L), dp(ctx, 9))
    }

    /** Styles a chip with clear selected/unselected contrast (tones and retry chips). */
    fun applyChip(view: TextView, selected: Boolean) {
        val ctx = view.context
        view.background = ContextCompat.getDrawable(
            ctx,
            if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip,
        )
        view.setTextColor(color(ctx, if (selected) onAccent else text))
        view.textSize = TEXT_LABEL
        view.setPadding(dp(ctx, SPACE_M), dp(ctx, SPACE_S), dp(ctx, SPACE_M), dp(ctx, SPACE_S))
    }

    /** Styles a segmented-control segment (e.g. reply mode). Active segment uses the accent. */
    fun applySegment(view: TextView, selected: Boolean) {
        val ctx = view.context
        view.background = ContextCompat.getDrawable(
            ctx,
            if (selected) R.drawable.bg_segment_active else android.R.color.transparent,
        )
        view.setTextColor(color(ctx, if (selected) onAccent else text))
        view.textSize = TEXT_LABEL
        gravityCenter(view)
        view.setPadding(dp(ctx, SPACE_M), dp(ctx, 8), dp(ctx, SPACE_M), dp(ctx, 8))
    }

    /** Styles the suggestion body text: calm, readable, light, with comfortable line spacing. */
    fun applyBodyText(view: TextView) {
        val ctx = view.context
        view.setTextColor(color(ctx, text))
        view.textSize = TEXT_BODY
        view.setLineSpacing(sp(ctx, 3f), 1f)
        view.setPadding(dp(ctx, SPACE_XS), dp(ctx, SPACE_S), dp(ctx, SPACE_XS), dp(ctx, SPACE_S))
    }

    private fun gravityCenter(view: TextView) {
        view.gravity = android.view.Gravity.CENTER
    }

    /** Creates a thin full-width divider View in the divider color. */
    fun divider(context: Context): View = View(context).apply {
        setBackgroundColor(color(context, divider))
    }
}
