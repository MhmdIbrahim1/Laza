package com.example.laza.fragments.loginAndRegister

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.example.laza.R
import com.example.laza.databinding.FragmentGetStartedBinding
import com.google.android.material.snackbar.Snackbar

class GetStartedFragment : Fragment() {
    private lateinit var binding: FragmentGetStartedBinding
    private var doubleBackToExitPressedOnce = false
    private val doublePressHandler = Handler()

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
            if (doubleBackToExitPressedOnce){
                requireActivity().finish()
            } else {
                doubleBackToExitPressedOnce = true
                showExitSnackBar()

                doublePressHandler.postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000) // Delay for resetting the double press flag
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            if (doubleBackToExitPressedOnce){
                requireActivity().finish()
            } else {
                doubleBackToExitPressedOnce = true
                showExitSnackBar()

                doublePressHandler.postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000) // Delay for resetting the double press flag
            }
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