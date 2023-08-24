package com.example.laza.helper

fun Float?.getProductPrice(price: Float): Float {
    //this --> Percentage
    if (this == null)
        return price
    return price - (price * this / 100)
}

fun Float?.formatPrice(): String{
    if (this == null)
        return "0.0"
    return String.format("%.2f", this)
}