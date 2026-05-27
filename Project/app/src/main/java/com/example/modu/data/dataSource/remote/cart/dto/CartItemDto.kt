package com.example.modu.data.dataSource.remote.cart.dto

data class CartItemDto(
    val id: Int,
    val productId: Int? = null,
    val productVariantId: Int? = null,
    val quantity: Int? = null,
    val unitPrice: Double? = null,
)