package com.example.laza

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize SharedPreferences
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // Check if the app is being launched for the first time
        val isFirstLaunch = !sharedPreferences.contains("isFirstLaunch")

        if (isFirstLaunch) {
            // Detect the current system theme (light or dark)
            val systemNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isSystemDarkMode = systemNightMode == Configuration.UI_MODE_NIGHT_YES

            // Set the initial theme based on the system theme
            sharedPreferences.edit()
                .putBoolean("isDarkMode", isSystemDarkMode)
                .putBoolean("isFirstLaunch", true) // Mark as not the first launch
                .apply()
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}

