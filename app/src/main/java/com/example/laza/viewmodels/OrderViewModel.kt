package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.order.Order
import com.example.laza.utils.Constants.CART_COLLECTION
import com.example.laza.utils.Constants.ORDER_COLLECTION
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
class OrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {


    private var _orders = MutableStateFlow<NetworkResult<Order>>(NetworkResult.UnSpecified())
    val orders = _orders.asStateFlow()


    fun placeOrder(order: Order) {
        viewModelScope.launch {
            _orders.emit(NetworkResult.Loading())
        }
        //TODO: Add order to users collection
        //TODO: Add order into orders collection
        //TODO: Delete the products from user cart collection

        firestore.runBatch {
            firestore.collection(USER_COLLECTION)
                .document(auth.uid!!).collection(ORDER_COLLECTION)
                .document().set(order)

            firestore.collection(ORDER_COLLECTION)
                .document().set(order)

            firestore.collection(USER_COLLECTION)
                .document(auth.uid!!).collection(CART_COLLECTION)
                .get().addOnSuccessListener { snapshot ->
                    snapshot.documents.forEach {
                        it.reference.delete()
                    }
                }
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _orders.emit(NetworkResult.Success(order))
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _orders.emit(NetworkResult.Error(it.message.toString()))
                    }
                }
        }
    }
}