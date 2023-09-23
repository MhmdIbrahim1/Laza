package com.example.laza.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.laza.R
import com.example.laza.data.Product
import com.example.laza.data.WishlistProduct
import com.example.laza.databinding.WishlistItemBinding
import com.example.laza.helper.getProductPrice
import com.example.laza.utils.WishlistUtil

class WishListAdapter(private val wishlistUtil: WishlistUtil? = null) : RecyclerView.Adapter<WishListAdapter.WishListViewHolder>() {

    inner class WishListViewHolder(private val binding: WishlistItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(wishlistProduct: WishlistProduct,position: Int) {
            binding.apply {
                brandImage.load(wishlistProduct.product.images[0]) {
                    crossfade(1000)
                }
                brandName.text = wishlistProduct.product.name
                brandPriceBeforeOffer.text = root.context.getString(R.string.egp_).plus(wishlistProduct.product.price)

                // Check if offerPercentage is not null before using it
                if (wishlistProduct.product.offerPercentage != null) {
                    brandPriceAfterOffer.text =
                        wishlistProduct.product.offerPercentage.getProductPrice(wishlistProduct.product.price).toString()
                    val newPrice =wishlistProduct. product.offerPercentage.getProductPrice(wishlistProduct.product.price)
                    brandPriceAfterOffer.text =  root.context.getString(R.string.egp_).plus(newPrice)
                    brandPriceAfterOffer.visibility = View.VISIBLE
                    brandPriceBeforeOffer.paintFlags =
                        brandPriceBeforeOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    llLinearOffer.visibility = View.VISIBLE
                    offerText.text = wishlistProduct.product.offerPercentage.toString().plus("% OFF")
                    if (wishlistProduct.product.offerPercentage > 65) {
                        dealText.text =  root.context.getString(R.string.limited_deal)
                    } else if (wishlistProduct.product.offerPercentage < 25) {
                        dealText.visibility = View.GONE
                    } else {
                        dealText.visibility = View.VISIBLE
                    }
                } else {
                    brandPriceAfterOffer.visibility = View.GONE
                    llLinearOffer.visibility = View.GONE
                }
            }

            binding.removeWishlistItem.setOnClickListener {

                wishlistUtil?.deleteItem(position)
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
        holder.bind(wishlistProduct,position)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(wishlistProduct.product)
        }

    }

    var onItemClickListener: ((Product) -> Unit)? = null
}
