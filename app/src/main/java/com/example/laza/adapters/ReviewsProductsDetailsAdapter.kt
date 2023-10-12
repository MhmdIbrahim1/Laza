package com.example.laza.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.laza.R
import com.example.laza.data.Reviews
import com.example.laza.data.User
import com.example.laza.databinding.ReviewRvItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewsProductsDetailsAdapter :
    RecyclerView.Adapter<ReviewsProductsDetailsAdapter.ReviewsPDViewHolder>() {

    inner class ReviewsPDViewHolder(private val binding: ReviewRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reviews: Reviews) {
            binding.apply {
                tvName.text = reviews.name
                tvReviewDescription.text = reviews.review
                tvReviewDescription.maxLines = 1
                tvReviewDescription.ellipsize = null

                // Check if order.date is a timestamp (milliseconds)
                if (reviews.date.toLongOrNull() != null) {
                    // Convert the timestamp to a Date
                    val date = Date(reviews.date.toLong())

                    // Format the date as needed
                    val sampleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    val formattedDate = sampleDateFormat.format(date)
                    tvReviewDate.text = formattedDate
                } else {
                    // If it's not a timestamp, display it as is
                    //  tvReviewDate.text = reviews.date
                }

                //reduce the length of the review
                if (reviews.review.length > 45) {
                    tvReviewDescription.text = reviews.review.substring(0, 45).plus("...")
                } else {
                    tvReviewDescription.text = reviews.review
                }

                tvRating.text = itemView.context.getString(
                    R.string.rating_with_number,
                    reviews.ratingStars.toString()
                )
                ratingBar.rating = reviews.ratingStars.toFloat()

                Glide.with(itemView.context)
                    .load(R.drawable.review_icon)
                    .into(profileUserImageReview)
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