package com.document.scanner.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.document.scanner.R
import com.document.scanner.activity.ScanActivity
import com.document.scanner.adapter.DocumentsAdapter
import com.document.scanner.databinding.FragmentHomeBinding
import com.document.scanner.extension.viewBinding
import com.document.scanner.extension.visibility
import com.document.scanner.viewmodel.MainActivityViewModel


class HomeFragment : BaseFragment<FragmentHomeBinding,MainActivityViewModel>() {

    override val viewModel :MainActivityViewModel by viewModels()
    private val documentsAdapter by lazy { DocumentsAdapter(requireActivity(), emptyList(), viewModel) }



    private fun onViewCreated() = with(viewBinding) {

        fab.setOnClickListener {
            startActivity(Intent(requireContext(),ScanActivity::class.java))
        }


        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = documentsAdapter
            viewModel.getAllDocuments()?.observe(this@HomeFragment) {
                if (it?.isNotEmpty() == true) {
                    empty.visibility(false)
                    recyclerView.visibility(true)
                    documentsAdapter.updateDocuments(it)
                }else {
                    empty.visibility(true)
                    recyclerView.visibility(false)
                }
            }
        }

    }

    override val viewBinding: FragmentHomeBinding by viewBinding(FragmentHomeBinding::inflate)


    override fun onLoadData() {
    }

    override fun onReady() {
        onViewCreated()
    }

}