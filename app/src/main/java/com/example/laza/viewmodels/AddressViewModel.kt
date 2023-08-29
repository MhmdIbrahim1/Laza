package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.Address
import com.example.laza.utils.Constants.ADDRESS_COLLECTION
import com.example.laza.utils.Constants.USER_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _addNewAddress =
        MutableStateFlow<NetworkResult<Address>>(NetworkResult.UnSpecified())
    val addNewAddress = _addNewAddress.asStateFlow()

    private val _deleteAddress = MutableStateFlow<NetworkResult<Unit>>(NetworkResult.UnSpecified())
    val deleteAddress = _deleteAddress.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()
    fun addAddress(address: Address) {
        val validate = validateAddress(address)
        if (validate) {
            viewModelScope.launch {
                _addNewAddress.emit(NetworkResult.Loading())
            }
            firestore.collection(USER_COLLECTION).document(auth.uid!!)
                .collection(ADDRESS_COLLECTION)
                .document().set(address)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _addNewAddress.emit(NetworkResult.Success(address))
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _addNewAddress.emit(NetworkResult.Error(it.message.toString()))
                    }
                }
        } else {
            viewModelScope.launch {
                _error.emit("All Fields Are Required")
            }
        }
    }

    fun updateAddress(address: Address) {
        viewModelScope.launch {
            _addNewAddress.emit(NetworkResult.Loading())
        }
        val validate = validateAddress(address)
        if (validate) {
            firestore.collection(USER_COLLECTION).document(auth.uid!!)
                .collection(ADDRESS_COLLECTION)
                .document(address.documentId)
                .set(address, SetOptions.merge()) // update the document
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _addNewAddress.emit(NetworkResult.Success(address))
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _addNewAddress.emit(NetworkResult.Error(it.message.toString()))
                    }
                }
        }else{
            viewModelScope.launch {
                _error.emit("All Fields Are Required")
            }
        }
    }

    fun deleteAddress(address: Address){
        viewModelScope.launch {
            _deleteAddress.emit(NetworkResult.Loading())
        }
        firestore.collection(USER_COLLECTION).document(auth.uid!!)
            .collection(ADDRESS_COLLECTION)
            .document(address.documentId)
            .delete()
            .addOnSuccessListener {
                viewModelScope.launch {
                    _deleteAddress.emit(NetworkResult.Success(Unit))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _deleteAddress.emit(NetworkResult.Error(it.message.toString()))
                }
            }

    }

    private fun validateAddress(address: Address): Boolean {
        return address.addressTitle.isNotEmpty() && address.fullName.isNotEmpty() &&
                address.street.isNotEmpty() && address.city.isNotEmpty() &&
                address.phone.isNotEmpty() && address.state.isNotEmpty()
    }
}