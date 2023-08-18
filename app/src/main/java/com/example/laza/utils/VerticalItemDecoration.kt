package com.example.storein.utils

import android.graphics.Rect
import android.icu.util.CurrencyAmount
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// add some extra space between the items in the recycler view
class VerticalItemDecoration(private val amount: Int = 30): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = amount
    }
}