
package com.document.scanner.view.crop.transform


import android.graphics.PointF


data class CropCoords(
    val topLeftCoord: PointF,
    val topRightCoord: PointF,
    val bottomLeftCoord: PointF,
    val bottomRightCoord: PointF
)


internal fun CropCoords.toList(): List<PointF> {
    return listOf(
        topLeftCoord,
        topRightCoord,
        bottomLeftCoord,
        bottomRightCoord
    )
}


internal fun fromList(coords: List<PointF>): CropCoords {
    return CropCoords(
        topLeftCoord = coords[0],
        topRightCoord = coords[1],
        bottomLeftCoord = coords[2],
        bottomRightCoord = coords[3]
    )
}