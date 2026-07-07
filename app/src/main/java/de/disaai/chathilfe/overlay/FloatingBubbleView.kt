package de.disaai.chathilfe.overlay

import android.content.Context
import android.graphics.Outline
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import de.disaai.chathilfe.R
import kotlin.math.abs

/**
 * Classic Android View for the floating bubble (no Compose, per D-003). It never touches
 * WindowManager itself - touch state is reported upward via [BubbleListener] so
 * [OverlayController] remains the sole caller of addView/removeView/updateViewLayout.
 *
 * The glass look is purely visual: a translucent dark circle with a fine contour stroke
 * (bg_floating_bubble) plus a centered accent chat icon. Drag/Tap behavior is unchanged.
 */
class FloatingBubbleView(context: Context) : FrameLayout(context) {

    interface BubbleListener {
        fun onDragMove(newX: Int, newY: Int)
        fun onDragEnd(finalX: Int, finalY: Int)
        fun onTap()
    }

    var listener: BubbleListener? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downRawX = 0f
    private var downRawY = 0f
    private var startX = 0
    private var startY = 0
    private var lastX = 0
    private var lastY = 0
    private var isDragging = false

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_floating_bubble)
        val size = sizePx(context)
        minimumWidth = size
        minimumHeight = size

        // Best-effort depth. On TYPE_APPLICATION_OVERLAY the elevation shadow frequently does
        // not render; the contour stroke in bg_floating_bubble carries the visual edge anyway.
        ViewCompat.setElevation(this, dp(8f))
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setOval(0, 0, view.width, view.height)
            }
        }

        val iconSize = (ICON_SIZE_DP * resources.displayMetrics.density).toInt()
        val icon = ImageView(context).apply {
            setImageResource(R.drawable.ic_bubble_spark)
            // Accent chat icon on the dark glass circle.
            ImageViewCompat.setImageTintList(
                this,
                ContextCompat.getColorStateList(context, OverlayStyle.accent),
            )
            scaleType = ImageView.ScaleType.CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        addView(icon, LayoutParams(iconSize, iconSize).apply { gravity = Gravity.CENTER })

        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription = context.getString(R.string.floating_bubble_description)
    }

    /** Must be called once, right after construction, before the view receives touch input. */
    fun setTrackedPosition(x: Int, y: Int) {
        startX = x
        startY = y
        lastX = x
        lastY = y
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                startX = lastX
                startY = lastY
                isDragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!isDragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                    isDragging = true
                }
                if (isDragging) {
                    lastX = startX + dx.toInt()
                    lastY = startY + dy.toInt()
                    listener?.onDragMove(lastX, lastY)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    listener?.onDragEnd(lastX, lastY)
                } else {
                    listener?.onTap()
                }
                isDragging = false
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    listener?.onDragEnd(lastX, lastY)
                }
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    companion object {
        private const val BUBBLE_SIZE_DP = 52
        private const val ICON_SIZE_DP = 24

        fun sizePx(context: Context): Int =
            (BUBBLE_SIZE_DP * context.resources.displayMetrics.density).toInt()

        /** Default position: right screen edge, vertically centered. */
        fun defaultPosition(context: Context, marginDp: Int = 16): Pair<Int, Int> {
            val metrics = context.resources.displayMetrics
            val size = sizePx(context)
            val margin = (marginDp * metrics.density).toInt()
            val x = metrics.widthPixels - size - margin
            val y = (metrics.heightPixels - size) / 2
            return x to y
        }
    }
}
