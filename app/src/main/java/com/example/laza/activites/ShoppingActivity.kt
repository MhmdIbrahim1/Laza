package com.example.laza.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.laza.R
import com.example.laza.databinding.ActivityShoppingBinding
import com.example.laza.fragments.shopping.HomeFragment
import com.example.laza.utils.NetworkResult
import com.example.laza.viewmodels.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity(), HomeFragment.DrawerOpener {
    private val binding by lazy { ActivityShoppingBinding.inflate(layoutInflater) }

    val viewModel  by viewModels<CartViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onLogout()
        changeStatusBarColor()
        // set up the navigation controller
        val navController = findNavController(R.id.shoppingHostFragment)
        binding.bottomNavigation.setupWithNavController(navController)

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

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_wishlist -> {
                    navController.navigate(R.id.wishlistFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.nav_cart -> {
                    navController.navigate(R.id.cartFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.nav_orders -> {
                    Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_settings -> {
                    Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_info -> {
                    Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        // observe the cart products
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartProduct.collectLatest {
                    when (it) {
                        is NetworkResult.Success -> {
                            val count = it.data?.size ?: 0
                            val bottomNavigation = binding.bottomNavigation
                            bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
                                number = count
                                backgroundColor = resources.getColor(R.color.g_blue, null)
                                badgeTextColor = resources.getColor(R.color.background_intro__mentv_2, null)
                            }
                        }
                        else -> Unit
                    }
                }
            }
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

}
