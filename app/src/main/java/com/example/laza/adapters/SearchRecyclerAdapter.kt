package com.example.laza.adapters

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.laza.R
import com.example.laza.data.Product
import com.example.laza.databinding.NewArrivalRvItemBinding
import com.example.laza.helper.getProductPrice

class SearchRecyclerAdapter(private val context: Context) : RecyclerView.Adapter<SearchRecyclerAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(val binding: NewArrivalRvItemBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun bind(product: Product) {
            binding.apply {
                brandImage.load(product.images[0]) {
                    crossfade(1000)
                }
                brandName.text = product.name
                brandPriceBeforeOffer.text = root.context.getString(R.string.egp_).plus(product.price)

                // Check if offerPercentage is not null before using it
                if (product.offerPercentage != null) {
                    brandPriceAfterOffer.text =
                        product.offerPercentage.getProductPrice(product.price).toString()
                    val newPrice = product.offerPercentage.getProductPrice(product.price)
                    brandPriceAfterOffer.text = root.context.getString(R.string.egp_).plus(newPrice)
                    brandPriceAfterOffer.visibility = View.VISIBLE
                    brandPriceBeforeOffer.paintFlags =
                        brandPriceBeforeOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    llLinearOffer.visibility = View.VISIBLE
                    offerText.text = product.offerPercentage.toString().plus("% OFF")
                    if (product.offerPercentage > 65) {
                        dealText.text = root.context.getString(R.string.limited_deal)
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

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            NewArrivalRvItemBinding.inflate(
                parent.context.getSystemService(android.view.LayoutInflater::class.java),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
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

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }
    }

    var onItemClick: ((Product) -> Unit)? = null

}
