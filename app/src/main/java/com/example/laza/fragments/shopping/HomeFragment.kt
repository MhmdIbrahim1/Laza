package com.example.laza.fragments.shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.laza.R
import com.example.laza.adapters.BrandAdapter
import com.example.laza.adapters.NewArrivalAdapter
import com.example.laza.databinding.FragmentHomeBinding
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.HomeFragmentViewModel
import com.example.storein.utils.HorizontalItemDecoration
import com.example.storein.utils.VerticalItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var drawerOpener: DrawerOpener
    private lateinit var brandItem: List<BrandAdapter.BrandItem>
    private lateinit var newArrivalAdapter: NewArrivalAdapter
    private lateinit var brandAdapter: BrandAdapter
    private val viewModel by viewModels<HomeFragmentViewModel>()
    // Interface to communicate with the activity
    interface DrawerOpener {
        fun openDrawer()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        changeStatusBarColor(R.color.white)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDrawer()
        brandItem = listOf(
            BrandAdapter.BrandItem(R.drawable.adidas, "Adidas"),
            BrandAdapter.BrandItem(R.drawable.nike, "Nike"),
            BrandAdapter.BrandItem(R.drawable.puma, "Puma"),
            BrandAdapter.BrandItem(R.drawable.fila, "Fila")
            // Add more BrandItems for other brands as needed
        )
        setUpBrandRV()
        setUpNewArrivalRV()
        observeNewArrival()
    }


    private fun observeNewArrival(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.newArrival.collectLatest {
                    when(it){
                        is NetworkResult.Loading -> {
                            showLoading()
                        }

                        is NetworkResult.Success -> {
                            Log.d("HomeFragment", "observeNewArrival: ${it.data}")
                            newArrivalAdapter.differ.submitList(it.data)
                            hideLoading()
                        }

                        is NetworkResult.Error -> {
                            Log.d("HomeFragment", "observeNewArrival: ${it.message}")
                           hideLoading()
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showLoading() {
       binding.progressBar.visibility = View.VISIBLE
    }

    private fun setDrawer(){
        // Check if the host activity implements the DrawerOpener interface
        if (activity is DrawerOpener) {
            drawerOpener = activity as DrawerOpener
        } else {
            throw IllegalArgumentException("Host activity must implement DrawerOpener interface")
        }

        binding.circleImageMenu.setOnClickListener {
            // Open the drawer by calling the interface method
            drawerOpener.openDrawer()
        }
    }

    private fun setUpBrandRV(){
        brandAdapter = BrandAdapter(brandItem)
        brandAdapter.onBrandClickListener = BrandAdapter.OnBrandClickListener{ position, _ ->
            when(position){
                0 -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToAdidasFragment()
                    requireView().findNavController().navigate(action)
                }
                1 -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToNikeFragment()
                    requireView().findNavController().navigate(action)
                }
                2 -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToPumaFragment()
                    requireView().findNavController().navigate(action)
                }
                3 -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToFilaFragment()
                    requireView().findNavController().navigate(action)
                }
            }
        }
        binding.brandRv.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.brandRv.addItemDecoration(HorizontalItemDecoration())
            adapter = brandAdapter
        }
    }

    private fun setUpNewArrivalRV(){
        newArrivalAdapter = NewArrivalAdapter()
        binding.newArrivalsRv.apply {
            val layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL,
            )
            layoutManager.gapStrategy =
                StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            binding.newArrivalsRv.layoutManager = layoutManager
            binding.newArrivalsRv.addItemDecoration(ItemSpacingDecoration(10))
            adapter = newArrivalAdapter

        }
    }

    private fun changeStatusBarColor(color: Int){
        requireActivity().window.statusBarColor = color
    }
}
