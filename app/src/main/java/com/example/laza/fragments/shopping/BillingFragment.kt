package com.example.laza.fragments.shopping

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import com.example.laza.adapters.AddressAdapter
import com.example.laza.adapters.BillingProductAdapter
import com.example.laza.data.Address
import com.example.laza.data.CartProduct
import com.example.laza.data.order.Order
import com.example.laza.data.order.OrderStatus
import com.example.laza.databinding.FragmentBillingBinding
import com.example.laza.helper.formatPrice
import com.example.laza.helper.setGArrowImageBasedOnLayoutDirection
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.BillingViewModel
import com.example.laza.viewmodels.OrderViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BillingFragment : Fragment() {
    private var _binding: FragmentBillingBinding? = null
    private val binding get() = _binding!!

    private val addressAdapter by lazy { AddressAdapter() }
    private val billingAdapter by lazy { BillingProductAdapter(requireContext()) }
    private val viewModel by viewModels<BillingViewModel>()
    private val orderViewModel by viewModels<OrderViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f
    private var selectedAddress: Address? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.products.toList()
        totalPrice = args.totalPrice
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillingBinding.inflate(inflater, container, false)
        setGArrowImageBasedOnLayoutDirection(resources,binding.arrow1)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpAddressRv()
        setUpBillingRv()

        observeAddresses()
        observeOrder()

        if (!args.payment) {
            binding.apply {
                buttonPlaceOrder.visibility = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                middleLine.visibility = View.INVISIBLE
                bottomLine.visibility = View.INVISIBLE
            }
        }

        billingAdapter.differ.submitList(products)
        binding.tvTotalPrice.text = resources.getString(R.string.egp, totalPrice.formatPrice())

        addressAdapter.onClick = {
            selectedAddress = it
            if (!args.payment) {
                val action = BillingFragmentDirections.actionBillingFragmentToAddressesFragment(it)
                findNavController().navigate(action)
            }
        }


        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressesFragment)
        }
        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null){
                Toast.makeText(requireContext(), "Please select an address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }

        // if the previous destination was the HomeFragment, and the user clicked on the back button then navigate to the HomeFragment
        requireActivity().onBackPressedDispatcher.addCallback {
            if (findNavController().previousBackStackEntry?.destination?.id == R.id.homeFragment) {
                findNavController().navigate(R.id.action_billingFragment_to_homeFragment)
            } else {
                findNavController().navigateUp()
            }
        }

    }

    private fun showOrderConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.order_confirmation_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            .setView(dialogView)
            .create()

        val cancelButton = dialogView.findViewById<View>(R.id.confirm_cancel_button)
        val confirmButton = dialogView.findViewById<View>(R.id.confirm_yes__button)

        cancelButton.setOnClickListener {
            alertDialogBuilder.dismiss()
        }
        confirmButton.setOnClickListener {
            val order = Order(
                OrderStatus.Ordered.status,
                totalPrice,
                products,
                selectedAddress!!,
                date = System.currentTimeMillis().toString()
            )
            orderViewModel.placeOrder(order)
            alertDialogBuilder.dismiss()
        }

        alertDialogBuilder.show()

    }

    private fun setUpBillingRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingAdapter
            binding.rvProducts.addItemDecoration(ItemSpacingDecoration(15))
        }
    }

    private fun setUpAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
            binding.rvAddress.addItemDecoration(ItemSpacingDecoration(10))
        }
    }


    private fun observeAddresses() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addresses.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            binding.progressbarAddress.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            addressAdapter.differ.submitList(it.data)
                            binding.progressbarAddress.visibility = View.GONE
                        }

                        is NetworkResult.Error -> {
                            binding.progressbarAddress.visibility = View.GONE
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeOrder(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                orderViewModel.orders.collectLatest {
                    when(it){
                        is NetworkResult.Loading -> {
                            binding.buttonPlaceOrder.startAnimation()
                        }

                        is NetworkResult.Success -> {
                            binding.buttonPlaceOrder.revertAnimation()
                           val action = BillingFragmentDirections.actionBillingFragmentToOrderConfirmationFragment()
                            findNavController().navigate(action)
                        }

                        is NetworkResult.Error -> {
                            binding.buttonPlaceOrder.revertAnimation()
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}