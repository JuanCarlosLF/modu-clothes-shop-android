package com.juancarloslf.modu.domain.entity.cart

import java.math.BigDecimal

data class Cart(
    val createdAt: String,
    val updatedAt: String,
    val subTotal: BigDecimal,
    val shippingCost: BigDecimal,
    val total: BigDecimal,
    val items: List<CartItem>
)