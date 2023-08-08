package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import com.example.laza.data.User
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
   private val firebaseAuth: FirebaseAuth
):ViewModel() {
    private val _register =
        MutableStateFlow<NetworkResult<FirebaseUser>>(NetworkResult.UnSpecified())
    val register:Flow<NetworkResult<FirebaseUser>> = _register

    fun createAccountWithEmailAndPassword(user: User, password: String){
        runBlocking {
            _register.emit(NetworkResult.Loading())
        }
        firebaseAuth.createUserWithEmailAndPassword(user.emil,password)
            .addOnSuccessListener {
                it.user?.let {
                    _register.value = NetworkResult.Success(it)
                }
            }
            .addOnFailureListener {
                _register.value = NetworkResult.Error(it.message.toString())
            }
    }
}