package com.document.scanner.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.GONE
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.document.scanner.R
import com.document.scanner.ScannerApp
import com.document.scanner.constants.INTENT_FRAME_POSITION
import com.document.scanner.data.Frame
import com.document.scanner.databinding.ActivityEditBinding
import com.document.scanner.extension.viewBinding
import com.document.scanner.task.backGroundThread
import com.document.scanner.task.uiThread
import com.document.scanner.utils.BrightnessAndContrastController
import com.document.scanner.utils.Filter
import com.document.scanner.utils.Utils
import com.document.scanner.viewmodel.EditActivityViewModel
import com.document.scanner.viewmodel.ScanActivityViewModel

import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.Utils.matToBitmap
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.*

class EditActivity : BaseActivity<ActivityEditBinding,EditActivityViewModel>(), View.OnClickListener, OnSeekBarChangeListener,
    NavigationBarView.OnItemSelectedListener {

    override val viewModel: EditActivityViewModel by viewModels()

    private var croppedMat: Mat? = null
    private var editedMat: Mat? = null
    private  var frame: Frame? = null
    private  var brightnessAndContrastController: BrightnessAndContrastController? = null

    private var currentActiveId = R.id.iv_original_image
    private var modifyToolsIsVisible = false

    private val frameId by lazy { intent.getLongExtra(INTENT_FRAME_POSITION, -1)}


    private fun setupPreview() = with(viewBinding) {
        croppedMat = Utils.readMat(frame?.croppedUri)
        if (frame?.editedUri == null) {
            editedMat = Mat()
            croppedMat?.copyTo(editedMat)
        } else {
            editedMat = Utils.readMat(frame?.editedUri)
        }
        previewMat(editedMat!!)
        pbEdit.visibility = View.GONE
    }

    private fun filterImageButton(resourceId: Int, processImage: ProcessImage) {
        lifecycleScope.launch(Dispatchers.Default) {
            val height = croppedMat?.height()?.toDouble()
            val width = croppedMat?.width()?.toDouble()
            Mat().let { result ->
                Imgproc.resize(croppedMat, result, Size(width!!, height!!))
                processImage.process(result).let {
                    val bmp = Bitmap.createBitmap(
                        it.width(),
                        it.height(),
                        Bitmap.Config.ARGB_8888
                    )
                    matToBitmap(it, bmp)
                    lifecycleScope.launch(Dispatchers.Main) {
                        (findViewById<View?>(resourceId) as ImageView).setImageBitmap(bmp)
                    }
                }
            }
        }
    }

    private fun filterImage(processImage: ProcessImage) = with(viewBinding){
        pbEdit.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.Default) {
            editedMat = processImage.process(croppedMat!!)
            previewMat(editedMat!!)
            lifecycleScope.launch(Dispatchers.Main) { pbEdit.visibility = View.GONE }
        }
    }

    private fun setupFilterButtons() {
        filterImageButton(R.id.iv_original_image, object : ProcessImage {
            override fun process(mat: Mat): Mat {
                return croppedMat!!
            }
        })
        filterImageButton(
            R.id.iv_black_and_white, object : ProcessImage {
                override fun process(mat: Mat): Mat {
                    return Filter.thresholdOTSU(mat)
                }
            })
        filterImageButton(R.id.iv_auto, object : ProcessImage {
            override fun process(mat: Mat): Mat {
                return Filter.auto(mat)
            }
        })
        filterImageButton(R.id.iv_grayscale, object : ProcessImage {
            override fun process(mat: Mat): Mat {
                return Filter.grayscale(mat)
            }
        })
        filterImageButton(R.id.iv_magic, object : ProcessImage {
            override fun process(mat: Mat): Mat {
                return Filter.magicColor(mat)
            }
        })
    }

    private fun setActive(activeId: Int) {
        findViewById<View>(currentActiveId).apply {
            alpha = 0.6f
            setPadding(12, 12, 12, 12)
        }
        findViewById<View>(activeId).apply {
            alpha = 1f
            setPadding(0, 0, 0, 0)
        }
        currentActiveId = activeId
    }

     fun onCreate() {
        setActive(R.id.iv_auto)

         viewBinding.tool.titleTv.text = "Edit"
         viewBinding.tool.back.setOnClickListener {
             finish()
         }
        if (frameId == -1L) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
            finish()
            return
        }


        viewModel.getFrame(frameId)?.observe(this) { frame ->
            this.frame = frame
            setupPreview()
            setupFilterButtons()
            setupBrightnessAndContrast()
        }

        viewBinding.let {
            it.bottomNavigationView.setOnItemSelectedListener(this)
            it.ivBlackAndWhite.setOnClickListener(this)
            it.ivAuto.setOnClickListener(this)
            it.ivGrayscale.setOnClickListener(this)
            it.ivMagic.setOnClickListener(this)
            it.ivOriginalImage.setOnClickListener(this)
        }
    }


    private fun setupBrightnessAndContrast() {
        brightnessAndContrastController = BrightnessAndContrastController(0.0, 1.0)
        viewBinding.sbBrightness.let {
            it.max = 200
            it.progress = 100
            it.setOnSeekBarChangeListener(this)
        }
        viewBinding.sbContrast.let {
            it.max = 200
            it.progress = 100
            it.setOnSeekBarChangeListener(this)
        }
        viewBinding.llModifyTools.visibility = GONE
    }

    private fun resetBrightnessAndContrast() {
        brightnessAndContrastController?.brightness = 0.0
        brightnessAndContrastController?.contrast = 0.0
        viewBinding.sbBrightness.progress = 100
        viewBinding.sbContrast.progress = 100
    }

    private fun previewMat(mat: Mat) {
        val bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        matToBitmap(mat, bitmap)
        uiThread {
            viewBinding.ivEdit.setImageBitmap(bitmap)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                saveImage()
            }
            R.id.menu_rotate_left -> {
                rotateLeft()
            }
            R.id.menu_retake -> {
                setResult(RESULT_CANCELED)
                finish()
            }
            R.id.menu_modify -> {
                modifyToolsIsVisible = !modifyToolsIsVisible
                viewBinding.llModifyTools.visibility =
                    if (modifyToolsIsVisible) View.VISIBLE else View.GONE
            }
        }
        return false
    }

    private fun saveImage() {
        viewBinding.pbEdit.visibility = View.VISIBLE
        uiThread {
            brightnessAndContrastController?.mat?.let {
                editedMat = it
            }
            try {
                Utils.let {
                    it.saveMat(editedMat, frame?.editedUri)
                    it.saveMat(croppedMat, frame?.croppedUri)
                }
            }catch (out : OutOfMemoryError) {
               System.gc()
            }
            Intent().let {
                it.putExtra(
                    INTENT_FRAME_POSITION,
                    intent.getIntExtra(INTENT_FRAME_POSITION, 0)
                )
                setResult(RESULT_OK, it)
            }
            editedMat?.release()
            croppedMat?.release()
           finish()
        }
    }

    private fun rotateLeft() {
        Core.rotate(croppedMat, croppedMat, Core.ROTATE_90_COUNTERCLOCKWISE)
        Core.rotate(editedMat, editedMat, Core.ROTATE_90_COUNTERCLOCKWISE)
        previewMat(editedMat!!)
    }

    override fun onClick(view: View) {
        setActive(view.id)
        resetBrightnessAndContrast()
        when (view.id) {
            R.id.iv_black_and_white -> {
                filterImage(object : ProcessImage {
                    override fun process(mat: Mat): Mat {
                        return Filter.thresholdOTSU(mat)
                    }
                })
            }
            R.id.iv_auto -> {
                filterImage(object : ProcessImage {
                    override fun process(mat: Mat): Mat {
                        return Filter.auto(mat)
                    }
                })
            }
            R.id.iv_grayscale -> {
                filterImage(object : ProcessImage {
                    override fun process(mat: Mat): Mat {
                        return Filter.grayscale(mat)
                    }
                })
            }
            R.id.iv_magic -> {
                filterImage(object : ProcessImage {
                    override fun process(mat: Mat): Mat {
                        return Filter.magicColor(mat)
                    }
                })
            }
            R.id.iv_original_image -> {
                editedMat = croppedMat?.clone()
                previewMat(editedMat!!)
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.sb_contrast -> {
                viewBinding.tvContrast.text = String.format(
                    Locale.getDefault(),
                    "Contrast • %d%%",
                    i - 100
                )
                brightnessAndContrastController?.setContrast(editedMat!!, i / 100.0)
            }
            R.id.sb_brightness -> {
                viewBinding.tvBrightness.text = String.format(
                    Locale.getDefault(),
                    "Brightness • %d%%",
                    i - 100
                )
                brightnessAndContrastController?.setBrightness(editedMat!!, (i - 100).toDouble())
            }
            else -> {
                editedMat
            }
        }.let {
            previewMat(it!!)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    internal interface ProcessImage {
        fun process(mat: Mat): Mat
    }

    override val viewBinding: ActivityEditBinding by viewBinding(ActivityEditBinding::inflate)


    override fun onLoadData() {
    }

    override fun onResult(result: ActivityResult, requestCode: Int) {
    }

    override fun onReady() {
       onCreate()
    }
}