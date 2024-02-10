package com.example.laza.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.laza.R

class TestFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.test, rootKey)


    }
}