package com.example.laza.helper

import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import com.example.laza.R

fun Float?.getProductPrice(price: Float): Float {
    if (this == null)
        return price
    return price - (price * this / 100)
}

fun Float?.formatPrice(): String{
    if (this == null)
        return "0.0"
    return String.format("%.2f", this)
}

fun setGArrowImageBasedOnLayoutDirection(resources: Resources, imageView: ImageView) {
    val layoutDirection = resources.configuration.layoutDirection
    val arrowDrawableResId = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
        R.drawable.g_arrow_right
    } else {
        R.drawable.g_arrow_left
    }
    imageView.setImageResource(arrowDrawableResId)
}

fun setSubArrowImageBasedOnLayoutDirection(resources: Resources, imageView: ImageView) {
    val layoutDirection = resources.configuration.layoutDirection
    val arrowDrawableResId = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
        R.drawable.sub_left_arrow
    } else {
        R.drawable.sub_right_arrow
    }
    imageView.setImageResource(arrowDrawableResId)
}



