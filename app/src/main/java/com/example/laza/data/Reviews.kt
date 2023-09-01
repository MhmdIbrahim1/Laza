package com.example.laza.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reviews(
    val name: String,
    val review: String,
    val ratingStars: Double,
    val image: String,
    val documentId: String = ""
): Parcelable {
    constructor():this("","",0.0,"","")
}