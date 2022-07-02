
package com.document.scanner.view.crop.handles

import android.graphics.PointF
import android.graphics.RectF


internal interface Handle {

    val bounds: RectF

}


internal val Handle.x: Float
    get() = bounds.left

internal val Handle.y: Float
    get() = bounds.top


internal fun Handle.toPointF(): PointF {
    return PointF(x, y)
}