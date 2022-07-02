package com.document.scanner.extension

import android.view.View
import androidx.annotation.BoolRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntegerRes

fun View.getColor(@ColorRes colorId: Int): Int {
    return context.getCompatColor(colorId)
}


fun View.getDimensionPixelSize(@DimenRes dimenId: Int): Int {
    return context.getDimensionPixelSize(dimenId)
}


fun View.getInteger(@IntegerRes intId: Int): Int {
    return context.getInteger(intId)
}


fun View.getDimension(@DimenRes dimenId: Int): Float {
    return context.getDimension(dimenId)
}

