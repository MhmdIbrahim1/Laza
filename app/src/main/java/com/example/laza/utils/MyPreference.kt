package com.example.laza.utils

import android.content.Context

val PREFERENCE_NAME = "SharedPreferenceExample"
val PREFERENCE_LANGUAGE = "Language"

class MyPreference(context : Context){


    val preference = context.getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE)

    fun getLanguage() : String{
        return preference.getString(PREFERENCE_LANGUAGE,"en").toString()
    }

    fun setLanguage(Language:String){
        val editor = preference.edit()
        editor.putString(PREFERENCE_LANGUAGE,Language)
        editor.apply()
    }

}