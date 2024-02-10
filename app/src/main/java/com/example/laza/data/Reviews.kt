package com.example.laza.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
data class Reviews(
    val name: String,
    val review: String,
    val ratingStars: Double,
    val image: String,
    val documentId: String = "",
    val productId: String = "",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
    ): Parcelable {
    constructor():this("","",0.0,"","")
}