package com.document.scanner.view


import android.widget.FrameLayout
import android.content.*
import android.util.AttributeSet

/**
 * A RelativeLayout that will always be square -- same width and height,
 * where the height is based off the width.
 */
class SquareFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}