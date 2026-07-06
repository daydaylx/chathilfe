package de.disaai.chathilfe.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.WindowManager

/**
 * Sole owner of WindowManager.addView/removeView/updateViewLayout for the overlay bubble.
 * One instance is created per [OverlayService] lifetime - not an app-wide singleton.
 */
class OverlayController(private val context: Context) {

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var bubbleView: FloatingBubbleView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    val isAttached: Boolean
        get() = bubbleView != null

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

    private companion object {
        const val TAG = "OverlayController"
    }
}
