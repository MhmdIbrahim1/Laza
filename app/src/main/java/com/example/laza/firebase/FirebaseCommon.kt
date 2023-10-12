package com.example.laza.firebase

import com.example.laza.data.CartProduct
import com.example.laza.data.WishlistProduct
import com.example.laza.utils.Constants.CART_COLLECTION
import com.example.laza.utils.Constants.PRODUCT_COLLECTION
import com.example.laza.utils.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FirebaseCommon(
    private val firestore: FirebaseFirestore,
    firebaseAuth: FirebaseAuth?
) {
    private val cartCollection = firebaseAuth?.uid?.let { uid ->
        firestore.collection(USER_COLLECTION)
            .document(uid)
            .collection(CART_COLLECTION)
    }

    private val wishlistCollection = firebaseAuth?.uid?.let { uid ->
        firestore.collection(USER_COLLECTION)
            .document(uid)
            .collection("wishlist")
    }

    private val productsCollection = Firebase.firestore.collection(PRODUCT_COLLECTION)


    fun addProductToCart(cartProduct: CartProduct, onResult: (CartProduct?, Exception?) -> Unit) {
        if (cartCollection != null) {
            cartCollection.document()
                .set(cartProduct)
                .addOnSuccessListener {
                    onResult(cartProduct, null)
                }
                .addOnFailureListener {
                    onResult(null, it)
                }
        }else {
            // Handle the case where cartCollection is null (e.g., user not authenticated)
            onResult(null, Exception("Check Network Connection"))
        }
    }


    fun addProductToWishList(wishlistProduct: WishlistProduct, onResult: (WishlistProduct?, Exception?) -> Unit) {
        if (wishlistCollection != null) {
            wishlistCollection.document(wishlistProduct.product.id)
                .set(wishlistProduct)
                .addOnSuccessListener {
                    onResult(wishlistProduct, null)
                }
                .addOnFailureListener {
                    onResult(null, it)
                }
        } else {
            // Handle the case where wishlistCollection is null (e.g., user not authenticated)
            onResult(null, Exception("Check Network Connection"))
        }
    }

    fun removeProductFromWishList(wishlistProduct: WishlistProduct, onResult: (WishlistProduct?, Exception?) -> Unit) {
        if (wishlistCollection != null) {
            wishlistCollection.document(wishlistProduct.product.id)
                .delete()
                .addOnSuccessListener {
                    onResult(wishlistProduct, null)
                }
                .addOnFailureListener {
                    onResult(null, it)
                }
        } else {
            // Handle the case where wishlistCollection is null (e.g., user not authenticated)
            onResult(null, Exception("Check Network Connection"))
        }
    }

    fun increaseQuantity(documentId: String, onResult: (String?, Exception?) -> Unit) {
        firestore.runTransaction { transition ->
            val documentRef = cartCollection!!.document(documentId)
            val document = transition.get(documentRef)
            val productObject = document.toObject(CartProduct::class.java)
            productObject?.let { cartProduct ->
                val newQuantity = cartProduct.quantity + 1
                val newProductObject = cartProduct.copy(quantity = newQuantity)
                transition.set(documentRef, newProductObject)
            }
        }.addOnSuccessListener {
            onResult(documentId, null)
        }.addOnFailureListener {
            onResult(null, it)
        }
    }

    fun decreaseQuantity(documentId: String, onResult: (String?, Exception?) -> Unit) {
        firestore.runTransaction { transition ->
            val documentRef = cartCollection!!.document(documentId)
            val document = transition.get(documentRef)
            val productObject = document.toObject(CartProduct::class.java)
            productObject?.let { cartProduct ->
                val newQuantity = cartProduct.quantity - 1
                val newProductObject = cartProduct.copy(quantity = newQuantity)
                transition.set(documentRef, newProductObject)
            }
        }.addOnSuccessListener {
            onResult(documentId, null)
        }.addOnFailureListener {
            onResult(null, it)
        }
    }

    fun searchProducts(searchQuery: String) = productsCollection
        .orderBy("title")
        .startAt(searchQuery)
        .endAt("\u03A9+$searchQuery")
        .limit(5)
        .get()


    enum class QuantityChanging {
        INCREASE, DECREASE
    }

}