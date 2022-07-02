
package com.document.scanner.view.crop

import android.graphics.Bitmap
import android.graphics.PointF
import com.document.scanner.extension.toBitmap
import com.document.scanner.extension.toMat
import com.document.scanner.extension.toPoint

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import kotlin.math.sqrt


interface ImagePerspectiveTransformer {

    data class SourceShapeCoords(
        val topLeftCoord: PointF,
        val topRightCoord: PointF,
        val bottomLeftCoord: PointF,
        val bottomRightCoord: PointF
    )

    fun transformPerspective(sourceImage: Bitmap, sourceShapeCoords: SourceShapeCoords): Bitmap

}


internal class OpenCvImagePerspectiveTransformer @Inject constructor() : ImagePerspectiveTransformer {


    override fun transformPerspective(sourceImage: Bitmap, sourceShapeCoords: ImagePerspectiveTransformer.SourceShapeCoords): Bitmap {
        val sourceImageMat = sourceImage.toMat()
        val sourceShapeCoordsMat = sourceShapeCoords.toMat()
        val destImageSize = computeDestinationImageSize(sourceShapeCoords)
        val destImageMat = Mat.zeros(destImageSize, sourceImageMat.type())
        val destShapeCoordsMat = computeDestinationShapeCoordsMat(destImageMat)
        val transformationMat = Imgproc.getPerspectiveTransform(sourceShapeCoordsMat, destShapeCoordsMat)

        Imgproc.warpPerspective(sourceImageMat, destImageMat, transformationMat, destImageSize)

        return destImageMat.toBitmap()
            .also { sourceImageMat.release() }
    }


    private fun ImagePerspectiveTransformer.SourceShapeCoords.toMat(): MatOfPoint2f {
        val shapeCoordsArray = arrayOf(
            topLeftCoord.toPoint(),
            topRightCoord.toPoint(),
            bottomLeftCoord.toPoint(),
            bottomRightCoord.toPoint()
        )
        val shapeCoordsMat = MatOfPoint2f().apply { fromArray(*shapeCoordsArray) }

        return shapeCoordsMat
    }


    private fun computeDestinationImageSize(coords: ImagePerspectiveTransformer.SourceShapeCoords): Size {
        val topDistance = calculateDistance(coords.topLeftCoord.toPoint(), coords.topRightCoord.toPoint())
        val bottomDistance = calculateDistance(coords.bottomLeftCoord.toPoint(), coords.bottomRightCoord.toPoint())
        val leftDistance = calculateDistance(coords.topLeftCoord.toPoint(), coords.bottomLeftCoord.toPoint())
        val rightDistance = calculateDistance(coords.topRightCoord.toPoint(), coords.bottomRightCoord.toPoint())

        val averageWidth = ((topDistance + bottomDistance) / 2f)
        val averageHeight = ((leftDistance + rightDistance) / 2f)

        return Size(averageWidth, averageHeight)
    }


    private fun calculateDistance(point1: Point, point2: Point): Double {
        val dx = (point2.x - point1.x)
        val dy = (point2.y - point1.y)

        return sqrt((dx * dx) + (dy * dy))
    }


    private fun computeDestinationShapeCoordsMat(imageMat: Mat): MatOfPoint2f {
        val topLeftCoord = Point(0.0, 0.0)
        val topRightCoord = Point(imageMat.cols().toDouble(), 0.0)
        val bottomLeftCoord = Point(0.0, imageMat.rows().toDouble())
        val bottomRightCoord = Point(imageMat.cols().toDouble(), imageMat.rows().toDouble())
        val coords = arrayOf(topLeftCoord, topRightCoord, bottomLeftCoord, bottomRightCoord)

        return MatOfPoint2f().apply { fromArray(*coords) }
    }


}