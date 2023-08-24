package com.example.laza.utils

import android.view.View
import androidx.fragment.app.Fragment
import com.example.laza.R
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.HideBottomNavigation() {
    val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
    bottomNavigationView?.visibility = View.GONE
}

fun Fragment.ShowBottomNavigation() {
    val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
    bottomNavigationView?.visibility = View.VISIBLE
}