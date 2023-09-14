package com.example.laza.fragments.shopping

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.laza.R
import com.example.laza.adapters.ReviewsAdapter
import com.example.laza.data.Product
import com.example.laza.data.Reviews
import com.example.laza.databinding.FragmentReviewBinding
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.ReviewsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ReviewFragment : Fragment() {
    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var reviewsAdapter: ReviewsAdapter
    private val viewModel by viewModels<ReviewsViewModel>()

    // Fragment arguments
    private val args: ReviewFragmentArgs by navArgs()

    // Product instance
    private lateinit var product: Product

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        product = args.product

        setUpReviewsRv()
        observeReviews()
        observeTotalReviewsCount()
        observeTotalRating()

        binding.addReview.setOnClickListener {
            val action = ReviewFragmentDirections.actionReviewFragmentToAddReviewsFragment(
                reviews = Reviews(
                    name = "",
                    review = "",
                    ratingStars = 0.0,
                    image = "",
                    documentId = product.id
                )
            )
            findNavController().navigate(action)

        }

        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeReviews() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchReviews.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            binding.progressBar.visibility = View.GONE
                            reviewsAdapter.differ.submitList(it.data)
                            Log.d("Reviews", it.data.toString())
                        }

                        is NetworkResult.Error -> {
                            binding.progressBar.visibility = View.GONE
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeTotalRating() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalRating.collect {
                    viewModel.fetchTotalRating(product.id)
                    updateTotalRating(it)
                }
            }
        }
    }

    private fun observeTotalReviewsCount() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalReviewsCount.collect{
                    viewModel.fetchTotalReviewsCount(product.id)
                    updateTotalReviewsCount(it)
                }
            }
        }
    }

        private fun updateTotalRating(it: Float) {
        if (it == 0f){
            binding.tvRating.text = "0.0"
            binding.ratingBar.rating = 0.0F
        }else{
            binding.tvRating.text = String.format("%.1f", it)
            binding.ratingBar.rating = it
        }
    }


    private fun updateTotalReviewsCount(itemCount: Int) {
        if (itemCount == 0){
            binding.reviewsItemCount.text = resources.getString(R.string.no_reviews)
            binding.tvRating.text = "0.0"
            binding.ratingBar.rating = 0.0F
        }else{
            binding.reviewsItemCount.text = itemCount.toString().plus(" ").plus(resources.getString(R.string.reviews))
        }
    }

    private fun setUpReviewsRv() {
        reviewsAdapter = ReviewsAdapter(requireContext())
        binding.reviesRv.apply {
            adapter = reviewsAdapter
            layoutManager =
                GridLayoutManager(requireContext(), 1, GridLayoutManager.VERTICAL, false)
            val itemSpacingDecoration = ItemSpacingDecoration(10)
            addItemDecoration(itemSpacingDecoration)

            viewModel.fetchReviews(product.id)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}