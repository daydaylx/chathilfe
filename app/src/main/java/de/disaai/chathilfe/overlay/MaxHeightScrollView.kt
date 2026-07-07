package de.disaai.chathilfe.overlay

import android.content.Context
import android.widget.ScrollView

/**
 * Minimal ScrollView whose height is capped at [maxHeightPx]. Used by [ResultPanelView] so the
 * suggestion body scrolls internally instead of growing the panel to full screen on long text.
 * AT_MOST lets it shrink to the content when the text is short.
 */
class MaxHeightScrollView(context: Context) : ScrollView(context) {

    var maxHeightPx: Int = 0

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        if (maxHeightPx > 0) {
            super.onMeasure(widthSpec, android.view.View.MeasureSpec.makeMeasureSpec(maxHeightPx, android.view.View.MeasureSpec.AT_MOST))
        } else {
            super.onMeasure(widthSpec, heightSpec)
        }
    }
}
