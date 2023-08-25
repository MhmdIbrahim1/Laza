package com.example.laza.utils

import android.content.Context
import android.content.SharedPreferences

class WishlistIconManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "wishlist_icon_prefs",
        Context.MODE_PRIVATE
    )

    fun saveIconState(productId: String, isFavorite: Boolean) {
        sharedPreferences.edit().putBoolean(productId, isFavorite).apply()
    }

    fun getIconState(productId: String): Boolean {
        return sharedPreferences.getBoolean(productId, false)
    }
}
