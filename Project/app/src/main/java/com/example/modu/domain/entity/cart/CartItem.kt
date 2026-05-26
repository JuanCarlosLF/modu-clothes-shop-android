package com.example.modu.domain.entity.cart

data class CartItem(
    val id: Int,
    val productVariantId: Int,
    val currentStock: Int,
    val quantity: Int,
    val unitPrice: Long,
    val totalPrice: Long
)