package com.example.laza.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.laza.data.Product
import com.example.laza.firebase.FirebaseCommon
import com.example.laza.utils.NetworkResult
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseCommon: FirebaseCommon
) : ViewModel() {

    val search = MutableLiveData<NetworkResult<List<Product>>>()


    fun searchProducts(searchQuery: String) {
        search.postValue(NetworkResult.Loading())
        searchProduct(searchQuery).addOnCompleteListener {
            if (it.isSuccessful) {
                val productsList = it.result!!.toObjects(Product::class.java)
                search.postValue(NetworkResult.Success(productsList))

            } else
                search.postValue(NetworkResult.Error(it.exception.toString()))

        }
    }

    private fun searchProduct(searchQuery: String): Task<QuerySnapshot> {
        val normalizedQuery = searchQuery.toLowerCase(Locale.ROOT)
        Log.d("testSearch", "Searching for: $normalizedQuery")
        return Firebase.firestore.collection("products")
            .orderBy("lowercaseName")
            .startAt(normalizedQuery)
            .endAt("${normalizedQuery}\uf8ff")
            .get()
    }
}
