package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class CartItemDto(
    val id: Int,
    @SerializedName("product_variant_id")
    val productVariantId: Int,
    @SerializedName("current_stock")
    val currentStock: Int,
    val quantity: Int,
    @SerializedName("unit_price")
    val unitPrice: Double,
    @SerializedName("total_price")
    val totalPrice: Double
)