package com.document.scanner.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.document.scanner.viewmodel.BaseViewModel

abstract class BaseActivity<
        VB : ViewBinding,
        VM : BaseViewModel,
        > : AppCompatActivity() {

    protected abstract val viewBinding: VB
    protected abstract val viewModel: VM?

    protected abstract fun onLoadData()
    protected abstract fun onResult(result: ActivityResult, requestCode: Int)

    protected abstract fun onReady()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        onPostInit()
    }


    @CallSuper
    protected open fun onPostInit() {
        loadData()
        onReady()
    }


    private fun loadData() {
        lifecycleScope.launchWhenResumed {
            onLoadData()
        }
    }

    open var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onResult(result,90)
    }
}