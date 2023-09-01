package com.example.laza.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import com.example.laza.data.Address
import com.example.laza.data.Reviews
import com.example.laza.databinding.ReviewRvItemBinding

class ReviewsProductsDetailsAdapter: RecyclerView.Adapter<ReviewsProductsDetailsAdapter.ReviewsPDViewHolder>() {

    inner class ReviewsPDViewHolder(private val binding: ReviewRvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reviews: Reviews) {
            binding.apply {
                tvName.text = reviews.name
                tvReviewDescription.text = reviews.review
                tvRating.text = "${reviews.ratingStars} rating"
                ratingBar.rating = reviews.ratingStars.toFloat()
                profileUserImageReview.setImageResource(R.drawable.profile)
            }
        }
    }

    private val differUtil = object : DiffUtil.ItemCallback<Reviews>() {
        override fun areItemsTheSame(oldItem: Reviews, newItem: Reviews): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Reviews, newItem: Reviews): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differUtil)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsPDViewHolder {
        return ReviewsPDViewHolder(
            ReviewRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ReviewsPDViewHolder, position: Int) {
        val review = differ.currentList[position]
        holder.bind(review)
    }

}