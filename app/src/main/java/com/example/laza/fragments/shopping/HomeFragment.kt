package com.example.laza.fragments.shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laza.R
import com.example.laza.adapters.BrandRvItemsAdapter
import com.example.laza.adapters.NewArrivalAdapter
import com.example.laza.data.Product
import com.example.laza.databinding.FragmentHomeBinding
import com.example.laza.helper.getProductPrice
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.utils.ShowBottomNavigation
import com.example.laza.viewmodels.DetailsViewModel
import com.example.laza.viewmodels.HomeFragmentViewModel
import com.example.laza.viewmodels.ReviewsViewModel
import com.example.storein.utils.HorizontalItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val args by navArgs<ProductDetailsFragmentArgs>()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var drawerOpener: DrawerOpener
    private lateinit var brandItem: List<BrandRvItemsAdapter.BrandItem>
    private lateinit var newArrivalAdapter: NewArrivalAdapter
    private lateinit var brandAdapter: BrandRvItemsAdapter
    private lateinit var nestedScrollView: NestedScrollView
    private val viewModel by viewModels<HomeFragmentViewModel>()
    // Interface to communicate with the activity
    interface DrawerOpener {
        fun openDrawer()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDrawer()
        brandItem = listOf(
            BrandRvItemsAdapter.BrandItem(R.drawable.adidas, "Adidas"),
            BrandRvItemsAdapter.BrandItem(R.drawable.vector, "Nike"),
            BrandRvItemsAdapter.BrandItem(R.drawable.puma, "Puma"),
            BrandRvItemsAdapter.BrandItem(R.drawable.fila, "Fila")
            // Add more BrandItems for other brands as needed
        )
        setUpBrandRV()
        setUpNewArrivalRV()
        observeNewArrival()
        observeAddWishlist()
        observeRemoveWishlist()

        nestedScrollView = binding.nestedScrollMainCategory
        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            val reachEnd =
                scrollY >= (nestedScrollView.getChildAt(0).measuredHeight - nestedScrollView.measuredHeight)
            if (reachEnd) {
                viewModel.fetchNewArrival()
            }
        })

        newArrivalAdapter.onItemClickListener = {
            if (isAdded) {
                // Calculate the price after applying the offer percentage (if available)
                val priceAfterOffer = it.offerPercentage?.getProductPrice(it.price) ?: it.price

                // Create a new product object with the updated price
                val updatedProduct = it.copy(price = priceAfterOffer)

                // Navigate to the product details fragment and pass the updated product object
                val action =
                    HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(updatedProduct)
                view.findNavController().navigate(action)
            }
        }
    }


    private fun observeNewArrival() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.newArrival.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            showLoading()
                        }

                        is NetworkResult.Success -> {
                            Log.d("HomeFragment", "observeNewArrival: ${it.data}")
                            newArrivalAdapter.differ.submitList(it.data)
                            hideLoading()
                        }

                        is NetworkResult.Error -> {
                            Log.d("HomeFragment", "observeNewArrival: ${it.message}")
                            hideLoading()
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeAddWishlist() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wishlist.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                        }

                        is NetworkResult.Success -> {
                            Toast.makeText(
                                requireContext(),
                                (R.string.AddedToWishlist),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is NetworkResult.Error -> {
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeRemoveWishlist() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wishlist.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                        }

                        is NetworkResult.Success -> {
                            Toast.makeText(
                                requireContext(),
                                (R.string.RemovedFromWishlist),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is NetworkResult.Error -> {
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.newArrivalsTv.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.newArrivalsTv.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun setDrawer() {
        // Check if the host activity implements the DrawerOpener interface
        if (activity is DrawerOpener) {
            drawerOpener = activity as DrawerOpener
        } else {
            throw IllegalArgumentException("Host activity must implement DrawerOpener interface")
        }

        binding.circleImageMenu.setOnClickListener {
            // Open the drawer by calling the interface method
            drawerOpener.openDrawer()
        }
    }

    private fun setUpBrandRV() {
        brandAdapter = BrandRvItemsAdapter(brandItem)
        brandAdapter.onBrandClickListener =
            BrandRvItemsAdapter.OnBrandClickListener { _, brandItem ->
                val action = HomeFragmentDirections.actionHomeFragmentToBrandsFragment(
                    brandItem.imageResId,
                    brandItem.brandName
                )
                view?.findNavController()?.navigate(action)
            }

        binding.brandRv.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.brandRv.addItemDecoration(HorizontalItemDecoration())
            adapter = brandAdapter
        }
    }


    private fun setUpNewArrivalRV() {
        newArrivalAdapter = NewArrivalAdapter()
        binding.newArrivalsRv.apply {
            val layoutManager = GridLayoutManager(
                requireContext(),
                2,
                GridLayoutManager.VERTICAL,
                false
            )
            binding.newArrivalsRv.layoutManager = layoutManager
            binding.newArrivalsRv.addItemDecoration(ItemSpacingDecoration(20))
            adapter = newArrivalAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        ShowBottomNavigation()
    }
}