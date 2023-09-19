package com.example.laza.activites

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.LoginViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.launch

class TwitterActivity : LoginRegisterActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    //    private val firestore = FirebaseFirestore.getInstance()
    private val viewModel by viewModels<LoginViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val provider: OAuthProvider.Builder = OAuthProvider.newBuilder("twitter.com")
        provider.addCustomParameter("lang", "en")
        val pendingResultTask: Task<AuthResult>? = auth.pendingAuthResult
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                .addOnSuccessListener { authResult: AuthResult? ->
                    // Handle the successful Twitter authentication result
                    viewModel.signInWithTwitter(authResultTask = authResult)
                }
                .addOnFailureListener { e: Exception? ->
                    Toast.makeText(this, "Failed ${e?.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // Start Twitter authentication
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener { authResult: AuthResult? ->
                    // Handle the successful Twitter authentication result
                    viewModel.signInWithTwitter(authResultTask = authResult)
                }
                .addOnFailureListener { e: Exception? ->
                    Toast.makeText(this, "Failed ${e?.message}", Toast.LENGTH_LONG).show()
                }
        }
        observeLogin()
    }

    private fun observeLogin() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.login.collect {
                    when (it) {
                        is NetworkResult.Loading -> {
                        }

                        is NetworkResult.Success -> {
                            Intent(
                                this@TwitterActivity,
                                ShoppingActivity::class.java
                            ).also { intent ->
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                            Toast.makeText(
                                this@TwitterActivity,
                                "Login Success",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(this@TwitterActivity, it.message, Toast.LENGTH_LONG)
                                .show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}