package com.example.laza.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.CartProduct
import com.example.laza.data.Reviews
import com.example.laza.data.WishlistProduct
import com.example.laza.firebase.FirebaseCommon
import com.example.laza.utils.Constants
import com.example.laza.utils.Constants.CART_COLLECTION
import com.example.laza.utils.Constants.REVIEWS_COLLECTION
import com.example.laza.utils.Constants.USER_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) : ViewModel() {

    private val _addToCart =
        MutableStateFlow<NetworkResult<CartProduct>>(NetworkResult.UnSpecified())
    val addToCart = _addToCart.asStateFlow()

    private val _addToWishList =
        MutableStateFlow<NetworkResult<WishlistProduct>>(NetworkResult.UnSpecified())
    val addToWishList = _addToWishList.asStateFlow()

    private val _removeFromWishList =
        MutableStateFlow<NetworkResult<WishlistProduct>>(NetworkResult.UnSpecified())
    val removeFromWishList = _removeFromWishList.asStateFlow()
    // Use a MutableStateFlow to store the wishlist status

    private val _wishlistStatus = MutableStateFlow<Boolean?>(null)
    val wishlistStatus = _wishlistStatus.asStateFlow()

    private val _fetchReviews =
        MutableStateFlow<NetworkResult<List<Reviews>>>(NetworkResult.UnSpecified())
    val fetchReviews = _fetchReviews.asStateFlow()

    private val _colorWarningVisible =MutableLiveData<Boolean>()
    val colorWarningVisible = _colorWarningVisible

    private val _sizeWarningVisible = MutableLiveData<Boolean>()
    val sizeWarningVisible = _sizeWarningVisible


    fun fetchInitialWishlistStatus(productId: String) {
        // Query Firestore to check if the product is in the user's wishlist
        firestore.collection(USER_COLLECTION)
            .document(auth.uid!!)
            .collection("wishlist")
            .whereEqualTo("product.id", productId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _wishlistStatus.value = null
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _wishlistStatus.value = !snapshot.isEmpty
                }
            }
    }

    fun addUpdateProductInCart(cartProduct: CartProduct) {
        viewModelScope.launch {
            _addToCart.emit(NetworkResult.Loading())
        }
        firestore.collection(USER_COLLECTION)
            .document(auth.uid!!)
            .collection(CART_COLLECTION)
            .whereEqualTo("product.id", cartProduct.product.id)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.let {
                    if (it.isEmpty()) { // add new Product
                        addNewProduct(cartProduct)
                    } else { // update existing product
                        val product = it.first().toObject(CartProduct::class.java)
                        if (product == cartProduct) { // increase quantity
                            val documentId = it.first().id
                            increaseQuantity(documentId, cartProduct)
                        } else { // aa new product
                            addNewProduct(cartProduct)
                        }
                    }
                }
            }
            .addOnFailureListener {
                _addToCart.value = NetworkResult.Error(it.message.toString())
            }
    }

    private fun addNewProduct(cartProduct: CartProduct) {
        firebaseCommon.addProductToCart(cartProduct) { addedProduct, e ->
            viewModelScope.launch {
                if (e == null) {
                    _addToCart.emit(NetworkResult.Success(addedProduct!!))
                } else {
                    _addToCart.emit(NetworkResult.Error(e.message.toString()))
                }
            }
        }
    }

    fun addToWishList(wishlistProduct: WishlistProduct) {
        viewModelScope.launch {
            _addToWishList.emit(NetworkResult.Loading())
        }
        firestore.collection(USER_COLLECTION)
            .document(auth.uid!!)
            .collection("wishlist")
            .whereEqualTo("product.id", wishlistProduct.product.id)
            .get()
            .addOnSuccessListener { it ->
                it.documents.let {
                    if (it.isEmpty()) {
                        firebaseCommon.addProductToWishList(wishlistProduct) { addedProduct, e ->
                            viewModelScope.launch {
                                if (e == null) {
                                    _addToWishList.emit(NetworkResult.Success(addedProduct!!))

                                } else {
                                    _addToWishList.emit(NetworkResult.Error(e.message.toString()))
                                }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                _addToWishList.value = NetworkResult.Error(it.message.toString())
            }
    }

    fun removeFromWishList(wishlistProduct: WishlistProduct) {
        viewModelScope.launch {
            _removeFromWishList.emit(NetworkResult.Loading())
        }
        firebaseCommon.removeProductFromWishList(wishlistProduct) { removedProduct, e ->
            viewModelScope.launch {
                if (e == null) {
                    _removeFromWishList.emit(NetworkResult.Success(removedProduct!!))
                } else {
                    _removeFromWishList.emit(NetworkResult.Error(e.message.toString()))
                }
            }
        }
    }

    private fun increaseQuantity(documentId: String, cartProduct: CartProduct) {
        firebaseCommon.increaseQuantity(documentId) { _, e ->
            viewModelScope.launch {
                if (e == null) {
                    _addToCart.emit(NetworkResult.Success(cartProduct))
                } else {
                    _addToCart.emit(NetworkResult.Error(e.message.toString()))
                }
            }
        }
    }

    fun fetchReviews(productId: String) {
        viewModelScope.launch {
            _fetchReviews.emit(NetworkResult.Loading())
        }
        firestore.collection(Constants.PRODUCT_COLLECTION)
            .document(productId)
            .collection(REVIEWS_COLLECTION)
            .limit(2)
            .get()
            .addOnSuccessListener {
                val reviews = it.toObjects(Reviews::class.java)
                viewModelScope.launch {
                    _fetchReviews.emit(NetworkResult.Success(reviews))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _fetchReviews.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

    // Method to set color warning visibility
    fun setColorWarningVisibility(isVisible: Boolean) {
        _colorWarningVisible.postValue(isVisible)
    }

    // Method to set size warning visibility
    fun setSizeWarningVisibility(isVisible: Boolean) {
        _sizeWarningVisible.postValue(isVisible)
    }



    private fun clear() {
        viewModelScope.launch {
            _addToCart.emit(NetworkResult.UnSpecified())
            _addToWishList.emit(NetworkResult.UnSpecified())
            _removeFromWishList.emit(NetworkResult.UnSpecified())
            _wishlistStatus.emit(null)
            _fetchReviews.emit(NetworkResult.UnSpecified())
        }

        viewModelScope.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        clear()
    }
}