package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.Reviews
import com.example.laza.utils.Constants
import com.example.laza.utils.NetworkResult
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val _fetchReviews =
        MutableStateFlow<NetworkResult<List<Reviews>>>(NetworkResult.UnSpecified())
    val fetchReviews = _fetchReviews.asStateFlow()

    private val _totalReviewsCount = MutableStateFlow(0)
    val totalReviewsCount = _totalReviewsCount.asStateFlow()

    private val _totalRating = MutableStateFlow(0f)
    val totalRating = _totalRating.asStateFlow()

    // Keep track of whether listeners are active to avoid adding duplicate listeners
    private var reviewsListenerActive = false

    fun fetchReviews(productId: String) {
        viewModelScope.launch {
            _fetchReviews.emit(NetworkResult.Loading())
        }
        firestore.collection(Constants.PRODUCT_COLLECTION)
            .document(productId)
            .collection(Constants.REVIEWS_COLLECTION)
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
    fun fetchTotalReviewsCount(productId: String) {
        val reviewsRef = firestore.collection(Constants.PRODUCT_COLLECTION)
            .document(productId)
            .collection(Constants.REVIEWS_COLLECTION)

        // Listen for changes in the reviews collection
        reviewsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                viewModelScope.launch {
                    _fetchReviews.emit(NetworkResult.Error(error.message))
                }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                viewModelScope.launch {
                    val totalReviewsCount = snapshot.size()
                    _totalReviewsCount.emit(totalReviewsCount)
                }
            }
        }
    }

    fun fetchTotalRating(productId: String) {
        val reviewsRef = firestore.collection(Constants.PRODUCT_COLLECTION)
            .document(productId)
            .collection(Constants.REVIEWS_COLLECTION)

        // Listen for changes in the reviews collection
        reviewsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                viewModelScope.launch {
                    _fetchReviews.emit(NetworkResult.Error(error.message))
                }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                viewModelScope.launch {
                    // Calculate the average rating
                    val totalRating = snapshot.sumOf { it.getDouble("ratingStars") ?: 0.0 }.toFloat()
                    val averageRating = if (snapshot.size() > 0) totalRating / snapshot.size() else 0f
                    _totalRating.emit(averageRating)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove listeners when the ViewModel is cleared
        reviewsListenerActive = false
        clear()
    }

    private fun clear() {
        viewModelScope.launch {
            _fetchReviews.emit(NetworkResult.UnSpecified())
            _totalReviewsCount.emit(0)
            _totalRating.emit(0f)
        }
    }
}
