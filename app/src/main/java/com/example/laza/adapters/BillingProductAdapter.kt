package com.example.laza.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.laza.R
import com.example.laza.data.CartProduct
import com.example.laza.databinding.BillingProductsRvItemBinding


class BillingProductAdapter(private val context: Context):
    RecyclerView.Adapter<BillingProductAdapter.BillingProductViewHolder>() {

    inner class BillingProductViewHolder(val binding: BillingProductsRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(billingProduct: CartProduct) {
            binding.apply {
                imageCartProduct.load(billingProduct.product.images.getOrNull(0))
                tvProductCartName.text = billingProduct.product.name
                tvBillingProductQuantity.text = billingProduct.quantity.toString()

                tvProductCartPrice.text = context.getString(
                    R.string.egp,
                    billingProduct.product.price.toString()
                )

                imageCartProductColor.setImageDrawable(
                    ColorDrawable(
                        billingProduct.selectedColor?.toInt() ?: Color.TRANSPARENT
                    )
                )
                tvCartProductSize.text = billingProduct.selectedSize
                    ?: "N/A".also { imageCartProductSize.setImageDrawable(ColorDrawable(Color.TRANSPARENT)) }

            }
        }

    }

    private val diffUtil = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingProductViewHolder {
        return BillingProductViewHolder(
            BillingProductsRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BillingProductViewHolder, position: Int) {
        val billingProduct = differ.currentList[position]
        holder.bind(billingProduct)
    }

}