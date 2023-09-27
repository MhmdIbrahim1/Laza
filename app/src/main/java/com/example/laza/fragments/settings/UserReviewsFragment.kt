package com.example.laza.fragments.settings

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laza.R
import com.example.laza.adapters.UserReviewsAdapter
import com.example.laza.data.Address
import com.example.laza.databinding.FragmentUserReviewsBinding
import com.example.laza.fragments.shopping.AddressesFragmentArgs
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.utils.UserReviewsUtil
import com.example.laza.viewmodels.UserReviewsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserReviewsFragment : Fragment(), UserReviewsUtil {
    private var _binding: FragmentUserReviewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var userReviewsAdapter: UserReviewsAdapter
    private val viewModel by viewModels<UserReviewsViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        observeUserReviews()


        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeUserReviews() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchUserReviews.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            //binding.progressBar.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            //binding.progressBar.visibility = View.GONE
                            userReviewsAdapter.differ.submitList(it.data)
                        }

                        is NetworkResult.Error -> {
                            //binding.progressBar.visibility = View.GONE
                            Log.d("UserReviewsFragment", it.message.toString())
                        }

                        else -> {
                            //binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        userReviewsAdapter = UserReviewsAdapter(requireContext(), this)
        binding.rvUserReviews.apply {
            adapter = userReviewsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(ItemSpacingDecoration(10))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun deleteReview(position: Int) {
        TODO("Not yet implemented")
    }



}