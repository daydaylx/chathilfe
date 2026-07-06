package de.disaai.chathilfe.overlay

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import de.disaai.chathilfe.R
import kotlin.math.abs

/**
 * Classic Android View for the floating bubble (no Compose, per D-003). It never touches
 * WindowManager itself - touch state is reported upward via [BubbleListener] so
 * [OverlayController] remains the sole caller of addView/removeView/updateViewLayout.
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

    companion object {
        private const val BUBBLE_SIZE_DP = 52

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
