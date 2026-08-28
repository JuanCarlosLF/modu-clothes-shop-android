package com.juancarloslf.modu.data.dataSource.remote.cart.dto

import java.math.BigDecimal

data class CartCheckoutRequestDto(
    val isPaid: Boolean? = null,
    val specialInstructions: String? = null,
    val shippingCosts: BigDecimal? = null,
    val cartToOrder: CartToOrderDto? = null
)