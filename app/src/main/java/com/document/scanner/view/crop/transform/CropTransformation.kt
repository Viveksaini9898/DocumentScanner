
package com.document.scanner.view.crop.transform


import android.graphics.Bitmap
import android.graphics.PointF
import com.document.scanner.imageloading.Transformation
import com.document.scanner.view.crop.ImagePerspectiveTransformer

internal class CropTransformation(
    private val imagePerspectiveTransformer: ImagePerspectiveTransformer,
    private val cropCoords: CropCoords,
    private val viewSize: Size
) : Transformation {


    override val key: String
        get() = "Crop. Coords: $cropCoords. View Size: $viewSize."


    override fun transform(source: Bitmap): Bitmap {
        val xRatio = (source.width.toFloat() / viewSize.width)
        val yRatio = (source.height.toFloat() / viewSize.height)

        val scaledCoords = cropCoords.toList()
            .map {
                val x = (it.x * xRatio)
                val y = (it.y * yRatio)

                PointF(x, y)
            }
            .let(::fromList)

        val shourceShapeCoords = ImagePerspectiveTransformer.SourceShapeCoords(
            topLeftCoord = scaledCoords.topLeftCoord,
            topRightCoord = scaledCoords.topRightCoord,
            bottomLeftCoord = scaledCoords.bottomLeftCoord,
            bottomRightCoord = scaledCoords.bottomRightCoord
        )

        val finalBitmap = imagePerspectiveTransformer.transformPerspective(
            sourceImage = source,
            sourceShapeCoords = shourceShapeCoords
        )

        if(finalBitmap != source) {
            source.recycle()
        }

        return finalBitmap
    }


}