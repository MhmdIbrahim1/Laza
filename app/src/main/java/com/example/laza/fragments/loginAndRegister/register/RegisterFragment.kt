package com.example.laza.fragments.loginAndRegister.register

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.laza.R
import com.example.laza.data.User
import com.example.laza.databinding.FragmentRegisterBinding
import com.example.laza.helper.setGArrowImageBasedOnLayoutDirection
import com.example.laza.utils.NetworkResult
import com.example.laza.utils.RegisterValidation
import com.example.laza.viewmodels.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RegisterViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        setGArrowImageBasedOnLayoutDirection(resources,binding.arrow1)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register()
        observeRegister()
        observeValidation()

        binding.backArrow.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun register(){
        binding.apply {
            createAccount.setOnClickListener {
                val user = User(
                    edFirstNameReg.text.toString().trim(),
                    edLastName.text.toString().trim(),
                    edEmailReg.text.toString().trim(),
                )
                val password = edPassword.text.toString()
                viewModel.createAccountWithEmailAndPassword(user,password)
            }
        }
    }

    private fun observeRegister() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.register.collect {
                    when (it) {
                        is NetworkResult.Loading -> {
                            showProgressBar()
                        }

                        is NetworkResult.Success -> {
                            Log.d("RegisterFragment", it.data.toString())
                            hideProgressBar()
                            binding.apply {
                                edFirstNameReg.setText("")
                                edLastName.setText("")
                                edEmailReg.setText("")
                                edPassword.setText("")
                            }
                            findNavController().navigate(R.id.action_registerFragment_to_getStartedFragment)
                            // Navigate to login screen
                            Toast.makeText(
                                requireContext(),
                                "Register success",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is NetworkResult.Error -> {
                            Log.e("test", it.message.toString())
                            hideProgressBar()
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeValidation() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validation.collect { validation ->
                    if (validation.email is RegisterValidation.Failed) {
                        withContext(Dispatchers.Main) {
                            binding.edEmailReg.apply {
                                requestFocus()
                                error = validation.email.message
                            }
                        }
                    }
                    if (validation.password is RegisterValidation.Failed) {
                        withContext(Dispatchers.Main) {
                            binding.edPassword.apply {
                                requestFocus()
                                error = validation.password.message
                            }
                        }
                    }
                }
            }
        }
    }
    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}