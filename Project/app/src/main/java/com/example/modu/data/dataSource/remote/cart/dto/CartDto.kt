package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class CartDto(
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("updated_at")
    val updatedAt: Long,
    @SerializedName("cart_items")
    val cartItems: List<CartItemDto>,
    @SerializedName("price_changed_alert")
    val priceChangedAlert: PriceChangedAlertDto?,
    @SerializedName("insufficient_stock_alert")
    val insufficientStockAlert: InsufficientStockAlertDto?
)