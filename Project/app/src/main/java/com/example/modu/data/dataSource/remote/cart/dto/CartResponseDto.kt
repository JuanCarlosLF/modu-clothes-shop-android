package com.example.modu.data.dataSource.remote.cart.dto

import java.math.BigDecimal

data class CartResponseDto(
    val deviceId: String,
    val createdAt: String,
    val updatedAt: String,
    val subTotalPrice: BigDecimal,
    val shippingCosts: BigDecimal,
    val totalPrice: BigDecimal,
    val cartItems: List<CartItemDto>
)