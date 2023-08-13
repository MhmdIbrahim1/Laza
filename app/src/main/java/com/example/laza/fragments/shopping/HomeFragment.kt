package com.example.laza.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laza.R
import com.example.laza.adapters.BrandAdapter
import com.example.laza.databinding.FragmentHomeBinding
import com.example.laza.fragments.brands.AdidasFragment
import com.example.laza.fragments.brands.FilaFragment
import com.example.laza.fragments.brands.NikeFragment
import com.example.laza.fragments.brands.PumaFragment
import com.example.laza.utils.ItemSpacingDecoration

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
        setDrawer()

        // Create a list of BrandItem instances
        val brandItems = listOf(
            BrandAdapter.BrandItem(AdidasFragment(), R.drawable.adidas, "Adidas", R.id.action_homeFragment_to_adidasFragment),
            BrandAdapter.BrandItem(NikeFragment(), R.drawable.nike, "Nike", R.id.action_homeFragment_to_nikeFragment),
            BrandAdapter.BrandItem(PumaFragment(), R.drawable.puma, "Puma", R.id.action_homeFragment_to_pumaFragment),
            BrandAdapter.BrandItem(FilaFragment(), R.drawable.fila, "Fila", R.id.action_homeFragment_to_filaFragment)
            // Add more BrandItems for other brands as needed
        )

        // Create an adapter and pass the list of BrandItems
        val adapter = BrandAdapter(brandItems)
        binding.brandRv.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.brandRv.adapter = adapter

        // Add spacing between items
        binding.brandRv.addItemDecoration(ItemSpacingDecoration(16))
    }


   private fun setDrawer(){
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
