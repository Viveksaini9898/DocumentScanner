
package com.document.scanner.view.crop

import android.graphics.PointF
import com.document.scanner.view.crop.transform.CropCoords


fun DocCropBorder.toCropCoords(): CropCoords {
    return CropCoords(
        topLeftCoord = topLeftCoord,
        topRightCoord = topRightCoord,
        bottomLeftCoord = bottomLeftCoord,
        bottomRightCoord = bottomRightCoord
    )
}


fun DocShape.toDocCropBorder(): DocCropBorder {
    return DocCropBorder(
        topLeftCoord = topLeftCoord,
        topRightCoord = topRightCoord,
        bottomLeftCoord = bottomLeftCoord,
        bottomRightCoord = bottomRightCoord
    )
}


data class DocShape(
    val topLeftCoord: PointF,
    val topRightCoord: PointF,
    val bottomLeftCoord: PointF,
    val bottomRightCoord: PointF
)
