package com.example.laza.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.laza.R
import com.example.laza.data.Product
import com.example.laza.data.WishlistProduct
import com.example.laza.databinding.NewArrivalRvItemBinding
import com.example.laza.helper.getProductPrice

class NewArrivalAdapter() :
    RecyclerView.Adapter<NewArrivalAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NewArrivalRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                brandImage.load(product.images[0]) {
                    crossfade(1000)
                }
                brandName.text = product.name
                brandPriceBeforeOffer.text = "E£ ${product.price}"

                // Check if offerPercentage is not null before using it
                if (product.offerPercentage != null) {
                    brandPriceAfterOffer.text =
                        product.offerPercentage.getProductPrice(product.price).toString()
                    brandPriceAfterOffer.visibility = View.VISIBLE
                    brandPriceBeforeOffer.paintFlags =
                        brandPriceBeforeOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    brandPriceAfterOffer.text = "" // Set a default value or empty text
                }


            }
        }

    }

    private val diffCallBack = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            NewArrivalRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)
        // Attach click listener to the entire item view
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(product)
        }
    }


    var onItemClickListener: ((Product) -> Unit)? = null


}