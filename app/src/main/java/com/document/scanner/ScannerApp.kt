package com.document.scanner

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.document.scanner.extension.GlobalContextWrapper
import com.document.scanner.utils.MyDatabase
import net.gotev.speech.Speech
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class ScannerApp : Application() {
    val database by lazy { MyDatabase.geDatabase(this) }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            if (status == SUCCESS) {
                Log.i("OpenCV", "OpenCV loaded successfully")
            } else {
                super.onManagerConnected(status)
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        Speech.init(this, packageName)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        GlobalContextWrapper.bindContext(this)



        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }
}