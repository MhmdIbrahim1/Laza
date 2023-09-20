package com.example.laza.fragments.settings

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import br.com.simplepass.loadingbutton.BuildConfig
import com.bumptech.glide.Glide
import com.example.laza.R
import com.example.laza.databinding.FragmentSettingsBinding
import com.example.laza.helper.setGArrowImageBasedOnLayoutDirection
import com.example.laza.helper.setSubArrowImageBasedOnLayoutDirection
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    val binding get() = _binding!!

    private val viewModel by viewModels<UserAccountViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater)
        setGArrowImageBasedOnLayoutDirection(resources,binding.arrow01)
        setSubArrowImageBasedOnLayoutDirection(resources,binding.arrow1)
        setSubArrowImageBasedOnLayoutDirection(resources,binding.arrow2)
        setSubArrowImageBasedOnLayoutDirection(resources,binding.arrow3)
        setSubArrowImageBasedOnLayoutDirection(resources,binding.arrow4)
        setSubArrowImageBasedOnLayoutDirection(resources,binding.arrow5)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeGetUser()

        binding.constraintProfile.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_userAccountFragment)
        }

        binding.linearAllOrders.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_allOrdersFragment)
        }

        binding.linearLanguage.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_languageFragment)
        }

        binding.tvVersion.text =  BuildConfig.VERSION_NAME

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.arrow01.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeGetUser() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            binding.progressbarSettings.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            binding.progressbarSettings.visibility = View.GONE
                            Glide.with(requireContext()).load(it.data!!.imagePath)
                                .error(R.drawable.profile)
                                .into(binding.imageUser)
                            binding.tvUserName.text = "${it.data.firstName} ${it.data.lastName}"
                        }

                        is NetworkResult.Error -> {
                            binding.progressbarSettings.visibility = View.GONE
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        HideBottomNavigation()
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}