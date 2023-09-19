package com.example.laza.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laza.MyApp
import com.example.laza.data.User
import com.example.laza.utils.Constants.USER_COLLECTION
import com.example.laza.utils.NetworkResult
import com.example.laza.utils.RegisterValidation
import com.example.laza.utils.validateEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: StorageReference,
    app: Application
) : AndroidViewModel(app) {

    private val _user = MutableStateFlow<NetworkResult<User>>(NetworkResult.UnSpecified())
    val user = _user.asStateFlow()

    private val _updateInfo = MutableStateFlow<NetworkResult<User>>(NetworkResult.UnSpecified())
    val updateInfo = _updateInfo.asStateFlow()

    private val _resetPassword = MutableStateFlow<NetworkResult<User>>(NetworkResult.UnSpecified())
    val resetPassword = _resetPassword.asStateFlow()


    init {
        getUser()
        //getUserRealTime()
    }

    private fun getUser(){
        viewModelScope.launch {
            _user.emit(NetworkResult.Loading())
        }
        firestore.collection(USER_COLLECTION).document(auth.uid!!)
            .addSnapshotListener { value, error ->
                if (error != null){
                    viewModelScope.launch {
                        _user.emit(NetworkResult.Error(error.message.toString()))
                    }
                }else{
                    val user = value?.toObject(User::class.java)
                    user?.let {
                        viewModelScope.launch {
                            _user.emit(NetworkResult.Success(user))
                        }
                    }
                }
            }
    }
    fun updateUser(user: User, imageUri: Uri?){
        val areInputsValid = user.email?.let { validateEmail(it) } is RegisterValidation.Success
                && (user.firstName?.trim()?.isNotEmpty() ?: false)
                && (user.lastName?.trim()?.isNotEmpty() ?: false)


        if (!areInputsValid) {
            viewModelScope.launch {
                _updateInfo.emit(NetworkResult.Error("Invalid inputs"))
            }
            return
        }

        viewModelScope.launch {
            _updateInfo.emit(NetworkResult.Loading())
        }


        if (imageUri == null) {
            saveUserInformation(user, true)
        } else {
            saveUserInformationWithNewImage(user, imageUri)
        }

    }

    private fun saveUserInformationWithNewImage(user: User, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val imageBitmap = MediaStore.Images.Media.getBitmap(
                    getApplication<MyApp>().contentResolver,
                    imageUri
                )
                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
                val imageByteArray = byteArrayOutputStream.toByteArray()
                val imageDirectory =
                    storage.child("profileImages/${auth.uid}/${UUID.randomUUID()}")
                val result = imageDirectory.putBytes(imageByteArray).await()
                val imageUrl = result.storage.downloadUrl.await().toString()
                saveUserInformation(user.copy(imagePath = imageUrl), false)
            } catch (e: Exception) {
                viewModelScope.launch {
                    _user.emit(NetworkResult.Error(e.message.toString()))
                }
            }
        }
    }

    private fun saveUserInformation(user: User, shouldRetrievedOldImage: Boolean) {
        firestore.runTransaction { transaction ->
            val docRef = firestore.collection(USER_COLLECTION).document(auth.uid!!)
            if (shouldRetrievedOldImage) {
                val currentUser = transaction.get(docRef).toObject(User::class.java)
                val newUser = user.copy(imagePath = currentUser?.imagePath ?: "")
                transaction.set(docRef, newUser)
            }else{
                transaction.set(docRef, user)
            }
        }.addOnSuccessListener {
            viewModelScope.launch {
                _updateInfo.emit(NetworkResult.Success(user))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _updateInfo.emit(NetworkResult.Error(it.message.toString()))
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPassword.emit(NetworkResult.Loading())
        }

        if (validateEmail(email) is RegisterValidation.Failed) {
            viewModelScope.launch {
                _resetPassword.emit(NetworkResult.Error("Invalid email"))
            }
            return
        }

        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            viewModelScope.launch {
                _resetPassword.emit(NetworkResult.Success(User()))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _resetPassword.emit(NetworkResult.Error(it.message.toString()))
            }
        }
    }



    private fun getUserRealTime() {
        val docRef = firestore.collection(USER_COLLECTION).document(auth.uid!!)
        docRef.addSnapshotListener { documentSnapshot, error ->
            if (error != null) {
                // Handle the error
                viewModelScope.launch {
                    _user.emit(NetworkResult.Error(error.message.toString()))
                }
                return@addSnapshotListener
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Document exists, parse it to User object and emit success
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    viewModelScope.launch {
                        _user.emit(NetworkResult.Success(it))
                    }
                }
            } else {
                // Document doesn't exist, emit an error
                viewModelScope.launch {
                    _user.emit(NetworkResult.Error("User document doesn't exist"))
                }
            }
        }
    }


    fun clearUpdateInfo() {
        _updateInfo.value = NetworkResult.UnSpecified()
    }

}