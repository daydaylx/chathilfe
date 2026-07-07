package de.disaai.chathilfe.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import de.disaai.chathilfe.model.ReplySuggestion
import de.disaai.chathilfe.model.ToneOption

/**
 * Sole owner of WindowManager.addView/removeView/updateViewLayout for all overlay views
 * (bubble, Input-Bar, Result-Panel). One instance is created per [OverlayService] lifetime -
 * not an app-wide singleton. The bubble and the Input-Bar/Result-Panel content never coexist.
 */
class OverlayController(private val context: Context) {

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var bubbleView: FloatingBubbleView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var contentView: View? = null

    val isAttached: Boolean
        get() = bubbleView != null

    val isContentAttached: Boolean
        get() = contentView != null

    fun show(x: Int, y: Int, listener: FloatingBubbleView.BubbleListener) {
        if (isAttached) return

        val view = FloatingBubbleView(context).apply {
            this.listener = listener
            setTrackedPosition(x, y)
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }

        try {
            windowManager.addView(view, params)
            bubbleView = view
            layoutParams = params
        } catch (e: Exception) {
            Log.w(TAG, "Failed to add overlay bubble", e)
        }
    }

    fun updatePosition(x: Int, y: Int) {
        val view = bubbleView ?: return
        val params = layoutParams ?: return
        params.x = x
        params.y = y
        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update overlay bubble position", e)
        }
    }

    fun remove() {
        val view = bubbleView
        try {
            if (view != null) {
                windowManager.removeView(view)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to remove overlay bubble", e)
        } finally {
            bubbleView = null
            layoutParams = null
        }
    }

    fun showInputBar(tone: ToneOption, listener: InputBarView.Listener) {
        replaceContent(focusable = true) {
            InputBarView(context).apply {
                this.listener = listener
                setTone(tone)
            }
        }
    }

    fun showResultPanel(
        suggestions: List<ReplySuggestion>,
        originalText: String,
        tone: ToneOption,
        listener: ResultPanelView.Listener,
    ) {
        hideKeyboardFromContent()
        replaceContent(focusable = false) {
            ResultPanelView(context).apply {
                this.listener = listener
                show(suggestions, originalText, tone)
            }
        }
    }

    fun hideContent() {
        val view = contentView
        try {
            if (view != null) {
                windowManager.removeView(view)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to remove overlay content", e)
        } finally {
            contentView = null
        }
    }

    private fun replaceContent(focusable: Boolean, factory: () -> View) {
        hideContent()

        val metrics = context.resources.displayMetrics
        val margin = (CONTENT_MARGIN_DP * metrics.density).toInt()
        val flags = if (focusable) 0 else WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        val params = WindowManager.LayoutParams(
            metrics.widthPixels - margin * 2,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = margin
            y = margin
        }

        try {
            val view = factory()
            windowManager.addView(view, params)
            contentView = view
        } catch (e: Exception) {
            Log.w(TAG, "Failed to add overlay content", e)
        }
    }

    private fun hideKeyboardFromContent() {
        val view = contentView ?: return
        val imm = context.getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private companion object {
        const val TAG = "OverlayController"
        const val CONTENT_MARGIN_DP = 12
    }
}
