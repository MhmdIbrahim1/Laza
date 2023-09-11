package com.example.laza.activites

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.laza.R
import com.example.laza.databinding.ActivityShoppingBinding
import com.example.laza.fragments.brands.BrandsFragmentDirections
import com.example.laza.fragments.shopping.CartFragmentDirections
import com.example.laza.fragments.shopping.HomeFragment
import com.example.laza.fragments.shopping.HomeFragmentDirections
import com.example.laza.fragments.shopping.ProductDetailsFragmentDirections
import com.example.laza.fragments.shopping.WishlistFragmentDirections
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity(), HomeFragment.DrawerOpener {
    private val binding by lazy { ActivityShoppingBinding.inflate(layoutInflater) }
    private lateinit var navController: NavController

    val viewModel  by viewModels<CartViewModel>()

    private lateinit var sharedPreferences: SharedPreferences
    private val isDark = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set up the theme of the app based on the user's choice (light or dark)
        sharedPreferences = getSharedPreferences("isDark", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDark", isDark)
        setContentView(binding.root)
        onLogout()
        changeStatusBarColor()
        bottomNavigationListener()
        setNavigationItemSelectedListener()

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

    }

    // Implementation of the DrawerOpener interface
    override fun openDrawer() {
        // Implement the logic to open the drawer here
        val drawerLayout = binding.drawerLayout
        drawerLayout.openDrawer(GravityCompat.START)
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

    private fun observeCartProductNumbers(){
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

    private fun bottomNavigationListener(){
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                }
                R.id.brandsFragment -> {
                    navController.navigate(R.id.brandsFragment)
                }
                R.id.wishlistFragment -> {
                    navController.navigate(R.id.wishlistFragment)
                }
                R.id.cartFragment -> {
                    navController.navigate(R.id.cartFragment)
                }
            }
            true
        }
    }

    private fun setNavigationItemSelectedListener(){
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_orders -> {
                    Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_settings -> {
                    Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_info -> {
                    Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
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
            R.id.productDetailsFragment -> {
                val action =
                    ProductDetailsFragmentDirections.actionProductDetailsFragmentToBillingFragment(
                        totalPrice = 0.0f, // Replace with the actual total price value
                        products = emptyArray(), // Replace with the actual products array
                        payment = false // Replace with the actual payment value
                    )
                navController.navigate(action)
            }
            R.id.cartFragment -> {
                val action =
                    CartFragmentDirections.actionCartFragmentToBillingFragment(
                        totalPrice = 0.0f, // Replace with the actual total price value
                        products = emptyArray(), // Replace with the actual products array
                        payment = false // Replace with the actual payment value
                    )
                navController.navigate(action)
            }
            R.id.brandsFragment -> {
                val action =
                    BrandsFragmentDirections.actionBrandsFragmentToBillingFragment(
                        totalPrice = 0.0f, // Replace with the actual total price value
                        products = emptyArray(), // Replace with the actual products array
                        payment = false // Replace with the actual payment value
                    )
                navController.navigate(action)
            }
            R.id.wishlistFragment -> {
                val action =
                    WishlistFragmentDirections.actionWishlistFragmentToBillingFragment(
                        totalPrice = 0.0f, // Replace with the actual total price value
                        products = emptyArray(), // Replace with the actual products array
                        payment = false // Replace with the actual payment value
                    )
                navController.navigate(action)
            }
        }
    }
}
