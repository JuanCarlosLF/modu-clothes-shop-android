package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CartItemDto(
    val id: Int? = null,
    val productId: Int? = null,
    val productVariantId: Int? = null,
    @SerializedName("stock")
    val currentStock: Int? = 0,
    val quantity: Int? = null,
    val unitPrice: BigDecimal? = null,
    val totalPrice: BigDecimal? = null
)