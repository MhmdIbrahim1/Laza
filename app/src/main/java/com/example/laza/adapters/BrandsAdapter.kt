package com.example.laza.adapters

import android.content.Context
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
import com.example.laza.databinding.BrandRvItemBinding
import com.example.laza.helper.getProductPrice

class BrandsAdapter(private val context: Context) : RecyclerView.Adapter<BrandsAdapter.ViewHolder>() {

    inner class ViewHolder( val binding: BrandRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                brandImage.load(product.images[0]) {
                    crossfade(1000)
                }
                brandName.text = product.name
                brandPriceBeforeOffer.text = context.getString(R.string.egp_).plus(product.price)

                // Check if offerPercentage is not null before using it
                if (product.offerPercentage != null) {
                    val newPrice = product.offerPercentage.getProductPrice(product.price)
                    brandPriceAfterOffer.text = context.getString(R.string.egp_).plus(newPrice)
                    brandPriceAfterOffer.visibility = View.VISIBLE
                    brandPriceBeforeOffer.paintFlags =
                        brandPriceBeforeOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    llLinearOffer.visibility = View.VISIBLE
                    offerText.text = product.offerPercentage.toString().plus("% OFF")
                    if (product.offerPercentage > 65) {
                        dealText.text = context.getString(R.string.limited_deal)
                    } else if (product.offerPercentage < 25) {
                        dealText.visibility = View.GONE
                    } else {
                        dealText.visibility = View.VISIBLE
                    }
                } else {
                    brandPriceAfterOffer.visibility = View.GONE
                    llLinearOffer.visibility = View.GONE
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
            holder.binding.tvRating.text = String.format("%.1f", product.ratings.average())
            holder.binding.reviewsItemCount.text = context.getString(R.string.reviews_item_count_, product.ratings.size.toString())
        }



        // Set up item click listener
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(product) }
        }

        holder.binding.addToWishlist.setOnClickListener {

        }

    }
//
//    // Add a function to update the dataset when the wishlist state changes
//    fun updateWishlistStatus(wishlistProduct: WishlistProduct) {
//        val position = differ.currentList.indexOfFirst { it.id == wishlistProduct.product.id }
//        if (position != -1) {
//            val product = differ.currentList[position]
//            product.isInWishlist = wishlistProduct.isInWishlist
//            notifyItemChanged(position)
//        }
//    }

    var onItemClickListener: ((Product) -> Unit)? = null

   // var onItemHeartClickListener: ((WishlistProduct) -> Unit)? = null

}
