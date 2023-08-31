package com.example.laza.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartProduct(
    val product: Product,
    val quantity: Int,
    val selectedColor: Double? = null,
    val selectedSize: String? = null
):Parcelable{
    constructor(): this(Product(), 1, null, null)
}