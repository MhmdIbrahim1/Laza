package com.example.laza.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laza.R
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.example.laza.adapters.CartProductAdapter
import com.example.laza.databinding.FragmentCartBinding
import com.example.laza.firebase.FirebaseCommon
import com.example.laza.helper.formatPrice
import com.example.laza.helper.setGArrowImageBasedOnLayoutDirection
import com.example.laza.utils.CartUtil
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.utils.ShowBottomNavigation
import com.example.laza.viewmodels.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : Fragment(), CartUtil {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartProductAdapter: CartProductAdapter
    private val viewModel by activityViewModels<CartViewModel>() // activityViewModels because we want to share the same view-model with the activity
    private var totalPrice = 0f


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        setGArrowImageBasedOnLayoutDirection(resources, binding.arrow1)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCartRV()
        observeCartProduct()
        observeProductPrice()
        observeDialog()
        cartProductAdapter.onProductClick = {
            val b = Bundle().apply { putParcelable("product", it.product) }
            findNavController().navigate(R.id.action_cartFragment_to_productDetailsFragment, b)
        }

        cartProductAdapter.onPlusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.INCREASE)
        }

        cartProductAdapter.onMinusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.DECREASE)
        }

        binding.arrow1.setOnClickListener {
            findNavController().navigate(R.id.action_cartFragment_to_homeFragment)
        }

        binding.buttonCheckout.setOnClickListener {
            val action = CartFragmentDirections.actionCartFragmentToBillingFragment(
                totalPrice,
                cartProductAdapter.differ.currentList.toTypedArray(),
                true
            )
            findNavController().navigate(action)
        }

        // if the previous destination was the HomeFragment, and the user clicked on the back button then navigate to the HomeFragment
        requireActivity().onBackPressedDispatcher.addCallback {
            findNavController().navigate(R.id.action_cartFragment_to_homeFragment)
        }
    }

    private fun setUpCartRV() {
        cartProductAdapter = CartProductAdapter(requireContext(), this)
        binding.rvCart.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = cartProductAdapter

            addItemDecoration(ItemSpacingDecoration(16))
        }
    }

    private fun observeCartProduct() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartProduct.collectLatest {
                    when (it) {

                        is NetworkResult.Loading -> {
                            binding.progressbarCart.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            binding.progressbarCart.visibility = View.INVISIBLE
                            if (it.data!!.isEmpty()) {
                                showEmptyCart()
                                hideOtherViews()
                            } else {
                                hideEmptyCart()
                                showOtherViews()
                                cartProductAdapter.differ.submitList(it.data)
                            }
                        }

                        is NetworkResult.Error -> {
                            binding.progressbarCart.visibility = View.INVISIBLE
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }


    private fun observeProductPrice() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsPrice.collectLatest { price ->
                    price?.let {
                        totalPrice = it
                        binding.tvTotalPrice.text =
                            resources.getString(R.string.egp, totalPrice.formatPrice())
                    }

                }
            }
        }
    }

    private fun observeDialog() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteDialog.collectLatest { cartProduct ->
                    val dialogView =
                        layoutInflater.inflate(R.layout.delete_confirmation_dialog, null)

                    val alertDialogBuilder =
                        AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                            .setView(dialogView)
                            .create()

                    val cancelButton =
                        dialogView.findViewById<AppCompatButton>(R.id.confirm_cancel_button)
                    val deleteButton =
                        dialogView.findViewById<AppCompatButton>(R.id.confirm_delete__button)

                    cancelButton.setOnClickListener {
                        alertDialogBuilder.dismiss()
                    }

                    deleteButton.setOnClickListener {
                        viewModel.deleteCartProduct(cartProduct)
                        alertDialogBuilder.dismiss()
                    }

                    alertDialogBuilder.show()
                }
            }
        }
    }

    private fun showOtherViews() {
        binding.apply {
            rvCart.visibility = View.VISIBLE
            totalBoxContainer.visibility = View.VISIBLE
            buttonCheckout.visibility = View.VISIBLE
        }
    }

    private fun hideOtherViews() {
        binding.apply {
            rvCart.visibility = View.GONE
            totalBoxContainer.visibility = View.GONE
            buttonCheckout.visibility = View.GONE
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAdded) {
            ShowBottomNavigation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun deleteItem(position: Int) {

        val dialogView =
            layoutInflater.inflate(R.layout.delete_confirmation_dialog, null)

        val alertDialogBuilder =
            AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                .setView(dialogView)
                .create()

        val cancelButton =
            dialogView.findViewById<AppCompatButton>(R.id.confirm_cancel_button)
        val deleteButton =
            dialogView.findViewById<AppCompatButton>(R.id.confirm_delete__button)

        cancelButton.setOnClickListener {
            alertDialogBuilder.dismiss()
        }

        deleteButton.setOnClickListener {
            val item = cartProductAdapter.differ.currentList[position]
            // First, delete the product from Firestore
            viewModel.deleteProduct(item)

            // Then, remove it from the local list
            val newList = cartProductAdapter.differ.currentList.toMutableList()
            newList.removeAt(position)
            cartProductAdapter.differ.submitList(newList)
            alertDialogBuilder.dismiss()
        }

        alertDialogBuilder.show()
    }

}

