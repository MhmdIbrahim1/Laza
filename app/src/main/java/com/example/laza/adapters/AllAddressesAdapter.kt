package com.example.laza.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.data.Address
import com.example.laza.databinding.AddressesItemBinding
import com.example.laza.utils.AllAddressesUtil

class AllAddressesAdapter(private val allAddressesUtil: AllAddressesUtil ? = null) : RecyclerView.Adapter<AllAddressesAdapter.AllAddressesViewHolder>() {
    inner class AllAddressesViewHolder(private val binding: AddressesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(address: Address,position: Int) {
            binding.apply {
                tvAddressLocation.text = address.addressTitle
                tvAddressName.text = address.fullName
                tvAddressInfo.text = "${address.state}  ${address.city}  ${address.street}"
                tvAddressPhone.text = address.phone
            }

            binding.tvAddressEdit.setOnClickListener {
                allAddressesUtil?.editItem(address,position)
            }
            binding.tvAddressDelete.setOnClickListener {
                allAddressesUtil?.deleteItem(position)
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAddressesViewHolder {
        return AllAddressesViewHolder(
            AddressesItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AllAddressesViewHolder, position: Int) {
        val address = differ.currentList[position]
        holder.bind(address,position)
    }
}