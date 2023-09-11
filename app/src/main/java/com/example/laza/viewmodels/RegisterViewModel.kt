package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.data.User
import com.example.laza.utils.Constants.USER_COLLECTION
import com.example.laza.utils.NetworkResult
import com.example.laza.utils.RegisterFailedState
import com.example.laza.utils.RegisterValidation
import com.example.laza.utils.validateEmail
import com.example.laza.utils.validatePassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {
    private val _register =
        MutableStateFlow<NetworkResult<User>>(NetworkResult.UnSpecified())
    val register: Flow<NetworkResult<User>> = _register

    private val _validation = Channel<RegisterFailedState>()
    val validation = _validation.receiveAsFlow()
    fun createAccountWithEmailAndPassword(user: User, password: String) {
        if (checkValidation(user, password)) {
            runBlocking {
                _register.emit(NetworkResult.Loading())
            }
            firebaseAuth.createUserWithEmailAndPassword(user.emil, password)
                .addOnSuccessListener {firebaseUser ->
                    firebaseUser.user?.let {
                        saveUserInfo(it.uid,user)
                    }
                }
                .addOnFailureListener {
                    _register.value = NetworkResult.Error(it.message.toString())
                }
        }else{
            val registerValidation = RegisterFailedState(
                validateEmail(user.emil),
                validatePassword(password)
            )
            viewModelScope.launch {
                _validation.send(registerValidation)
            }
        }
    }

    private fun saveUserInfo(userUid: String,user: User) {
        db.collection(USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                _register.value = NetworkResult.Success(user)
                sendConfirmationEmail()
            }
            .addOnFailureListener {
                _register.value = NetworkResult.Error(it.message.toString())
            }
    }

    private fun checkValidation(user: User, password: String): Boolean {
        val emailValidation = validateEmail(user.emil)
        val passwordValidation = validatePassword(password)
        return emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success
    }
    private fun sendConfirmationEmail() {
        val user = firebaseAuth.currentUser
        user?.let {
            it.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            _register.emit(NetworkResult.Success(User()))
                        }
                    } else {
                        viewModelScope.launch {
                            _register.emit(NetworkResult.Error(task.exception?.message.toString()))
                        }
                    }
                }
        }
    }


}