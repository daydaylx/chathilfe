package de.disaai.chathilfe.overlay

import android.content.Context
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import de.disaai.chathilfe.R

/**
 * Central style layer for the WindowManager overlay (classic Views, per D-003). One source of
 * truth for the glass palette, dimensions and typography shared by [FloatingBubbleView],
 * [InputBarView] and [ResultPanelView]. Does NOT create shapes itself — backgrounds live in
 * res/drawable XML; this object only references them and applies text/tint/padding.
 *
 * Keeps the Setup/MainActivity Compose palette ([R.color.chathilfe_*]) untouched.
 */
object OverlayStyle {

    // --- Glass palette (color resource IDs) ---
    val glass = R.color.overlay_glass
    val contour = R.color.overlay_contour
    val text = R.color.overlay_text
    val textMuted = R.color.overlay_text_muted
    val accent = R.color.overlay_accent
    val error = R.color.overlay_error
    val onAccent = R.color.overlay_on_accent
    val field = R.color.overlay_field

    // --- Dimensions (dp) ---
    const val ICON_SIZE_DP = 40
    const val BUBBLE_SIZE_DP = 52
    const val CONTENT_MAX_WIDTH_DP = 560
    const val CONTENT_MARGIN_DP = 16
    const val BODY_MAX_HEIGHT_FRACTION_PERCENT = 40 // 40% of screen height

    // --- Typography (sp) ---
    const val TEXT_SIZE_BODY = 15f
    const val TEXT_SIZE_PILL = 13f
    const val TEXT_SIZE_CHIP = 13f
    const val TEXT_SIZE_HINT = 12f

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
        view.setPadding(dp(ctx, 8), dp(ctx, 8), dp(ctx, 8), dp(ctx, 8))
        view.isClickable = true
        view.isFocusable = true
    }

    /** Styles a tonal glass pill (e.g. tone selector, 1/3 position indicator). */
    fun applyTextPill(view: TextView) {
        val ctx = view.context
        view.background = ContextCompat.getDrawable(ctx, R.drawable.bg_pill_glass)
        view.setTextColor(color(ctx, text))
        view.textSize = TEXT_SIZE_PILL
        view.setPadding(dp(ctx, 14), dp(ctx, 7), dp(ctx, 14), dp(ctx, 7))
    }

    /** Styles a primary accent pill (e.g. Copy button). Text sits dark on green. */
    fun applyAccentPill(view: TextView) {
        val ctx = view.context
        view.background = ContextCompat.getDrawable(ctx, R.drawable.bg_pill_accent)
        view.setTextColor(color(ctx, onAccent))
        view.textSize = TEXT_SIZE_PILL
        view.setPadding(dp(ctx, 18), dp(ctx, 9), dp(ctx, 18), dp(ctx, 9))
    }

    /** Styles a chip with clear selected/unselected contrast (used for tones and retry chips). */
    fun applyChip(view: TextView, selected: Boolean) {
        val ctx = view.context
        view.background = ContextCompat.getDrawable(
            ctx,
            if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip,
        )
        view.setTextColor(color(ctx, if (selected) onAccent else text))
        view.textSize = TEXT_SIZE_CHIP
        view.setPadding(dp(ctx, 14), dp(ctx, 8), dp(ctx, 14), dp(ctx, 8))
    }

    /** Styles the suggestion body text: larger, calmer, light, with comfortable line spacing. */
    fun applyBodyText(view: TextView) {
        val ctx = view.context
        view.setTextColor(color(ctx, text))
        view.textSize = TEXT_SIZE_BODY
        view.setLineSpacing(sp(ctx, 3f), 1f)
        view.setPadding(dp(ctx, 2), dp(ctx, 10), dp(ctx, 2), dp(ctx, 10))
    }
}
