package com.example.laza.data

data class WishlistProduct(
    val product: Product,
    var isFavorite: Boolean,
){
    constructor(): this(Product(), false)
}
