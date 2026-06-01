package com.example.modu.data.dataSource.remote.cart.dto

import java.math.BigDecimal

data class UpdateCartRequestDto(
    val shippingCosts: BigDecimal = 100.toBigDecimal(),
    val cartItems: List<CartItemDto>? = null
)