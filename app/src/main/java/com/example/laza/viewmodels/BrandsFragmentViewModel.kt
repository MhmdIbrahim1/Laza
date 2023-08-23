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
class BrandsFragmentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _brandsData =
        MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val brandsData = _brandsData.asStateFlow()

    // Add a mutable state flow to hold the total item count
    private val _totalItemCount = MutableStateFlow(0)
    val totalItemCount = _totalItemCount.asStateFlow()

    private val brandsPagingInfo = BrandsPagingInfo()

    fun fetchBrandsData(brandName: String) {
        if (!brandsPagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _brandsData.emit(NetworkResult.Loading())
            }
            // Fetch the actual items with pagination
            firestore.collection(PRODUCT_COLLECTION).whereEqualTo("brand", brandName)
                .limit(brandsPagingInfo.page * 20)
                .get()
                .addOnSuccessListener { result ->
                    val brandsList = result.toObjects(Product::class.java)
                    brandsPagingInfo.isPagingEnd =
                        brandsList == brandsPagingInfo.oldNewArrivalList
                    brandsPagingInfo.oldNewArrivalList = brandsList

                    viewModelScope.launch {
                        _brandsData.emit(NetworkResult.Success(brandsList))
                    }
                    brandsPagingInfo.page++
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _brandsData.emit(NetworkResult.Error(it.message.toString()))
                    }
                }
        }
    }

    fun fetchTotalItemCount(brandName: String) {
        firestore.collection(PRODUCT_COLLECTION).whereEqualTo("brand", brandName)
            .get()
            .addOnSuccessListener { result ->
                viewModelScope.launch {
                    val totalCount = result.size() // Get the total count of documents
                    _totalItemCount.emit(totalCount)
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _brandsData.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

    internal data class BrandsPagingInfo(
        var page: Long = 1,
        var oldNewArrivalList: List<Product> = emptyList(),
        var isPagingEnd: Boolean = false
    )
}