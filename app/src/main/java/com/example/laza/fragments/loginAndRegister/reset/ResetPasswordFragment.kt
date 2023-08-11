package com.example.laza.fragments.loginAndRegister.reset

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.laza.databinding.FragmentResetPasswordBinding
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {
    private lateinit var binding: FragmentResetPasswordBinding
    private val viewModel by viewModels<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)


        binding.arrow1.setOnClickListener {
           findNavController().navigateUp()
        }

        binding.sendResetTv.setOnClickListener {
            val email = binding.edEmialReset.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.resetPassword(email)
            }
        }
        observeResetPassword()
        return binding.root
    }

    private fun observeResetPassword() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resetPassword.collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            showProgress()
                        }

                        is NetworkResult.Success -> {
                            hideProgress()
                            Log.d("ResetPasswordFragment", "observeResetPassword: ${result.data}")
                            Snackbar.make(requireView(), "Reset link sent to your email", Snackbar.LENGTH_LONG).show()
                            binding.edEmialReset.text.clear()
                        }

                        is NetworkResult.Error -> {
                            hideProgress()
                            Snackbar.make(requireView(), "Error: ${result.message}", Snackbar.LENGTH_LONG).show()
                            Log.e("ResetPasswordFragment", "observeResetPassword: ${result.message}")
                        }

                        else -> Unit
                    }
                }
            }
        }
    }


    private fun showProgress(){
        binding.apply {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideProgress(){
        binding.apply {
            progressBar.visibility = View.GONE
        }
    }
}

