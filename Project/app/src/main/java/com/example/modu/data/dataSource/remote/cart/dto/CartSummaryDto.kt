package com.example.modu.data.dataSource.remote.cart.dto

import java.math.BigDecimal

data class CartSummaryDto(
    val deviceId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val subTotalPrice: BigDecimal? = null,
    val shippingCosts: BigDecimal? = null,
    val totalPrice: BigDecimal? = null,
    val cartItems: List<CartItemDto>? = emptyList()
)