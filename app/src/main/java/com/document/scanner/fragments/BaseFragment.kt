package com.document.scanner.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.document.scanner.viewmodel.BaseViewModel

abstract  class BaseFragment <
        VB : ViewBinding,
        VM : BaseViewModel,
        > : Fragment() {

    protected abstract val viewBinding: VB
    protected abstract val viewModel: VM?

    protected abstract fun onLoadData()
    protected abstract fun onReady()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onPostInit()
    }

    @CallSuper
    protected open fun onPostInit() {
        onReady()
        loadData()
    }


    private fun loadData() {
        activity?.lifecycleScope?.launchWhenResumed {
            onLoadData()
        }
    }


}