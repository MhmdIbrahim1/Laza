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
class BrandsFragmentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Mutable state flows to hold data
    private val _brandsData =
        MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val brandsData = _brandsData.asStateFlow()

    private val _totalItemCount = MutableStateFlow(0)
    val totalItemCount = _totalItemCount.asStateFlow()

    // Paging information
    private val brandsPagingInfo = BrandsPagingInfo()

    // Suspend function to fetch brand data from Firestore and handle errors
    private suspend fun fetchBrandDataFromFirestore(brandName: String) {
        try {
            val result = firestore.collection(PRODUCT_COLLECTION)
                .whereEqualTo("brand", brandName)
                .limit(brandsPagingInfo.page * 20)
                .get()
                .await()

            val brandList = result.toObjects(Product::class.java)
            brandsPagingInfo.isPagingEnd = brandList == brandsPagingInfo.oldBrandList
            brandsPagingInfo.oldBrandList = brandList
            _brandsData.emit(NetworkResult.Success(brandList))
            brandsPagingInfo.page++
        } catch (e: Exception) {
            // Call the common error handler to emit error
            emitError(e)
        }
    }

    // Fetch brand data while emitting loading state
    fun fetchBrandsData(brandName: String) {
        if (!brandsPagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _brandsData.emit(NetworkResult.Loading())
                // Call the suspend function for fetching data from Firestore
                fetchBrandDataFromFirestore(brandName)
            }
        }
    }

    // Fetch total item count from Firestore
    fun fetchTotalItemCount(brandName: String) {
        viewModelScope.launch {
            try {
                val result = firestore.collection(PRODUCT_COLLECTION)
                    .whereEqualTo("brand", brandName)
                    .get()
                    .await()

                val totalCount = result.size()
                _totalItemCount.emit(totalCount)
            } catch (e: Exception) {
                // Call the common error handler to emit error
                emitError(e)
            }
        }
    }


    // Common error handling function
    private suspend fun emitError(exception: Exception) {
        _brandsData.emit(NetworkResult.Error(exception.message.toString()))
    }

    // Data class to store pagination info
    internal data class BrandsPagingInfo(
        var page: Long = 1,
        var oldBrandList: List<Product> = emptyList(),
        var isPagingEnd: Boolean = false
    )
}
