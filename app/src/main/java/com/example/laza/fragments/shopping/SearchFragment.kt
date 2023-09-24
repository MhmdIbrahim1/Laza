package com.example.laza.fragments.shopping

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.laza.R
import com.example.laza.adapters.SearchRecyclerAdapter
import com.example.laza.databinding.FragmentSearchBinding
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.SearchViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding

    private val viewModel by viewModels<SearchViewModel>()
    private lateinit var searchAdapter: SearchRecyclerAdapter
    private var isTypingOrSearching = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupSearchRecyclerView()
        showKeyboardAutomatically()
        onHomeClick()
        onCancelTvClick()
        searchProducts()
        observeSearch()
        onSearchProductClick()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (isTypingOrSearching) {
                // User is typing or searching, hide the bottom navigation view
                isTypingOrSearching = false
                val bottomNav =
                    activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNav?.visibility = View.VISIBLE
            }
            findNavController().navigateUp()
        }
    }


    private fun setupSearchRecyclerView() {
        searchAdapter = SearchRecyclerAdapter(requireContext())
        binding.rvSearch.apply {
            val layoutManager = GridLayoutManager(
                requireContext(),
                2,
                GridLayoutManager.VERTICAL,
                false
            )
            binding.rvSearch.layoutManager = layoutManager
            adapter = searchAdapter
        }

        // add spacing between grid items
        binding.rvSearch.addItemDecoration(ItemSpacingDecoration(20))

    }



    private fun observeSearch() {
        viewModel.search.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Loading -> {
                    Log.d("testSearch", "Loading")
                }

                is NetworkResult.Success -> {
                    val products = response.data
                    searchAdapter.differ.submitList(products)
                    showChancelTv()
                    Log.d("testSearch", "Success - Product count: ${products?.size}")
                }

                is NetworkResult.Error -> {
                    showChancelTv()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("testSearch", "Error: ${response.message}")

                }

                else -> Unit
            }
        }
    }

    private fun searchProducts() {
        var job: Job? = null
        binding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                job?.cancel()
                job = MainScope().launch {
                    delay(500L)
                    s?.let {
                        if (s.toString().isNotEmpty()) {
                            viewModel.searchProducts(s.toString())
                        } else {
                            // If the search query is empty, clear the adapter
                            searchAdapter.differ.submitList(emptyList())
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun onSearchProductClick() {
        searchAdapter.onItemClick = { product ->
            val bundle = Bundle()
            bundle.putParcelable("product", product)

            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(requireView().windowToken, 0)


            findNavController().navigate(
                R.id.action_searchFragment_to_productDetailsFragment,
                bundle
            )
        }
    }

    private fun onHomeClick() {
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setOnItemReselectedListener {
            if (isTypingOrSearching) {
                // User is typing or searching, hide the bottom navigation view
                isTypingOrSearching = false
                bottomNav.visibility = View.VISIBLE
            }
        }
        bottomNav?.menu?.getItem(0)?.setOnMenuItemClickListener {
            activity?.onBackPressed()
            true
        }
    }

    private fun showKeyboardAutomatically() {
        binding.edSearch.requestFocus()
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.edSearch, InputMethodManager.SHOW_IMPLICIT)
    }


    private fun showChancelTv() {
        binding.tvCancel.visibility = View.VISIBLE
        binding.cardMic.visibility = View.GONE
    }

    private fun hideCancelTv() {
        binding.tvCancel.visibility = View.GONE
        binding.cardMic.visibility = View.VISIBLE
    }

    private fun onCancelTvClick() {
        binding.tvCancel.setOnClickListener {
            searchAdapter.differ.submitList(emptyList())
            binding.edSearch.setText("")
            hideCancelTv()

            // User has canceled the search, show the bottom navigation view
            isTypingOrSearching = false
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        showKeyboardAutomatically()
        HideBottomNavigation()
    }


}