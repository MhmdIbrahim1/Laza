package com.example.laza.fragments.loginAndRegister

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.laza.R
import com.example.laza.data.User
import com.example.laza.databinding.FragmentRegisterBinding
import com.example.laza.utils.NetworkResult
import com.example.laza.utils.RegisterValidation
import com.example.laza.viewmodels.RegisterViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register()
        observeRegister()
        observeValidation()
    }

    private fun register(){
        binding.apply {
            createAccount.setOnClickListener {
                val user = User(
                    edFirstName.text.toString().trim(),
                    edLastName.text.toString().trim(),
                    edEmail.text.toString().trim(),
                )
                val password = edPassword.text.toString()
                viewModel.createAccountWithEmailAndPassword(user,password)
            }
        }
    }

    private fun observeRegister(){
        lifecycleScope.launchWhenStarted {
            viewModel.register.collect{
                when(it){
                    is NetworkResult.Loading -> {
                        showProgressBar()
                    }

                    is NetworkResult.Success -> {
                        Log.d("RegisterFragment", it.data.toString())
                        hideProgressBar()
                        // Navigate to login screen
                        Snackbar.make(
                            requireView(),
                            "Register success",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    is NetworkResult.Error -> {
                        Log.e("test", it.message.toString())
                        hideProgressBar()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun observeValidation(){
        lifecycleScope.launchWhenStarted {
            viewModel.validation.collect{ validation ->
                if (validation.email is RegisterValidation.Failed){
                    withContext(Dispatchers.Main){
                        binding.edEmail.apply {
                            requestFocus()
                            error = validation.email.message
                        }
                    }
                }
                if (validation.password is RegisterValidation.Failed){
                    withContext(Dispatchers.Main){
                        binding.edPassword.apply {
                            requestFocus()
                            error = validation.password.message
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
}