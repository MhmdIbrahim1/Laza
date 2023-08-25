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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeFragmentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Mutable state flow to hold new arrival data
    private val _newArrival = MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val newArrival = _newArrival.asStateFlow()

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
                        .limit(newArrivalPagingInfo.page * 15)
                        .get()
                        .await()

                    val newArrivalList = result.toObjects(Product::class.java)

                    // Update paging info
                    newArrivalPagingInfo.isPagingEnd = newArrivalList == newArrivalPagingInfo.oldNewArrivalList
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
