package com.example.laza.fragments.brands

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.laza.R
import com.example.laza.adapters.BrandsAdapter
import com.example.laza.databinding.FragmentBrandsBinding
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.BrandsFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

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
        observeBrandData()
        setUpRecyclerView()
    }

    private fun observeBrandData(){
        lifecycleScope.launchWhenStarted {
            viewModel.brandsData.collectLatest {
                when(it){
                    is NetworkResult.Success -> {
                        brandsAdapter.differ.submitList(it.data)
                        it.data?.let { it1 -> updateItemCount(it1.size) }
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
                    else ->{}
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

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.brandsRv.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.brandsRv.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }


}