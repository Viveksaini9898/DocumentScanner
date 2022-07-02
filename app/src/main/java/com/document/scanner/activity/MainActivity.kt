package com.document.scanner.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.document.scanner.fragments.HomeFragment
import com.document.scanner.R
import com.document.scanner.databinding.ActivityMainBinding
import com.document.scanner.extension.viewBinding
import com.document.scanner.fragments.GalleryFragment
import com.document.scanner.utils.loadFragment
import com.document.scanner.viewmodel.BaseViewModel
import com.google.android.material.navigation.NavigationBarView
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class MainActivity : BaseActivity<ActivityMainBinding,BaseViewModel>(), NavigationBarView.OnItemSelectedListener {



    private val requiredPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)


    fun onCreate() {

        viewBinding.toolbar.title = getString(R.string.app_name)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE_PERMISSIONS)
        }

        viewBinding.bottomBar.setOnItemSelectedListener(this)
        HomeFragment().loadFragment(this,R.id.container)
    }


    override fun onBackPressed() {
        val selectedItemId = viewBinding.bottomBar.selectedItemId
        if (R.id.menu_home != selectedItemId) {
            viewBinding.bottomBar.selectedItemId = R.id.menu_home
        } else {
            super.onBackPressed()
        }
    }

    public override fun onResume() {
        super.onResume()

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
        if (item.itemId == viewBinding.bottomBar.selectedItemId) {
            return false
        }
        loadFragment(item.itemId)
        return true
    }

    private fun loadFragment(itemId: Int) {
        when(itemId){
            R.id.menu_home ->{
                viewBinding.toolbar.title = getString(R.string.app_name)
                HomeFragment().loadFragment(this,R.id.container)
            }
            R.id.menu_gallery ->{
                viewBinding.toolbar.title = getString(R.string.menu_gallery)
                GalleryFragment().loadFragment(this,R.id.container)
            }
            R.id.menu_settings ->{
                viewBinding.toolbar.title = getString(R.string.settings)

            }
        }
    }

    override val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)
    override val viewModel: BaseViewModel? by viewModels()

    override fun onLoadData() {
    }

    override fun onResult(result: ActivityResult, requestCode: Int) {
    }

    override fun onReady() {
        onCreate()
    }
}
