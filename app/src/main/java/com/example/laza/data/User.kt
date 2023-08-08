package com.example.laza.data

data class User(
    val firstName: String,
    val lastName: String,
    val emil: String,
    var imagePath: String =""
){
    constructor():this("","","","")
}
