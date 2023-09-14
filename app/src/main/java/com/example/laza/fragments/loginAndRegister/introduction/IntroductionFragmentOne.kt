package com.example.laza.fragments.loginAndRegister.introduction

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.laza.R
import com.example.laza.activites.ShoppingActivity
import com.example.laza.databinding.FragmentIntroductionOneBinding
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.IntroductionViewModel
import com.example.laza.viewmodels.IntroductionViewModel.Companion.ACCOUNT_GET_STARTED_FRAGMENT
import com.example.laza.viewmodels.IntroductionViewModel.Companion.SHOPPING_ACTIVITY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IntroductionFragmentOne : Fragment() {
    private var _binding: FragmentIntroductionOneBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<IntroductionViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroductionOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.navigateState.collect{
                when(it){
                    SHOPPING_ACTIVITY -> {
                        Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }

                    ACCOUNT_GET_STARTED_FRAGMENT -> {
                        findNavController().navigate(it)
                    }

                    else -> {Unit}
                }
            }
        }
        binding.womenBtn.setOnClickListener {
            findNavController().navigate(R.id.action_introductionFragmentOne_to_introductionFragmentSecond)
        }

        binding.skip.setOnClickListener {
            viewModel.startButtonClicked()
            findNavController().navigate(R.id.action_introductionFragmentOne_to_getStartedFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}