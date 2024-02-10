package com.example.laza.activites

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import com.example.laza.R
import com.example.laza.utils.MyContextWrapper
import com.example.laza.utils.MyPreference

class StartedScreen : AppCompatActivity() {
    private lateinit var myPreference: MyPreference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false) // Default to false if not found

        // Set up the theme based on the stored setting
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_splash_screen)
        changeStatusBarColor()
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(Intent(this@StartedScreen, LoginRegisterActivity::class.java))
            finish()
        }, 2000)
    }

    private fun changeStatusBarColor() {
        window.statusBarColor = resources.getColor(R.color.status_bar, null)
    }

    override fun attachBaseContext(newBase: Context?) {
        myPreference = MyPreference(newBase!!)
        val lang = myPreference.getLanguage()
        super.attachBaseContext(MyContextWrapper.wrap(newBase,lang))
    }
}
