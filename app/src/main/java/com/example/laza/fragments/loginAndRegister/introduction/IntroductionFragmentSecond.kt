package com.example.laza.fragments.loginAndRegister.introduction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.laza.R
import com.example.laza.databinding.FragmentIntroductionSecondBinding

class IntroductionFragmentSecond : Fragment() {
    private lateinit var binding: FragmentIntroductionSecondBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntroductionSecondBinding.inflate(inflater, container, false)

        binding.menBtn.setOnClickListener {
            findNavController().navigate(R.id.action_introductionFragmentSecond_to_introductionFragmentOne)
        }

        binding.skip.setOnClickListener {
            findNavController().navigate(R.id.action_introductionFragmentSecond_to_getStartedFragment)
        }
        return binding.root
    }
}