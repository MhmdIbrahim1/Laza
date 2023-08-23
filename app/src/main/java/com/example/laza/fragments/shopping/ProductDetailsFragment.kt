package com.example.laza.fragments.shopping

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.laza.R
import com.example.laza.adapters.ViewPager2Images
import com.example.laza.databinding.FragmentProductDetailsBinding


class ProductDetailsFragment : Fragment() {
    private lateinit var  biding: FragmentProductDetailsBinding
    private val viePagerAdapter  by lazy { ViewPager2Images() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_details, container, false)
    }

}