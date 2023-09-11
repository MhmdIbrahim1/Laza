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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laza.R
import com.example.laza.adapters.ColorAdapter
import com.example.laza.adapters.ReviewsProductsDetailsAdapter
import com.example.laza.adapters.SizeAdapter
import com.example.laza.adapters.ViewPager2Images
import com.example.laza.data.CartProduct
import com.example.laza.data.Product
import com.example.laza.data.Reviews
import com.example.laza.data.WishlistProduct
import com.example.laza.databinding.FragmentProductDetailsBinding
import com.example.laza.helper.formatPrice
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.DetailsViewModel
import com.example.laza.viewmodels.ReviewsViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.vejei.viewpagerindicator.indicator.CircleIndicator
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {
    // Fragment arguments
    private val args by navArgs<ProductDetailsFragmentArgs>()

    // View binding
    private lateinit var binding: FragmentProductDetailsBinding

    // Adapters
    private val viePagerAdapter by lazy { ViewPager2Images() }
    private val sizeAdapter by lazy { SizeAdapter() }
    private val colorAdapter by lazy { ColorAdapter() }

    // private val reviewsAdapter by lazy { ReviewsProductsDetailsAdapter() }
    private lateinit var reviewsAdapter: ReviewsProductsDetailsAdapter

    // ViewModel
    private val viewModel by viewModels<DetailsViewModel>()
    private val reviewsViewModel by viewModels<ReviewsViewModel>()

    // Selected color and size variables to store user choices
    private var selectedColor: Long? = null
    private var selectedSize: String? = null

    // Product instance
    private lateinit var product: Product

    // Fragment lifecycle methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get product from arguments
        product = args.product

        // Set up RecyclerViews and ViewPager
        setUpColorRv()
        setUpSizeRv()
        setUpViewPager()
        setUpReviewsRv()

        // Set up button click listeners and observers
        onAddToCart()
        observeAddToCart()
        observeAddToWishlist()
        observeRemoveFromWishlist()
        observeFetchReviews()
        observeTotalReviewsCount()
        observeTotalRating()


        // Fetch the initial wishlist status for the product
        viewModel.fetchInitialWishlistStatus(product.id)
        observeWishlistStatus()

        // Set up item click listeners for sizes and colors
        sizeAdapter.onItemClick = { size ->
            selectedSize = size
        }

        colorAdapter.onItemClick = { color ->
            selectedColor = color
        }

        // Populate the UI with product details
        binding.apply {
            productName.text = product.name
            productPrice.text = resources.getString(R.string.egp, product.price.formatPrice())
            tvProductDescription.text = product.description

            // Hide the labels for color and size if they are not available
            if (product.colors.isNullOrEmpty()) {
                tvProductColor.visibility = View.INVISIBLE
            }
            if (product.sizes.isNullOrEmpty()) {
                tvProductSize.visibility = View.INVISIBLE
            }

            if (product.offerPercentage != null) {
                offerCard.visibility = View.VISIBLE
                offerText.text = product.offerPercentage.toString().plus("% OFF")
                if (product.offerPercentage!! > 65) {
                    dealText.text = resources.getString(R.string.limited_deal)
                } else if (product.offerPercentage!! < 25) {
                    dealText.visibility = View.GONE
                } else {
                    dealText.visibility = View.VISIBLE
                }
            } else {
                offerCard.visibility = View.GONE
                dealText.visibility = View.GONE
            }
        }

        // Update the RecyclerViews and ViewPager2 with data from the product object
        viePagerAdapter.differ.submitList(product.images)
        product.colors?.let { colorAdapter.differ.submitList(it) }
        product.sizes?.let { sizeAdapter.differ.submitList(it) }

        // Navigate back when arrow button is clicked
        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }

        // Set up the "Add to Wishlist" button listener
        binding.addToWishlist.setOnClickListener {
            if (viewModel.wishlistStatus.value == false) {
                // Product is not in the wishlist, add it
                viewModel.addToWishList(WishlistProduct(product, true),true)
            } else {
                // Product is in the wishlist, remove it
                viewModel.removeFromWishList(WishlistProduct(product, false),false)
            }
        }


        // if no reviews, navigate to add review fragment else navigate to reviews fragment
        binding.viewAll.setOnClickListener {
            if (reviewsAdapter.differ.currentList.isEmpty()) {
                val action =
                    ProductDetailsFragmentDirections.actionProductDetailsFragmentToAddReviewsFragment(
                        reviews = Reviews(
                            name = "",
                            review = "",
                            ratingStars = 0.0,
                            image = "",
                            documentId = product.id
                        )
                    )
                findNavController().navigate(action)
            } else {
                val action =
                    ProductDetailsFragmentDirections.actionProductDetailsFragmentToReviewFragment(
                        product = Product(
                            id = product.id,
                            name = "",
                            brand = "",
                            price = 0f,
                            offerPercentage = null,
                            description = null,
                            colors = null,
                            sizes = null,
                            images = emptyList()
                        )
                    )
                findNavController().navigate(action)
            }
        }
    }


    // Observe the wishlist status
    private fun observeWishlistStatus() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wishlistStatus.collect { status ->
                    if (status == true) {
                        binding.addToWishlist.text = resources.getString(R.string.in_wishlist)
                        binding.addToWishlist.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_wishlist_filled,
                            0,
                            0,
                            0
                        )
                    } else {
                        binding.addToWishlist.text = resources.getString(R.string.add_to_wishlist)
                        binding.addToWishlist.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_wishlist_unfilled,
                            0,
                            0,
                            0
                        )
                    }
                }
            }
        }
    }

    // Observe the "Add to Wishlist"
    private fun observeAddToWishlist() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addToWishList.collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            Toast.makeText(
                                requireContext(),
                                ("Added To WishList"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeRemoveFromWishlist() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.removeFromWishList.collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            Toast.makeText(
                                requireContext(),
                                ("Removed From WishList"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    // Observe the "Add to Cart"
    private fun observeAddToCart() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addToCart.collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            binding.btnAddToCart.startAnimation()
                        }

                        is NetworkResult.Success -> {
                            binding.btnAddToCart.revertAnimation()
                            Toast.makeText(
                                requireContext(),
                                (R.string.AddedToCart),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        is NetworkResult.Error -> {
                            binding.btnAddToCart.revertAnimation()
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeFetchReviews() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchReviews.collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {}

                        is NetworkResult.Success -> {
                            if (result.data.isNullOrEmpty()) {
                                binding.tvProductReviewsTitle.visibility = View.INVISIBLE
                                binding.viewAll.text = getString(R.string.add_review)
                            } else {
                                reviewsAdapter.differ.submitList(result.data)
                            }
                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }


    // Handle "Add to Cart" button click
    private fun onAddToCart() {
        binding.btnAddToCart.setOnClickListener {
            // Check if there are available colors and sizes for the product
            val availableColors = product.colors
            val availableSizes = product.sizes

            if (availableColors.isNullOrEmpty() && availableSizes.isNullOrEmpty()) {
                Toast.makeText(requireContext(), (R.string.OutOfStock), Toast.LENGTH_SHORT).show()
            } else if (availableColors.isNullOrEmpty()) {
                if (selectedSize == null) {
                    Toast.makeText(
                        requireContext(),
                        (R.string.PleaseSelectSize),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                viewModel.addUpdateProductInCart(CartProduct(product, 1, null, selectedSize))
            } else if (availableSizes.isNullOrEmpty()) {
                if (selectedColor == null) {
                    Toast.makeText(
                        requireContext(),
                        (R.string.PleaseSelectColor),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                viewModel.addUpdateProductInCart(CartProduct(product, 1, selectedColor, null))
            } else {
                if (selectedColor == null || selectedSize == null) {
                    Toast.makeText(
                        requireContext(),
                        (R.string.PleaseSelectColorAndSize),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                viewModel.addUpdateProductInCart(
                    CartProduct(
                        product,
                        1,
                        selectedColor,
                        selectedSize
                    )
                )
            }
        }
    }

    private fun observeTotalRating() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                reviewsViewModel.totalRating.collect {
                    reviewsViewModel.fetchTotalRating(product.id)
                    updateTotalRating(it)
                }
            }
        }
    }

    private fun observeTotalReviewsCount() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                reviewsViewModel.totalReviewsCount.collect {
                    reviewsViewModel.fetchTotalReviewsCount(product.id)
                    updateTotalReviewsCount(it)
                }
            }
        }
    }

    private fun updateTotalRating(it: Float) {
        if (it == 0f) {
            binding.tvRating.text = "0.0"
            binding.ratingBar.rating = 0.0F
        } else {
            // round to 1 decimal place
            binding.tvRating.text = String.format("%.1f", it)
            binding.ratingBar.rating = it
        }
    }


    private fun updateTotalReviewsCount(itemCount: Int) {
        if (itemCount == 0) {
            binding.reviewsItemCount.text = resources.getString(R.string.no_reviews)
            binding.tvRating.text = "0.0"
            binding.ratingBar.rating = 0.0F
        } else {
            binding.reviewsItemCount.text = resources.getString(R.string.reviews_item_count, itemCount.toString())
        }
    }


    // Set up ViewPager
    private fun setUpViewPager() {
        binding.viewPagerProductImages.adapter = viePagerAdapter
        binding.viewPagerProductImages.postDelayed(object : Runnable {
            override fun run() {
                binding.viewPagerProductImages.currentItem =
                    (binding.viewPagerProductImages.currentItem + 1) % product.images.size
                binding.viewPagerProductImages.postDelayed(this, 4000)

            }
        }, 2000)
        binding.circleIndicator.setWithViewPager2(binding.viewPagerProductImages)
        binding.circleIndicator.itemCount = (args.product.images.size)
        binding.circleIndicator.setAnimationMode(CircleIndicator.AnimationMode.SLIDE)
        binding.circleIndicator.setAnimationMode(CircleIndicator.AnimationMode.SCALE)
    }

    // Set up Size RecyclerView
    private fun setUpSizeRv() {
        binding.rvSizes.apply {
            adapter = sizeAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            val itemSpacingDecoration = ItemSpacingDecoration(5)
            addItemDecoration(itemSpacingDecoration)
        }
    }

    // Set up Color RecyclerView
    private fun setUpColorRv() {
        binding.rvColors.apply {
            adapter = colorAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            val itemSpacingDecoration = ItemSpacingDecoration(5)
            addItemDecoration(itemSpacingDecoration)
        }
    }

    private fun setUpReviewsRv() {
        reviewsAdapter = ReviewsProductsDetailsAdapter()
        binding.rvReviews.apply {
            adapter = reviewsAdapter
            layoutManager =
                GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false)

            val itemSpacingDecoration = ItemSpacingDecoration(10)
            addItemDecoration(itemSpacingDecoration)

            viewModel.fetchReviews(product.id)
        }
    }

    // Hide bottom navigation on resume
    override fun onResume() {
        super.onResume()
        HideBottomNavigation()
    }
}
