package com.example.laza.fragments.shopping

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import com.example.laza.adapters.WishListAdapter
import com.example.laza.databinding.FragmentWishlistBinding
import com.example.laza.helper.getProductPrice
import com.example.laza.helper.setGArrowImageBasedOnLayoutDirection
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.utils.ShowBottomNavigation
import com.example.laza.utils.WishlistUtil
import com.example.laza.viewmodels.DetailsViewModel
import com.example.laza.viewmodels.WishListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WishlistFragment : Fragment(), WishlistUtil {
    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<WishListViewModel>()
    private val _viewModel by viewModels<DetailsViewModel>()
    private lateinit var wishListAdapter: WishListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeWishListData()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        setGArrowImageBasedOnLayoutDirection(resources,binding.arrow1)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        observeTotalItemCount()
        setupSwipeToDelete()
        observeRemoveProductFromWishListStatus()

        binding.arrow1.setOnClickListener {
           activity?.onBackPressed()
        }
        binding.cartFromWishlist.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
        }

        wishListAdapter.onItemClickListener = {
            if (isAdded){
                // Calculate the price after applying the offer percentage (if available)
                val priceAfterOffer = it.offerPercentage?.getProductPrice(it.price) ?: it.price

                // Create a new product object with the updated price
                val updatedProduct = it.copy(price = priceAfterOffer)

                // Navigate to the product details fragment and pass the updated product object
                val action = WishlistFragmentDirections.actionWishlistFragmentToProductDetailsFragment(
                    updatedProduct
                )
                findNavController().navigate(action)
            }
        }

        binding.cartFromWishlist.setOnClickListener {
            if (isResumed) {
                findNavController().navigate(R.id.action_wishlistFragment_to_cartFragment)
            }
        }

        // if the previous destination was the HomeFragment, and the user clicked on the back button then navigate to the HomeFragment
        requireActivity().onBackPressedDispatcher.addCallback {
            findNavController().navigateUp()
        }

    }

    private fun observeWishListData() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wishListData.collect {
                    when (it) {
                        is NetworkResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            binding.progressBar.visibility = View.GONE
                            wishListAdapter.differ.submitList(it.data)
                            Log.d( "differ","observeWishListData: ${wishListAdapter.differ.currentList.size} ")
                        }

                        is NetworkResult.Error -> {
                            binding.progressBar.visibility = View.GONE
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun observeRemoveProductFromWishListStatus() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.removeProductFromWishListStatus.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
//                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
//                            binding.progressBar.visibility = View.GONE
                        }

                        is NetworkResult.Error -> {
//                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun observeTotalItemCount() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalItemCount.collect {
                    updateItemCount(it)
                }
            }
        }
    }
    private fun updateItemCount(itemCount: Int) {
        if (itemCount == 0) {
            binding.itemCount.text = resources.getString(R.string.no_items_found)
            binding.availableOnStock.visibility = View.GONE
            binding.itemCount.visibility = View.VISIBLE
        } else {
            binding.itemCount.text = itemCount.toString().plus(" items")
            binding.availableOnStock.visibility = View.VISIBLE
            binding.itemCount.visibility = View.VISIBLE
        }
    }


    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = wishListAdapter.differ.currentList[position]

                // Remove the item from the wishlist when swiped
                _viewModel.removeFromWishList(item)
                Toast.makeText(
                    requireContext(),
                    item.product.name + " removed from wishlist",
                    Toast.LENGTH_SHORT
                ).show()
                // Refresh the RecyclerView to reflect the changes
                wishListAdapter.notifyItemRemoved(position)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                val context = recyclerView.context

                var icon: Drawable? = null
                var background: Drawable? = null
                val itemView = viewHolder.itemView

                // Set the icon and background based on the swipe direction
                if (dX < 0) { // Swiping to the left
                    icon =
                        ResourcesCompat.getDrawable(context.resources, R.drawable.delete_cart, null)
                    background = ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.swipe_to_delete_background,
                        null
                    )
                }

                // Calculate the size of the icon
                val iconSize = (itemView.height * 0.2).toInt()
                val iconMargin = (itemView.width - iconSize) / 2
                val iconTop = itemView.top + (itemView.height - iconSize) / 2
                val iconBottom = iconTop + iconSize

                // Set the bounds of the icon in the middle-right
                if (dX < 0) { // Swiping to the left
                    val iconLeft = itemView.right - iconSize - iconMargin
                    val iconRight = itemView.right - iconMargin
                    icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                }

                // Calculate the position of the background
                val backgroundLeft = itemView.left
                val backgroundRight = itemView.right
                val backgroundTop = itemView.top
                val backgroundBottom = itemView.bottom
                background?.setBounds(
                    backgroundLeft,
                    backgroundTop,
                    backgroundRight,
                    backgroundBottom
                )

                // Draw the background and icon
                background?.draw(c)
                icon?.draw(c)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.brandsRv)
    }
    private fun setUpRecyclerView() {
        wishListAdapter = WishListAdapter(this,requireContext())
        binding.brandsRv.apply {
            val layoutManager = GridLayoutManager(
                requireContext(),
                2,
                GridLayoutManager.VERTICAL,
                false
            )
            binding.brandsRv.layoutManager = layoutManager
            binding.brandsRv.addItemDecoration(ItemSpacingDecoration(20))
            adapter = wishListAdapter
        }
    }



    override fun onResume() {
        super.onResume()
        ShowBottomNavigation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun deleteItem(position: Int) {
        val item = wishListAdapter.differ.currentList[position]
        _viewModel.removeFromWishList(item)

        val newList = wishListAdapter.differ.currentList.toMutableList()
        newList.removeAt(position)
        wishListAdapter.differ.submitList(newList)
        Toast.makeText(
            requireContext(),
            item.product.name + " removed from wishlist",
            Toast.LENGTH_SHORT
        ).show()
    }

}