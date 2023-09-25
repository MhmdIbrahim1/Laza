package com.example.laza.fragments.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laza.R
import androidx.navigation.Navigation.findNavController
import com.example.laza.adapters.AllAddressesAdapter
import com.example.laza.data.Address
import com.example.laza.databinding.FragmentAllAddressesBinding
import com.example.laza.utils.AllAddressesUtil
import com.example.laza.utils.ItemSpacingDecoration
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.AllAddressesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AllAddressesFragment : Fragment(), AllAddressesUtil {
    private var _binding: FragmentAllAddressesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AllAddressesViewModel>()
    private lateinit var addressAdapter: AllAddressesAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllAddressesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeGetAllAddresses()

        binding.btnAddAddress.setOnClickListener {
            if (findNavController(binding.root).currentDestination?.id == R.id.allAddressesFragment) {
                findNavController(binding.root).navigate(R.id.action_allAddressesFragment_to_addressesFragment)
            }
        }

        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }
    }


    private fun observeGetAllAddresses() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllAddresses.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            binding.progressbarCart.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            binding.progressbarCart.visibility = View.GONE
                            addressAdapter.differ.submitList(it.data)
                        }

                        is NetworkResult.Error -> {
                            binding.progressbarCart.visibility = View.GONE
                        }

                        else -> Unit
                    }

                }
            }
        }
    }

    private fun setupRecyclerView() {
        addressAdapter = AllAddressesAdapter(this)
        binding.rvAllAddresses.apply {
            adapter = addressAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(ItemSpacingDecoration(10))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun deleteItem(position: Int) {
        val dialogView =
            layoutInflater.inflate(R.layout.delete_address_confirmation_dialog, null)

        val alertDialogBuilder =
            AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                .setView(dialogView)
                .create()

        val cancelButton =
            dialogView.findViewById<AppCompatButton>(R.id.cancel_button)
        val deleteButton =
            dialogView.findViewById<AppCompatButton>(R.id.delete_button)

        cancelButton.setOnClickListener {
            alertDialogBuilder.dismiss()
        }

        deleteButton.setOnClickListener {
            viewModel.deleteAddress(addressAdapter.differ.currentList[position])
            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()
    }

    override fun editItem(address: Address, position: Int) {
        val action = AllAddressesFragmentDirections.actionAllAddressesFragmentToAddressesFragment(
            address
        )
        findNavController().navigate(action)
    }

}