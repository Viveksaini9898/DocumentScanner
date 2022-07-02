package com.document.scanner.view.crop

import android.graphics.PointF

data class DocCropBorder(
    val topLeftCoord: PointF,
    val topRightCoord: PointF,
    val bottomLeftCoord: PointF,
    val bottomRightCoord: PointF
)