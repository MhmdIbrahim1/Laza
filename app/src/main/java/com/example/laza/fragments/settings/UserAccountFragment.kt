package com.example.laza.fragments.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.laza.R
import com.example.laza.data.User
import com.example.laza.databinding.FragmentUserAccountBinding
import com.example.laza.dialog.setUpBottomSheetDialog
import com.example.laza.utils.HideBottomNavigation
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class UserAccountFragment : Fragment() {
    private var _binding: FragmentUserAccountBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by activityViewModels<UserAccountViewModel>()
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>

    private var imageUri: Uri? = null

    // Coroutine jobs for observing data
    private var updateUserInfoJob: Job? = null
    private var getUserInfoJob: Job? = null
    private var resetPasswordJob: Job? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                imageUri = it.data?.data
                Glide.with(this).load(imageUri).into(binding.imageUser)
            }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeGetUser()
        observeUpdateUser()
        observeResetPassword()


        binding.imageEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imageActivityResultLauncher.launch(intent)
        }


        binding.buttonSave.setOnClickListener {
            binding.apply {
                val firstName = edFirstName.text.toString().trim()
                val lastName = edLastName.text.toString().trim()
                val email = edEmail.text.toString().trim()
                val user = User(firstName, lastName, email)
                userViewModel.updateUser(user, imageUri)
            }
        }

        binding.tvUpdatePassword.setOnClickListener {
            setUpBottomSheetDialog {
                userViewModel.resetPassword(it)
            }
        }

        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }

        //disable the email field from editing
        binding.edEmail.isEnabled = false



    }

    private fun observeUpdateUser() {
        updateUserInfoJob?.cancel()
        updateUserInfoJob = lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.updateInfo.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            binding.buttonSave.startAnimation()
                        }

                        is NetworkResult.Success -> {
                            binding.buttonSave.revertAnimation()
                            findNavController().navigateUp()
                            Toast.makeText(requireContext(), "User Information Updated", Toast.LENGTH_SHORT).show()
                        }

                        is NetworkResult.Error -> {
                            binding.buttonSave.revertAnimation()
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }


    private fun observeGetUser() {
        getUserInfoJob?.cancel()
        getUserInfoJob = lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.user.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            showUserLoading()
                        }

                        is NetworkResult.Success -> {
                            hideUserLoading()
                            showUserInformation(it.data!!)
                            Log.d("UserAccountFragment", it.data.toString())
                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }



    private fun observeResetPassword() {
        resetPasswordJob?.cancel()
        resetPasswordJob = lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.resetPassword.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
                            //todo
                        }

                        is NetworkResult.Success -> {
                            Toast.makeText(requireContext(), "Password reset email sent", Toast.LENGTH_SHORT).show()
                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun hideUserLoading() {
        binding.apply {
            progressbarAccount.visibility = View.GONE
            imageUser.visibility = View.VISIBLE
            imageEdit.visibility = View.VISIBLE
            edFirstName.visibility = View.VISIBLE
            edLastName.visibility = View.VISIBLE
            edEmail.visibility = View.VISIBLE
            tvUpdatePassword.visibility = View.VISIBLE
            buttonSave.visibility = View.VISIBLE
        }
    }

    private fun showUserLoading() {
        binding.apply {
            progressbarAccount.visibility = View.VISIBLE
            imageUser.visibility = View.INVISIBLE
            imageEdit.visibility = View.INVISIBLE
            edFirstName.visibility = View.INVISIBLE
            edLastName.visibility = View.INVISIBLE
            edEmail.visibility = View.INVISIBLE
            tvUpdatePassword.visibility = View.INVISIBLE
            buttonSave.visibility = View.INVISIBLE
        }
    }


    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@UserAccountFragment).load(data.imagePath)
                .error(ResourcesCompat.getDrawable(resources, R.drawable.profile, null))
                .into(imageUser)
            edFirstName.setText(data.firstName)
            edLastName.setText(data.lastName)
            edEmail.setText(data.email)
            Log.d("UserAccountFragment", data.toString())
        }
    }


    override fun onResume() {
        super.onResume()
        HideBottomNavigation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Clear the updateInfo StateFlow when the view is destroyed
        userViewModel.clearUpdateInfo()


    }
}