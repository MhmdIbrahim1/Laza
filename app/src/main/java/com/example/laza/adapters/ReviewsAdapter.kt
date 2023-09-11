package com.example.laza.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import com.example.laza.data.Reviews
import com.example.laza.databinding.ReviewRvItemBinding

class ReviewsAdapter(private val context: Context) : RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>() {

    inner class ReviewsViewHolder(private val binding: ReviewRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reviews: Reviews) {
            binding.apply {
                tvName.text = reviews.name
                tvReviewDescription.text = reviews.review
                tvRating.text =  context.getString(R.string.rating_with_number, reviews.ratingStars.toString())
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




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        return ReviewsViewHolder(
            ReviewRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val review = differ.currentList[position]
        holder.bind(review)
    }
}