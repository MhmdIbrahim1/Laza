package com.example.laza.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.utils.Constants.USER_COLLECTION
import com.example.laza.utils.NetworkResult
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // Shared flows to send one-time events
    private val _login = MutableSharedFlow<NetworkResult<String>>()
    val login = _login.asSharedFlow()

    private val _resetPassword = MutableSharedFlow<NetworkResult<Boolean>>()
    val resetPassword = _resetPassword.asSharedFlow()

    private val firestore = FirebaseFirestore.getInstance()

    // Function for email/password login
    fun login(email: String, password: String) {
        // Check for empty fields
        if (email.isEmpty() || password.isEmpty()) {
            viewModelScope.launch {
                _login.emit(NetworkResult.Error("Email or password cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            _login.emit(NetworkResult.Loading())
        }

        // Perform login with email and password
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                viewModelScope.launch {
                    it.user?.let {
                        _login.emit(NetworkResult.Success("Login Success"))
                    }
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _login.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

    // Function for resetting password
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPassword.emit(NetworkResult.Loading())
        }

        // Send password reset email
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                viewModelScope.launch {
                    _resetPassword.emit(NetworkResult.Success(true))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _resetPassword.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

    // Function for signing in with Google
    fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                viewModelScope.launch {
                    authResult.user?.let { user ->
                        val userDocumentRef = firestore.collection(USER_COLLECTION).document(user.uid)

                        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                // User document already exists, update display name and photo URL
                                userDocumentRef.update(
                                    "displayName", user.displayName,
                                    "photoUrl", user.photoUrl.toString()
                                ).addOnSuccessListener {
                                    viewModelScope.launch { _login.emit(NetworkResult.Success("Login Success")) }
                                }.addOnFailureListener { error ->
                                    viewModelScope.launch {
                                        _login.emit(NetworkResult.Error("Failed to update user document: ${error.message}"))
                                    }
                                }
                            } else {
                                // User document doesn't exist, create a new one
                                val userData = hashMapOf(
                                    "uid" to user.uid,
                                    "email" to user.email,
                                    "displayName" to user.displayName,
                                    "photoUrl" to user.photoUrl.toString()
                                )

                                userDocumentRef.set(userData)
                                    .addOnSuccessListener {
                                        viewModelScope.launch { _login.emit(NetworkResult.Success("Login Success")) }

                                    }
                                    .addOnFailureListener { error ->
                                        viewModelScope.launch {
                                            _login.emit(NetworkResult.Error("Failed to create user document: ${error.message}"))
                                        }
                                    }
                            }
                        }.addOnFailureListener { error ->
                            viewModelScope.launch {
                                _login.emit(NetworkResult.Error("Failed to check user document: ${error.message}"))
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { error ->
                viewModelScope.launch {
                    _login.emit(NetworkResult.Error("Google sign-in failed: ${error.message}"))
                }
            }
    }



    // Function for signing in with Twitter
    fun signInWithTwitter(authResultTask: AuthResult?) {
        if (authResultTask != null && authResultTask.user != null) {
            val user = authResultTask.user
            // Handle user data in Firestore
            handleUserFirestoreData(user!!)
        }
    }

    // Function to handle user data in Firestore
    private fun handleUserFirestoreData(user: FirebaseUser) {
        val userDocumentRef = firestore.collection(USER_COLLECTION).document(user.uid)

        // Check if the user document already exists
        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // User document already exists, update display name and photo URL
                userDocumentRef.update(
                    "displayName", user.displayName,
                    "photoUrl", user.photoUrl.toString()
                ).addOnSuccessListener {
                    viewModelScope.launch { _login.emit(NetworkResult.Success("Login Success")) }
                }.addOnFailureListener { error ->
                    viewModelScope.launch {
                        _login.emit(NetworkResult.Error("Failed to update user document: ${error.message}"))
                    }
                }
            } else {
                // User document doesn't exist, create a new one
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl.toString()
                )

                userDocumentRef.set(userData)
                    .addOnSuccessListener {
                        viewModelScope.launch { _login.emit(NetworkResult.Success("Login Success")) }
                    }
                    .addOnFailureListener { error ->
                        viewModelScope.launch {
                            _login.emit(NetworkResult.Error("Failed to create user document: ${error.message}"))
                        }
                    }
            }
        }.addOnFailureListener { error ->
            viewModelScope.launch {
                _login.emit(NetworkResult.Error("Failed to check user document: ${error.message}"))
            }
        }
    }
}
