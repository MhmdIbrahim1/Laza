package com.example.laza.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val addressTitle: String,
    val fullName: String,
    val street: String,
    val city: String,
    val phone: String,
    val state: String,
    val documentId: String = ""
):Parcelable {
    constructor():this("","","","","","")
}