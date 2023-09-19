package com.example.laza

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize SharedPreferences
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        val isDarkMode = sharedPreferences.getBoolean("isDark", false)
        // Set the theme based on the stored setting
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
