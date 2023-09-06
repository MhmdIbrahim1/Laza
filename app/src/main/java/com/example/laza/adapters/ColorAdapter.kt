package com.example.laza.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer


import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.databinding.ColorRvItemBinding

class ColorAdapter : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedPosition = -1

    inner class ColorViewHolder(private val binding: ColorRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(color: Long, position: Int) {
            val imageDrawable = ColorDrawable(color.toInt())
            binding.imageColor.setImageDrawable(imageDrawable)
            if (position == selectedPosition) { // color is  selected
                binding.apply {
                    imageShadow.visibility = View.VISIBLE
                    imagePicked.visibility = View.VISIBLE
                }
            }else{// color is not  selected
                binding.apply {
                    imageShadow.visibility = View.INVISIBLE
                    imagePicked.visibility = View.INVISIBLE
                }
            }
        }


    }


    private val diffCallback = object : DiffUtil.ItemCallback<Long>() {
        override fun areItemsTheSame(oldItem: Long, newItem: Long): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Long, newItem: Long): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(
            ColorRvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = differ.currentList[position]
        holder.bind(color, position)

        holder.itemView.setOnClickListener {
            if (selectedPosition >=0)
                notifyItemChanged(selectedPosition) // unselect previous color
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)// select new color

            onItemClick?.invoke(color) // send selected color to ProductDetailsFragment
        }
    }

    var onItemClick : ((Long) -> Unit)? = null
}
