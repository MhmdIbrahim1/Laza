package com.example.laza.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.laza.data.Product
import com.example.laza.databinding.BrandRvItemBinding
import com.example.laza.helper.getProductPrice
import dagger.hilt.android.AndroidEntryPoint

class BrandsAdapter : RecyclerView.Adapter<BrandsAdapter.ViewHolder>() {

    inner class ViewHolder( val binding: BrandRvItemBinding) :
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
                    val discountedPrice = product.offerPercentage.getProductPrice(product.price)
                    val newPrice = product.offerPercentage.getProductPrice(product.price)
                    brandPriceAfterOffer.text = "E£ $newPrice"
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
        val binding = BrandRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        // check if the rating is null or not
        if (product.ratings.isEmpty()) {
            holder.binding.ratingBar.visibility = View.GONE
            holder.binding.tvRating.visibility = View.GONE
            holder.binding.reviewsItemCount.visibility = View.GONE
        } else {
            holder.binding.ratingBar.rating = product.ratings.average().toFloat()
            holder.binding.tvRating.text = product.ratings.average().toString()
            holder.binding.reviewsItemCount.text = product.reviewCount.toString()

        }

        // Set up item click listener
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(product) }
        }
    }

    var onItemClickListener: ((Product) -> Unit)? = null

}
