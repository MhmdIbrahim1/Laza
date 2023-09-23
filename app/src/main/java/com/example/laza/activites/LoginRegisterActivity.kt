package com.example.laza.activites

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.example.laza.R
import com.example.laza.network.NetworkManager
import com.example.laza.utils.MyContextWrapper
import com.example.laza.utils.MyPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
open class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var myPreference: MyPreference
    private lateinit var sharedPreferences: SharedPreferences


    private lateinit var dialog: AlertDialog
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


        setContentView(R.layout.activity_login_register)
        showNoInternetDialog()
        changeStatusBarColor()
    }

    private fun changeStatusBarColor(){
        // if theme is dark change status bar color to black
        if (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            window.statusBarColor = resources.getColor(R.color.purple_500)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        myPreference = MyPreference(newBase!!)
        val lang = myPreference.getLanguage()
        super.attachBaseContext(MyContextWrapper.wrap(newBase,lang))
    }

    private fun showNoInternetDialog() {
        dialog = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
            .setView(R.layout.no_internet_connection_dialog)
            .setCancelable(false)
            .create()

        val btnRetry = dialog.findViewById<Button>(R.id.no_internet_connection_button)
        val networkManager = NetworkManager(this)
        networkManager.observe(this) {
            if (it) {
                dialog.dismiss()
            } else {
                dialog.show()
            }
        }
        btnRetry?.setOnClickListener {
            networkManager.observe(this) {
                if (it) {
                    dialog.dismiss()
                } else {
                    dialog.show()
                }
            }
        }

    }
}