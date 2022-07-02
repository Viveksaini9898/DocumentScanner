
package com.document.scanner.view.crop.transform


import com.document.scanner.imageloading.Transformation
import com.document.scanner.view.crop.ImagePerspectiveTransformer

import javax.inject.Inject
import javax.inject.Provider


interface CropTransformationFactory {

    fun createCropTransformation(cropCoords: CropCoords, viewSize: Size): Transformation

}


internal class CropTransformationFactoryImpl @Inject constructor(
    private val imagePerspectiveTransformer: Provider<ImagePerspectiveTransformer>
) : CropTransformationFactory {


    override fun createCropTransformation(cropCoords: CropCoords, viewSize: Size): Transformation {
        return CropTransformation(
            imagePerspectiveTransformer = imagePerspectiveTransformer.get(),
            cropCoords = cropCoords,
            viewSize = viewSize
        )
    }


}