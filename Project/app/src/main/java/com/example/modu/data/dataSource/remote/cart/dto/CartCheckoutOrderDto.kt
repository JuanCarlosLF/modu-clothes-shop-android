package com.example.modu.data.dataSource.remote.cart.dto

import java.math.BigDecimal

data class CartCheckoutOrderDto(
    val id: Int? = null,
    val deviceId: String? = null,
    val createdAt: String? = null,
    val specialInstructions: String? = null,
    val subtotalPrice: BigDecimal? = null,
    val shippingCosts: BigDecimal? = null,
    val totalPrice: BigDecimal? = null,
    val orderItems: List<CartCheckoutOrderItemDto>? = null
)