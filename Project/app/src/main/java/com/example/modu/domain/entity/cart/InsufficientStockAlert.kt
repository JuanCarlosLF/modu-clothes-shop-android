package com.example.modu.domain.entity.cart

data class InsufficientStockAlert(
    val items: List<StockAlertItem>
)

data class StockAlertItem(
    val productVariantId: Int,
    val requestedQuantity: Int,
    val availableStock: Int
)