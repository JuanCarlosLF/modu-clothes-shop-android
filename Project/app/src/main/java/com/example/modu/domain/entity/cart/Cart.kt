package com.example.modu.domain.entity.cart

data class Cart(
    val deviceId: String,
    val createdAt: String,
    val updatedAt: String,
    val items: List<CartItem>,
    val priceAlert: PriceChangedAlert?,
    val stockAlert: InsufficientStockAlert?
)