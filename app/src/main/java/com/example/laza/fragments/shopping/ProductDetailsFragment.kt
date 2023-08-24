package com.example.laza.fragments.shopping

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laza.R
import com.example.laza.adapters.ColorAdapter
import com.example.laza.adapters.SizeAdapter
import com.example.laza.adapters.ViewPager2Images
import com.example.laza.databinding.FragmentProductDetailsBinding
import com.example.laza.helper.formatPrice
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.ItemSpacingDecoration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator


class ProductDetailsFragment : Fragment() {
    private val args by navArgs<ProductDetailsFragmentArgs>()
    private lateinit var binding: FragmentProductDetailsBinding
    private val viePagerAdapter by lazy { ViewPager2Images() }
    private val sizeAdapter by lazy { SizeAdapter() }
    private val colorAdapter by lazy { ColorAdapter() }

    // Selected color and size variables to store user choices
    private var selectedColor: Double? = null
    private var selectedSize: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        setUpColorRv()
        setUpSizeRv()
        setUpViewPager()

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