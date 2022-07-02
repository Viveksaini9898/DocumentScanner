
package com.document.scanner.detector

import android.graphics.PointF
import com.document.scanner.view.crop.DocShape
import javax.inject.Inject


interface DocCoordsOrderer {

    /**
     * Orders a list of document coordinates and returns them
     * in the ordered way. If ordering cannot be achieved, null
     * is returned.
     *
     * @param coords a list of coordinates of a document with
     * no particular order applied
     *
     * @return a data structure with ordered coordinates or null
     * if ordering cannot be achieved
     */
    fun order(coords: List<PointF>): DocShape?

}


internal class DocCoordsOrdererImpl @Inject constructor() : DocCoordsOrderer {


    private companion object {

        val STUB_COORD = PointF(-1f, -1f)

    }


    override fun order(coords: List<PointF>): DocShape? {
        val centerCoord = calculateCenterCoord(coords)

        var topLeftCoord = STUB_COORD
        var topRightCoord = STUB_COORD
        var bottomLeftCoord = STUB_COORD
        var bottomRightCoord = STUB_COORD

        for(coord in coords) {
            topLeftCoord = (if(isTopLeftCoord(coord, centerCoord)) coord else topLeftCoord)
            topRightCoord = (if(isTopRightCoord(coord, centerCoord)) coord else topRightCoord)
            bottomLeftCoord = (if(isBottomLeftCoord(coord, centerCoord)) coord else bottomLeftCoord)
            bottomRightCoord = (if(isBottomRightCoord(coord, centerCoord)) coord else bottomRightCoord)
        }

        if((topLeftCoord == STUB_COORD) ||
            (topRightCoord == STUB_COORD) ||
            (bottomLeftCoord == STUB_COORD) ||
            (bottomRightCoord == STUB_COORD)) {
            return null
        }

        return DocShape(
            topLeftCoord = topLeftCoord,
            topRightCoord = topRightCoord,
            bottomLeftCoord = bottomLeftCoord,
            bottomRightCoord = bottomRightCoord
        )
    }


    private fun calculateCenterCoord(coords: List<PointF>): PointF {
        val centerCoord = PointF()
        val coordCount = coords.size

        for(coord in coords) {
            centerCoord.x += (coord.x / coordCount)
            centerCoord.y += (coord.y / coordCount)
        }

        return centerCoord
    }


    private fun isTopLeftCoord(coord: PointF, centerCoord: PointF): Boolean {
        return ((coord.x < centerCoord.x) && (coord.y < centerCoord.y))
    }


    private fun isTopRightCoord(coord: PointF, centerCoord: PointF): Boolean {
        return ((coord.x > centerCoord.x) && (coord.y < centerCoord.y))
    }


    private fun isBottomLeftCoord(coord: PointF, centerCoord: PointF): Boolean {
        return ((coord.x < centerCoord.x) && (coord.y > centerCoord.y))
    }


    private fun isBottomRightCoord(coord: PointF, centerCoord: PointF): Boolean {
        return ((coord.x > centerCoord.x) && (coord.y > centerCoord.y))
    }


}