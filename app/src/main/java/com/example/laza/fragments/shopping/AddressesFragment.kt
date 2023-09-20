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
import com.example.laza.data.Address
import com.example.laza.databinding.FragmentAddressesBinding
import com.example.laza.helper.setGArrowImageBasedOnLayoutDirection
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.AddressViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AddressesFragment : Fragment() {
    private var _binding: FragmentAddressesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AddressViewModel>()
    private val args by navArgs<AddressesFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeAddAddress()
        observeError()
        observeDeleteAddress()
        observeUpdateAddress()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentAddressesBinding.inflate(inflater, container, false)
        setGArrowImageBasedOnLayoutDirection(resources,binding.arrow1)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addressArgs = args.address

        if (addressArgs == null) {
            binding.buttonDelelte.visibility = View.GONE
        } else {
            binding.apply {
                edAddressTitle.setText(addressArgs.addressTitle)
                edFullName.setText(addressArgs.fullName)
                edStreet.setText(addressArgs.street)
                edPhone.setText(addressArgs.phone)
                edCity.setText(addressArgs.city)
                edState.setText(addressArgs.state)
            }
        }

        binding.apply {
            buttonSave.setOnClickListener {
                val addressTitle = edAddressTitle.text.toString()
                val fullName = edFullName.text.toString()
                val street = edStreet.text.toString()
                val phone = edPhone.text.toString()
                val city = edCity.text.toString()
                val state = edState.text.toString()

                val address = Address(
                    addressTitle = addressTitle,
                    fullName = fullName,
                    street = street,
                    phone = phone,
                    city = city,
                    state = state
                )

                if (addressArgs == null) {
                    viewModel.addAddress(address)
                } else {
                    viewModel.updateAddress(address.copy(documentId = addressArgs.documentId))
                    Toast.makeText(requireContext(), "Address Updated", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonDelelte.setOnClickListener {
            if (addressArgs != null) {
                viewModel.deleteAddress(addressArgs)
            }
        }

    }

    private fun observeAddAddress(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.addNewAddress.collectLatest{
                    when(it){
                       is NetworkResult.Loading -> {
                           binding.progressBar.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            binding.progressBar.visibility = View.GONE
                            findNavController().navigateUp()
                        }

                        is NetworkResult.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else ->{}
                    }
                }
            }
        }
    }

    private fun observeDeleteAddress() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteAddress.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            binding.progressBar.visibility = View.INVISIBLE
                            findNavController().navigateUp()

                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeUpdateAddress(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.addNewAddress.collectLatest{
                    when(it){
                        is NetworkResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is NetworkResult.Success -> {
                            binding.progressBar.visibility = View.INVISIBLE
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
    private fun observeError(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.error.collectLatest{
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
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