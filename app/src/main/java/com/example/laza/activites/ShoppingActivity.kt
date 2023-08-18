package com.example.laza.activites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.laza.R
import com.example.laza.databinding.ActivityShoppingBinding
import com.example.laza.fragments.shopping.HomeFragment
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView

@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity(), HomeFragment.DrawerOpener {
    private val binding by lazy { ActivityShoppingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        // set up the navigation controller
        val navController = findNavController(R.id.shoppingHostFragment)
        binding.bottomNavigation.setupWithNavController(navController)

    }

    // Implementation of the DrawerOpener interface
    override fun openDrawer() {
        // Implement the logic to open the drawer here
        val drawerLayout = binding.drawerLayout
        drawerLayout.openDrawer(GravityCompat.START)
    }
}
