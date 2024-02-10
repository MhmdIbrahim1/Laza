package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.Reviews
import com.example.laza.utils.Constants
import com.example.laza.utils.NetworkResult
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
            try {
                val snapshot = withContext(Dispatchers.IO) {
                    firestore.collection(Constants.PRODUCT_COLLECTION)
                        .document(productId)
                        .collection(Constants.REVIEWS_COLLECTION)
                        .get()
                        .await()
                }

                val reviews = snapshot.toObjects(Reviews::class.java)
                _fetchReviews.emit(NetworkResult.Success(reviews))
            } catch (e: Exception) {
                _fetchReviews.emit(NetworkResult.Error(e.message.toString()))
            }
        }
    }

    fun fetchTotalReviewsCount(productId: String) {
        viewModelScope.launch {
            val reviewsRef = firestore.collection(Constants.PRODUCT_COLLECTION)
                .document(productId)
                .collection(Constants.REVIEWS_COLLECTION)

            try {
                val snapshot = withContext(Dispatchers.IO) {
                    reviewsRef.get().await()
                }
                val totalReviewsCount = snapshot.size()
                _totalReviewsCount.emit(totalReviewsCount)
            } catch (e: Exception) {
                _fetchReviews.emit(NetworkResult.Error(e.message.toString()))
            }
        }
    }

    fun fetchTotalRating(productId: String) {
        viewModelScope.launch {
            val reviewsRef = firestore.collection(Constants.PRODUCT_COLLECTION)
                .document(productId)
                .collection(Constants.REVIEWS_COLLECTION)

            try {
                val snapshot = withContext(Dispatchers.IO) {
                    reviewsRef.get().await()
                }

                // Calculate the average rating
                val totalRating = snapshot.sumOf { it.getDouble("ratingStars") ?: 0.0 }.toFloat()
                val averageRating = if (snapshot.size() > 0) totalRating / snapshot.size() else 0f
                _totalRating.emit(averageRating)
            } catch (e: Exception) {
                _fetchReviews.emit(NetworkResult.Error(e.message.toString()))
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
