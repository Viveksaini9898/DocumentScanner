
package com.document.scanner.view.crop.handles

internal class Handles(
    val topLeft: EdgeHandle,
    val topRight: EdgeHandle,
    val bottomRight: EdgeHandle,
    val bottomLeft: EdgeHandle,
    val middleLeft: MiddleHandle,
    val middleTop: MiddleHandle,
    val middleRight: MiddleHandle,
    val middleBottom: MiddleHandle
) {


    @OptIn(ExperimentalStdlibApi::class)
    fun allHandlesList(): List<Handle> {
        return buildList {
            addAll(edgeHandlesList())
            addAll(middleHandlesList())
        }
    }


    fun edgeHandlesList(): List<EdgeHandle> {
        return listOf(topLeft, topRight, bottomRight, bottomLeft)
    }


    fun middleHandlesList(): List<MiddleHandle> {
        return listOf(middleLeft, middleTop, middleRight, middleBottom)
    }


}