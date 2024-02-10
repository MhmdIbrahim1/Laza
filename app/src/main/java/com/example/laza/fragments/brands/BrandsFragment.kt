package com.example.laza.fragments.brands

import com.example.laza.viewmodels.BrandsFragmentViewModel
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import androidx.navigation.Navigation.findNavController
import com.example.laza.adapters.BrandsAdapter
import com.example.laza.databinding.FragmentBrandsBinding
import com.example.laza.helper.getProductPrice
import com.example.laza.helper.setGArrowImageBasedOnLayoutDirection
import com.example.laza.utils.CommonActivity
import com.example.laza.utils.CommonActivity.showToast
import com.example.laza.utils.Coroutines.main
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BrandsFragment : Fragment() {
    private lateinit var binding: FragmentBrandsBinding
    private lateinit var brandsAdapter: BrandsAdapter
    private val viewModel by viewModels<BrandsFragmentViewModel>()
    private var brandName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBrandsBinding.inflate(inflater, container, false)
        this.binding = binding // Assign the inflated binding instance to the property

        setGArrowImageBasedOnLayoutDirection(resources,binding.arrow1)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageResId = arguments?.getInt("brandImage") ?: 0
        brandName = arguments?.getString("brandName") ?: ""
        Log.d("BrandsFragment", "Brand Name: $brandName")

        binding.imageBrand.setImageResource(imageResId)

        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }

        // Fetch the total item count before fetching the actual items
        viewModel.fetchTotalItemCount(brandName)

        observeBrandData()
        setUpRecyclerView()
        observeTotalItemCount()

        brandsAdapter.onItemClickListener = {
            if (isAdded) {
                // Calculate the price after applying the offer percentage (if available)
                val priceAfterOffer = it.offerPercentage?.getProductPrice(it.price) ?: it.price

                // Create a new product object with the updated price
                val updatedProduct = it.copy(price = priceAfterOffer)

                // Navigate to the product details fragment and pass the updated product object
                if (findNavController(binding.root).currentDestination?.id == R.id.brandsFragment) {
                    val action = BrandsFragmentDirections.actionBrandsFragmentToProductDetailsFragment(updatedProduct)
                    findNavController().navigate(action)
                }
            }
        }

        binding.cartFromBrands.setOnClickListener {
            findNavController().navigate(R.id.action_brandsFragment_to_cartFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            findNavController().navigateUp()
        }
    }


    private fun observeBrandData() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.brandsData.collectLatest {
                    when (it) {
                        is NetworkResult.Success -> {
                            brandsAdapter.differ.submitList(it.data)
                            hideLoading()
                        }

                        is NetworkResult.Error -> {
                            Log.d("BrandsFragment", "Error: ${it.message}")
                            hideLoading()
                        }

                        is NetworkResult.Loading -> {
                            Log.d("BrandsFragment", "Loading...")
                            showLoading()
                        }

                        else -> {}
                    }
                }
            }
        }
        viewModel.fetchBrandsData(brandName)
    }

    private fun setUpRecyclerView() {
        brandsAdapter = BrandsAdapter(requireContext())
        binding.brandsRv.apply {
            val layoutManager = GridLayoutManager(
                requireContext(),
                2,
                GridLayoutManager.VERTICAL,
                false
            )
            binding.brandsRv.layoutManager = layoutManager
            binding.brandsRv.addItemDecoration(ItemSpacingDecoration(20))
            adapter = brandsAdapter
            // Add the scroll listener here
            addOnScrollListener(createOnScrollListener(layoutManager))
        }
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.brandsRv.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.brandsRv.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun createOnScrollListener(layoutManager: GridLayoutManager): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Check if the user has scrolled to the end of pagination and fetch more data
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && totalItemCount >= 20
                ) {
                    viewModel.fetchBrandsData(brandName)
                }
            }
        }
    }

    private fun observeTotalItemCount() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalItemCount.collect { itemCount ->
                    updateItemCount(itemCount)
                }
            }
        }
    }

    private fun updateItemCount(itemCount: Int) {
        if (itemCount == 0) {
            binding.reviewsItemCount.text = resources.getString(R.string.no_items_found)
            binding.availableOnStock.visibility = View.GONE
            binding.reviewsItemCount.visibility = View.VISIBLE
        } else {
            binding.reviewsItemCount.text = itemCount.toString().plus(" items")
            binding.availableOnStock.visibility = View.VISIBLE
            binding.reviewsItemCount.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        HideBottomNavigation()
    }
}
