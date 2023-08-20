package com.example.laza.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.R
import com.example.laza.utils.Constants.INTRODUCTION_KEY
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroductionViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _navigateState = MutableStateFlow(0)
    val navigateState = _navigateState.asStateFlow()

    companion object {
        const val SHOPPING_ACTIVITY = 23
        val ACCOUNT_GET_STARTED_FRAGMENT get() = R.id.action_introductionFragmentOne_to_getStartedFragment
    }

    init {
        val isButtonClicked = sharedPreferences.getBoolean(INTRODUCTION_KEY, false)
        val user = firebaseAuth.currentUser

        if(user != null){
            viewModelScope.launch {
                _navigateState.emit(SHOPPING_ACTIVITY)
            }
        }else if(isButtonClicked){
            viewModelScope.launch {
                _navigateState.emit(ACCOUNT_GET_STARTED_FRAGMENT)
            }
        }else{ Unit }
    }

    fun startButtonClicked() {
        sharedPreferences.edit().putBoolean(INTRODUCTION_KEY, true).apply()
    }
}