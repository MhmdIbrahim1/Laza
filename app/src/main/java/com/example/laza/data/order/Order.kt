package com.example.laza.data.order

import com.example.laza.data.Address
import com.example.laza.data.CartProduct
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import kotlin.random.Random.Default.nextLong

data class Order(
    val orderStatus: String,
    val totalPrice: Float,
    val products: List<CartProduct>,
    val address: Address,
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
    val orderId: Long = nextLong(0, 100_000_000) + totalPrice.toLong()
){
    constructor(): this("", 0f, listOf<CartProduct>(), Address(),"",0)
}
