package com.document.scanner.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.document.scanner.R
import com.document.scanner.activity.GalleryListFramesActivity
import com.document.scanner.activity.ListFramesActivity
import com.document.scanner.adapter.GalleryAdapter
import com.document.scanner.constants.INTENT_GALLERY
import com.document.scanner.constants.INTENT_URIS
import com.document.scanner.databinding.FragmentGalleryBinding
import com.document.scanner.extension.viewBinding
import com.document.scanner.task.backGroundThread
import com.document.scanner.task.uiThread
import com.document.scanner.viewmodel.ImageViewModel
import com.document.scanner.viewmodel.ScanActivityViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class GalleryFragment : BaseFragment<FragmentGalleryBinding, ImageViewModel>() {

    private var adapter: GalleryAdapter? = null
    val scanViewModel: ScanActivityViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        adapter?.reset()
    }


    override val viewBinding: FragmentGalleryBinding by viewBinding(FragmentGalleryBinding::inflate)


    override val viewModel: ImageViewModel by viewModels()

    override fun onLoadData() {

    }

    override fun onReady() {
        onCreate()
    }

    fun onCreate() = with(viewBinding) {

        adapter = GalleryAdapter(activity, emptyList())
        recyclerView.let {
            it.setHasFixedSize(true)
            it.layoutManager = GridLayoutManager(activity, 5)
            it.adapter = adapter
        }

        viewModel.getAllUris.observe(this@GalleryFragment){
            adapter?.setImagePaths(it)
            adapter?.notifyDataSetChanged()
        }
        fab.setOnClickListener {
            adapter?.getSelectedUris().let { uris ->
                if (uris?.isNotEmpty() == true) {
                    Intent(activity, GalleryListFramesActivity::class.java).let {
                        it.putExtra(INTENT_URIS, uris)
                        adapter?.clearSelection()
                        startActivity(it)
                    }
                }else {
                    Toast.makeText(requireContext(), "Please select image", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }
}