package com.example.laza.fragments.loginAndRegister.introduction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.laza.R
import com.example.laza.databinding.FragmentIntroductionOneBinding

class IntroductionFragmentOne : Fragment() {
    private lateinit var binding: FragmentIntroductionOneBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntroductionOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.womenBtn.setOnClickListener {
            findNavController().navigate(R.id.action_introductionFragmentOne_to_introductionFragmentSecond)
        }

        binding.skip.setOnClickListener {
            findNavController().navigate(R.id.action_introductionFragmentOne_to_getStartedFragment)
        }
    }
}