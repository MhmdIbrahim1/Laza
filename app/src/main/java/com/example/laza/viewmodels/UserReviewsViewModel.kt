package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.Reviews
import com.example.laza.utils.Constants
import com.example.laza.utils.Constants.PRODUCT_COLLECTION
import com.example.laza.utils.Constants.REVIEWS_COLLECTION
import com.example.laza.utils.Constants.USER_COLLECTION
import com.example.laza.utils.Constants.USER_REVIEWS_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserReviewsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _fetchUserReviews =
        MutableStateFlow<NetworkResult<List<Reviews>>>(NetworkResult.UnSpecified())
    val fetchUserReviews = _fetchUserReviews.asStateFlow()

    private val _removeUserReview =
        MutableStateFlow<NetworkResult<Reviews>>(NetworkResult.UnSpecified())
    val removeUserReview = _removeUserReview.asStateFlow()
    init {
        fetchUserReviews()
    }

    private fun fetchUserReviews() {
        viewModelScope.launch { _fetchUserReviews.emit(NetworkResult.Loading()) }

        // Reference to the user's reviews collection
        val userReviewsCollection = firestore.collection(USER_COLLECTION)
            .document(auth.uid!!)
            .collection(USER_REVIEWS_COLLECTION)

        userReviewsCollection.get()
            .addOnSuccessListener {
                val reviews = it.toObjects(Reviews::class.java)
                viewModelScope.launch {
                    _fetchUserReviews.emit(NetworkResult.Success(reviews))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _fetchUserReviews.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

    fun removeUserReview(productId: String,  review: Reviews) {
        viewModelScope.launch { _removeUserReview.emit(NetworkResult.Loading()) }

        // Reference to the user's reviews collection
        firestore.collection(USER_COLLECTION)
            .document(auth.uid!!)
            .collection(USER_REVIEWS_COLLECTION)
            .get().addOnSuccessListener { snapshot ->
                snapshot.documents.forEach {
                    it.reference.delete()
                }
            }.addOnSuccessListener {
                viewModelScope.launch {
                    _removeUserReview.emit(NetworkResult.Success(review))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _removeUserReview.emit(NetworkResult.Error(it.message.toString()))
                }
            }

        // Reference to the product's reviews collection
         firestore.collection(PRODUCT_COLLECTION)
            .document(productId)
            .collection(REVIEWS_COLLECTION)
            .get().addOnSuccessListener { snapshot ->
                snapshot.documents.forEach {
                    it.reference.delete()
                }
            }.addOnSuccessListener {
                viewModelScope.launch {
                    _removeUserReview.emit(NetworkResult.Success(review))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _removeUserReview.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

}