package com.example.laza.activites

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.laza.R
import com.example.laza.databinding.ActivityShoppingBinding
import com.example.laza.fragments.shopping.HomeFragment
import com.example.laza.fragments.shopping.HomeFragmentDirections
import com.example.laza.utils.MyContextWrapper
import com.example.laza.utils.MyPreference
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.CartViewModel
import com.example.laza.viewmodels.UserAccountViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity(), HomeFragment.DrawerOpener {
    private val binding by lazy { ActivityShoppingBinding.inflate(layoutInflater) }
    private lateinit var navController: NavController

    val viewModel by viewModels<CartViewModel>()
    private val userViewModel by viewModels<UserAccountViewModel>()

    private lateinit var sharedPreferences: SharedPreferences
    private val isDark = false
    private var doubleBackToExitPressedOnce = false
    private val doublePressHandler = Handler(Looper.myLooper()!!)
    private var shouldHandleBackPress = true

    lateinit var myPreference: MyPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set up the theme of the app based on the user's choice (light or dark)
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDark", isDark)

        setContentView(binding.root)
        onLogout()
        changeStatusBarColor()
        setNavigationItemSelectedListener()
        observeGetUser()

        // observe the cart products numbers
        observeCartProductNumbers()

        // set up the navigation controller
        navController = findNavController(R.id.shoppingHostFragment)
        binding.bottomNavigation.setupWithNavController(navController)

        // Initialize the switch state based on the saved value
        val switchTheme = binding.navView.findViewById<SwitchCompat>(R.id.switch_theme)
        switchTheme.isChecked = isDarkMode

        // Set up the switch listener
        switchTheme.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            // Save the switch state when it changes
            val editor = sharedPreferences.edit()
            editor.putBoolean("isDark", isChecked)
            editor.apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                delegate.applyDayNight()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                delegate.applyDayNight()
            }

            val intent = Intent(this, ShoppingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        binding.navView.getHeaderView(0).setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToUserAccountFragment()
            navController.navigate(action)
            // Close the drawer after navigating
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        onBackPressedDispatcher.addCallback {
            if (shouldHandleBackPress) {
                if (doubleBackToExitPressedOnce) {
                    finish()
                } else {
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(
                        this@ShoppingActivity,
                        R.string.press_back_again_to_exit,
                        Toast.LENGTH_SHORT
                    ).show()

                    doublePressHandler.postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 4000) // Delay for resetting the double press flag
                }
            } else {
                // If not handling the back press, let the activity handle it
                isEnabled = false
                onBackPressed()
                isEnabled = true
            }
        }
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

    }

    // Implementation of the DrawerOpener interface
    override fun openDrawer() {
        val drawerLayout = binding.drawerLayout

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer
        } else {
            drawerLayout.openDrawer(GravityCompat.START) // Open the drawer
        }
    }


    private fun onLogout() {
        binding.navView.findViewById<TextView>(R.id.logout).setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }


    private fun showLogoutConfirmationDialog() {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.logout_confirmation_dialog, null)
        val dialog =
            AlertDialog.Builder(this, R.style.AlertDialogCustom) // Use the custom style here
                .setView(dialogView)
                .create()

        val btnCancel = dialogView.findViewById<Button>(R.id.confirm_no_logout_button)
        val btnConfirm = dialogView.findViewById<Button>(R.id.confirm_yes_logout_button)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        dialog.show()
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
    }

    private fun changeStatusBarColor() {
        window.statusBarColor = resources.getColor(R.color.status_bar, null)
    }

    private fun observeCartProductNumbers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartProduct.collectLatest {
                    when (it) {
                        is NetworkResult.Success -> {
                            val count = it.data?.size ?: 0
                            val bottomNavigation = binding.bottomNavigation
                            bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
                                number = count
                                backgroundColor = resources.getColor(R.color.badge_coloe, null)
                                badgeTextColor = resources.getColor(R.color.white, null)
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeGetUser() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.user.collectLatest {
                    when (it) {
                        is NetworkResult.Loading -> {
//                            showUserLoading()
                        }

                        is NetworkResult.Success -> {
                            val headerView = binding.navView.getHeaderView(0)
                            val userName = headerView.findViewById<TextView>(R.id.tvUserName)
                            val userImage =
                                headerView.findViewById<ImageView>(R.id.profileUserImageReview)
                            userName.text = it.data?.firstName + " " + it.data?.lastName
                            Glide.with(this@ShoppingActivity).load(it.data?.imagePath)
                                .error(
                                    ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.profile,
                                        null
                                    )
                                )
                                .into(userImage)
                            Log.d("UserAccountFragment", it.data.toString())
                        }

                        is NetworkResult.Error -> {
                            Log.d("UserAccountFragment", it.message.toString())
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setNavigationItemSelectedListener() {
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_orders -> {
                    // navigate to the all orders fragment
                    val action = HomeFragmentDirections.actionHomeFragmentToAllOrdersFragment()
                    navController.navigate(action)
                }

                R.id.nav_settings -> {
                    // navigate to the settings fragment
                    val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
                    navController.navigate(action)
                }

                R.id.nav_info -> {
                    // navigate to the user account fragment
                    val action = HomeFragmentDirections.actionHomeFragmentToUserAccountFragment()
                    navController.navigate(action)
                }

                R.id.nav_billing -> {
                    navigateToBilling()
                }

            }
            // Close the drawer after navigating
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

    }

    private fun navigateToBilling() {
        when (navController.currentDestination?.id) {
            R.id.homeFragment -> {
                val action = HomeFragmentDirections.actionHomeFragmentToBillingFragment(
                    totalPrice = 0.0f, // Replace with the actual total price value
                    products = emptyArray(), // Replace with the actual products array
                    payment = false // Replace with the actual payment value
                )
                navController.navigate(action)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.bottomNavigation.setOnItemSelectedListener(null)
    }

    override fun attachBaseContext(newBase: Context?) {
        myPreference = MyPreference(newBase!!)
        val lang = myPreference.getLanguage()
        super.attachBaseContext(MyContextWrapper.wrap(newBase,lang))
    }
}
