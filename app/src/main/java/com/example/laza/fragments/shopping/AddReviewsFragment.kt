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
import com.example.laza.R
import com.example.laza.data.Reviews
import com.example.laza.databinding.FragmentReviewsBinding
import com.example.laza.helper.setGArrowImageBasedOnLayoutDirection
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.AddReviewsViewModel
import com.example.laza.viewmodels.HomeFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddReviewsFragment : Fragment() {
    private var _binding: FragmentReviewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AddReviewsViewModel>()
    private val homeViewModel by viewModels<HomeFragmentViewModel>()
    private val args by navArgs<AddReviewsFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewsBinding.inflate(inflater, container, false)
        setGArrowImageBasedOnLayoutDirection(resources,binding.arrow1)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeAddReviews()

        val reviewsArgs = args.reviews
        val productId = reviewsArgs.documentId

        binding.edName.setText(reviewsArgs.name)
        binding.edReviewDescription.setText(reviewsArgs.review)
        binding.ratingBar.rating = reviewsArgs.ratingStars.toFloat()

        binding.apply {
            btnSubmitReview.setOnClickListener {
                val name = edName.text.toString()
                val reviewText = edReviewDescription.text.toString()
                if (ratingBar.rating == 0f) {
                    Toast.makeText(
                        requireContext(),
                        "Please select a rating.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val rating = ratingBar.rating
                // add default image from drawable
                val image = R.drawable.profile
                val date = System.currentTimeMillis().toString()

                if (name.isNotEmpty() && reviewText.isNotEmpty()) {
                    val review = Reviews(
                        name = name,
                        review = reviewText,
                        ratingStars = rating.toDouble(),
                        image = image.toString(),
                        date = date
                    )
                    viewModel.addReview(productId, review)

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please fill out all fields.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.apply {
            arrow1.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun observeAddReviews() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addNewReview.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            val productId = args.reviews.documentId
                            homeViewModel.setTotalReviewsCount(productId)
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Review submitted successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            binding.progressBar.visibility = View.GONE
                        }

                        else -> {}
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}