
package com.document.scanner.view.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.document.scanner.R
import com.document.scanner.databinding.ViewDocScannerBinding
import com.document.scanner.detector.DocShapeDetector
import com.document.scanner.extension.getDimensionPixelSize
import com.document.scanner.extension.layoutInflater
import com.document.scanner.extension.observeChanges
import com.document.scanner.imageloading.*
import com.document.scanner.imageloading.Target
import com.document.scanner.providers.CoroutineScopeProvider
import com.document.scanner.providers.DispatcherProvider
import com.document.scanner.providers.StringProvider
import com.document.scanner.view.crop.transform.CropTransformationFactory
import com.document.scanner.view.crop.transform.Size
import com.document.scanner.view.crop.transform.CropCoords
import com.squareup.picasso.Picasso

import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DocScannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    private val imageMargin = context.getDimensionPixelSize(R.dimen.doc_scanner_image_margin)
    private val docCropBorderHandleSize = context.getDimensionPixelSize(R.dimen.doc_crop_border_handle_size)

    private val hasImageFile: Boolean
        get() = (imageFile != null)

    private var isImageVisible: Boolean
        set(value) { binding.imageView.isVisible = value }
        get() = binding.imageView.isVisible

    private var isDocCropBorderVisible: Boolean
        set(value) { binding.docCropBorderView.isVisible = value }
        get() = binding.docCropBorderView.isVisible

    private var isProgressBarVisible: Boolean
        set(value) { binding.progressBar.isVisible = value }
        get() = binding.progressBar.isVisible

    private var shouldRunShapeDetection = true

    private var currentImageRotation = 0f
        set(value) {
            field = (value % 360)
            shouldRunShapeDetection = true
            reloadImage()
        }

    private val currentBitmap: Bitmap?
        get() = (binding.imageView.drawable as? BitmapDrawable)?.bitmap

    private var scannedDocTarget: Target? = null

    private val binding = ViewDocScannerBinding.inflate(context.layoutInflater, this)

    var imageFile by observeChanges<Uri?>(null) { _, _ ->
        reloadImage()
    }


    @Inject  var stringProvider: StringProvider? = null
    @Inject  var imageLoader: ImageLoader? = null
    @Inject  var coroutineScopeProvider: CoroutineScopeProvider? = null
    @Inject  var dispatcherProvider: DispatcherProvider? = null
    @Inject  var docShapeDetector: DocShapeDetector? = null
    @Inject  var cropTransformationFactory: CropTransformationFactory? = null

    var picasso : Picasso? = null

    init {
        initDefaults()
        picasso = Picasso.get()
    }


    private fun initDefaults() {
        isDocCropBorderVisible = false
        isProgressBarVisible = false
    }


    fun rotateLeft() {
        currentImageRotation -= 90f
    }


    fun rotateRight() {
        currentImageRotation += 90f
    }


    private fun reloadImage() {
        if(!hasImageFile) return

        isImageVisible = true
        isDocCropBorderVisible = false
        isProgressBarVisible = true

        val imageWidth = (width - (2 * imageMargin))
        val imageHeight = (height - (2 * imageMargin))

        Toast.makeText(context, "$imageHeight  $imageWidth", Toast.LENGTH_SHORT).show()
        ImageLoaderImpl(picasso!!)?.loadImage(
            Config.Builder()
                .centerInside()
                .rotate(currentImageRotation)
                .resize(imageWidth, imageHeight)
                .source(Config.Source.Uri(checkNotNull(imageFile)))
                .destination(Config.Destination.View(binding.imageView))
                .onSuccess(::onImageLoaded)
                .onFailure {
                    Toast.makeText(context, "fail", Toast.LENGTH_SHORT).show()
                }
                .build()
        )
    }


    private fun onImageLoaded() {
        resetDocCropBorder()
        isProgressBarVisible = false
    }


    private fun resetDocCropBorder() {
        val currentBitmap = checkNotNull(currentBitmap)

        binding.docCropBorderView.updateLayoutParams {
            this.width = (currentBitmap.width + docCropBorderHandleSize)
            this.height = (currentBitmap.height + docCropBorderHandleSize)
        }

        detectDocumentShape(currentBitmap)
    }


    private fun detectDocumentShape(currentBitmap: Bitmap) {
        if(!shouldRunShapeDetection) {
            isDocCropBorderVisible = true
            return
        }

        coroutineScopeProvider?.launch(dispatcherProvider?.computation!!) {
            val docShape = docShapeDetector?.detectShape(currentBitmap)

            withContext(dispatcherProvider?.main!!) {
                binding.docCropBorderView.setCropBorder(docShape?.toDocCropBorder()!!)

                shouldRunShapeDetection = false
                isDocCropBorderVisible = true
            }
        }
    }


    fun scanDocument(onSuccess: (Bitmap) -> Unit) {
        if(!hasImageFile) return

        if(!binding.docCropBorderView.hasValidBorder()) {
            Toast.makeText(context, stringProvider?.getString(R.string.error_cannot_crop), Toast.LENGTH_SHORT).show()
            return
        }

        val docCropBorder = checkNotNull(binding.docCropBorderView.getCropBorder())
        val viewSize = Size(binding.imageView.width.toFloat(), binding.imageView.height.toFloat())
        val cropTransformation = cropTransformationFactory?.createCropTransformation(
            cropCoords = docCropBorder.toCropCoords(),
            viewSize = viewSize
        )

        scannedDocTarget = TargetAdapter(
            onLoaded = { onSuccess(it) },
            onFailed = ::onDocScanFailed
        )

        isImageVisible = false
        isDocCropBorderVisible = false
        isProgressBarVisible = true

        imageLoader?.loadImage(
            Config.Builder()
                .transformation(cropTransformation!!)
                .rotate(currentImageRotation)
                //.source(Config.Source.File(checkNotNull(imageFile)))
                .destination(Config.Destination.Callback(checkNotNull(scannedDocTarget)))
                .build()
        )
    }


    private fun onDocScanFailed(error: Exception) {
        Toast.makeText(context, stringProvider?.getString(R.string.error_scan_failed), Toast.LENGTH_SHORT).show()

    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        coroutineScopeProvider?.cancelChildren()
    }


}