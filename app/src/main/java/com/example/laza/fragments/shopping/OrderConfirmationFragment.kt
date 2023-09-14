package com.example.laza.fragments.shopping

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.example.laza.R
import com.example.laza.databinding.FragmentOrderConfirmationBinding
import com.example.laza.utils.HideBottomNavigation


class OrderConfirmationFragment : Fragment() {
    private var _binding: FragmentOrderConfirmationBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.arrow1.setOnClickListener {
            findNavController().navigate(R.id.action_orderConfirmationFragment_to_homeFragment)
        }
        binding.continueShopping.setOnClickListener {
            findNavController().navigate(R.id.action_orderConfirmationFragment_to_homeFragment)
        }
        binding.goToOrders.setOnClickListener {
            findNavController().navigate(R.id.action_orderConfirmationFragment_to_allOrdersFragment)
        }

        val lottieAnimationView = binding.imageOrderConfirm
        lottieAnimationView.setAnimation(R.raw.confirmed)
        lottieAnimationView.repeatCount = LottieDrawable.INFINITE
        lottieAnimationView.playAnimation()
    }

    override fun onResume() {
        super.onResume()
        HideBottomNavigation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}