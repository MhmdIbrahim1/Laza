package com.example.laza.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import com.example.laza.data.Reviews
import com.example.laza.databinding.UserReviewRvItemBinding
import com.example.laza.fragments.settings.UserReviewsFragmentDirections
import com.example.laza.utils.UserReviewsUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserReviewsAdapter(private val context: Context, private val userReviewsUtil: UserReviewsUtil? = null): RecyclerView.Adapter<UserReviewsAdapter.UserReviewsViewHolder>() {
     inner class UserReviewsViewHolder(private val binding: UserReviewRvItemBinding):
         RecyclerView.ViewHolder(binding.root) {
         fun bind(reviews: Reviews) {
             binding.apply {
                 tvName.text = reviews.name
                 tvReviewDescription.text = reviews.review
                 tvRating.text =
                     context.getString(R.string.rating_with_number, reviews.ratingStars.toString())
                 ratingBar.rating = reviews.ratingStars.toFloat()
                 profileUserImageReview.setImageResource(R.drawable.profile)

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
                     //tvReviewDate.text = reviews.date
                 }
             }
             binding.menuPopUp.setOnClickListener {
                 showMenu(it, R.menu.edit_or_delete_review_menu, adapterPosition)
             }
         }

         private fun showMenu(view: View, @MenuRes menuRes: Int, position: Int) {
             val popup = PopupMenu(view.context, view)
             popup.menuInflater.inflate(menuRes, popup.menu)
             popup.setOnMenuItemClickListener { menuItem: MenuItem? ->

                 if (menuItem?.itemId?.equals(R.id.editReview) == true){
//                     Navigation.findNavController(view).navigate(
//                         UserReviewsFragmentDirections.actionUserReviewsFragmentToAddReviewsFragment(
//                             reviews = Reviews(
//                                 name = differ.currentList[position].name,
//                                 review = differ.currentList[position].review,
//                                 ratingStars = differ.currentList[position].ratingStars,
//                                 image = differ.currentList[position].image,
//                                 date = differ.currentList[position].date,
//                                 documentId = differ.currentList[position].documentId
//                             )
//                         )
//                     )
                     val action = UserReviewsFragmentDirections.actionUserReviewsFragmentToAddReviewsFragment(
                            reviews = Reviews(
                                name = differ.currentList[position].name,
                                review = differ.currentList[position].review,
                                ratingStars = differ.currentList[position].ratingStars,
                                image = differ.currentList[position].image,
                                date = differ.currentList[position].date,
                                documentId = differ.currentList[position].documentId
                            ),
                            productId = differ.currentList[position].productId
                        )
                        Navigation.findNavController(view).navigate(action)
                     true
                 }else{
                     false
                 }
             }
             popup.setOnDismissListener { menu: PopupMenu? -> }
             // Show the popup menu.
             //popup.show()
             Toast.makeText(context, "This option not available yet", Toast.LENGTH_SHORT).show()
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReviewsViewHolder {
        return UserReviewsViewHolder(
            UserReviewRvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: UserReviewsViewHolder, position: Int) {
        val reviews = differ.currentList[position]
        holder.bind(reviews)
    }

}