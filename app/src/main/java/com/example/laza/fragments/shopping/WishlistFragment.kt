package com.example.laza.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.laza.R
import com.example.laza.adapters.WishListAdapter
import com.example.laza.databinding.FragmentWishlistBinding
import com.example.laza.helper.getProductPrice
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.WishListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WishlistFragment : Fragment() {
    private lateinit var binding: FragmentWishlistBinding
    private val viewModel by viewModels<WishListViewModel>()
    private lateinit var wishListAdapter: WishListAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        observeWishListData()
        observeTotalItemCount()

        binding.arrow1.setOnClickListener {
            findNavController().navigate(R.id.action_wishlistFragment_to_homeFragment)
        }
        binding.cartFromWishlist.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
        }

        wishListAdapter.onItemClickListener = {
            if (isAdded){
                // Calculate the price after applying the offer percentage (if available)
                val priceAfterOffer = it.offerPercentage?.getProductPrice(it.price) ?: it.price

                // Create a new product object with the updated price
                val updatedProduct = it.copy(price = priceAfterOffer)

                // Navigate to the product details fragment and pass the updated product object
                val action = WishlistFragmentDirections.actionWishlistFragmentToProductDetailsFragment(
                    updatedProduct
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun observeWishListData() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wishListData.collect {
                    when (it) {
                        is NetworkResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            binding.progressBar.visibility = View.GONE
                            wishListAdapter.differ.submitList(it.data)
                        }

                        is NetworkResult.Error -> {
                            binding.progressBar.visibility = View.GONE
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        wishListAdapter = WishListAdapter()
        binding.brandsRv.apply {
            val layoutManager = GridLayoutManager(
                requireContext(),
                2,
                GridLayoutManager.VERTICAL,
                false
            )
            binding.brandsRv.layoutManager = layoutManager
            binding.brandsRv.addItemDecoration(ItemSpacingDecoration(20))
            adapter = wishListAdapter
        }
    }

    private fun observeTotalItemCount() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalItemCount.collect {
                    updateItemCount(it)
                }
            }
        }
    }
    private fun updateItemCount(itemCount: Int) {
        if (itemCount == 0) {
            binding.itemCount.text = "No items found"
            binding.availableOnStock.visibility = View.GONE
            binding.itemCount.visibility = View.VISIBLE
        } else {
            binding.itemCount.text = "$itemCount items"
            binding.availableOnStock.visibility = View.VISIBLE
            binding.itemCount.visibility = View.VISIBLE
        }
    }
}