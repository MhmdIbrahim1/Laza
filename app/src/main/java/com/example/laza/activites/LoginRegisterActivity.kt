package com.example.laza.activites

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.laza.R
import com.example.laza.utils.MyContextWrapper
import com.example.laza.utils.MyPreference
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
open class LoginRegisterActivity : AppCompatActivity() {

    lateinit var myPreference: MyPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_login_register)
        changeStatusBarColor()
    }

    private fun changeStatusBarColor(){
        window.statusBarColor = resources.getColor(R.color.status_bar, null)
    }

    override fun attachBaseContext(newBase: Context?) {
        myPreference = MyPreference(newBase!!)
        val lang = myPreference.getLanguage()
        super.attachBaseContext(MyContextWrapper.wrap(newBase,lang))
    }
}