package com.example.laza.fragments.shopping

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laza.R
import com.example.laza.adapters.ColorAdapter
import com.example.laza.adapters.SizeAdapter
import com.example.laza.adapters.ViewPager2Images
import com.example.laza.data.CartProduct
import com.example.laza.data.Product
import com.example.laza.databinding.FragmentProductDetailsBinding
import com.example.laza.helper.formatPrice
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.DetailsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {
    private val args by navArgs<ProductDetailsFragmentArgs>()
    private lateinit var binding: FragmentProductDetailsBinding
    private val viePagerAdapter by lazy { ViewPager2Images() }
    private val sizeAdapter by lazy { SizeAdapter() }
    private val colorAdapter by lazy { ColorAdapter() }
    private val viewModel by viewModels<DetailsViewModel>()

    // Selected color and size variables to store user choices
    private var selectedColor: Double? = null
    private var selectedSize: String? = null
    private lateinit var product: Product
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         product = args.product

        setUpColorRv()
        setUpSizeRv()
        setUpViewPager()
        // Handle "Add to Cart" button click
        onAddToCart()
        observeAddToCart()

        // Set up item click listeners for sizes and colors
        sizeAdapter.onItemClick = { size ->
            // Update the selected size when the user selects one from the Sizes RecyclerView
            selectedSize = size
        }

        colorAdapter.onItemClick = { color ->
            // Update the selected color when the user selects one from the Colors RecyclerView
            selectedColor = color
        }


        // Populate the UI with product details
        binding.apply {
            productName.text = product.name
            productPrice.text = "E£ ${(product.price.formatPrice())}"
            tvProductDescription.text = product.description

            // Hide the labels for color and size if they are not available
            if (product.colors.isNullOrEmpty()) {
                tvProductColor.visibility = View.INVISIBLE
            }
            if (product.sizes.isNullOrEmpty()) {
                tvProductSize.visibility = View.INVISIBLE
            }
        }

        // Update the RecyclerViews and ViewPager2 with data from the product object
        viePagerAdapter.differ.submitList(product.images)
        product.colors?.let { colorAdapter.differ.submitList(it) }
        product.sizes?.let { sizeAdapter.differ.submitList(it) }

        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.cartFromBrands.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeAddToCart(){
        // Observe the "addToCart"  for changes and react accordingly
        lifecycleScope.launchWhenStarted {
            viewModel.addToCart.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        // Show loading animation when adding to cart
                        binding.btnAddToCart.startAnimation()
                    }
                    is NetworkResult.Success -> {
                        // Show success animation and a toast message when the product is added to cart successfully
                        binding.btnAddToCart.revertAnimation()
                        Toast.makeText(requireContext(), (R.string.AddedToCart), Toast.LENGTH_SHORT).show()
                    }
                    is NetworkResult.Error -> {
                        // Show error message in a toast if there's an issue adding the product to cart
                        binding.btnAddToCart.revertAnimation()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
    private fun onAddToCart(){
        binding.btnAddToCart.setOnClickListener {
            // Check if there are available colors and sizes for the product
            val availableColors = product.colors
            val availableSizes = product.sizes

            // Ask the user to select a color and size before adding to cart
            if (availableColors.isNullOrEmpty() && availableSizes.isNullOrEmpty()) {
                // If both colors and sizes are empty or null, the product is out of stock
                Toast.makeText(requireContext(), (R.string.OutOfStock), Toast.LENGTH_SHORT).show()
            } else if (availableColors.isNullOrEmpty()) {
                // If only colors are empty or null, prompt the user to select a size
                if (selectedSize == null) {
                    Toast.makeText(requireContext(), (R.string.PleaseSelectSize), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Add the product to the cart with the selected size
                viewModel.addUpdateProductInCart(CartProduct(product, 1, null, selectedSize))
            } else if (availableSizes.isNullOrEmpty()) {
                // If only sizes are empty or null, prompt the user to select a color
                if (selectedColor == null) {
                    Toast.makeText(requireContext(), (R.string.PleaseSelectColor), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Add the product to the cart with the selected color
                viewModel.addUpdateProductInCart(CartProduct(product, 1, selectedColor, null))
            } else {
                // Both colors and sizes are available, ask the user to select one
                if (selectedColor == null || selectedSize == null) {
                    Toast.makeText(requireContext(), (R.string.PleaseSelectColorAndSize), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Add the product to the cart with the selected color and size
                viewModel.addUpdateProductInCart(CartProduct(product, 1, selectedColor, selectedSize))
            }
        }
    }
    private fun setUpViewPager() {
        binding.apply {
            viewPagerProductImages.adapter = viePagerAdapter
            TabLayoutMediator(tabLayoutImageIndicator, viewPagerProductImages) { _, _ -> }.attach()
        }
    }

    private fun setUpSizeRv() {
        binding.rvSizes.apply {
            adapter = sizeAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            // add item decoration to add space between items
            val itemSpacingDecoration = ItemSpacingDecoration(5)
            addItemDecoration(itemSpacingDecoration)
        }
    }
    private fun setUpColorRv() {
        binding.rvColors.apply {
            adapter = colorAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            // add item decoration to add space between items
            val itemSpacingDecoration = ItemSpacingDecoration(5)
            addItemDecoration(itemSpacingDecoration)
        }
    }

    override fun onResume() {
        super.onResume()
        HideBottomNavigation()

    }
}