package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.Address
import com.example.laza.utils.Constants.ADDRESS_COLLECTION
import com.example.laza.utils.Constants.USER_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _addresses = MutableStateFlow<NetworkResult<List<Address>>>(NetworkResult.UnSpecified())
    val addresses = _addresses.asStateFlow()


    init {
        getUserAddress()
    }


    private fun getUserAddress() {
        viewModelScope.launch { _addresses.emit(NetworkResult.Loading()) }

        firestore.collection(USER_COLLECTION).document(auth.uid!!).collection(ADDRESS_COLLECTION)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch { _addresses.emit(NetworkResult.Error(error.message.toString())) }
                    return@addSnapshotListener
                }
                val addressList = value?.toObjects(Address::class.java)
                viewModelScope.launch { _addresses.emit(NetworkResult.Success(addressList!!)) }
            }

    }

    private fun clear() {
        viewModelScope.launch { _addresses.emit(NetworkResult.UnSpecified()) }
        viewModelScope.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        clear()
    }
}