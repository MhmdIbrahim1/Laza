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
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.AddressViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AddressesFragment : Fragment() {
    private lateinit var binding: FragmentAddressesBinding
    private val viewModel by viewModels<AddressViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeAddAddress()
        observeError()
        observeAddAddress()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentAddressesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            buttonSave.setOnClickListener {
                val addressTitle = binding.edAddressTitle.text.toString()
                val fullName = binding.edFullName.text.toString()
                val street = binding.edStreet.text.toString()
                val city = binding.edCity.text.toString()
                val phone = binding.edPhone.text.toString()
                val state = binding.edState.text.toString()

                val address = Address(addressTitle,fullName,street,city,phone,state)
                viewModel.addAddress(address)
            }
        }
        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
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
}