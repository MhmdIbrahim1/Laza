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
import com.example.laza.data.WishlistProduct
import com.example.laza.databinding.WishlistItemBinding

class WishListAdapter : RecyclerView.Adapter<WishListAdapter.WishListViewHolder>() {

    inner class WishListViewHolder(private val binding: WishlistItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(wishlistProduct: WishlistProduct) {
            binding.apply {
                brandImage.load(wishlistProduct.product.images[0]) {
                    crossfade(1000)
                }
                brandName.text = wishlistProduct.product.name
                brandPriceBeforeOffer.text = "$${wishlistProduct.product.price}"

                // Check if offerPercentage is not null before using it
                if (wishlistProduct.product.offerPercentage != null) {
                    val discountedPrice =
                        wishlistProduct.product.price - (wishlistProduct.product.price * wishlistProduct.product.offerPercentage / 100)
                    brandPriceAfterOffer.text = "$$discountedPrice"
                    brandPriceAfterOffer.visibility = View.VISIBLE
                    brandPriceBeforeOffer.paintFlags =
                        brandPriceBeforeOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    brandPriceAfterOffer.text = "" // Set a default value or empty text
                }

            }
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<WishlistProduct>() {
        override fun areItemsTheSame(oldItem: WishlistProduct, newItem: WishlistProduct): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(
            oldItem: WishlistProduct,
            newItem: WishlistProduct
        ): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishListViewHolder {
        return WishListViewHolder(
            WishlistItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: WishListViewHolder, position: Int) {
        val wishlistProduct = differ.currentList[position]
        holder.bind(wishlistProduct)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(wishlistProduct.product)
        }
    }

    var onItemClickListener: ((Product) -> Unit)? = null

}
