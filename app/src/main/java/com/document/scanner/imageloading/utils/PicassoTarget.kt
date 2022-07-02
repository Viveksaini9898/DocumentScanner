
package com.document.scanner.imageloading.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.document.scanner.imageloading.Target as MyTarget

internal class PicassoTarget(
    private val target: MyTarget
): Target {


    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
        target.onBitmapLoadingSucceeded(bitmap)
    }


    override fun onBitmapFailed(exception: Exception, errorDrawable: Drawable?) {
        target.onBitmapLoadingFailed(exception)
    }


    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        target.onPrepareLoad()
    }


}