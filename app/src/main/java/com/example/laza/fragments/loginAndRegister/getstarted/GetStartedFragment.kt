package com.example.laza.fragments.loginAndRegister.getstarted

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.laza.R
import com.example.laza.activites.ShoppingActivity
import com.example.laza.activites.TwitterActivity
import com.example.laza.databinding.FragmentGetStartedBinding
import com.example.laza.utils.getGoogleSignInClient
import com.example.laza.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetStartedFragment : Fragment() {
    private lateinit var binding: FragmentGetStartedBinding
    private val viewModel by viewModels<LoginViewModel>()

    private var doubleBackToExitPressedOnce = false
    private val doublePressHandler = Handler(Looper.myLooper()!!)
    private var shouldHandleBackPress = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGetStartedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createAccountGetStarted.setOnClickListener {
            findNavController().navigate(R.id.action_getStartedFragment_to_registerFragment)
        }

        binding.logIn.setOnClickListener {
            findNavController().navigate(R.id.action_getStartedFragment_to_loginFragment)
        }

        binding.arrow1.setOnClickListener {
            if (doubleBackToExitPressedOnce) {
                requireActivity().finish()
            } else {
                doubleBackToExitPressedOnce = true
                showExitSnackBar()

                doublePressHandler.postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000) // Delay for resetting the double press flag
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (shouldHandleBackPress) {
                if (doubleBackToExitPressedOnce) {
                    requireActivity().finish()
                } else {
                    doubleBackToExitPressedOnce = true
                    showExitSnackBar()

                    doublePressHandler.postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000) // Delay for resetting the double press flag
                }
            } else {
                // If not handling the back press, let the activity handle it
                isEnabled = false
                requireActivity().onBackPressed()
                isEnabled = true
            }
        }

    binding.logInWithGoogle.setOnClickListener {
            val signInClient = getGoogleSignInClient(requireContext())
            Log.d("GetStartedFragment", "Launching Google Sign-In Intent")
            googleSignInLauncher.launch(signInClient.signInIntent)
        }

        binding.logInWithTwitter.setOnClickListener {
            val intent = Intent(requireContext(), TwitterActivity::class.java)
            startActivity(intent)
        }
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    viewModel.signInWithGoogle(it.idToken!!)
                    // Navigate to ShoppingActivity
                    val shoppingActivityIntent =
                        Intent(requireActivity(), ShoppingActivity::class.java)
                    shoppingActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(shoppingActivityIntent)
                } ?: run {
                    Toast.makeText(
                        requireContext(),
                        "Google sign-in failed: No account",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(
                    requireContext(),
                    "Google sign-in failed: ${e.statusCode}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    private fun showExitSnackBar() {
        Snackbar.make(binding.root, "Press again to exit the app", Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        doublePressHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        doubleBackToExitPressedOnce = false // Reset the flag when the fragment is resumed
    }

    override fun onPause() {
        super.onPause()
        doublePressHandler.removeCallbacksAndMessages(null) // Remove callbacks when the fragment is paused
    }
}