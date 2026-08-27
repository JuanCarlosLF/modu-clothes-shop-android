package com.juancarloslf.modu.data.dataSource.remote.cart.dto

import java.math.BigDecimal

data class CartCheckoutOrderItemDto(
    val id: Int? = null,
    val orderId: Int? = null,
    val productId: Int? = null,
    val productVariantId: Int? = null,
    val quantity: Int? = null,
    val unitPrice: BigDecimal? = null,
    val totalPrice: BigDecimal? = null
)