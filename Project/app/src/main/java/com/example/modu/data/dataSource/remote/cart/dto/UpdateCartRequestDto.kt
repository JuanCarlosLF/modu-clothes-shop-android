package com.example.modu.data.dataSource.remote.cart.dto

import java.math.BigDecimal

private const val SHIPPING_COST_TO_BIG_DECIMAL_NUMBER = 100
data class UpdateCartRequestDto(
    val shippingCosts: BigDecimal = SHIPPING_COST_TO_BIG_DECIMAL_NUMBER.toBigDecimal(),
    val cartItems: List<CartItemDto>? = null
)