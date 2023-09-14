package com.example.laza.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.laza.R
import com.example.laza.databinding.FragmentSearchBinding

class SearchFragment: Fragment(R.layout.fragment_search){
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

// if the previous destination was the HomeFragment, and the user clicked on the back button then navigate to the HomeFragment
        requireActivity().onBackPressedDispatcher.addCallback {
            findNavController().navigateUp()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}