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
        //        val isFavorite = wishlistIconManager.getIconState(product.id)
//        holder.binding.addToWishlist.tag = isFavorite
//
//        if (isFavorite) {
//            holder.binding.addToWishlist.setImageResource(R.drawable.ic_wishlist_filled)
//        } else {
//            holder.binding.addToWishlist.setImageResource(R.drawable.wishlist)
//        }

        // Attach click listener to the wishlist icon
//        holder.binding.addToWishlist.setOnClickListener {
//            onWishListClickListener?.onWishListClick(
//                WishlistProduct(
//                    product,
//                    holder.binding.addToWishlist.tag as Boolean
//                ), onResult = { wishlistProduct, exception ->
//                    if (exception == null) {
//                        // Update the tag on the wishlist icon
//                        holder.binding.addToWishlist.tag = wishlistProduct!!.isFavorite
//
//                        if (wishlistProduct.isFavorite) {
//
//                            it.tag = wishlistProduct.isFavorite
//
//                            // Set the wishlist icon to be filled
//                            holder.binding.addToWishlist.setImageResource(R.drawable.ic_wishlist_filled)
//                        } else {
//                            // Set the wishlist icon to be empty
//                            holder.binding.addToWishlist.setImageResource(R.drawable.wishlist)
//                        }
//                    }
//                })
//        }
    }
//
//    interface OnWishlistClickListener {
//        fun onWishListClick(
//            wishlistProduct: WishlistProduct,
//            onResult: (WishlistProduct?, Exception?) -> Unit
//        )
//    }

    var onItemClickListener: ((Product) -> Unit)? = null

}