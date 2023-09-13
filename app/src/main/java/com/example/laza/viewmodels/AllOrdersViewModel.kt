package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.order.Order
import com.example.laza.utils.Constants
import com.example.laza.utils.Constants.ORDER_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AllOrdersViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _allOrders =
        MutableStateFlow<NetworkResult<List<Order>>>(NetworkResult.UnSpecified())
    val allOrders = _allOrders.asStateFlow()

    init {
        getAllOrders()
    }

    private fun getAllOrders(){
        viewModelScope.launch {
            _allOrders.emit(NetworkResult.Loading())
        }

        firestore.collection(Constants.USER_COLLECTION).document(auth.uid!!).collection(ORDER_COLLECTION).get()
            .addOnSuccessListener {
                val orders = it.toObjects(Order::class.java)
                viewModelScope.launch {
                    _allOrders.emit(NetworkResult.Success(orders))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _allOrders.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }
}
