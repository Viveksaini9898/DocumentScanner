package com.document.scanner.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.document.scanner.R
import com.document.scanner.activity.ScanActivity
import com.document.scanner.databinding.FragmentHomeBinding


class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         binding = FragmentHomeBinding.bind(view)

        binding?.fab?.setOnClickListener {
            startActivity(Intent(requireContext(),ScanActivity::class.java))
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()

    }
}