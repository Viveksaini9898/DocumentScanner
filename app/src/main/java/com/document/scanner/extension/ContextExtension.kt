package com.document.scanner.extension

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntegerRes
import androidx.core.content.ContextCompat

fun Context.getDimensionPixelSize(@DimenRes dimenId: Int): Int {
    return resources.getDimensionPixelSize(dimenId)
}


fun Context.getInteger(@IntegerRes intId: Int): Int {
    return resources.getInteger(intId)
}


fun Context.getDimension(@DimenRes dimenId: Int): Float {
    return resources.getDimension(dimenId)
}

fun Context.getCompatColor(@ColorRes colorId: Int): Int {
    return ContextCompat.getColor(this, colorId)
}

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)