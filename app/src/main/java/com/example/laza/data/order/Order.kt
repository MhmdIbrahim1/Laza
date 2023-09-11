package com.example.laza.data.order

import com.example.laza.data.Address
import com.example.laza.data.CartProduct

data class Order(
    val orderStatus: String,
    val totalPrice: Float,
    val products: List<CartProduct>,
    val address: Address
){
    constructor(): this("", 0f, listOf<CartProduct>(), Address())
}
