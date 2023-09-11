package com.example.laza.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import com.example.laza.data.Address
import com.example.laza.databinding.AddressRvItemBinding


class AddressAdapter : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {
    inner class AddressViewHolder(val binding: AddressRvItemBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun bind(address: Address, isSelected: Boolean) {
            binding.apply {
                buttonAddress.text = address.addressTitle
                if (isSelected) {
                    buttonAddress.background =
                        ColorDrawable(itemView.context.resources.getColor(R.color.g_blue, null))

                }
                else {
                    buttonAddress.background =
                        ColorDrawable(itemView.context.resources.getColor(R.color.g_light_gray, null))
                }
            }
        }

    }

    private val differUtil = object : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        return AddressViewHolder(
            AddressRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var selectedAddress = -1
    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = differ.currentList[position]
        holder.bind(address, selectedAddress == position)

        holder.binding.buttonAddress.setOnClickListener {
            if (selectedAddress >=0)
                notifyItemChanged(selectedAddress)
            selectedAddress = holder.adapterPosition
            notifyItemChanged(selectedAddress)

            onClick?.invoke(address)
        }
    }

    init {
        differ.addListListener{_, _ ->
            notifyItemChanged(selectedAddress)

        }
    }
    var onClick: ((Address) -> Unit)? = null
}