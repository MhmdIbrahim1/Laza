package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.Product
import com.example.laza.data.WishlistProduct
import com.example.laza.utils.Constants
import com.example.laza.utils.Constants.PRODUCT_COLLECTION
import com.example.laza.utils.Constants.USER_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeFragmentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Mutable state flow to hold new arrival data
    private val _newArrival =
        MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val newArrival = _newArrival.asStateFlow()

    // Mutable state flow for wishlist
    private val _wishlist =
        MutableStateFlow<NetworkResult<List<WishlistProduct>>>(NetworkResult.UnSpecified())
    val wishlist = _wishlist.asStateFlow()


    // Paging information

    private val newArrivalPagingInfo = NewArrivalPagingInfo()

    // Function to fetch new arrival data

    init {
        fetchNewArrival()
    }

    fun fetchNewArrival() {
        if (!newArrivalPagingInfo.isPagingEnd) {
            viewModelScope.launch {
                // Emit loading state
                _newArrival.emit(NetworkResult.Loading())

                try {
                    val result = firestore.collection(PRODUCT_COLLECTION)
                        .whereIn("brand", listOf("Adidas", "New Arrival"))
                        .limit(newArrivalPagingInfo.page * 10)
                        .get()
                        .await()

                    val newArrivalList = result.toObjects(Product::class.java)

                    // Update paging info
                    newArrivalPagingInfo.isPagingEnd =
                        newArrivalList == newArrivalPagingInfo.oldNewArrivalList
                    newArrivalPagingInfo.oldNewArrivalList = newArrivalList
                    newArrivalPagingInfo.page++

                    // Emit success state
                    _newArrival.emit(NetworkResult.Success(newArrivalList))
                } catch (e: Exception) {
                    // Emit error state
                    emitError(e)
                }
            }
        }
    }

    // Function to fetch  the data in realtime
//    fun fetchNewArrival() {
//        if (!newArrivalPagingInfo.isPagingEnd) {
//            // Remove any existing snapshot listener
//            val snapshotListener = firestore.collection(PRODUCT_COLLECTION)
//                .whereIn("brand", listOf("Adidas", "New Arrival"))
//                .limit(newArrivalPagingInfo.page * 10)
//                .addSnapshotListener { snapshot, exception ->
//                    if (exception != null) {
//                        // Handle the error here and emit an error state
//                        viewModelScope.launch {
//                            emitError(exception)
//                        }
//                        return@addSnapshotListener
//                    }
//
//                    if (snapshot != null) {
//                        val newArrivalList = snapshot.toObjects(Product::class.java)
//
//                        // Update paging info
//                        newArrivalPagingInfo.isPagingEnd =
//                            newArrivalList == newArrivalPagingInfo.oldNewArrivalList
//                        newArrivalPagingInfo.oldNewArrivalList = newArrivalList
//                        newArrivalPagingInfo.page++
//
//                        // Emit success state
//                        _newArrival.value = NetworkResult.Success(newArrivalList)
//                    }
//                }
//        }
//    }

    fun setTotalReviewsCount(productId: String) {
        val productRef = firestore.collection(PRODUCT_COLLECTION).document(productId)

        productRef.get().addOnSuccessListener { documentSnapshot ->
            val currentTotalReviewsCount = documentSnapshot.getLong("reviewCount") ?: 0
            val newTotalReviewsCount = currentTotalReviewsCount + 1

            productRef.update("reviewCount", newTotalReviewsCount)
                .addOnSuccessListener {
                    // Successfully incremented the total reviews count
                }
                .addOnFailureListener { _ ->
                    // Handle the error here
                }
        }.addOnFailureListener { _ ->
            // Handle the error here
        }
    }



    // Common error handling function
    private suspend fun emitError(exception: Exception) {
        _newArrival.emit(NetworkResult.Error(exception.message.toString()))
    }

    // Data class to store pagination info
    internal data class NewArrivalPagingInfo(
        var page: Long = 1,
        var oldNewArrivalList: List<Product> = emptyList(),
        var isPagingEnd: Boolean = false
    )
}