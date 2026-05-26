package com.example.modu.domain.entity.cart

data class StockAlertItem(
    val productVariantId: Int,
    val requestedQuantity: Int,
    val availableStock: Int
)