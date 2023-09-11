package com.example.laza.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.laza.R

class StartedScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_splash_screen)

        changeStatusBarColor()
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(Intent(this@StartedScreen, LoginRegisterActivity::class.java))
            finish()
        }, 3000)
    }

    private fun changeStatusBarColor() {
        window.statusBarColor = resources.getColor(R.color.status_bar, null)
    }
}
