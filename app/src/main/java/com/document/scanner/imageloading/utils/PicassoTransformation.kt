
package com.document.scanner.imageloading.utils

import android.graphics.Bitmap
import com.squareup.picasso.Transformation
import com.document.scanner.imageloading.Transformation as MyTransformation

internal class PicassoTransformation(
    private val transformation: MyTransformation
) : Transformation {


    override fun transform(source: Bitmap): Bitmap {
        return transformation.transform(source)
    }


    override fun key(): String {
        return transformation.key
    }


}