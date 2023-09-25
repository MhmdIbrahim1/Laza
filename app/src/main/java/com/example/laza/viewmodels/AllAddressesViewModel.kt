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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllAddressesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _getAllAddresses =
        MutableStateFlow<NetworkResult<List<Address>>>(NetworkResult.UnSpecified())
    val getAllAddresses = _getAllAddresses.asStateFlow()

    private val _deleteAddress = MutableStateFlow<NetworkResult<Unit>>(NetworkResult.UnSpecified())
    val deleteAddress = _deleteAddress.asStateFlow()
    init {
        getAllUserAddresses()
    }

    private fun getAllUserAddresses(){
        viewModelScope.launch { _getAllAddresses.emit(NetworkResult.Loading()) }
        firestore.collection(USER_COLLECTION).document(auth.uid!!)
            .collection(ADDRESS_COLLECTION)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch { _getAllAddresses.emit(NetworkResult.Error(error.message.toString())) }
                    return@addSnapshotListener
                }
                val addressList = value?.toObjects(Address::class.java)
                viewModelScope.launch { _getAllAddresses.emit(NetworkResult.Success(addressList!!)) }
            }
    }

    fun deleteAddress(address: Address){
        viewModelScope.launch { _deleteAddress.emit(NetworkResult.Loading()) }
        firestore.collection(USER_COLLECTION).document(auth.uid!!)
            .collection(ADDRESS_COLLECTION).document(address.documentId)
            .delete()
            .addOnSuccessListener {
                viewModelScope.launch { _deleteAddress.emit(NetworkResult.Success(Unit)) }
            }
            .addOnFailureListener {
                viewModelScope.launch { _deleteAddress.emit(NetworkResult.Error(it.message.toString())) }
            }
    }
}