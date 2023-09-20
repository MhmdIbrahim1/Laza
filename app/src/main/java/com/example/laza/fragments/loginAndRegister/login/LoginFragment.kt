package com.example.laza.fragments.loginAndRegister.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.laza.R
import com.example.laza.activites.ShoppingActivity
import com.example.laza.databinding.FragmentLoginBinding
import com.example.laza.helper.setGArrowImageBasedOnLayoutDirection
import com.example.laza.utils.NetworkResult
import com.example.laza.utils.getGoogleSignInClient
import com.example.laza.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        setGArrowImageBasedOnLayoutDirection(resources,binding.arrow1)

        login()
        observeLogin()

        binding.forgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }

        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.forgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }

        return binding.root
    }

    private fun login(){
        binding.apply {
            loginTv.setOnClickListener {
                val email = edEmail.text.toString().trim()
                val password = edPassword.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.login(email, password)
                } else {
                    Snackbar.make(requireView(), "Please enter the email and password!!", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeLogin() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.login.collect {
                    when (it) {
                        is NetworkResult.Loading -> {
                            showProgress()
                        }

                        is NetworkResult.Success -> {
                            Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                            hideProgress()
                            Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_SHORT)
                                .show()
                        }

                        is NetworkResult.Error -> {
                            hideProgress()
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        binding.loginWithGoogle.setOnClickListener {
            val signInClient = getGoogleSignInClient(requireContext())
            googleSignInLauncher.launch(signInClient.signInIntent)
        }

    }
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    viewModel.signInWithGoogle(it.idToken!!)
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign-in failed", Toast.LENGTH_LONG).show()
            }
        }

    private fun hideProgress() {
        binding.progressBar2.visibility = View.GONE
    }

    private fun showProgress() {
        binding.progressBar2.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}