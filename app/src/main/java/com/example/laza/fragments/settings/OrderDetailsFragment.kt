package com.example.laza.fragments.settings

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import com.example.laza.adapters.BillingProductAdapter
import com.example.laza.data.order.OrderStatus
import com.example.laza.data.order.getOrderStatus
import com.example.laza.databinding.FragmentOrderDetailsBinding
import com.example.laza.helper.formatPrice
import com.example.laza.utils.HideBottomNavigation
import com.example.storein.utils.VerticalItemDecoration
import com.shuhart.stepview.StepView

class OrderDetailsFragment : Fragment() {
    private var _binding: FragmentOrderDetailsBinding? = null
    private val binding get() = _binding!!

    private val billingProductAdapter by lazy { BillingProductAdapter(requireContext()) }
    private val args by navArgs<OrderDetailsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpOrderDetailRv()

        val order = args.order

        binding.apply {
            tvOrderId.text = "Order #${order.orderId}"
            val steps = mutableListOf(
                OrderStatus.Ordered.status,
                OrderStatus.Confirmed.status,
                OrderStatus.Shipped.status,
                OrderStatus.Delivered.status,
            )

            val orderStatus = getOrderStatus(order.orderStatus)
            if (orderStatus == OrderStatus.Canceled) {
                steps.add(0, OrderStatus.Canceled.status)
            }
            if (orderStatus == OrderStatus.Returned) {
                steps.add(0, OrderStatus.Returned.status)
            }

            stepView.setSteps(steps)

            val currentOrderStep = when (orderStatus) {
                OrderStatus.Ordered -> 0
                OrderStatus.Confirmed -> 1
                OrderStatus.Shipped -> 2
                OrderStatus.Delivered -> 3
                OrderStatus.Canceled -> 0
                OrderStatus.Returned -> 0
            }

            // Animate the step view to the current step
            stepView.state
                .animationType(StepView.ANIMATION_ALL)
                .commit()
            animateStepView(stepView, currentOrderStep)

            // Mark the current step as done
            if (currentOrderStep == 3 && orderStatus != OrderStatus.Canceled && orderStatus != OrderStatus.Returned) {
                stepView.done(true)
            }

            // Change the color of the step view to red if the order is canceled
            if (orderStatus == OrderStatus.Canceled) {
                stepView.done(false)
                // paint the step view with red color
                stepView.state
                    .selectedCircleColor(ContextCompat.getColor(requireContext(), R.color.g_red))
                    .selectedTextColor(ContextCompat.getColor(requireContext(), R.color.g_red))
                    .commit()
            }

            if (orderStatus == OrderStatus.Returned) {
                stepView.done(false)
                // paint the step view with red color
                stepView.state
                    .selectedCircleColor(ContextCompat.getColor(requireContext(), R.color.g_red))
                    .selectedTextColor(ContextCompat.getColor(requireContext(), R.color.g_red))
                    .commit()
            }


            tvFullName.text = order.address.fullName
            tvAddress.text =
                "${order.address.street} ${order.address.city} ${order.address.state}"
            tvPhoneNumber.text = order.address.phone
            tvTotalPrice.text = "E£ ${order.totalPrice.formatPrice()}"
        }

        billingProductAdapter.differ.submitList(order.products)

        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setUpOrderDetailRv() {
        binding.rvProducts.apply {
            adapter = billingProductAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }
    }

    private fun animateStepView(stepView: StepView, targetStep: Int) {
        val animator = ValueAnimator.ofInt(0, targetStep)
        animator.duration = 700 // Adjust the duration as needed
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            stepView.go(animatedValue, true)
        }
        animator.start()
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
