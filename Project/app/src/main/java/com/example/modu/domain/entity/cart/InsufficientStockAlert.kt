package com.example.modu.domain.entity.cart

data class InsufficientStockAlert(
    val items: List<StockAlertItem>
)