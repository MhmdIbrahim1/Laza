package com.example.laza.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.laza.R
import com.example.laza.data.order.Order
import com.example.laza.data.order.OrderStatus
import com.example.laza.data.order.getOrderStatus
import com.example.laza.databinding.OrderItemBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllOrdersAdapter : RecyclerView.Adapter<AllOrdersAdapter.OrdersViewHolder>() {

    inner class OrdersViewHolder(private val binding: OrderItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.apply {
                tvOrderId.text = order.orderId.toString()
                // Check if order.date is a timestamp (milliseconds)
                if (order.date.toLongOrNull() != null) {
                    // Convert the timestamp to a Date
                    val date = Date(order.date.toLong())

                    // Format the date as needed
                    val sampleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    val formattedDate = sampleDateFormat.format(date)
                    tvOrderDate.text = formattedDate
                } else {
                    // If it's not a timestamp, display it as is
                    tvOrderDate.text = order.date
                }

                val resources = itemView.resources

                val colorDrawable = when (getOrderStatus(order.orderStatus)) {
                    is OrderStatus.Ordered -> {
                        ColorDrawable(resources.getColor(R.color.g_orange_yellow))
                    }
                    is OrderStatus.Confirmed -> {
                        ColorDrawable(resources.getColor(R.color.g_green))
                    }
                    is OrderStatus.Delivered -> {
                        ColorDrawable(resources.getColor(R.color.g_green))
                    }
                    is OrderStatus.Shipped -> {
                        ColorDrawable(resources.getColor(R.color.g_green))
                    }
                    is OrderStatus.Canceled -> {
                        ColorDrawable(resources.getColor(R.color.g_red))
                    }
                    is OrderStatus.Returned -> {
                        ColorDrawable(resources.getColor(R.color.g_red))
                    }
                }

                imageOrderState.setImageDrawable(colorDrawable)

            }
        }
    }


    private val diffUtil = object : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.products == newItem.products
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        return OrdersViewHolder(
            OrderItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.bind(order)

        holder.itemView.setOnClickListener {
            onClick?.invoke(order)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Order) -> Unit)? = null
}