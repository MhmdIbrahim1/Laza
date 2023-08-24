package com.example.laza.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val price: Float,
    val offerPercentage: Float? = null,
    val description: String? = null,
    val colors: List<Double>? = null,
    val sizes: List<String>? = null,
    val images: List<String>
):Parcelable{
    constructor(): this("", "", "", 0f, null, null, null, null, emptyList())
}