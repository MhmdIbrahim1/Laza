package com.example.laza.activites

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.example.laza.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
open class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val isDark = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set up the theme of the app based on the user's choice (light or dark)
        sharedPreferences = getSharedPreferences("isDark", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDark", isDark)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            delegate.applyDayNight()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            delegate.applyDayNight()
        }

        setContentView(R.layout.activity_login_register)
        changeStatusBarColor()
    }

    private fun changeStatusBarColor(){
        window.statusBarColor = resources.getColor(R.color.status_bar, null)
    }
}