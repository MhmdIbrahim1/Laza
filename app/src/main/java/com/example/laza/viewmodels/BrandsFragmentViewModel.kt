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
    class BrandsFragmentViewModel@Inject constructor(
        private val firestore: FirebaseFirestore
    ) : ViewModel(){
        private val _brandsData = MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
        val brandsData = _brandsData.asStateFlow()


        fun fetchBrandsData(brandName: String){
            viewModelScope.launch {
                _brandsData.emit(NetworkResult.Loading())
            }
            firestore.collection(PRODUCT_COLLECTION).whereEqualTo("brand", brandName)
                .get()
                .addOnSuccessListener { result ->
                    val brandsList = result.toObjects(Product::class.java)
                    viewModelScope.launch {
                        _brandsData.emit(NetworkResult.Success(brandsList))
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _brandsData.emit(NetworkResult.Error(it.message.toString()))
                    }
                }

        }

    }