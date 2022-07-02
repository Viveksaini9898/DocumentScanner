package com.document.scanner.extension

import android.graphics.Rect
import android.graphics.RectF

val Rect.halfWidth: Int
    get() = (width() / 2)

val Rect.halfHeight: Int
    get() = (height() / 2)

val RectF.halfWidth: Float
    get() = (width() * 0.5f)

val RectF.halfHeight: Float
    get() = (height() * 0.5f)


fun Rect.setBounds(
    left: Int = this.left,
    top: Int = this.top,
    right: Int = this.right,
    bottom: Int = this.bottom
) {
    set(left, top, right, bottom)
}


fun RectF.setBounds(
    left: Float = this.left,
    top: Float = this.top,
    right: Float = this.right,
    bottom: Float = this.bottom
) {
    set(left, top, right, bottom)
}