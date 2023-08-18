package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.Product
import com.example.laza.utils.Constants.PRODUCT_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeFragmentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel(){

    private val _newArrival = MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val newArrival = _newArrival.asStateFlow()

    init {
        fetchNewArrival()
    }

    private fun fetchNewArrival(){
        viewModelScope.launch {
            _newArrival.emit(NetworkResult.Loading())
        }
        firestore.collection(PRODUCT_COLLECTION).whereEqualTo("brand","New Arrival")
            .get()
            .addOnSuccessListener {result ->
                val newArrivalList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _newArrival.emit(NetworkResult.Success(newArrivalList))
                }

            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _newArrival.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

}