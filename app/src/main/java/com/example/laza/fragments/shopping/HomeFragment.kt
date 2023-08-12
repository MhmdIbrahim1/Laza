package com.example.laza.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.laza.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var drawerOpener: DrawerOpener

    // Interface to communicate with the activity
    interface DrawerOpener {
        fun openDrawer()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if the host activity implements the DrawerOpener interface
        if (activity is DrawerOpener) {
            drawerOpener = activity as DrawerOpener
        } else {
            throw IllegalArgumentException("Host activity must implement DrawerOpener interface")
        }

        binding.circleImageMenu.setOnClickListener {
            // Open the drawer by calling the interface method
            drawerOpener.openDrawer()
        }
    }
}
