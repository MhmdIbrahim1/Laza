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

    private val newArrivalPagingInfo = NewArrivalPagingInfo()
    init {
        fetchNewArrival()
    }

     fun fetchNewArrival(){
         if (!newArrivalPagingInfo.isPagingEnd) {
             viewModelScope.launch {
                 _newArrival.emit(NetworkResult.Loading())
             }
             firestore.collection(PRODUCT_COLLECTION).whereIn("brand", listOf("Adidas", "New Arrival")) // or for fetch just one field we use WhereEqualTo
             .limit(newArrivalPagingInfo.page * 10)
                 .get()
                 .addOnSuccessListener { result ->
                     val newArrivalList = result.toObjects(Product::class.java)
                     newArrivalPagingInfo.isPagingEnd =
                         newArrivalList == newArrivalPagingInfo.oldNewArrivalList
                     newArrivalPagingInfo.oldNewArrivalList = newArrivalList
                     viewModelScope.launch {
                         _newArrival.emit(NetworkResult.Success(newArrivalList))
                     }
                     newArrivalPagingInfo.page++
                 }
                 .addOnFailureListener {
                     viewModelScope.launch {
                         _newArrival.emit(NetworkResult.Error(it.message.toString()))
                     }
                 }
         }
    }

    internal data class NewArrivalPagingInfo(
        var page: Long = 1,
        var oldNewArrivalList: List<Product> = emptyList(),
        var isPagingEnd: Boolean = false
    )

}