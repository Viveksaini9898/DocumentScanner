package com.document.scanner.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.document.scanner.R
import com.document.scanner.ScannerApp
import com.document.scanner.constants.INTENT_ANGLE
import com.document.scanner.constants.INTENT_CROPPED_PATH
import com.document.scanner.constants.INTENT_DOCUMENT_ID
import com.document.scanner.constants.INTENT_SOURCE_PATH
import com.document.scanner.databinding.ActivityScanBinding
import com.document.scanner.extension.viewBinding
import com.document.scanner.task.uiThread
import com.document.scanner.utils.DetectBox
import com.document.scanner.utils.Utils
import com.document.scanner.utils.YuvToRgbConverter
import com.document.scanner.viewmodel.ScanActivityViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

class ScanActivity : BaseActivity<ActivityScanBinding,ScanActivityViewModel>() {

    override val viewModel:ScanActivityViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private val requestCodePermissions = 1001
    private val requiredPermissions =
        arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE")
    private var count = 0
    private var angle = 0

    private lateinit var executor: Executor
    private lateinit var converter: YuvToRgbConverter


    private fun confirm() {
        val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault())
        val name = getString(R.string.app_name) + " " + simpleDateFormat.format(Date())
        viewModel.capture(name, angle, count, this)
    }

     @SuppressLint("UnsafeOptInUsageError")
     fun onCreate() {

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissions, requestCodePermissions)
        }

        executor = ContextCompat.getMainExecutor(this)
        converter = YuvToRgbConverter(this)
        intent.getStringExtra(INTENT_DOCUMENT_ID)?.let { docId ->
            viewModel.getPageCount(docId).observe(this) { count -> this.count = count }
        }
        viewBinding.ivRecentCapture.setOnClickListener {
            confirm()
        }


        viewBinding.flash.setOnClickListener {
            when (imageCapture?.flashMode) {
                FLASH_MODE_OFF -> {
                    viewBinding.flash.setImageResource(R.drawable.ic_flash_on_black_24dp)
                    imageCapture?.flashMode = FLASH_MODE_ON
                }
                FLASH_MODE_ON -> {
                    viewBinding.flash.setImageResource(R.drawable.ic_flash_auto_black_24dp)
                    imageCapture?.flashMode = FLASH_MODE_AUTO
                }
                FLASH_MODE_AUTO -> {
                    viewBinding.flash.setImageResource(R.drawable.ic_flash_off_black_24dp)
                    imageCapture?.flashMode = FLASH_MODE_OFF
                }
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }


    @ExperimentalGetImage
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodePermissions) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    @ExperimentalGetImage
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    @ExperimentalGetImage
    fun processImage(imageProxy: ImageProxy) {
        imageProxy.image?.apply {
            if (this.format == ImageFormat.YUV_420_888) {
                uiThread {
                    Bitmap.createBitmap(this@apply.width, this@apply.height, Bitmap.Config.ARGB_8888)
                        .let { bitmap ->
                            converter.yuvToRgb(this@apply, bitmap)
                            DetectBox.findCorners(bitmap, angle).let {
                                imageProxy.close()
                                viewBinding.scanView.setBoundingRect(it)
                            }
                        }
                }
            } else {
                imageProxy.close()
            }
        }
    }

    @ExperimentalGetImage
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val builder = Builder()
        imageCapture = builder.build()
        val imageAnalysis = ImageAnalysis.Builder().build()


        preview.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis)
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy: ImageProxy ->
            angle = imageProxy.imageInfo.rotationDegrees
            processImage(imageProxy)
        }


        viewBinding.btnCapture.setOnClickListener {
            imageCapture?.flashMode
            viewBinding.pbScan.visibility = View.VISIBLE
            val file = Utils.createPhotoFile(this)
            imageCapture?.takePicture(
                OutputFileOptions.Builder(file).build(),
                executor,
                object : OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: OutputFileResults) {
                        Intent(this@ScanActivity, CropActivity::class.java).let {
                            it.putExtra(INTENT_SOURCE_PATH, file.absolutePath)
                            it.putExtra(INTENT_ANGLE, angle)
                            resultLauncher.launch(it)
                        }
                        viewBinding.pbScan.visibility = View.GONE
                    }

                    override fun onError(error: ImageCaptureException) {
                        Log.e(TAG, Log.getStackTraceString(error))
                    }
                })

        }
    }

    override var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    val croppedPath = intent.getStringExtra(INTENT_CROPPED_PATH)
                    val sourcePath = intent.getStringExtra(INTENT_SOURCE_PATH)
                    viewModel.addPath(sourcePath!!, croppedPath!!)
                    viewBinding.let {
                        it.ivRecentCapture.setImageBitmap(BitmapFactory.decodeFile(croppedPath))
                        if (it.pageCount.visibility != View.VISIBLE) it.pageCount.visibility =
                            View.VISIBLE
                        it.pageCount.text = viewModel.pathsCount().toString()
                    }
                }
            }
        }

    companion object {
        val TAG: String = ScanActivity::class.java.simpleName
    }

    override val viewBinding: ActivityScanBinding by viewBinding(ActivityScanBinding::inflate)

    override fun onLoadData() {
    }

    override fun onResult(result: ActivityResult, requestCode: Int) {
    }

    override fun onReady() {
        onCreate()
    }
}