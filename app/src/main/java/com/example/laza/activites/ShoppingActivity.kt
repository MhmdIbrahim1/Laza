package com.example.laza.activites

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
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
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.laza.R
import com.example.laza.databinding.ActivityShoppingBinding
import com.example.laza.fragments.shopping.HomeFragment
import com.example.laza.fragments.shopping.HomeFragmentDirections
import com.example.laza.network.NetworkManager
import com.example.laza.utils.MyContextWrapper
import com.example.laza.utils.MyPreference
import com.example.laza.utils.NetworkResult
import com.example.laza.utils.getGoogleSignInClient
import com.example.laza.viewmodels.CartViewModel
import com.example.laza.viewmodels.UserAccountViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    private lateinit var myPreference: MyPreference

    private lateinit var dialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false) // Default to false if not found

        // Set up the theme based on the stored setting
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(binding.root)
        onLogout()
        setNavigationItemSelectedListener()
        observeGetUser()
        showNoInternetDialog()

        // observe the cart products numbers
        observeCartProductNumbers()

        // set up the navigation controller
        navController = findNavController(R.id.shoppingHostFragment)
        binding.bottomNavigation.setupWithNavController(navController)

        // Set up the switch listener
        val switchTheme = binding.navView.findViewById<SwitchCompat>(R.id.switch_theme)
        switchTheme.isChecked = isDarkMode



        // Set up the switch listener
        switchTheme.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            // Save the switch state when it changes
            val editor = sharedPreferences.edit()
            editor.putBoolean("isDarkMode", isChecked)
            editor.apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                delegate.applyDayNight()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                delegate.applyDayNight()
            }

            // Restart the activity to apply the new theme
            val intent = Intent(this, ShoppingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
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

    private fun showNoInternetDialog() {
        dialog = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
            .setView(R.layout.no_internet_connection_dialog)
            .setCancelable(false)
            .create()

        val btnRetry = dialog.findViewById<Button>(R.id.no_internet_connection_button)
        val networkManager = NetworkManager(this)
        networkManager.observe(this) {
            if (it) {
                dialog.dismiss()
            } else {
                dialog.show()
            }
        }
        btnRetry?.setOnClickListener {
            networkManager.observe(this) {
                if (it) {
                    dialog.dismiss()
                } else {
                    dialog.show()
                }
            }
        }

    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()

        // google sign out
        val googleSignInClient = getGoogleSignInClient(this)
        googleSignInClient.signOut()


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
                    if (navController.currentDestination?.id == R.id.homeFragment) {
                        val action = HomeFragmentDirections.actionHomeFragmentToAllOrdersFragment()
                        navController.navigate(action)
                    }
                }

                R.id.nav_settings -> {
                    // navigate to the settings fragment
                    if (navController.currentDestination?.id == R.id.homeFragment) {
                        val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
                        navController.navigate(action)
                    }
                }

                R.id.nav_info -> {
                    // navigate to the user account fragment
                    if (navController.currentDestination?.id == R.id.homeFragment) {
                        val action = HomeFragmentDirections.actionHomeFragmentToUserAccountFragment()
                        navController.navigate(action)
                    }
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

    override fun onResume() {
        super.onResume()
            // status bar color if the theme is dark
        val systemNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = systemNightMode == Configuration.UI_MODE_NIGHT_YES
        if (isSystemDarkMode) {
            window.statusBarColor = resources.getColor(R.color.darker, null)
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


