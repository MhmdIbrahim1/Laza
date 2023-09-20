package com.example.laza.fragments.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import com.example.laza.adapters.AllOrdersAdapter
import com.example.laza.databinding.FragmentAllOrdersBinding
import com.example.laza.helper.setGArrowImageBasedOnLayoutDirection
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.AllOrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllOrdersFragment : Fragment() {
    private var _binding: FragmentAllOrdersBinding? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<AllOrdersViewModel>()
    private val ordersAdapter by lazy { AllOrdersAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllOrdersBinding.inflate(inflater, container, false)
        setGArrowImageBasedOnLayoutDirection(resources,binding.arrow1)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOrdersRv()
        observeGetAllOrders()

        ordersAdapter.onClick = {
            val action = AllOrdersFragmentDirections.actionAllOrdersFragmentToOrderDetailsFragment(it)
            findNavController().navigate(action)
        }

        binding.arrow1.setOnClickListener {
           findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback {
              findNavController().navigate(R.id.action_allOrdersFragment_to_homeFragment)
        }

    }

    private fun setupOrdersRv() {
        binding.rvAllOrders.apply {
            adapter = ordersAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }

    private fun observeGetAllOrders() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allOrders.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            binding.progressbarAllOrders.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            binding.progressbarAllOrders.visibility = View.GONE
                            ordersAdapter.differ.submitList(it.data)
                            if (it.data.isNullOrEmpty()) {
                                binding.tvEmptyOrders.visibility = View.VISIBLE
                            }
                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            binding.progressbarAllOrders.visibility = View.GONE
                        }

                        else -> Unit
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        HideBottomNavigation()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}