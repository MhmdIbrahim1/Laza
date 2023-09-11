package com.example.laza.activites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.laza.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
open class LoginRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login_register)
        changeStatusBarColor()
    }

    private fun changeStatusBarColor(){
        window.statusBarColor = resources.getColor(R.color.status_bar, null)
    }
}