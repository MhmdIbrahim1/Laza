package com.example.laza.fragments.brands

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.laza.R
import com.example.laza.databinding.FragmentBrandsBinding

class BrandsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBrandsBinding.inflate(inflater, container, false)
        val brandName = arguments?.getString("brandName") ?: ""
        binding.brandName.text = brandName
        return binding.root
    }
}