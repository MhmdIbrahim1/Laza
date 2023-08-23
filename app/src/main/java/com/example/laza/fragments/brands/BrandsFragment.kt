package com.example.laza.fragments.brands

import com.example.laza.viewmodels.BrandsFragmentViewModel
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.adapters.BrandsAdapter
import com.example.laza.databinding.FragmentBrandsBinding
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageResId = arguments?.getInt("brandImage") ?: 0
        Log.d("BrandsFragment", "Image Resource ID: $imageResId")
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
        brandsAdapter = BrandsAdapter()
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
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val isAtBottomOfList = lastVisibleItemPosition == brandsAdapter.itemCount-1
                    if (isAtBottomOfList) {
                        viewModel.fetchBrandsData(brandName)
                    }
                }
            }
        }
    }

    private fun observeTotalItemCount() {
        lifecycleScope.launchWhenStarted {
            viewModel.totalItemCount.collect { itemCount ->
                updateItemCount(itemCount)
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