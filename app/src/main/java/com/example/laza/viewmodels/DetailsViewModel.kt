package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.CartProduct
import com.example.laza.firebase.FirebaseCommon
import com.example.laza.utils.Constants
import com.example.laza.utils.Constants.CART_COLLECTION
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
class DetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
): ViewModel(){

    private val _addToCart = MutableStateFlow<NetworkResult<CartProduct>>(NetworkResult.UnSpecified())
    val addToCart = _addToCart.asStateFlow()

    fun addUpdateProductInCart(cartProduct: CartProduct){
        viewModelScope.launch {
            _addToCart.emit(NetworkResult.Loading())
        }
        firestore.collection(USER_COLLECTION)
            .document(auth.uid!!)
            .collection(CART_COLLECTION)
            .whereEqualTo("product.id", cartProduct.product.id)
            .get()
            .addOnSuccessListener {
                it.documents.let {
                    if (it.isEmpty()){ // add new Product
                        addNewProduct(cartProduct)
                    }else{ // update existing product
                        val product  = it.first().toObject(CartProduct::class.java)
                        if (product == cartProduct){ // increase quantity
                            val documentId = it.first().id
                            increaseQuantity(documentId, cartProduct)
                        }else{ // aa new product
                            addNewProduct(cartProduct)
                        }
                    }
                }
            }
            .addOnFailureListener {
                _addToCart.value = NetworkResult.Error(it.message.toString())
            }
    }

    private fun addNewProduct(cartProduct: CartProduct){
        firebaseCommon.addProductToCart(cartProduct){addedProduct , e ->
            viewModelScope.launch {
                if (e == null){
                    _addToCart.emit(NetworkResult.Success(addedProduct!!))
                }else{
                    _addToCart.emit(NetworkResult.Error(e.message.toString()))
                }
            }
        }
    }

    private fun increaseQuantity(documentId: String,cartProduct: CartProduct){
        firebaseCommon.increaseQuantity(documentId){ _, e ->
            viewModelScope.launch {
                if (e == null){
                    _addToCart.emit(NetworkResult.Success(cartProduct))
                }else{
                    _addToCart.emit(NetworkResult.Error(e.message.toString()))
                }
            }
        }
    }
}