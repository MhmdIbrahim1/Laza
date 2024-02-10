package com.example.laza.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.laza.R
import kotlin.math.roundToInt

object Constants {
    const val USER_COLLECTION = "user"
    const val PRODUCT_COLLECTION = "products"
    const val CART_COLLECTION = "cart"
    const val ADDRESS_COLLECTION = "address"
    const val REVIEWS_COLLECTION = "reviews"
    const val ORDER_COLLECTION = "orders"
    const val USER_REVIEWS_COLLECTION = "userReviews"
    const val INTRODUCTION_KEY = "IntroductionKey"
}

object UiHelper{
    fun Activity?.navigate(@IdRes navigation: Int, arguments: Bundle? = null) {
        try {
            if (this is FragmentActivity) {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.shoppingHostFragment) as? NavHostFragment?
                navHostFragment?.navController?.navigate(navigation, arguments)
            }
        } catch (t: Throwable) {
//            logError(t)
        }
    }

    fun View.isLtr() = this.layoutDirection == View.LAYOUT_DIRECTION_LTR
    @ColorInt
    fun Context.getResourceColor(@AttrRes resource: Int, alphaFactor: Float = 1f): Int {
        val typedArray = obtainStyledAttributes(intArrayOf(resource))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()

        if (alphaFactor < 1f) {
            val alpha = (color.alpha * alphaFactor).roundToInt()
            return Color.argb(alpha, color.red, color.green, color.blue)
        }

        return color
    }

}