package com.juancarloslf.modu.data.dataSource.remote.cart.dto

data class CartCheckoutResponseDto(
    val orderPlaced: Boolean? = null,
    val orderId: Int? = null,
    val order: CartCheckoutOrderDto? = null,
    val cartResponse: CartDto? = null
)