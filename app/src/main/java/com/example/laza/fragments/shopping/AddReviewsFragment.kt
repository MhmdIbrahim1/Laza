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
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.AddReviewsViewModel
import com.example.laza.viewmodels.HomeFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddReviewsFragment : Fragment() {
    private lateinit var binding: FragmentReviewsBinding
    private  val viewModel by viewModels<AddReviewsViewModel>()
    private val homeViewModel by viewModels<HomeFragmentViewModel>()
    private val args by navArgs<AddReviewsFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReviewsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeAddReviews()

        val reviewsArgs = args.reviews
        val productId = reviewsArgs.documentId


        binding.apply {
            btnSubmitReview.setOnClickListener {
                val name = edName.text.toString()
                val reviewText = edReviewDescription.text.toString()
                val rating = ratingBar.rating
                // add default image from drawable
                val image = R.drawable.profile

                if (name.isNotEmpty() && reviewText.isNotEmpty()) {
                    val review = Reviews(name = name, review = reviewText, ratingStars = rating.toDouble(), image = image.toString())
                    viewModel.addReview(productId, review)

                } else {
                    Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.apply {
            arrow1.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun observeAddReviews(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.addNewReview.collectLatest {
                    when(it){
                        is NetworkResult.Loading -> {}

                        is NetworkResult.Success -> {
                            val productId = args.reviews.documentId
                            homeViewModel.setTotalReviewsCount(productId)
                            Toast.makeText(requireContext(), "Review submitted successfully.", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else ->{}
                    }
                }
            }
        }
    }


}