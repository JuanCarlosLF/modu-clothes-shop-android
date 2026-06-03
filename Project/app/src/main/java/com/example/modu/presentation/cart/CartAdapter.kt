package com.example.modu.presentation.cart

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.modu.R
import com.example.modu.databinding.ItemProductCartBinding
import com.example.modu.domain.entity.cart.CartItem

class CartAdapter(
    private val onDeleteClick: (CartItem) -> Unit,
    private val onAddQuantityClick: (CartItem) -> Unit,
    private val onRemoveQuantityClick: (CartItem) -> Unit,
    private val onMaxStockReached: () -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback()) {

    inner class CartViewHolder(private val binding: ItemProductCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) = with(binding) {
            imgProduct.load(item.imageUrl)
            textItemCartTitle.text = item.title
            textItemCartSizeColor.text =
                root.context.getString(R.string.cart_text_size_and_color, item.size, item.color)
            textItemCartPrice.text =
                root.context.getString(R.string.cart_text_price, item.unitPrice)
            textItemCartTotalPrice.text =
                root.context.getString(R.string.cart_text_total_price, item.totalPrice)
            txtQuantityCart.text = item.quantity.toString()

            val isUnavailable = item.isAvailableAlert == false
            val isOutOfStock = item.stockAlertAvailable != null
            val hasAlert = isUnavailable || isOutOfStock

            overlayUnavailable.isVisible = hasAlert

            when {
                isUnavailable -> textUnavailableBadge.text = root.context.getString(R.string.alert_unavailable)
                isOutOfStock -> textUnavailableBadge.text = root.context.getString(R.string.alert_out_of_stock)
            }

            if (item.oldPriceAlert != null) {
                textOldPrice.isVisible = true
                textOldPrice.paintFlags = textOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                textOldPrice.text = root.context.getString(R.string.cart_text_price, item.oldPriceAlert)
            } else {
                textOldPrice.isVisible = false
            }

            val isMaxStock = item.quantity >= item.currentStock

            btnCartAddQuantity.isEnabled = !hasAlert
            btnCartRemoveQuantity.isEnabled = !hasAlert

            btnCartAddQuantity.alpha = if (hasAlert || isMaxStock) 0.5f else 1.0f
            btnCartRemoveQuantity.alpha = if (hasAlert) 0.5f else 1.0f

            icDeleteItemCart.setOnClickListener {
                onDeleteClick(item)
            }

            btnCartAddQuantity.setOnClickListener {
                if (isMaxStock) {
                    onMaxStockReached()
                } else {
                    onAddQuantityClick(item)
                }
            }

            btnCartRemoveQuantity.setOnClickListener {
                onRemoveQuantityClick(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartViewHolder {
        val binding = ItemProductCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(
            oldItem: CartItem,
            newItem: CartItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CartItem,
            newItem: CartItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}