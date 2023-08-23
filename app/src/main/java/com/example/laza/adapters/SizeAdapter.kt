package com.example.laza.adapters

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.databinding.SizeRvItemBinding

class SizeAdapter: RecyclerView.Adapter<SizeAdapter.SizeViewHolder>() {

    private var selectedPosition = -1

    inner class SizeViewHolder(private val binding: SizeRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(size: String, position: Int) {
            binding.tvSize.text = size
            if (position == selectedPosition) { // size is  selected
                binding.apply {
                    imageShadow.visibility = View.VISIBLE
                }
            } else {// size is not  selected
                binding.apply {
                    imageShadow.visibility = View.INVISIBLE
                }
            }
        }
    }


    private val diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeViewHolder {
        return SizeViewHolder(
            SizeRvItemBinding.inflate(
                parent.context.getSystemService(android.view.LayoutInflater::class.java),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SizeViewHolder, position: Int) {
        val size = differ.currentList[position]
        holder.bind(size, position)

        holder.itemView.setOnClickListener {
            if (selectedPosition >=0)
                notifyItemChanged(selectedPosition) // unselect previous color
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)// select new color

            onItemClick?.invoke(size) // send selected color to ProductDetailsFragment
        }
    }
    var onItemClick: ((String) -> Unit)? = null

}