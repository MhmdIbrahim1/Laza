package com.example.laza

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        // Set up the theme of the app based on the user's choice (light or dark)
        val sharedPreferences = getSharedPreferences("isDark", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDark", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        FirebaseApp.initializeApp(this)
    }
}