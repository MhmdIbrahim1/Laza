package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.CartProduct
import com.example.laza.firebase.FirebaseCommon
import com.example.laza.utils.Constants.CART_COLLECTION
import com.example.laza.utils.Constants.USER_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) : ViewModel() {

    private val _cartProducts =
        MutableStateFlow<NetworkResult<List<CartProduct>>>(NetworkResult.UnSpecified())
    val cartProduct = _cartProducts.asStateFlow()

    private val _deleteDialog = Channel <CartProduct>(Channel.CONFLATED)
    val deleteDialog = _deleteDialog.receiveAsFlow()

    private var cartProductDocumented = emptyList<DocumentSnapshot>()


    init {
        getCardProducts()
    }

    private fun getCardProducts() {
        viewModelScope.launch {
            _cartProducts.emit(NetworkResult.Loading())
        }
        firestore.collection(USER_COLLECTION).document(auth.uid!!).collection(CART_COLLECTION)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) {
                    viewModelScope.launch {
                        _cartProducts.emit(NetworkResult.Error(error?.message.toString()))
                    }
                } else {
                    cartProductDocumented = value.documents
                    val cartProducts = value.toObjects(CartProduct::class.java)
                    viewModelScope.launch {
                        _cartProducts.emit(NetworkResult.Success(cartProducts))
                    }
                }
            }
    }


    fun changeQuantity(
        cartProduct: CartProduct,
        quantityChanging: FirebaseCommon.QuantityChanging
    ){

        val index = _cartProducts.value.data?.indexOf(cartProduct) // return the index of cart product inside the cart product state

        /**
         * index could be equal to -1 if the fun [getCartProducts()] delays which will also delay the result we expect to be inside the [_cartProducts]
         * and to prevent the app from crashing we check if the index is not null and not equal to -1
         */
        if (index != null && index != -1) {

            val documentId = cartProductDocumented[index].id

            when(quantityChanging){
                FirebaseCommon.QuantityChanging.INCREASE -> {
                    increaseQuantity(documentId)
                }

                FirebaseCommon.QuantityChanging.DECREASE -> {
                    if (cartProduct.quantity == 1) {
                        viewModelScope.launch {
                            _deleteDialog.send(cartProduct)
                        }
                        return
                    }
                    viewModelScope.launch { _cartProducts.emit(NetworkResult.Loading()) }
                    decreaseQuantity(documentId)
                }
            }
        }
    }

    private fun increaseQuantity(documentId: String) {
        firebaseCommon.increaseQuantity(documentId) { _, e ->
            if (e != null)
                viewModelScope.launch {
                    _cartProducts.emit(NetworkResult.Error(e.message.toString()))
                }
        }
    }

    private fun decreaseQuantity(documentId: String) {
        firebaseCommon.decreaseQuantity(documentId) { _, e ->
            if (e != null)
                viewModelScope.launch {
                    _cartProducts.emit(NetworkResult.Error(e.message.toString()))
                }
        }
    }
}