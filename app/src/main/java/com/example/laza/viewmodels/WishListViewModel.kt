package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.Product
import com.example.laza.data.WishlistProduct
import com.example.laza.utils.Constants
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
class WishListViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _wishListData =
        MutableStateFlow<NetworkResult<List<WishlistProduct>>>(NetworkResult.UnSpecified())
    val wishListData = _wishListData.asStateFlow()

    private val _totalItemCount = MutableStateFlow(0)
    val totalItemCount = _totalItemCount.asStateFlow()

    private val _removeProductFromWishListStatus =
        MutableStateFlow<NetworkResult<String>>(NetworkResult.UnSpecified())
    val removeProductFromWishListStatus = _removeProductFromWishListStatus.asStateFlow()

    init {
        fetchWishListData()
        fetchTotalItemCount()
    }

    private fun fetchWishListData() {
        viewModelScope.launch {
            _wishListData.emit(NetworkResult.Loading())

            // Reference to the wishlist collection
            val wishlistRef = firestore.collection(USER_COLLECTION)
                .document(auth.currentUser!!.uid)
                .collection("wishlist")

            // Listen for changes in the wishlist collection
            wishlistRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    viewModelScope.launch {
                        _wishListData.emit(NetworkResult.Error(error.message))
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val wishList = snapshot.toObjects(WishlistProduct::class.java)
                    viewModelScope.launch {
                        _wishListData.emit(NetworkResult.Success(wishList))
                    }
                }
            }
        }
    }

    private fun fetchTotalItemCount() {
        viewModelScope.launch {
            // Reference to the wishlist collection
            val wishlistRef = firestore.collection(USER_COLLECTION)
                .document(auth.currentUser!!.uid)
                .collection("wishlist")

            // Listen for changes in the wishlist collection
            wishlistRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Call the emitError function to handle the error
                    emitError(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                   viewModelScope.launch {
                       val totalItemCount = snapshot.size()
                       _totalItemCount.emit(totalItemCount)
                   }
                }
            }
        }
    }

    fun removeProductFromWishList(productId: String,newState: Boolean) {
        viewModelScope.launch {
            _removeProductFromWishListStatus.emit(NetworkResult.Loading())

            // Reference to the wishlist collection
            val wishlistRef = firestore.collection(USER_COLLECTION)
                .document(auth.currentUser!!.uid)
                .collection("wishlist")
            // Remove the product from the wishlist
            wishlistRef.document(productId).delete()
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _removeProductFromWishListStatus.emit(NetworkResult.Success("Product removed from wishlist"))
                        updateProductWithNewWithListStatus(productId, newState)
                    }
                }
                .addOnFailureListener { exception ->
                    viewModelScope.launch {
                        _removeProductFromWishListStatus.emit(NetworkResult.Error(exception.message.toString()))
                    }
                }
        }
    }

    private fun updateProductWithNewWithListStatus(productId: String, newStatus: Boolean) {
        val productRef = firestore.collection(Constants.PRODUCT_COLLECTION).document(productId)

        firestore.runTransaction { transaction ->
            val product = transaction.get(productRef).toObject(Product::class.java)

            if (product != null) {
                val updatedProduct = product.copy(
                    inWishList = newStatus
                )

                transaction.set(productRef, updatedProduct)
            }
        }.addOnFailureListener { e ->
            // Handle the error here
            viewModelScope.launch {
                _wishListData.emit(NetworkResult.Error(e.message.toString()))
            }
        }
    }
    private  fun emitError(exception: Exception) {
        viewModelScope.launch {
            _wishListData.emit(NetworkResult.Error(exception.message.toString()))
        }
    }

}