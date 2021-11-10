package com.document.scanner.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.document.scanner.fragments.HomeFragment
import com.document.scanner.R
import com.document.scanner.databinding.ActivityMainBinding
import com.document.scanner.fragments.GalleryFragment
import com.document.scanner.utils.loadFragment
import com.google.android.material.navigation.NavigationBarView
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class MainActivity : BaseActivity(), NavigationBarView.OnItemSelectedListener {

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            if (status == SUCCESS) {
                Log.i("OpenCV", "OpenCV loaded successfully")
            } else {
                super.onManagerConnected(status)
            }
        }
    }

    private val requiredPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE_PERMISSIONS)
        }

        binding?.bottomBar?.setOnItemSelectedListener(this)
        HomeFragment().loadFragment(this,R.id.container)
    }


    override fun onBackPressed() {
        val selectedItemId = binding.bottomBar.selectedItemId
        if (R.id.menu_home != selectedItemId) {
            binding.bottomBar.selectedItemId = R.id.menu_home
        } else {
            super.onBackPressed()
        }
    }

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
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

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == binding.bottomBar.selectedItemId) {
            return false
        }
        loadFragment(item.itemId)
        return true
    }

    private fun loadFragment(itemId: Int) {
        when(itemId){
            R.id.menu_home ->{
                HomeFragment().loadFragment(this,R.id.container)
            }
            R.id.menu_gallery ->{
                GalleryFragment().loadFragment(this,R.id.container)
            }
            R.id.menu_settings ->{

            }
        }
    }
}
