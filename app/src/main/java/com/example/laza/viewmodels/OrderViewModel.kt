package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.order.Order
import com.example.laza.utils.Constants.CART_COLLECTION
import com.example.laza.utils.Constants.ORDER_COLLECTION
import com.example.laza.utils.Constants.USER_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _orders = MutableStateFlow<NetworkResult<Order>>(NetworkResult.UnSpecified())
    val orders = _orders.asStateFlow()

    fun placeOrder(order: Order) {
        viewModelScope.launch {
            try {
                _orders.emit(NetworkResult.Loading())

                val orderId = firestore.collection(ORDER_COLLECTION).document().id

                val userOrderRef = firestore.collection(USER_COLLECTION)
                    .document(auth.uid!!)
                    .collection(ORDER_COLLECTION)
                    .document(orderId)
                val genericOrderRef = firestore.collection(ORDER_COLLECTION)
                    .document(orderId)

                // Update Firestore with the order details
                withContext(Dispatchers.IO) {
                    val batch = firestore.batch()
                    batch.set(userOrderRef, order.copy(documentId = orderId, userId = auth.uid!!))
                    batch.set(genericOrderRef, order.copy(documentId = orderId, userId = auth.uid!!))
                    batch.commit().await()
                }

                // Delete products from the user's cart collection
                firestore.collection(USER_COLLECTION)
                    .document(auth.uid!!)
                    .collection(CART_COLLECTION)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val deleteTasks = mutableListOf<Task<Void>>()
                        snapshot.documents.forEach { document ->
                            deleteTasks.add(document.reference.delete())
                        }
                        Tasks.whenAllComplete(deleteTasks)
                    }
                    .await()

                _orders.emit(NetworkResult.Success(order))
            } catch (e: Exception) {
                _orders.emit(NetworkResult.Error(e.message.toString()))
            }
        }
    }

}
