package com.example.modu.data.dataSource.remote.cart.dto

import java.math.BigDecimal

data class CartItemDto(
    val id: Int,
    val productId: Int? = null,
    val productVariantId: Int? = null,
    val currentStock: Int? = null,
    val quantity: Int? = null,
    val unitPrice: BigDecimal? = null,
    val totalPrice: BigDecimal? = null
)