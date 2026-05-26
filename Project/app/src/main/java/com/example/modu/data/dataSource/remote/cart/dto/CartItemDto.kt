package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class CartItemDto(
    val id: Int,
    @SerializedName("product_variant_id")
    val productVariantId: Int? = null,
    @SerializedName("current_stock")
    val currentStock: Int? = null,
    val quantity: Int? = null,
    @SerializedName("unit_price")
    val unitPrice: Double? = null,
    @SerializedName("total_price")
    val totalPrice: Double? = null
)