package com.example.laza.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import com.example.laza.databinding.BrandListItemBinding

class BrandAdapter(private val brandItems: List<BrandItem>) : RecyclerView.Adapter<BrandAdapter.BrandViewHolder>() {

    class BrandViewHolder(binding : BrandListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        return BrandViewHolder(
            BrandListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return brandItems.size
    }
    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        val brandItem = brandItems[position]
        val brandLogo = holder.itemView.findViewById<ImageView>(R.id.imageBrand)
        val brandName = holder.itemView.findViewById<TextView>(R.id.tvBrandName)

        // Update brand logo and name based on the BrandItem
        brandLogo.setImageResource(brandItem.imageResId)
        brandName.text = brandItem.brandName

        holder.itemView.setOnClickListener {
            val navController = it.findNavController()
            navController.navigate(brandItem.navigationAction)
        }
    }
    data class BrandItem(val fragment: Fragment, val imageResId: Int, val brandName: String, val navigationAction: Int)

}
