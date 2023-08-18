package com.example.laza.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.laza.data.Product
import com.example.laza.databinding.NewArrivalRvItemBinding

class NewArrivalAdapter : RecyclerView.Adapter<NewArrivalAdapter.ViewHolder>() {


    inner class ViewHolder(private val binding: NewArrivalRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                brandImage.load(product.images[0]) {
                    crossfade(1000)
                }
                brandName.text = product.name
                brandPriceBeforeOffer.text = "$${product.price}"

                // Check if offerPercentage is not null before using it
                if (product.offerPercentage != null) {
                    val discountedPrice = product.price - (product.price * product.offerPercentage / 100)
                    brandPriceAfterOffer.text = "$$discountedPrice"
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
    }
}