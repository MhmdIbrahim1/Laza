package com.example.laza.fragments.loginAndRegister

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.laza.R
import com.example.laza.activites.ShoppingActivity
import com.example.laza.databinding.FragmentLoginBinding
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        login()
        observeLogin()

        return binding.root
    }

    private fun login(){
        binding.apply {
            loginTv.setOnClickListener {
                val email = edEmail.text.toString().trim()
                val password = edPassword.text.toString()
                viewModel.login(email,password)
            }
        }
    }

    private fun observeLogin(){
        lifecycleScope.launch {
            viewModel.login.collect(){
                when(it){
                    is NetworkResult.Loading ->{
                        showProgress()
                    }
                    is NetworkResult.Success ->{
                        Intent(requireActivity(), ShoppingActivity::class.java).also {intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        hideProgress()
                        Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_SHORT).show()
                    }

                    is NetworkResult.Error ->{
                        hideProgress()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }
                    else ->Unit
                }
            }
        }
    }

    private fun hideProgress() {
        binding.progressBar2.visibility = View.GONE
    }

    private fun showProgress() {
        binding.progressBar2.visibility = View.VISIBLE
    }

}