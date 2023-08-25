package com.example.laza.data

data class CartProduct(
    val product: Product,
    val quantity: Int,
    val selectedColor: Double? = null,
    val selectedSize: String? = null
){
    constructor(): this(Product(), 1, null, null)
}