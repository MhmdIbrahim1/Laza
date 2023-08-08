package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel  @Inject constructor(
    private val firebaseAuth: FirebaseAuth
): ViewModel(){

    // using shared flow to send one time event
    private val _login = MutableSharedFlow<NetworkResult<FirebaseUser>>()
    val login = _login.asSharedFlow()

    fun login(email: String, password: String){
        viewModelScope.launch {
            _login.emit(NetworkResult.Loading())
        }
        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                viewModelScope.launch {
                   it.user?.let {
                       _login.emit(NetworkResult.Success(it))
                   }
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _login.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }
}