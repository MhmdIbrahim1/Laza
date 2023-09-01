package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.Address
import com.example.laza.data.Reviews
import com.example.laza.utils.Constants.PRODUCT_COLLECTION
import com.example.laza.utils.Constants.REVIEWS_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddReviewsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
):ViewModel() {

    private val _addNewReview =
        MutableStateFlow<NetworkResult<Reviews>>(NetworkResult.UnSpecified())
    val addNewReview = _addNewReview.asStateFlow()

    fun addReview(productId: String, review: Reviews) {
        viewModelScope.launch {
            _addNewReview.emit(NetworkResult.Loading())

            val reviewsCollection = firestore.collection(PRODUCT_COLLECTION)
                .document(productId)
                .collection(REVIEWS_COLLECTION)

            val newDocRef = reviewsCollection.document()
            newDocRef.set(review.copy(documentId = newDocRef.id))
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _addNewReview.emit(NetworkResult.Success(review))
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _addNewReview.emit(NetworkResult.Error(it.message.toString()))
                    }
                }
        }
    }
}